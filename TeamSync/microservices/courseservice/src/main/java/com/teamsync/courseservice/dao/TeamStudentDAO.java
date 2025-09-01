package com.teamsync.courseservice.dao;

import com.teamsync.courseservice.model.TeamStudent;
import com.teamsync.courseservice.model.TeamCourseDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeamStudentDAO {
    private static final String URL = "jdbc:postgresql://postgres:5432/courseservice_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "password";

    public void createTeamStudentTable() {
        String sql = "CREATE TABLE IF NOT EXISTS team_student (" +
                     "team_id INT NOT NULL, " +
                     "student_id INT NOT NULL, " +
                     "PRIMARY KEY (team_id, student_id))";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean addTeamStudent(TeamStudent ts) {
        String insertSql = "INSERT INTO team_student (team_id, student_id) VALUES (?, ?)";
        String updateSql = "UPDATE team SET team_size = team_size + 1 WHERE team_id = ?";
    
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            conn.setAutoCommit(false); // Begin transaction
    
            try (
                PreparedStatement insertPs = conn.prepareStatement(insertSql);
                PreparedStatement updatePs = conn.prepareStatement(updateSql)
            ) {
                // Insert into team_student
                insertPs.setInt(1, ts.getTeamId());
                insertPs.setInt(2, ts.getStudentId());
                int affectedRows = insertPs.executeUpdate();
    
                // Only increment if insert was successful
                if (affectedRows > 0) {
                    updatePs.setInt(1, ts.getTeamId());
                    updatePs.executeUpdate();
                }
    
                conn.commit(); // Commit transaction
                return affectedRows > 0;
            } catch (SQLException e) {
                conn.rollback(); // Rollback on error
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true); // Restore autocommit
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeTeamStudent(int teamId, int studentId) {
        String deleteSql = "DELETE FROM team_student WHERE team_id = ? AND student_id = ?";
        String updateSql = "UPDATE team SET team_size = team_size - 1 WHERE team_id = ?";
    
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            conn.setAutoCommit(false); // Start transaction
    
            try (
                PreparedStatement deletePs = conn.prepareStatement(deleteSql);
                PreparedStatement updatePs = conn.prepareStatement(updateSql)
            ) {
                // Delete from team_student
                deletePs.setInt(1, teamId);
                deletePs.setInt(2, studentId);
                int affectedRows = deletePs.executeUpdate();
    
                // Only update team size if a row was actually deleted
                if (affectedRows > 0) {
                    updatePs.setInt(1, teamId);
                    updatePs.executeUpdate();
                }
    
                conn.commit(); // Commit transaction
                return affectedRows > 0;
            } catch (SQLException e) {
                conn.rollback(); // Rollback on error
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true); // Restore autocommit
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<TeamCourseDTO> getTeamsByStudent(int studentId) {
        String sql = "SELECT t.team_id, t.team_name, c.course_id, c.course_name " +
                     "FROM team_student ts " +
                     "JOIN team t ON ts.team_id = t.team_id " +
                     "JOIN course c ON t.course_id = c.course_id " +
                     "WHERE ts.student_id = ?";

        List<TeamCourseDTO> list = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new TeamCourseDTO(rs.getInt("team_id"), rs.getString("team_name"), rs.getInt("course_id"), rs.getString("course_name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public List<TeamStudent> getStudentsInTeam(int teamId) {
        List<TeamStudent> list = new ArrayList<>();
        String sql = "SELECT * FROM team_student WHERE team_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teamId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new TeamStudent(teamId, rs.getInt("student_id")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean isStudentInAnyTeam(int courseId, int studentId) {
        String sql = "SELECT * FROM team_student WHERE student_id = ? AND team_id IN (SELECT team_id FROM team WHERE course_id = ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, courseId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isStudentInTeam(int teamId, int studentId) {
        String sql = "SELECT * FROM team_student WHERE team_id = ? AND student_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teamId);
            ps.setInt(2, studentId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void removeAllStudentsFromTeam(int teamId) {
        String sql = "DELETE FROM team_student WHERE team_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teamId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
