import os
import json
import uuid
from datetime import datetime
from typing import List, Optional, Dict

class TransactionStorage:
    """File-based storage for transactions (legacy compatibility)"""
    
    def __init__(self, data_dir: str = "data"):
        self.data_dir = data_dir
        self.transactions_file = os.path.join(data_dir, "transactions.json")
        self._ensure_directory()
        self._load_transactions()
    
    def _ensure_directory(self):
        """Ensure the data directory exists"""
        if not os.path.exists(self.data_dir):
            os.makedirs(self.data_dir)
    
    def _load_transactions(self):
        """Load transactions from file"""
        if os.path.exists(self.transactions_file):
            try:
                with open(self.transactions_file, 'r') as f:
                    self.transactions = json.load(f)
            except (json.JSONDecodeError, IOError):
                self.transactions = []
        else:
            self.transactions = []
    
    def _save_transactions(self):
        """Save transactions to file"""
        try:
            with open(self.transactions_file, 'w') as f:
                json.dump(self.transactions, f, indent=2, default=str)
        except IOError as e:
            raise Exception(f"Failed to save transactions: {str(e)}")
    
    def create_transaction(self, transaction_data: Dict) -> Dict:
        """Create a new transaction"""
        transaction = {
            'id': str(uuid.uuid4()),
            'user_id': transaction_data['user_id'],
            'account_id': transaction_data.get('account_id'),
            'from_account_id': transaction_data.get('from_account_id'),
            'to_account_id': transaction_data.get('to_account_id'),
            'transaction_type': transaction_data['transaction_type'],
            'transfer_type': transaction_data.get('transfer_type'),
            'amount': transaction_data['amount'],
            'description': transaction_data.get('description', ''),
            'status': transaction_data.get('status', 'completed'),
            'timestamp': datetime.utcnow().isoformat(),
            'reference_number': f"TXN{uuid.uuid4().hex[:16].upper()}"
        }
        
        self.transactions.append(transaction)
        self._save_transactions()
        return transaction
    
    def get_transaction(self, transaction_id: str) -> Optional[Dict]:
        """Get a transaction by ID"""
        for transaction in self.transactions:
            if transaction['id'] == transaction_id:
                return transaction
        return None
    
    def get_transactions_by_user(self, user_id: str, limit: Optional[int] = None) -> List[Dict]:
        """Get all transactions for a user"""
        user_transactions = [
            t for t in self.transactions 
            if t['user_id'] == user_id
        ]
        
        # Sort by timestamp (newest first)
        user_transactions.sort(key=lambda x: x['timestamp'], reverse=True)
        
        if limit:
            return user_transactions[:limit]
        return user_transactions
    
    def get_transactions_by_account(self, account_id: str) -> List[Dict]:
        """Get all transactions for an account"""
        account_transactions = [
            t for t in self.transactions 
            if (t.get('account_id') == account_id or 
                t.get('from_account_id') == account_id or 
                t.get('to_account_id') == account_id)
        ]
        
        # Sort by timestamp (newest first)
        account_transactions.sort(key=lambda x: x['timestamp'], reverse=True)
        return account_transactions
    
    def get_all_transactions(self) -> List[Dict]:
        """Get all transactions (admin only)"""
        # Sort by timestamp (newest first)
        all_transactions = sorted(self.transactions, key=lambda x: x['timestamp'], reverse=True)
        return all_transactions
    
    def update_transaction_status(self, transaction_id: str, status: str) -> Optional[Dict]:
        """Update transaction status"""
        for transaction in self.transactions:
            if transaction['id'] == transaction_id:
                transaction['status'] = status
                self._save_transactions()
                return transaction
        return None
    
    def get_recent_transactions(self, user_id: str, limit: int = 10) -> List[Dict]:
        """Get recent transactions for a user"""
        return self.get_transactions_by_user(user_id, limit)