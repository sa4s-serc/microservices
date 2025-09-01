package com.teamsync.courseservice.dao;

import com.teamsync.courseservice.model.Course;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseDAO {
    private static final String URL = "jdbc:postgresql://postgres:5432/courseservice_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "password";

    public void createCourseTable() {
        String sql = "CREATE TABLE IF NOT EXISTS course (" +
                     "course_id SERIAL PRIMARY KEY, " +
                     "course_name VARCHAR(255) NOT NULL, " +
                     "team_size INT NOT NULL)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("Course table created successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addCourse(Course course) {
        String sql = "INSERT INTO course (course_name, team_size) VALUES (?, ?) RETURNING course_id";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
    
            ps.setString(1, course.getCourseName());
            ps.setInt(2, course.getTeamSize());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int generatedId = rs.getInt(1);
                course.setCourseId(generatedId); 
            }
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeCourse(int courseId) {
        String sql = "DELETE FROM course WHERE course_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Course> list(){
        String sql = "SELECT * FROM course";
        List<Course> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Course course = new Course(rs.getInt("course_id"), rs.getString("course_name"), rs.getInt("team_size"));
                list.add(course);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Course getCourseById(int courseId) {
        String sql = "SELECT * FROM course WHERE course_id = ?";
        Course course = null;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                course = new Course(rs.getInt("course_id"), rs.getString("course_name"), rs.getInt("team_size"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return course;
    }

    public boolean checkCourse(String course_name){
        String sql = "SELECT 1 FROM course WHERE course_name = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, course_name);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
