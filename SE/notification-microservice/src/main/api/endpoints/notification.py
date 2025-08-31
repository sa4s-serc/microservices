from fastapi import APIRouter, HTTPException, status
from typing import List
from datetime import datetime

from src.main.schemas.notification import (
    EmailNotificationCreate,
    EmailNotificationResponse,
    EmailNotificationUpdate,
    CalendarEventCreate,
    CalendarEventResponse,
    CalendarEventUpdate
)
from src.main.services.email_service import send_notification_to_user
from src.main.services.calendar_service import schedule_calendar_event

router = APIRouter()

# Email Notification Endpoints
@router.post("/email", response_model=EmailNotificationResponse, status_code=status.HTTP_201_CREATED)
def create_email_notification(notification: EmailNotificationCreate):
    """Create and send an email notification."""
    result = send_notification_to_user(
        user_id=notification.user_id,
        email=notification.email,
        subject=notification.subject,
        body=notification.body
    )
    
    if not result:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to create email notification"
        )
    
    # Convert datetime strings to datetime objects for response
    if isinstance(result["created_at"], str):
        result["created_at"] = datetime.fromisoformat(result["created_at"])
    
    if result.get("sent_at") and isinstance(result["sent_at"], str):
        result["sent_at"] = datetime.fromisoformat(result["sent_at"])
        
    return result

@router.post("/email/process", status_code=status.HTTP_200_OK)
def process_email_queue():
    """Process pending email notifications."""
    from src.main.services.email_service import process_pending_notifications
    
    result = process_pending_notifications()
    return {
        "message": f"Processed {result['total']} notifications: {result['success']} successful, {result['failure']} failed"
    }

# Calendar Event Endpoints
@router.post("/calendar", response_model=CalendarEventResponse, status_code=status.HTTP_201_CREATED)
def create_calendar_event(event: CalendarEventCreate):
    """Create and schedule a calendar event."""
    result = schedule_calendar_event(
        user_id=event.user_id,
        email=event.email,
        summary=event.summary,
        description=event.description,
        start_time=event.start_time,
        end_time=event.end_time
    )
    
    if not result:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to create calendar event"
        )
    
    # Convert datetime strings to datetime objects for response
    if isinstance(result["created_at"], str):
        result["created_at"] = datetime.fromisoformat(result["created_at"])
    
    if isinstance(result["start_time"], str):
        result["start_time"] = datetime.fromisoformat(result["start_time"])
        
    if isinstance(result["end_time"], str):
        result["end_time"] = datetime.fromisoformat(result["end_time"])
        
    return result

@router.post("/calendar/process", status_code=status.HTTP_200_OK)
def process_calendar_queue():
    """Process pending calendar events."""
    from src.main.services.calendar_service import process_pending_calendar_events
    
    result = process_pending_calendar_events()
    return {
        "message": f"Processed {result['total']} calendar events: {result['success']} successful, {result['failure']} failed"
    }

@router.get("/health", tags=["health"])
def notification_health_check():
    """Health check endpoint for notifications."""
    import os
    
    # Check if email credentials are set
    is_email_ready = bool(os.environ.get("EMAIL_USERNAME") and os.environ.get("EMAIL_PASSWORD"))
    
    # Check if Google Calendar API credentials are available
    is_calendar_ready = os.path.exists(os.environ.get("GOOGLE_CREDENTIALS_FILE", "credentials.json"))
    
    return {
        "status": "healthy",
        "service": "notification-microservice",
        "services": {
            "email": "ready" if is_email_ready else "not_configured",
            "calendar": "ready" if is_calendar_ready else "not_configured"
        }
    } 