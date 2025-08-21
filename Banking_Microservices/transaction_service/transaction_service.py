import os
import consul
import logging
import jwt
import requests
from datetime import datetime
from functools import wraps
from flask import Flask, request, jsonify
from transaction_service.transaction_models import db, Transaction
from transaction_service.transaction_storage import TransactionStorage

# Configure logging
logging.basicConfig(level=logging.DEBUG)
logger = logging.getLogger(__name__)

# Consul configuration
CONSUL_HOST = os.environ.get("CONSUL_HOST", "localhost")
CONSUL_PORT = int(os.environ.get("CONSUL_PORT", 8500))
SERVICE_NAME = "transaction-service"
SERVICE_ID = f"{SERVICE_NAME}-{os.urandom(8).hex()}"
SERVICE_PORT = 8003

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

# Initialize Flask app
app = Flask(__name__)
app.config["SQLALCHEMY_DATABASE_URI"] = os.environ.get("TRANSACTION_DATABASE_URL")
app.config["JWT_SECRET_KEY"] = os.environ.get("SESSION_SECRET", "transaction_service_secret_key")

# Initialize extensions
db.init_app(app)

# Create tables
with app.app_context():
    db.create_all()

# Register service on startup (unless disabled)
if not os.environ.get("DISABLE_CONSUL", "false").lower() == "true":
    with app.app_context():
        register_service()

# Deregister service on shutdown (unless disabled)
import atexit
if not os.environ.get("DISABLE_CONSUL", "false").lower() == "true":
    atexit.register(deregister_service)

# Initialize legacy storage for compatibility
storage = TransactionStorage()

# Service URLs
AUTH_SERVICE_URL = os.environ.get("AUTH_SERVICE_URL", "http://localhost:8001")
ACCOUNT_SERVICE_URL = os.environ.get("ACCOUNT_SERVICE_URL", "http://localhost:8002")

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

def validate_account_ownership(account_id, user_id, token):
    """Validate that the account belongs to the user"""
    try:
        response = requests.get(
            f"{ACCOUNT_SERVICE_URL}/api/accounts/details/{account_id}",
            headers={'Authorization': f'Bearer {token}'}
        )
        if not response.ok:
            return False, "Account not found"
        
        account_data = response.json()
        if account_data['user_id'] != user_id:
            return False, "Access denied"
        
        return True, account_data
    except requests.RequestException:
        return False, "Account service unavailable"

def validate_account_exists(account_id):
    """Validate that an account exists and is active"""
    try:
        response = requests.get(f"{ACCOUNT_SERVICE_URL}/api/accounts/validate/{account_id}")
        if response.ok:
            result = response.json()
            return result.get('valid', False), result.get('message', ''), result.get('account')
        return False, "Account validation failed", None
    except requests.RequestException:
        return False, "Account service unavailable", None

def update_account_balance(account_id, amount, operation):
    """Update account balance through account service"""
    try:
        response = requests.post(
            f"{ACCOUNT_SERVICE_URL}/api/accounts/balance/update",
            json={
                'account_id': account_id,
                'amount': amount,
                'operation': operation
            }
        )
        return response.ok, response.json() if response.ok else response.text
    except requests.RequestException:
        return False, "Account service unavailable"

# Routes
@app.route('/api/health', methods=['GET'])
def health_check():
    """Health check endpoint"""
    return jsonify({'status': 'healthy', 'service': 'transaction-service'}), 200

@app.route('/api/transactions/deposit', methods=['POST'])
@token_required
def create_deposit(current_user):
    """Process a deposit transaction"""
    data = request.json
    
    # Validate required fields
    required_fields = ['account_id', 'amount']
    if not all(field in data for field in required_fields):
        return jsonify({'message': 'Missing required fields: account_id, amount'}), 400
    
    account_id = data['account_id']
    amount = data['amount']
    description = data.get('description', 'Deposit')
    
    # Validate amount
    try:
        amount = float(amount)
        if amount <= 0:
            return jsonify({'message': 'Amount must be positive'}), 400
    except (ValueError, TypeError):
        return jsonify({'message': 'Invalid amount'}), 400
    
    # Validate account ownership
    token = request.headers.get('Authorization').split(' ')[1]
    is_valid, account_data = validate_account_ownership(account_id, current_user['user_id'], token)
    if not is_valid:
        return jsonify({'message': account_data}), 403
    
    # Update account balance
    success, result = update_account_balance(account_id, amount, 'credit')
    if not success:
        return jsonify({'message': f'Failed to update balance: {result}'}), 400
    
    # Create transaction record
    with app.app_context():
        transaction = Transaction(
            user_id=current_user['user_id'],
            account_id=account_id,
            transaction_type='deposit',
            amount=amount,
            description=description,
            status='completed'
        )
        
        db.session.add(transaction)
        db.session.commit()
        
        return jsonify({
            'message': 'Deposit processed successfully',
            'transaction': transaction.to_dict()
        }), 201

@app.route('/api/transactions/withdraw', methods=['POST'])
@token_required
def create_withdrawal(current_user):
    """Process a withdrawal transaction"""
    data = request.json
    
    # Validate required fields
    required_fields = ['account_id', 'amount']
    if not all(field in data for field in required_fields):
        return jsonify({'message': 'Missing required fields: account_id, amount'}), 400
    
    account_id = data['account_id']
    amount = data['amount']
    description = data.get('description', 'Withdrawal')
    
    # Validate amount
    try:
        amount = float(amount)
        if amount <= 0:
            return jsonify({'message': 'Amount must be positive'}), 400
    except (ValueError, TypeError):
        return jsonify({'message': 'Invalid amount'}), 400
    
    # Validate account ownership
    token = request.headers.get('Authorization').split(' ')[1]
    is_valid, account_data = validate_account_ownership(account_id, current_user['user_id'], token)
    if not is_valid:
        return jsonify({'message': account_data}), 403
    
    # Update account balance (debit operation will check sufficient funds)
    success, result = update_account_balance(account_id, amount, 'debit')
    if not success:
        return jsonify({'message': f'Failed to process withdrawal: {result}'}), 400
    
    # Create transaction record
    with app.app_context():
        transaction = Transaction(
            user_id=current_user['user_id'],
            account_id=account_id,
            transaction_type='withdrawal',
            amount=amount,
            description=description,
            status='completed'
        )
        
        db.session.add(transaction)
        db.session.commit()
        
        return jsonify({
            'message': 'Withdrawal processed successfully',
            'transaction': transaction.to_dict()
        }), 201

@app.route('/api/transactions/transfer', methods=['POST'])
@token_required
def create_transfer(current_user):
    """Process a transfer transaction"""
    data = request.json
    
    # Validate required fields
    required_fields = ['from_account_id', 'to_account_id', 'amount']
    if not all(field in data for field in required_fields):
        return jsonify({'message': 'Missing required fields: from_account_id, to_account_id, amount'}), 400
    
    from_account_id = data['from_account_id']
    to_account_id = data['to_account_id']
    amount = data['amount']
    description = data.get('description', 'Transfer')
    
    # Validate amount
    try:
        amount = float(amount)
        if amount <= 0:
            return jsonify({'message': 'Amount must be positive'}), 400
    except (ValueError, TypeError):
        return jsonify({'message': 'Invalid amount'}), 400
    
    # Check if transferring to the same account
    if from_account_id == to_account_id:
        return jsonify({'message': 'Cannot transfer to the same account'}), 400
    
    # Validate source account ownership
    token = request.headers.get('Authorization').split(' ')[1]
    is_valid, from_account_data = validate_account_ownership(from_account_id, current_user['user_id'], token)
    if not is_valid:
        return jsonify({'message': f'Source account: {from_account_data}'}), 403
    
    # Validate destination account exists
    to_valid, to_message, to_account_data = validate_account_exists(to_account_id)
    if not to_valid:
        return jsonify({'message': f'Destination account: {to_message}'}), 400
    
    # Determine transfer type
    transfer_type = 'internal' if to_account_data['user_id'] == current_user['user_id'] else 'external'
    
    # Process the transfer (two-phase operation)
    try:
        # Phase 1: Debit from source account
        success, result = update_account_balance(from_account_id, amount, 'debit')
        if not success:
            return jsonify({'message': f'Transfer failed: {result}'}), 400
        
        # Phase 2: Credit to destination account
        success, result = update_account_balance(to_account_id, amount, 'credit')
        if not success:
            # Rollback: Credit back to source account
            rollback_success, _ = update_account_balance(from_account_id, amount, 'credit')
            if not rollback_success:
                logger.error(f"CRITICAL: Failed to rollback transfer for account {from_account_id}")
            return jsonify({'message': f'Transfer failed during credit phase: {result}'}), 400
        
        # Create transaction record
        with app.app_context():
            transaction = Transaction(
                user_id=current_user['user_id'],
                from_account_id=from_account_id,
                to_account_id=to_account_id,
                transaction_type='transfer',
                transfer_type=transfer_type,
                amount=amount,
                description=description,
                status='completed'
            )
            
            db.session.add(transaction)
            db.session.commit()
            
            return jsonify({
                'message': 'Transfer processed successfully',
                'transaction': transaction.to_dict()
            }), 201
            
    except Exception as e:
        logger.error(f"Transfer error: {str(e)}")
        return jsonify({'message': 'Transfer failed due to internal error'}), 500

@app.route('/api/transactions/list', methods=['GET'])
@token_required
def list_transactions(current_user):
    """Get all transactions for the current user"""
    limit = request.args.get('limit', type=int)
    
    with app.app_context():
        query = Transaction.query.filter_by(user_id=current_user['user_id']).order_by(Transaction.timestamp.desc())
        
        if limit:
            transactions = query.limit(limit).all()
        else:
            transactions = query.all()
        
        return jsonify({
            'transactions': [transaction.to_dict() for transaction in transactions]
        }), 200

@app.route('/api/transactions/recent', methods=['GET'])
@token_required
def get_recent_transactions(current_user):
    """Get recent transactions for the current user"""
    limit = request.args.get('limit', default=10, type=int)
    
    with app.app_context():
        transactions = Transaction.query.filter_by(
            user_id=current_user['user_id']
        ).order_by(Transaction.timestamp.desc()).limit(limit).all()
        
        return jsonify({
            'transactions': [transaction.to_dict() for transaction in transactions]
        }), 200

@app.route('/api/transactions/account/<account_id>', methods=['GET'])
@token_required
def get_account_transactions(current_user, account_id):
    """Get all transactions for a specific account"""
    # Validate account ownership
    token = request.headers.get('Authorization').split(' ')[1]
    is_valid, account_data = validate_account_ownership(account_id, current_user['user_id'], token)
    if not is_valid:
        return jsonify({'message': account_data}), 403
    
    with app.app_context():
        transactions = Transaction.query.filter(
            (Transaction.account_id == account_id) |
            (Transaction.from_account_id == account_id) |
            (Transaction.to_account_id == account_id)
        ).order_by(Transaction.timestamp.desc()).all()
        
        return jsonify({
            'transactions': [transaction.to_dict() for transaction in transactions]
        }), 200

@app.route('/api/transactions/details/<transaction_id>', methods=['GET'])
@token_required
def get_transaction_details(current_user, transaction_id):
    """Get detailed information about a specific transaction"""
    with app.app_context():
        transaction = Transaction.query.filter_by(id=transaction_id).first()
        
        if not transaction:
            return jsonify({'message': 'Transaction not found'}), 404
        
        # Check if the transaction belongs to the current user
        if transaction.user_id != current_user['user_id'] and current_user['role'] != 'admin':
            return jsonify({'message': 'Access denied'}), 403
        
        return jsonify(transaction.to_dict()), 200

@app.route('/api/transactions/all', methods=['GET'])
@token_required
@admin_required
def get_all_transactions(current_user):
    """Get all transactions (admin only)"""
    limit = request.args.get('limit', type=int)
    
    with app.app_context():
        query = Transaction.query.order_by(Transaction.timestamp.desc())
        
        if limit:
            transactions = query.limit(limit).all()
        else:
            transactions = query.all()
        
        return jsonify({
            'transactions': [transaction.to_dict() for transaction in transactions]
        }), 200

@app.route('/api/transactions/record', methods=['POST'])
def record_transaction():
    """Internal endpoint for recording transactions (for service-to-service communication)"""
    # This endpoint should be protected in production but is left open for the demo
    data = request.json
    
    # Validate required fields
    required_fields = ['user_id', 'transaction_type', 'amount']
    if not all(field in data for field in required_fields):
        return jsonify({'message': 'Missing required fields'}), 400
    
    with app.app_context():
        transaction = Transaction(
            user_id=data['user_id'],
            account_id=data.get('account_id'),
            from_account_id=data.get('from_account_id'),
            to_account_id=data.get('to_account_id'),
            transaction_type=data['transaction_type'],
            transfer_type=data.get('transfer_type'),
            amount=data['amount'],
            description=data.get('description', ''),
            status=data.get('status', 'completed')
        )
        
        db.session.add(transaction)
        db.session.commit()
        
        return jsonify({
            'message': 'Transaction recorded successfully',
            'transaction': transaction.to_dict()
        }), 201

if __name__ == '__main__':
    app.run(debug=True, host='localhost', port=8003)