package com.teamsync.courseservice.dao;

import com.teamsync.courseservice.model.CourseStudent;
import com.teamsync.courseservice.model.Course;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseStudentDAO {
    private static final String URL = "jdbc:postgresql://postgres:5432/courseservice_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "password";

    public void createCourseStudentTable() {
        String sql = "CREATE TABLE IF NOT EXISTS course_student (" +
                "course_id INTEGER NOT NULL," +
                "student_id INTEGER NOT NULL," +
                "PRIMARY KEY (course_id, student_id)" +
                ");";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addCourseStudent(CourseStudent cs) {
        String sql = "INSERT INTO course_student (course_id, student_id) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cs.getCourseId());
            pstmt.setInt(2, cs.getStudentId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeCourseStudent(int courseId, int studentId) {
        String sql = "DELETE FROM course_student WHERE course_id = ? AND student_id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            pstmt.setInt(2, studentId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Course> getCoursesByUser(int studentId) {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT c.* FROM course_student cs " +
                     "JOIN course c ON cs.course_id = c.course_id " +
                     "WHERE cs.student_id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int courseId = rs.getInt("course_id");
                String courseName = rs.getString("course_name");
                int teamSize = rs.getInt("team_size");
                courses.add(new Course(courseId, courseName, teamSize));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return courses;
    }

    public List<CourseStudent> getStudentsInCourse(int courseId) {
        List<CourseStudent> list = new ArrayList<>();
        String sql = "SELECT * FROM course_student WHERE course_id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int studentId = rs.getInt("student_id");
                list.add(new CourseStudent(courseId, studentId));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public boolean isStudentInCourse(int courseId, int studentId){
        String sql = "SELECT 1 FROM course_student WHERE course_id = ? AND student_id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            pstmt.setInt(2, studentId);
            ResultSet rs = pstmt.executeQuery();

            return rs.next(); // If a row exists, the student is in the course
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Return false in case of an exception
        }
    }


    public void removeAllStudentsFromCourse(int courseId) {
        String sql = "DELETE FROM course_student WHERE course_id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
