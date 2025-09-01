package com.teamsync.notificationservice.dao;

import com.teamsync.notificationservice.model.Notification;
import com.teamsync.notificationservice.model.NotificationType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {
    private static final String URL = "jdbc:postgresql://postgres:5432/notificationservice_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "password";

    public void createNotificationTable() {
        String sql = "CREATE TABLE IF NOT EXISTS notification (" +
                     "id SERIAL PRIMARY KEY, " +
                     "from_student_id INT NOT NULL, " +
                     "to_team_id INT NOT NULL, " +
                     "type VARCHAR(50) NOT NULL, " +
                     "description TEXT)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("Notification table created successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addNotification(Notification notification) {
        String sql = "INSERT INTO notification (from_student_id, to_team_id, type, description) VALUES (?, ?, ?, ?) RETURNING id";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
    
            ps.setInt(1, notification.getFromStudentId());
            ps.setInt(2, notification.getToTeamId());
            ps.setString(3, notification.getType().toString());
            ps.setString(4, notification.getDescription());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int generatedId = rs.getInt(1);
                notification.setId(generatedId); 
            }
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeNotification(int notificationId) {
        String sql = "DELETE FROM notification WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, notificationId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Notification> getAllNotifications() {
        String sql = "SELECT * FROM notification";
        List<Notification> notifications = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Notification notification = mapResultSetToNotification(rs);
                notifications.add(notification);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notifications;
    }

    public List<Notification> getNotificationsByTeamId(int teamId) {
        String sql = "SELECT * FROM notification WHERE to_team_id = ?";
        List<Notification> notifications = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, teamId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Notification notification = mapResultSetToNotification(rs);
                notifications.add(notification);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notifications;
    }

    public Notification getNotificationById(int notificationId) {
        String sql = "SELECT * FROM notification WHERE id = ?";
        Notification notification = null;
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, notificationId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                notification = mapResultSetToNotification(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notification;
    }

    private Notification mapResultSetToNotification(ResultSet rs) throws SQLException {
        Notification notification = new Notification();
        notification.setId(rs.getInt("id"));
        notification.setFromStudentId(rs.getInt("from_student_id"));
        notification.setToTeamId(rs.getInt("to_team_id"));
        notification.setType(NotificationType.valueOf(rs.getString("type")));
        notification.setDescription(rs.getString("description"));
        return notification;
    }
} 