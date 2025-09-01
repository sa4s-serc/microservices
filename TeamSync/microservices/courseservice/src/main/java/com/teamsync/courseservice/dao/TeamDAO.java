package com.teamsync.courseservice.dao;

import com.teamsync.courseservice.model.Team;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeamDAO {
    private static final String URL = "jdbc:postgresql://postgres:5432/courseservice_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "password";

    public void createTeamTable() {
        String sql = "CREATE TABLE IF NOT EXISTS team (" +
                     "team_id SERIAL PRIMARY KEY, " +
                     "course_id INT NOT NULL, " +
                     "team_name VARCHAR(255) NOT NULL, "+
                     "team_size INT NOT NULL )";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Team addTeam(Team team) {
        String sql = "INSERT INTO team (course_id, team_name, team_size) VALUES (?, ?, ?) RETURNING team_id";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, team.getCourseId());
            ps.setString(2, team.getTeamName());
            ps.setInt(3, team.getTeamSize());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                team.setTeamId(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return team;
    }

    public void deleteTeam(int teamId) {
        String sql = "DELETE FROM team WHERE team_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teamId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Team> getTeamsByCourse(int courseId) {
        List<Team> teams = new ArrayList<>();
        String sql = "SELECT * FROM team WHERE course_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                teams.add(new Team(rs.getInt("team_id"), courseId, rs.getString("team_name"), rs.getInt("team_size")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return teams;
    }

    public Team getTeamById(int teamId) {
        String sql = "SELECT * FROM team WHERE team_id = ?";
        Team team = null;
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teamId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                team = new Team(teamId, rs.getInt("course_id"), rs.getString("team_name"), rs.getInt("team_size"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return team;
    }

    public boolean checkTeam(String teamName, int courseId) {
        String sql = "SELECT 1 FROM team WHERE team_name = ? AND course_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, teamName);
            pstmt.setInt(2, courseId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
