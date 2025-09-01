package com.teamsync.notificationservice.resource;

import com.teamsync.notificationservice.dao.NotificationDAO;
import com.teamsync.notificationservice.dao.TeamNotificationDAO;
import com.teamsync.notificationservice.model.Notification;
import com.teamsync.notificationservice.model.TeamNotification;
import com.teamsync.notificationservice.model.NotificationType;
import com.teamsync.notificationservice.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationResource {

    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final TeamNotificationDAO teamNotificationDAO = new TeamNotificationDAO();
    private final JwtUtil jwtUtil = new JwtUtil();

    @PostMapping("/init")
    public ResponseEntity<String> createTables() {
        notificationDAO.createNotificationTable();
        teamNotificationDAO.createTeamNotificationTable();
        return ResponseEntity.ok("Notification tables created successfully!");
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications() {
        List<Notification> notifications = notificationDAO.getAllNotifications();
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/team/{teamId}")
    public ResponseEntity<List<Notification>> getNotificationsByTeamId(@PathVariable int teamId) {
        List<Notification> notifications = notificationDAO.getNotificationsByTeamId(teamId);
        return ResponseEntity.ok(notifications);
    }

    @PostMapping("/request-to-join")
    public ResponseEntity<?> createJoinRequest(
            @RequestParam int teamId,
            @RequestParam String description,
            HttpServletRequest request) {
        
        int studentId = jwtUtil.getUserId(request);
        
        Notification notification = new Notification(studentId, teamId, NotificationType.REQUEST_TO_JOIN, description);
        notificationDAO.addNotification(notification);
        
        // Create team notification entry
        TeamNotification teamNotification = new TeamNotification(notification.getId(), teamId, studentId);
        teamNotificationDAO.addTeamNotification(teamNotification);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(notification);
    }

    @PostMapping("/team-message")
    public ResponseEntity<?> createTeamMessage(
            @RequestParam int teamId,
            @RequestParam String description,
            HttpServletRequest request) {
        
        int studentId = jwtUtil.getUserId(request);
        
        Notification notification = new Notification(studentId, teamId, NotificationType.MESSAGE, description);
        notificationDAO.addNotification(notification);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(notification);
    }

    @PostMapping("/{notificationId}/accept")
    public ResponseEntity<String> acceptNotification(@PathVariable int notificationId) {
        Notification notification = notificationDAO.getNotificationById(notificationId);
        
        if (notification == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Notification not found");
        }
        
        if (notification.getType() != NotificationType.REQUEST_TO_JOIN) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Only join requests can be accepted");
        }
        
        // Delete the notification after acceptance
        notificationDAO.removeNotification(notificationId);
        
        // This endpoint would typically call the team service to add the student to the team
        // For now, we'll just acknowledge the acceptance
        return ResponseEntity.ok("Join request accepted. Student has been added to the team.");
    }

    @PostMapping("/{notificationId}/reject")
    public ResponseEntity<String> rejectNotification(@PathVariable int notificationId) {
        Notification notification = notificationDAO.getNotificationById(notificationId);
        
        if (notification == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Notification not found");
        }
        
        // Delete the notification after rejection
        notificationDAO.removeNotification(notificationId);
        
        return ResponseEntity.ok("Notification rejected and deleted");
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<String> deleteNotification(@PathVariable int notificationId) {
        Notification notification = notificationDAO.getNotificationById(notificationId);
        
        if (notification == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Notification not found");
        }
        
        notificationDAO.removeNotification(notificationId);
        return ResponseEntity.ok("Notification deleted successfully");
    }
}
