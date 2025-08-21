import os
import uuid
from datetime import datetime
from flask_sqlalchemy import SQLAlchemy
from sqlalchemy.orm import DeclarativeBase

class Base(DeclarativeBase):
    pass

db = SQLAlchemy(model_class=Base)

class Transaction(db.Model):
    """Transaction model for transaction service"""
    __tablename__ = 'transactions'
    
    id = db.Column(db.String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    user_id = db.Column(db.String(36), nullable=False, index=True)
    account_id = db.Column(db.String(36), nullable=True, index=True)  # For deposits/withdrawals
    from_account_id = db.Column(db.String(36), nullable=True, index=True)  # For transfers
    to_account_id = db.Column(db.String(36), nullable=True, index=True)  # For transfers
    transaction_type = db.Column(db.String(20), nullable=False)  # deposit, withdrawal, transfer
    transfer_type = db.Column(db.String(20), nullable=True)  # internal, external for transfers
    amount = db.Column(db.Float, nullable=False)
    description = db.Column(db.String(200), nullable=True)
    status = db.Column(db.String(20), default='completed')  # pending, completed, failed
    timestamp = db.Column(db.DateTime, default=datetime.utcnow)
    reference_number = db.Column(db.String(32), unique=True, nullable=False,
                                default=lambda: f"TXN{uuid.uuid4().hex[:16].upper()}")
    
    def to_dict(self):
        """Convert Transaction object to dictionary"""
        return {
            'id': self.id,
            'user_id': self.user_id,
            'account_id': self.account_id,
            'from_account_id': self.from_account_id,
            'to_account_id': self.to_account_id,
            'transaction_type': self.transaction_type,
            'transfer_type': self.transfer_type,
            'amount': self.amount,
            'description': self.description,
            'status': self.status,
            'timestamp': self.timestamp.isoformat(),
            'reference_number': self.reference_number
        }
    
    @classmethod
    def from_dict(cls, data):
        """Create Transaction object from dictionary"""
        transaction = cls(
            user_id=data.get('user_id'),
            account_id=data.get('account_id'),
            from_account_id=data.get('from_account_id'),
            to_account_id=data.get('to_account_id'),
            transaction_type=data.get('transaction_type'),
            transfer_type=data.get('transfer_type'),
            amount=data.get('amount'),
            description=data.get('description'),
            status=data.get('status', 'completed'),
            id=data.get('id')
        )
        return transaction