package com.teamsync.courseservice.resource;

import com.teamsync.courseservice.model.Course;
import com.teamsync.courseservice.model.CourseStudent;

import jakarta.servlet.http.HttpServletRequest;

import com.teamsync.courseservice.dao.CourseDAO;
import com.teamsync.courseservice.dao.CourseStudentDAO;
import com.teamsync.courseservice.dao.TeamDAO;
import com.teamsync.courseservice.dao.TeamStudentDAO;
import com.teamsync.courseservice.util.JwtUtil;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseResource {

    private final CourseDAO courseDAO = new CourseDAO();
    private final CourseStudentDAO courseStudentDAO = new CourseStudentDAO();
    private final TeamDAO teamDAO = new TeamDAO();
    private final TeamStudentDAO teamStudentDAO = new TeamStudentDAO();
    private final JwtUtil jwtUtil = new JwtUtil();

    @PostMapping("/init")
    public ResponseEntity<String> createTable() {
        courseDAO.createCourseTable();
        courseStudentDAO.createCourseStudentTable();
        return ResponseEntity.ok("Course tables created.");
    }

    @GetMapping
    public ResponseEntity<List<Course>> listCourses() {
        List<Course> courses = courseDAO.list();
        return ResponseEntity.ok(courses);
    }

    @PostMapping
    public ResponseEntity<?> addCourse(
        @RequestBody Course course,
        HttpServletRequest request) {

        int userId = jwtUtil.getUserId(request);
        if (userId != 0) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("You are not allowed to create a course.");
        }

        if (course.getCourseName() == null || course.getCourseName().isEmpty() || course.getTeamSize() == null || course.getTeamSize() <= 0) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Course must have a valid name and size.");
        }
        
        if (courseDAO.checkCourse(course.getCourseName())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Course with the name \"" + course.getCourseName() + "\" already exists.");
        }

        courseDAO.addCourse(course);
        return ResponseEntity.status(HttpStatus.CREATED).body(course);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> removeCourse(
        @PathVariable int id,
        HttpServletRequest request) {

        int userId = jwtUtil.getUserId(request);
        if (userId != 0) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("You are not allowed to delete a course.");
        }

        Course course = courseDAO.getCourseById(id);
        if (course == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found.");
        }
        courseStudentDAO.removeAllStudentsFromCourse(id);

        teamDAO.getTeamsByCourse(id).forEach(team -> {
            teamStudentDAO.removeAllStudentsFromTeam(team.getTeamId());
            teamDAO.deleteTeam(team.getTeamId());
        });

        courseDAO.removeCourse(id);

        return ResponseEntity.ok("Course with ID " + id + " removed.");
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<?> getCourse(@PathVariable int courseId) {
        Course course = courseDAO.getCourseById(courseId);
        if (course == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found.");
        }
        return ResponseEntity.ok(course);
    }

    @GetMapping("/{courseId}/teams")
    public ResponseEntity<?> getTeamsByCourse(@PathVariable int courseId) {
        Course course = courseDAO.getCourseById(courseId);
        if (course == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found.");
        }
        return ResponseEntity.ok(teamDAO.getTeamsByCourse(courseId));
    }

    @PostMapping("/{courseId}/register")
    public ResponseEntity<String> registerCourse(
            @PathVariable int courseId,
            HttpServletRequest request) {

        int studentId = jwtUtil.getUserId(request);

        Course course = courseDAO.getCourseById(courseId);
        if (course == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found.");
        }

        if (courseStudentDAO.isStudentInCourse(courseId, studentId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Student already registered in the course.");
        }

        courseStudentDAO.addCourseStudent(new CourseStudent(courseId, studentId));
        return ResponseEntity.ok("Student registered for course.");
    }

    @DeleteMapping("/{courseId}/withdraw")
    public ResponseEntity<String> withdrawCourse(
            @PathVariable int courseId,
            HttpServletRequest request) {
        
        int studentId = jwtUtil.getUserId(request);

        Course course = courseDAO.getCourseById(courseId);
        if (course == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found.");
        }

        if(!courseStudentDAO.isStudentInCourse(courseId,studentId)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Student is not registered in the course.");
        }

        teamDAO.getTeamsByCourse(courseId).forEach(team -> {
            if (teamStudentDAO.isStudentInTeam(team.getTeamId(), studentId)) {
                teamStudentDAO.removeTeamStudent(team.getTeamId(), studentId);
            }
        });
        
        courseStudentDAO.removeCourseStudent(courseId, studentId);
        return ResponseEntity.ok("Student withdrawn from course.");
    }

    @GetMapping("/enrolled")
    public ResponseEntity<List<Course>> getCoursesByUser(
            HttpServletRequest request) {
        
        int userId = jwtUtil.getUserId(request);
    
        List<Course> courses = courseStudentDAO.getCoursesByUser(userId);
        return ResponseEntity.ok(courses);
    }


    @GetMapping("/{courseId}/students")
    public ResponseEntity<List<CourseStudent>> getStudentsInCourse(@PathVariable int courseId) {
        List<CourseStudent> students = courseStudentDAO.getStudentsInCourse(courseId);
        return ResponseEntity.ok(students);
    }
}
