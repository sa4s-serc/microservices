from datetime import datetime, timedelta
from typing import Dict
import jwt
import os

JWT_SECRET_KEY = "your-secret-key-for-development-only"  # Use env vars in production
JWT_ALGORITHM = "HS256"
TOKEN_EXPIRE_MINUTES = 60 * 24  # 24 hours

def create_access_token(data: Dict) -> str:
    """Create a new JWT token"""
    to_encode = data.copy()
    
    # Set expiration
    expire = datetime.utcnow() + timedelta(minutes=TOKEN_EXPIRE_MINUTES)
    to_encode.update({"exp": expire.timestamp()})
    
    # Create token
    encoded_jwt = jwt.encode(to_encode, JWT_SECRET_KEY, algorithm=JWT_ALGORITHM)
    return encoded_jwt