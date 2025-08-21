import os
import consul
import logging
import requests
import jwt
from datetime import datetime, timedelta
from functools import wraps
from flask import Flask, request, jsonify
from reporting_service.reporting_models import db, Report

# Configure logging
logging.basicConfig(level=logging.DEBUG)
logger = logging.getLogger(__name__)

# Initialize Flask app
app = Flask(__name__)
app.config["SQLALCHEMY_DATABASE_URI"] = os.environ.get("REPORTING_DATABASE_URL")
app.config["JWT_SECRET_KEY"] = os.environ.get("SESSION_SECRET", "reporting_service_secret_key")

# Initialize extensions
db.init_app(app)

# Create tables
with app.app_context():
    db.create_all()

# Consul configuration
CONSUL_HOST = os.environ.get("CONSUL_HOST", "localhost")
CONSUL_PORT = int(os.environ.get("CONSUL_PORT", 8500))
SERVICE_NAME = "reporting-service"
SERVICE_ID = f"{SERVICE_NAME}-{os.urandom(8).hex()}"
SERVICE_PORT = 8004

# Initialize Consul client
consul_client = consul.Consul(host=CONSUL_HOST, port=CONSUL_PORT)

# Register service with Consul
def register_service():
    consul_client.agent.service.register(
        name=SERVICE_NAME,
        service_id=SERVICE_ID,
        address="localhost",
        port=SERVICE_PORT,
        check=consul.Check.http(
            f"http://localhost:{SERVICE_PORT}/api/health",
            interval="10s",
            timeout="5s",
        ),
    )
    logger.info(f"Registered service with Consul as {SERVICE_ID}")

# Deregister service from Consul
def deregister_service():
    consul_client.agent.service.deregister(SERVICE_ID)
    logger.info(f"Deregistered service from Consul: {SERVICE_ID}")

# Register service on startup (unless disabled)
if not os.environ.get("DISABLE_CONSUL", "false").lower() == "true":
    with app.app_context():
        register_service()

# Deregister service on shutdown (unless disabled)
import atexit
if not os.environ.get("DISABLE_CONSUL", "false").lower() == "true":
    atexit.register(deregister_service)

# Service URLs
AUTH_SERVICE_URL = os.environ.get("AUTH_SERVICE_URL", "http://localhost:8001")
ACCOUNT_SERVICE_URL = os.environ.get("ACCOUNT_SERVICE_URL", "http://localhost:8002")
TRANSACTION_SERVICE_URL = os.environ.get("TRANSACTION_SERVICE_URL", "http://localhost:8003")

# Helper functions
def token_required(f):
    """Decorator for endpoints that require a valid JWT token"""
    @wraps(f)
    def decorated(*args, **kwargs):
        token = None
        auth_header = request.headers.get('Authorization')
        
        if auth_header and auth_header.startswith('Bearer '):
            token = auth_header.split(' ')[1]
        
        if not token:
            return jsonify({'message': 'Token is missing'}), 401
        
        try:
            # Verify token with auth service
            response = requests.get(
                f"{AUTH_SERVICE_URL}/api/auth/verify_token",
                headers={'Authorization': f'Bearer {token}'}
            )
            
            if not response.ok:
                return jsonify({'message': 'Invalid or expired token'}), 401
            
            current_user = response.json()
                
        except requests.RequestException:
            return jsonify({'message': 'Authorization service unavailable'}), 503
        except jwt.ExpiredSignatureError:
            return jsonify({'message': 'Token has expired'}), 401
        except jwt.InvalidTokenError:
            return jsonify({'message': 'Invalid token'}), 401
            
        return f(current_user, *args, **kwargs)
    
    return decorated

def admin_required(f):
    """Decorator for endpoints that require admin privileges"""
    @wraps(f)
    def decorated(current_user, *args, **kwargs):
        # Verify admin role with auth service
        token = request.headers.get('Authorization').split(' ')[1]
        
        try:
            response = requests.get(
                f"{AUTH_SERVICE_URL}/api/auth/verify_admin",
                headers={'Authorization': f'Bearer {token}'}
            )
            
            if not response.ok or not response.json().get('is_admin'):
                return jsonify({'message': 'Admin privilege required'}), 403
                
        except requests.RequestException:
            return jsonify({'message': 'Authorization service unavailable'}), 503
        
        return f(current_user, *args, **kwargs)
    
    return decorated

def parse_date(date_string):
    """Parse date string in various formats"""
    if not date_string:
        return None
    
    # Try different date formats
    formats = ['%Y-%m-%d', '%Y-%m-%dT%H:%M:%S', '%Y-%m-%d %H:%M:%S']
    for fmt in formats:
        try:
            return datetime.strptime(date_string, fmt)
        except ValueError:
            continue
    
    raise ValueError(f"Invalid date format: {date_string}")

def get_user_accounts(user_id, token):
    """Get all accounts for a user"""
    try:
        response = requests.get(
            f"{ACCOUNT_SERVICE_URL}/api/accounts/list",
            headers={'Authorization': f'Bearer {token}'}
        )
        if response.ok:
            return response.json().get('accounts', [])
    except requests.RequestException:
        pass
    return []

def get_account_details(account_id, token):
    """Get account details"""
    try:
        response = requests.get(
            f"{ACCOUNT_SERVICE_URL}/api/accounts/details/{account_id}",
            headers={'Authorization': f'Bearer {token}'}
        )
        if response.ok:
            return response.json()
    except requests.RequestException:
        pass
    return None

def get_account_transactions(account_id, token, start_date=None, end_date=None):
    """Get transactions for an account"""
    try:
        params = {}
        if start_date:
            params['start_date'] = start_date.isoformat()
        if end_date:
            params['end_date'] = end_date.isoformat()
            
        response = requests.get(
            f"{TRANSACTION_SERVICE_URL}/api/transactions/account/{account_id}",
            headers={'Authorization': f'Bearer {token}'},
            params=params
        )
        if response.ok:
            return response.json().get('transactions', [])
    except requests.RequestException:
        pass
    return []

def get_user_transactions(token, start_date=None, end_date=None):
    """Get all transactions for a user"""
    try:
        params = {}
        if start_date:
            params['start_date'] = start_date.isoformat()
        if end_date:
            params['end_date'] = end_date.isoformat()
            
        response = requests.get(
            f"{TRANSACTION_SERVICE_URL}/api/transactions/list",
            headers={'Authorization': f'Bearer {token}'},
            params=params
        )
        if response.ok:
            return response.json().get('transactions', [])
    except requests.RequestException:
        pass
    return []

def get_all_accounts(auth_header):
    """Get all accounts (admin only)"""
    try:
        response = requests.get(
            f"{ACCOUNT_SERVICE_URL}/api/accounts/all",
            headers={'Authorization': auth_header}
        )
        if response.ok:
            return response.json().get('accounts', [])
    except requests.RequestException:
        pass
    return []

def get_all_transactions(auth_header):
    """Get all transactions (admin only)"""
    try:
        response = requests.get(
            f"{TRANSACTION_SERVICE_URL}/api/transactions/all",
            headers={'Authorization': auth_header}
        )
        if response.ok:
            return response.json().get('transactions', [])
    except requests.RequestException:
        pass
    return []

def get_all_users(auth_header):
    """Get all users (admin only)"""
    try:
        response = requests.get(
            f"{AUTH_SERVICE_URL}/api/auth/users",
            headers={'Authorization': auth_header}
        )
        if response.ok:
            return response.json().get('users', [])
    except requests.RequestException:
        pass
    return []

def save_report(user_id, report_type, title, description, parameters, data):
    """Save report to database"""
    with app.app_context():
        report = Report(
            user_id=user_id,
            report_type=report_type,
            title=title,
            description=description,
            parameters=parameters,
            data=data
        )
        db.session.add(report)
        db.session.commit()
        # Refresh to get the ID
        db.session.refresh(report)
        return report

# Routes
@app.route('/api/health', methods=['GET'])
def health_check():
    """Health check endpoint"""
    return jsonify({'status': 'healthy', 'service': 'reporting-service'}), 200

@app.route('/api/reports/account/<account_id>', methods=['GET'])
@token_required
def generate_account_report(current_user, account_id):
    """Generate detailed account activity report"""
    try:
        # Get query parameters
        start_date_str = request.args.get('start_date')
        end_date_str = request.args.get('end_date')
        
        # Set default date range (last 30 days)
        end_date = parse_date(end_date_str) if end_date_str else datetime.now()
        start_date = parse_date(start_date_str) if start_date_str else end_date - timedelta(days=30)
        
        token = request.headers.get('Authorization')
        
        # Get account details
        account = get_account_details(account_id, token)
        if not account:
            return jsonify({'message': 'Account not found or access denied'}), 404
        
        # Verify account ownership (unless admin)
        if account.get('user_id') != current_user['user_id'] and current_user['role'] != 'admin':
            return jsonify({'message': 'Access denied'}), 403
        
        # Get transactions for the account
        transactions = get_account_transactions(account_id, token, start_date, end_date)
        
        # Calculate summary statistics
        total_transactions = len(transactions)
        total_deposits = sum(t['amount'] for t in transactions if t['transaction_type'] == 'deposit')
        total_withdrawals = sum(t['amount'] for t in transactions if t['transaction_type'] == 'withdrawal')
        total_transfers_in = sum(t['amount'] for t in transactions if t['transaction_type'] == 'transfer' and t.get('to_account_id') == account_id)
        total_transfers_out = sum(t['amount'] for t in transactions if t['transaction_type'] == 'transfer' and t.get('from_account_id') == account_id)
        
        # Group transactions by type
        transaction_types = {}
        for t in transactions:
            t_type = t['transaction_type']
            if t_type not in transaction_types:
                transaction_types[t_type] = {'count': 0, 'total_amount': 0}
            transaction_types[t_type]['count'] += 1
            transaction_types[t_type]['total_amount'] += t['amount']
        
        # Generate report data
        report_data = {
            'account': account,
            'period': {
                'start_date': start_date.isoformat(),
                'end_date': end_date.isoformat()
            },
            'summary': {
                'total_transactions': total_transactions,
                'total_deposits': total_deposits,
                'total_withdrawals': total_withdrawals,
                'total_transfers_in': total_transfers_in,
                'total_transfers_out': total_transfers_out,
                'net_change': total_deposits + total_transfers_in - total_withdrawals - total_transfers_out,
                'current_balance': account['balance']
            },
            'transaction_breakdown': transaction_types,
            'transactions': transactions
        }
        
        # Save report
        parameters = {
            'account_id': account_id,
            'start_date': start_date.isoformat(),
            'end_date': end_date.isoformat()
        }
        
        title = f"Account Report - {account['account_number']}"
        description = f"Activity report for account {account['account_number']} from {start_date.strftime('%Y-%m-%d')} to {end_date.strftime('%Y-%m-%d')}"
        
        report = save_report(current_user['user_id'], 'account', title, description, parameters, report_data)
        
        return jsonify({
            'report_id': report.id,
            'report': report_data
        }), 200
        
    except ValueError as e:
        return jsonify({'message': str(e)}), 400
    except Exception as e:
        logger.error(f"Error generating account report: {str(e)}")
        return jsonify({'message': 'Internal server error'}), 500

@app.route('/api/reports/transactions', methods=['GET'])
@token_required
def generate_transaction_report(current_user):
    """Generate user's cross-account transaction report"""
    try:
        # Get query parameters
        start_date_str = request.args.get('start_date')
        end_date_str = request.args.get('end_date')
        
        # Set default date range (last 30 days)
        end_date = parse_date(end_date_str) if end_date_str else datetime.now()
        start_date = parse_date(start_date_str) if start_date_str else end_date - timedelta(days=30)
        
        token = request.headers.get('Authorization')
        
        # Get user's accounts
        accounts = get_user_accounts(current_user['user_id'], token)
        
        # Get all transactions for the user
        transactions = get_user_transactions(token, start_date, end_date)
        
        # Calculate summary statistics
        total_transactions = len(transactions)
        total_deposits = sum(t['amount'] for t in transactions if t['transaction_type'] == 'deposit')
        total_withdrawals = sum(t['amount'] for t in transactions if t['transaction_type'] == 'withdrawal')
        total_transfers = sum(t['amount'] for t in transactions if t['transaction_type'] == 'transfer')
        
        # Group transactions by account
        account_breakdown = {}
        for account in accounts:
            account_id = account['id']
            account_transactions = [t for t in transactions if 
                                  t.get('account_id') == account_id or 
                                  t.get('from_account_id') == account_id or 
                                  t.get('to_account_id') == account_id]
            
            account_breakdown[account_id] = {
                'account': account,
                'transaction_count': len(account_transactions),
                'transactions': account_transactions
            }
        
        # Group transactions by type
        transaction_types = {}
        for t in transactions:
            t_type = t['transaction_type']
            if t_type not in transaction_types:
                transaction_types[t_type] = {'count': 0, 'total_amount': 0}
            transaction_types[t_type]['count'] += 1
            transaction_types[t_type]['total_amount'] += t['amount']
        
        # Generate report data
        report_data = {
            'user_id': current_user['user_id'],
            'username': current_user['username'],
            'period': {
                'start_date': start_date.isoformat(),
                'end_date': end_date.isoformat()
            },
            'summary': {
                'total_accounts': len(accounts),
                'total_transactions': total_transactions,
                'total_deposits': total_deposits,
                'total_withdrawals': total_withdrawals,
                'total_transfers': total_transfers,
                'total_balance': sum(account['balance'] for account in accounts)
            },
            'transaction_breakdown': transaction_types,
            'account_breakdown': account_breakdown,
            'transactions': transactions
        }
        
        # Save report
        parameters = {
            'start_date': start_date.isoformat(),
            'end_date': end_date.isoformat()
        }
        
        title = f"Transaction Report - {current_user['username']}"
        description = f"Cross-account transaction report for {current_user['username']} from {start_date.strftime('%Y-%m-%d')} to {end_date.strftime('%Y-%m-%d')}"
        
        report = save_report(current_user['user_id'], 'transactions', title, description, parameters, report_data)
        
        return jsonify({
            'report_id': report.id,
            'report': report_data
        }), 200
        
    except ValueError as e:
        return jsonify({'message': str(e)}), 400
    except Exception as e:
        logger.error(f"Error generating transaction report: {str(e)}")
        return jsonify({'message': 'Internal server error'}), 500

@app.route('/api/reports/system', methods=['GET'])
@token_required
@admin_required
def generate_system_report(current_user):
    """Generate system-wide report (admin only)"""
    try:
        # Get query parameters
        start_date_str = request.args.get('start_date')
        end_date_str = request.args.get('end_date')
        
        # Set default date range (last 30 days)
        end_date = parse_date(end_date_str) if end_date_str else datetime.now()
        start_date = parse_date(start_date_str) if start_date_str else end_date - timedelta(days=30)
        
        auth_header = request.headers.get('Authorization')
        
        # Get system-wide data
        users = get_all_users(auth_header)
        accounts = get_all_accounts(auth_header)
        transactions = get_all_transactions(auth_header)
        
        # Filter transactions by date
        filtered_transactions = []
        for t in transactions:
            try:
                t_date = datetime.fromisoformat(t['timestamp'].replace('Z', '+00:00')).replace(tzinfo=None)
                if start_date <= t_date <= end_date:
                    filtered_transactions.append(t)
            except:
                continue
        
        # Calculate system statistics
        total_users = len(users)
        active_users = len([u for u in users if u.get('status') == 'active'])
        total_accounts = len(accounts)
        active_accounts = len([a for a in accounts if a.get('status') == 'active'])
        closed_accounts = len([a for a in accounts if a.get('status') == 'closed'])
        
        # Account type distribution
        account_types = {}
        for account in accounts:
            a_type = account.get('account_type', 'unknown')
            if a_type not in account_types:
                account_types[a_type] = {'count': 0, 'total_balance': 0}
            account_types[a_type]['count'] += 1
            account_types[a_type]['total_balance'] += account.get('balance', 0)
        
        # Transaction statistics
        total_transactions = len(filtered_transactions)
        total_transaction_volume = sum(t.get('amount', 0) for t in filtered_transactions)
        
        # Transaction type breakdown
        transaction_types = {}
        for t in filtered_transactions:
            t_type = t.get('transaction_type', 'unknown')
            if t_type not in transaction_types:
                transaction_types[t_type] = {'count': 0, 'total_amount': 0}
            transaction_types[t_type]['count'] += 1
            transaction_types[t_type]['total_amount'] += t.get('amount', 0)
        
        # Financial metrics
        total_system_balance = sum(account.get('balance', 0) for account in accounts if account.get('status') == 'active')
        
        # Generate report data
        report_data = {
            'period': {
                'start_date': start_date.isoformat(),
                'end_date': end_date.isoformat()
            },
            'user_statistics': {
                'total_users': total_users,
                'active_users': active_users,
                'inactive_users': total_users - active_users
            },
            'account_statistics': {
                'total_accounts': total_accounts,
                'active_accounts': active_accounts,
                'closed_accounts': closed_accounts,
                'account_type_distribution': account_types,
                'total_system_balance': total_system_balance
            },
            'transaction_statistics': {
                'total_transactions': total_transactions,
                'total_transaction_volume': total_transaction_volume,
                'transaction_type_breakdown': transaction_types,
                'average_transaction_amount': total_transaction_volume / total_transactions if total_transactions > 0 else 0
            },
            'system_health': {
                'accounts_per_user': total_accounts / total_users if total_users > 0 else 0,
                'transactions_per_account': total_transactions / total_accounts if total_accounts > 0 else 0,
                'average_account_balance': total_system_balance / active_accounts if active_accounts > 0 else 0
            }
        }
        
        # Save report
        parameters = {
            'start_date': start_date.isoformat(),
            'end_date': end_date.isoformat()
        }
        
        title = f"System Report"
        description = f"System-wide statistics and analytics from {start_date.strftime('%Y-%m-%d')} to {end_date.strftime('%Y-%m-%d')}"
        
        report = save_report(current_user['user_id'], 'system', title, description, parameters, report_data)
        
        return jsonify({
            'report_id': report.id,
            'report': report_data
        }), 200
        
    except ValueError as e:
        return jsonify({'message': str(e)}), 400
    except Exception as e:
        logger.error(f"Error generating system report: {str(e)}")
        return jsonify({'message': 'Internal server error'}), 500

@app.route('/api/reports/list', methods=['GET'])
@token_required
def list_user_reports(current_user):
    """List user's historical reports"""
    try:
        with app.app_context():
            reports = Report.query.filter_by(user_id=current_user['user_id']).order_by(Report.created_at.desc()).all()
            
            return jsonify({
                'reports': [
                    {
                        'id': report.id,
                        'report_type': report.report_type,
                        'title': report.title,
                        'description': report.description,
                        'created_at': report.created_at.isoformat(),
                        'status': report.status
                    } for report in reports
                ]
            }), 200
            
    except Exception as e:
        logger.error(f"Error listing user reports: {str(e)}")
        return jsonify({'message': 'Internal server error'}), 500

@app.route('/api/reports/details/<report_id>', methods=['GET'])
@token_required
def get_report_details(current_user, report_id):
    """Retrieve specific report details"""
    try:
        with app.app_context():
            report = Report.query.filter_by(id=report_id).first()
            
            if not report:
                return jsonify({'message': 'Report not found'}), 404
            
            # Check if the report belongs to the current user (unless admin)
            if report.user_id != current_user['user_id'] and current_user['role'] != 'admin':
                return jsonify({'message': 'Access denied'}), 403
            
            return jsonify(report.to_dict()), 200
            
    except Exception as e:
        logger.error(f"Error getting report details: {str(e)}")
        return jsonify({'message': 'Internal server error'}), 500

@app.route('/api/reports/all', methods=['GET'])
@token_required
@admin_required
def list_all_reports(current_user):
    """List all system reports (admin only)"""
    try:
        with app.app_context():
            reports = Report.query.order_by(Report.created_at.desc()).all()
            
            return jsonify({
                'reports': [
                    {
                        'id': report.id,
                        'user_id': report.user_id,
                        'report_type': report.report_type,
                        'title': report.title,
                        'description': report.description,
                        'created_at': report.created_at.isoformat(),
                        'status': report.status
                    } for report in reports
                ]
            }), 200
            
    except Exception as e:
        logger.error(f"Error listing all reports: {str(e)}")
        return jsonify({'message': 'Internal server error'}), 500

if __name__ == '__main__':
    app.run(debug=True, host='localhost', port=8004)