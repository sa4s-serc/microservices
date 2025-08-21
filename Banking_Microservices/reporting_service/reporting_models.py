import os
import uuid
from datetime import datetime
from flask_sqlalchemy import SQLAlchemy
from sqlalchemy.orm import DeclarativeBase

class Base(DeclarativeBase):
    pass

db = SQLAlchemy(model_class=Base)

class Report(db.Model):
    """Report model for reporting service"""
    __tablename__ = 'reports'
    
    id = db.Column(db.String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    user_id = db.Column(db.String(36), nullable=False, index=True)
    report_type = db.Column(db.String(50), nullable=False)
    title = db.Column(db.String(200), nullable=False)
    description = db.Column(db.String(500))
    parameters = db.Column(db.JSON)  # Store report parameters as JSON
    data = db.Column(db.JSON)  # Store report data as JSON
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    status = db.Column(db.String(20), default='completed')
    
    def to_dict(self):
        """Convert Report object to dictionary"""
        return {
            'id': self.id,
            'user_id': self.user_id,
            'report_type': self.report_type,
            'title': self.title,
            'description': self.description,
            'parameters': self.parameters,
            'data': self.data,
            'created_at': self.created_at.isoformat(),
            'status': self.status
        }