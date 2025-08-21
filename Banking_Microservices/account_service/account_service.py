import os
import consul
import logging
import requests
import uuid
from datetime import datetime
from functools import wraps
from flask import Flask, request, jsonify
from account_service.account_models import db, Account

# Configure logging
logging.basicConfig(level=logging.DEBUG)
logger = logging.getLogger(__name__)

# Consul configuration
CONSUL_HOST = os.environ.get("CONSUL_HOST", "localhost")
CONSUL_PORT = int(os.environ.get("CONSUL_PORT", 8500))
SERVICE_NAME = "account-service"
SERVICE_ID = f"{SERVICE_NAME}-{os.urandom(8).hex()}"
SERVICE_PORT = 8002

# Initialize Consul client
consul_client = consul.Consul(host=CONSUL_HOST, port=CONSUL_PORT)

# Initialize Flask app
app = Flask(__name__)
app.config["SQLALCHEMY_DATABASE_URI"] = os.environ.get(
    "ACCOUNT_DATABASE_URL", 
    f"sqlite:///{os.path.join(os.path.dirname(__file__), 'account.db')}"
)
app.config["SQLALCHEMY_TRACK_MODIFICATIONS"] = False
app.config["JWT_SECRET_KEY"] = os.environ.get("SESSION_SECRET", "account_service_secret_key")

# Initialize extensions
db.init_app(app)

# Create tables
with app.app_context():
    db.create_all()

# Authentication service URL
AUTH_SERVICE_URL = os.environ.get("AUTH_SERVICE_URL", "http://localhost:8001")

# Register service with Consul
def register_service():
    """Register service with Consul"""
    try:
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
    except Exception as e:
        logger.warning(f"Failed to register with Consul: {e}")

# Deregister service from Consul
def deregister_service():
    """Deregister service from Consul"""
    try:
        consul_client.agent.service.deregister(SERVICE_ID)
        logger.info(f"Deregistered service from Consul: {SERVICE_ID}")
    except Exception as e:
        logger.warning(f"Failed to deregister from Consul: {e}")

# Register service on startup (unless disabled)
if not os.environ.get("DISABLE_CONSUL", "false").lower() == "true":
    with app.app_context():
        register_service()

# Deregister service on shutdown (unless disabled)
import atexit
if not os.environ.get("DISABLE_CONSUL", "false").lower() == "true":
    atexit.register(deregister_service)

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
                headers={'Authorization': f'Bearer {token}'},
                timeout=5
            )
            
            if not response.ok:
                return jsonify({'message': 'Invalid or expired token'}), 401
            
            current_user = response.json()
                
        except requests.RequestException as e:
            logger.error(f"Auth service request failed: {e}")
            return jsonify({'message': 'Authorization service unavailable'}), 503
        except Exception as e:
            logger.error(f"Token verification failed: {e}")
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
                headers={'Authorization': f'Bearer {token}'},
                timeout=5
            )
            
            if not response.ok or not response.json().get('is_admin'):
                return jsonify({'message': 'Admin privilege required'}), 403
                
        except requests.RequestException as e:
            logger.error(f"Admin verification request failed: {e}")
            return jsonify({'message': 'Authorization service unavailable'}), 503
        
        return f(current_user, *args, **kwargs)
    
    return decorated

def generate_account_number():
    """Generate a unique account number with ACC prefix"""
    prefix = "ACC"
    random_part = uuid.uuid4().hex[:8].upper()
    return f"{prefix}{random_part}"

# Routes
@app.route('/api/health', methods=['GET'])
def health_check():
    """Health check endpoint"""
    try:
        # Check database connection
        with app.app_context():
            db.session.execute(db.text('SELECT 1'))
        return jsonify({'status': 'healthy', 'service': 'account-service'}), 200
    except Exception as e:
        logger.error(f"Health check failed: {e}")
        return jsonify({'status': 'unhealthy', 'service': 'account-service'}), 503

@app.route('/api/accounts/list', methods=['GET'])
@token_required
def list_accounts(current_user):
    """List all accounts for the current user"""
    try:
        with app.app_context():
            accounts = Account.query.filter_by(user_id=current_user['user_id']).all()
            return jsonify({
                'success': True,
                'accounts': [account.to_dict() for account in accounts]
            }), 200
    except Exception as e:
        logger.error(f"Error listing accounts: {e}")
        return jsonify({'success': False, 'message': 'Failed to retrieve accounts'}), 500

@app.route('/api/accounts/create', methods=['POST'])
@token_required
def create_account(current_user):
    """Create a new account for the current user"""
    try:
        data = request.json
        
        # Validate required fields
        if 'account_type' not in data:
            return jsonify({'success': False, 'message': 'Account type is required'}), 400
        
        # Check for valid account types
        valid_types = ['checking', 'savings', 'fixed_deposit', 'investment']
        if data['account_type'] not in valid_types:
            return jsonify({
                'success': False,
                'message': f'Invalid account type. Must be one of: {", ".join(valid_types)}'
            }), 400
        
        # Handle initial deposit
        initial_deposit_raw = data.get('initial_deposit', '')
        if isinstance(initial_deposit_raw, str) and initial_deposit_raw.strip() == '':
            initial_deposit = 0.0
        else:
            try:
                initial_deposit = float(initial_deposit_raw)
            except (ValueError, TypeError):
                return jsonify({'success': False, 'message': 'Initial deposit must be a number'}), 400

        if initial_deposit < 0:
            return jsonify({'success': False, 'message': 'Initial deposit cannot be negative'}), 400
        
        with app.app_context():
            # Generate unique account number
            account_number = generate_account_number()
            
            # Ensure account number is unique
            while Account.query.filter_by(account_number=account_number).first():
                account_number = generate_account_number()
            
            # Create new account
            new_account = Account(
                user_id=current_user['user_id'],
                account_number=account_number,
                account_type=data['account_type'],
                balance=initial_deposit
            )
            
            db.session.add(new_account)
            db.session.commit()
            
            logger.info(f"Account created: {new_account.id} for user {current_user['user_id']}")
            
            return jsonify({
                'success': True,
                'message': 'Account created successfully',
                'account': new_account.to_dict()
            }), 201
    except Exception as e:
        logger.error(f"Error creating account: {e}")
        return jsonify({'success': False, 'message': 'Failed to create account'}), 500

@app.route('/api/accounts/details/<account_id>', methods=['GET'])
@token_required
def get_account_details(current_user, account_id):
    """Get details of a specific account"""
    try:
        with app.app_context():
            account = Account.query.filter_by(id=account_id).first()
            
            if not account:
                return jsonify({'success': False, 'message': 'Account not found'}), 404
            
            # Check if the account belongs to the current user or user is admin
            if account.user_id != current_user['user_id'] and current_user.get('role') != 'admin':
                return jsonify({'success': False, 'message': 'Access denied'}), 403
                
            return jsonify({
                'success': True,
                'account': account.to_dict()
            }), 200
    except Exception as e:
        logger.error(f"Error getting account details: {e}")
        return jsonify({'success': False, 'message': 'Failed to retrieve account details'}), 500

@app.route('/api/accounts/close/<account_id>', methods=['DELETE'])
@token_required
def close_account(current_user, account_id):
    """Close an account"""
    try:
        with app.app_context():
            account = Account.query.filter_by(id=account_id).first()
            
            if not account:
                return jsonify({'success': False, 'message': 'Account not found'}), 404
            
            # Check if the account belongs to the current user or user is admin
            if account.user_id != current_user['user_id'] and current_user.get('role') != 'admin':
                return jsonify({'success': False, 'message': 'Access denied'}), 403
            
            # Check if the account is already closed
            if account.status != 'active':
                return jsonify({'success': False, 'message': 'Account is already closed'}), 400
            
            # Check if the account has zero balance
            if account.balance != 0.0:
                return jsonify({
                    'success': False,
                    'message': 'Account must have zero balance before closing'
                }), 400
            
            # Close the account
            account.status = 'closed'
            db.session.commit()
            
            logger.info(f"Account closed: {account.id}")
            
            return jsonify({
                'success': True,
                'message': 'Account closed successfully',
                'account': account.to_dict()
            }), 200
    except Exception as e:
        logger.error(f"Error closing account: {e}")
        return jsonify({'success': False, 'message': 'Failed to close account'}), 500

@app.route('/api/accounts/all', methods=['GET'])
@token_required
@admin_required
def get_all_accounts(current_user):
    """Get all accounts (admin only)"""
    try:
        with app.app_context():
            accounts = Account.query.all()
            return jsonify({
                'success': True,
                'accounts': [account.to_dict() for account in accounts]
            }), 200
    except Exception as e:
        logger.error(f"Error getting all accounts: {e}")
        return jsonify({'success': False, 'message': 'Failed to retrieve accounts'}), 500

@app.route('/api/accounts/update/<account_id>', methods=['PUT'])
@token_required
@admin_required
def update_account(current_user, account_id):
    """Update account details (admin only)"""
    try:
        data = request.json
        
        with app.app_context():
            account = Account.query.filter_by(id=account_id).first()
            
            if not account:
                return jsonify({'success': False, 'message': 'Account not found'}), 404
            
            # Fields that admins can update
            allowed_fields = ['account_type', 'status']
            updates = {k: v for k, v in data.items() if k in allowed_fields}
            
            if not updates:
                return jsonify({'success': False, 'message': 'No valid fields to update'}), 400
            
            # Validate account_type if provided
            if 'account_type' in updates:
                valid_types = ['checking', 'savings', 'fixed_deposit', 'investment']
                if updates['account_type'] not in valid_types:
                    return jsonify({
                        'success': False,
                        'message': f'Invalid account type. Must be one of: {", ".join(valid_types)}'
                    }), 400
            
            # Validate status if provided
            if 'status' in updates:
                valid_statuses = ['active', 'closed']
                if updates['status'] not in valid_statuses:
                    return jsonify({
                        'success': False,
                        'message': f'Invalid status. Must be one of: {", ".join(valid_statuses)}'
                    }), 400
            
            for key, value in updates.items():
                setattr(account, key, value)
            
            db.session.commit()
            
            logger.info(f"Account updated: {account.id}")
            
            return jsonify({
                'success': True,
                'message': 'Account updated successfully',
                'account': account.to_dict()
            }), 200
    except Exception as e:
        logger.error(f"Error updating account: {e}")
        return jsonify({'success': False, 'message': 'Failed to update account'}), 500

@app.route('/api/accounts/user/<user_id>', methods=['GET'])
@token_required
@admin_required
def get_user_accounts(current_user, user_id):
    """Get accounts for a specific user (admin only)"""
    try:
        with app.app_context():
            accounts = Account.query.filter_by(user_id=user_id).all()
            return jsonify({
                'success': True,
                'accounts': [account.to_dict() for account in accounts]
            }), 200
    except Exception as e:
        logger.error(f"Error getting user accounts: {e}")
        return jsonify({'success': False, 'message': 'Failed to retrieve user accounts'}), 500

@app.route('/api/accounts/balance/update', methods=['POST'])
def update_balance():
    """Update account balance (internal use by transaction service)"""
    try:
        data = request.json
        
        # Validate required fields
        required_fields = ['account_id', 'amount', 'operation']
        if not all(field in data for field in required_fields):
            return jsonify({'success': False, 'message': 'Missing required fields'}), 400
        
        account_id = data['account_id']
        try:
            amount = float(data['amount'])
        except (ValueError, TypeError):
            return jsonify({'success': False, 'message': 'Amount must be a number'}), 400
        
        operation = data['operation']
        
        if operation not in ['credit', 'debit']:
            return jsonify({
                'success': False,
                'message': 'Invalid operation. Must be either "credit" or "debit"'
            }), 400
        
        if amount < 0:
            return jsonify({'success': False, 'message': 'Amount cannot be negative'}), 400
        
        with app.app_context():
            account = Account.query.filter_by(id=account_id).first()
            
            if not account:
                return jsonify({'success': False, 'message': 'Account not found'}), 404
            
            if account.status != 'active':
                return jsonify({'success': False, 'message': 'Account is not active'}), 400
            
            if operation == 'credit':
                account.balance += amount
            else:  # debit
                if account.balance < amount:
                    return jsonify({'success': False, 'message': 'Insufficient funds'}), 400
                account.balance -= amount
            
            db.session.commit()
            
            logger.info(f"Balance updated: {account.id}, operation: {operation}, amount: {amount}")
            
            return jsonify({
                'success': True,
                'message': 'Balance updated successfully',
                'account': account.to_dict()
            }), 200
    except Exception as e:
        logger.error(f"Error updating balance: {e}")
        return jsonify({'success': False, 'message': 'Failed to update balance'}), 500

@app.route('/api/accounts/validate/<account_id>', methods=['GET'])
def validate_account(account_id):
    """Validate if an account exists and is active (internal use)"""
    try:
        with app.app_context():
            account = Account.query.filter_by(id=account_id).first()
            
            if not account:
                return jsonify({
                    'valid': False,
                    'message': 'Account not found'
                }), 200
            
            if account.status != 'active':
                return jsonify({
                    'valid': False,
                    'message': 'Account is not active'
                }), 200
            
            return jsonify({
                'valid': True,
                'account': account.to_dict()
            }), 200
    except Exception as e:
        logger.error(f"Error validating account: {e}")
        return jsonify({
            'valid': False,
            'message': 'Error validating account'
        }), 500

if __name__ == '__main__':
    logger.info("Starting Account Service on port 8002")
    app.run(debug=True, host='0.0.0.0', port=8002)