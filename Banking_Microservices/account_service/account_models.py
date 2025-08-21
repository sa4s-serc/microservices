import os
import uuid
from datetime import datetime
from flask_sqlalchemy import SQLAlchemy
from sqlalchemy.orm import DeclarativeBase

class Base(DeclarativeBase):
    pass

db = SQLAlchemy(model_class=Base)

class Account(db.Model):
    """Account model for account service"""
    __tablename__ = 'accounts'
    
    id = db.Column(db.String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    user_id = db.Column(db.String(36), nullable=False, index=True)
    account_number = db.Column(db.String(20), unique=True, nullable=False)
    account_type = db.Column(db.String(20), nullable=False)
    balance = db.Column(db.Float, default=0.0)
    status = db.Column(db.String(20), default='active')
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    
    def __init__(self, user_id, account_number, account_type, balance=0.0, status='active'):
        """Initialize Account object"""
        self.id = str(uuid.uuid4())
        self.user_id = user_id
        self.account_number = account_number
        self.account_type = account_type
        self.balance = balance
        self.status = status
        self.created_at = datetime.utcnow()
    
    def to_dict(self):
        """Convert Account object to dictionary"""
        return {
            'id': self.id,
            'user_id': self.user_id,
            'account_number': self.account_number,
            'account_type': self.account_type,
            'balance': round(self.balance, 2),  # Round to 2 decimal places for currency
            'status': self.status,
            'created_at': self.created_at.isoformat() if self.created_at else None
        }
    
    @classmethod
    def from_dict(cls, data):
        """Create Account object from dictionary"""
        account = cls(
            user_id=data.get('user_id'),
            account_number=data.get('account_number'),
            account_type=data.get('account_type'),
            balance=data.get('balance', 0.0),
            status=data.get('status', 'active')
        )
        if 'id' in data:
            account.id = data['id']
        if 'created_at' in data:
            account.created_at = datetime.fromisoformat(data['created_at']) if isinstance(data['created_at'], str) else data['created_at']
        return account
    
    def __repr__(self):
        """String representation of Account object"""
        return f'<Account {self.account_number}: {self.account_type} - ${self.balance}>'