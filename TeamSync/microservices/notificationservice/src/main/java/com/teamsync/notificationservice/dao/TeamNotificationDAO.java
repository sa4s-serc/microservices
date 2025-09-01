package com.teamsync.notificationservice.dao;

import com.teamsync.notificationservice.model.TeamNotification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeamNotificationDAO {
    private static final String URL = "jdbc:postgresql://postgres:5432/notificationservice_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "password";

    public void createTeamNotificationTable() {
        String sql = "CREATE TABLE IF NOT EXISTS team_notification (" +
                     "notification_id INT REFERENCES notification(id) ON DELETE CASCADE, " +
                     "team_id INT NOT NULL, " +
                     "from_student_id INT NOT NULL, " +
                     "PRIMARY KEY (notification_id, team_id))";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("TeamNotification table created successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addTeamNotification(TeamNotification teamNotification) {
        String sql = "INSERT INTO team_notification (notification_id, team_id, from_student_id) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
    
            ps.setInt(1, teamNotification.getNotification());
            ps.setInt(2, teamNotification.getTeamId());
            ps.setInt(3, teamNotification.getFromStudentId());
            ps.executeUpdate();
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeTeamNotification(int notificationId) {
        String sql = "DELETE FROM team_notification WHERE notification_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, notificationId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<TeamNotification> getTeamNotificationsByTeamId(int teamId) {
        String sql = "SELECT * FROM team_notification WHERE team_id = ?";
        List<TeamNotification> teamNotifications = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, teamId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                TeamNotification teamNotification = mapResultSetToTeamNotification(rs);
                teamNotifications.add(teamNotification);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return teamNotifications;
    }
    
    public List<TeamNotification> getTeamNotificationsByStudentId(int studentId) {
        String sql = "SELECT * FROM team_notification WHERE from_student_id = ?";
        List<TeamNotification> teamNotifications = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                TeamNotification teamNotification = mapResultSetToTeamNotification(rs);
                teamNotifications.add(teamNotification);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return teamNotifications;
    }

    private TeamNotification mapResultSetToTeamNotification(ResultSet rs) throws SQLException {
        return new TeamNotification(
            rs.getInt("notification_id"),
            rs.getInt("team_id"),
            rs.getInt("from_student_id")
        );
    }
} 