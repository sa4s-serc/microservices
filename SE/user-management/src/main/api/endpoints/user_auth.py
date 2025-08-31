from fastapi import APIRouter, HTTPException, status
from typing import Annotated

# Adjust import paths
from ...schemas.user import UserCreate, UserLogin, UserResponse
from ...services.auth_service import (
    register_user_service,
    login_user_service,
    get_user_details_service
)
from ...utils.auth_token_gen import create_access_token

router = APIRouter()

@router.post("/register", response_model=UserResponse, status_code=status.HTTP_201_CREATED)
def register_user(user: UserCreate):
    """Registers a new user."""
    created_user = register_user_service(
        name=user.name,
        email=user.email,
        password=user.password,
        contact=user.contact
    )
    if not created_user:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT, # 409 Conflict for existing email
            detail="Email already registered."
        )
    # Ensure the response matches UserResponse schema (id, name, email, contact)
    return UserResponse(**created_user)

@router.post("/login")
def login_user(user: UserLogin):
    """Authenticates a user and returns user details and access token upon success."""
    logged_in_user = login_user_service(
        email=user.email,
        password=user.password
    )
    if not logged_in_user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect email or password",
            headers={"WWW-Authenticate": "Bearer"}, # Standard practice
        )
    
    # Create access token
    access_token = create_access_token(
        data={"sub": str(logged_in_user["id"]), "email": logged_in_user["email"]}
    )
    
    # Return user details along with access token
    return {
        **logged_in_user,
        "access_token": access_token,
        "token_type": "bearer"
    }

@router.get("/users/{user_id}", response_model=UserResponse)
def get_user_details(user_id: int):
    """Gets user details by ID."""
    user = get_user_details_service(user_id)
    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"User with ID {user_id} not found."
        )
    return UserResponse(**user) 