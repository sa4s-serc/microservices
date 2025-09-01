package com.teamsync.courseservice.resource;

import com.teamsync.courseservice.dao.TeamDAO;
import com.teamsync.courseservice.dao.TeamStudentDAO;
import com.teamsync.courseservice.dao.CourseStudentDAO;
import com.teamsync.courseservice.dao.CourseDAO;
import com.teamsync.courseservice.model.Course;
import com.teamsync.courseservice.model.Team;
import com.teamsync.courseservice.model.TeamStudent;
import com.teamsync.courseservice.model.TeamCourseDTO;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.teamsync.courseservice.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
public class TeamResource {
    private final TeamDAO teamDAO = new TeamDAO();
    private final TeamStudentDAO teamStudentDAO = new TeamStudentDAO();
    private final CourseDAO courseDAO = new CourseDAO();
    private final CourseStudentDAO coursestudentDAO = new CourseStudentDAO();
    private final JwtUtil jwtUtil = new JwtUtil(); 

    @PostMapping("/init")
    public ResponseEntity<String> createTeamTables() {
        teamDAO.createTeamTable();
        teamStudentDAO.createTeamStudentTable();
        return ResponseEntity.ok("Team tables created.");
    }

    @PostMapping
    public ResponseEntity<?> addTeam(
            @RequestBody Team team,
            HttpServletRequest request) {
        if(team.getTeamSize() == null){
            team.setTeamSize(0);
        }

        if (team.getCourseId() == null || team.getCourseId() <= 0 || team.getTeamName() == null || team.getTeamName().isEmpty() || team.getTeamSize() < 0) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Team must have a valid course ID, name, and size.");
        }

        int studentId = jwtUtil.getUserId(request);

        if (!coursestudentDAO.isStudentInCourse(team.getCourseId(), studentId)) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Student is not enrolled in this course.");
        }

        Course course = courseDAO.getCourseById(team.getCourseId());
        if (course == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Course not found.");
        }

        if (teamDAO.checkTeam(team.getTeamName(), team.getCourseId())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Team with the name \"" + team.getTeamName() + "\" already exists for this course.");
        }
    
        team = teamDAO.addTeam(team);

        teamDAO.getTeamsByCourse(team.getCourseId()).forEach(t -> {
            if (teamStudentDAO.isStudentInTeam(t.getTeamId(), studentId)) {
                teamStudentDAO.removeTeamStudent(t.getTeamId(), studentId);
            }
        });

        boolean success = teamStudentDAO.addTeamStudent(new TeamStudent(team.getTeamId(), studentId));
        if(!success){
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to add student to team.");
        }

        
        return ResponseEntity.ok(team);
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<?> getTeamsByCourse(@PathVariable int teamId) {
        Team team = teamDAO.getTeamById(teamId);
        if (team == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(team);
    }

    @PostMapping("/{teamId}/add")
    public ResponseEntity<String> addStudentToTeam(@PathVariable int teamId, @RequestParam int studentId) {
        Team team = teamDAO.getTeamById(teamId);
        if (team == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Team not found.");
        }

        Course course = courseDAO.getCourseById(team.getCourseId());
        if (team.getTeamSize() >= course.getTeamSize()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Team is full.");
        }

        if (!coursestudentDAO.isStudentInCourse(course.getCourseId(), studentId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Student is not enrolled in this course.");
        }

        if (teamStudentDAO.isStudentInAnyTeam(team.getCourseId(), studentId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Student is already in a team for this course.");
        }

        boolean success = teamStudentDAO.addTeamStudent(new TeamStudent(teamId, studentId));
        if(!success){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add student to team.");
        }

        return ResponseEntity.ok("Student added to team.");
    }

    @DeleteMapping("/{teamId}/remove")
    public ResponseEntity<String> removeStudentFromTeam(
            @PathVariable int teamId, 
            HttpServletRequest request) {
        
        int studentId = jwtUtil.getUserId(request);

        Team team = teamDAO.getTeamById(teamId);
        if (team == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Team not found.");
        }

        if(teamStudentDAO.isStudentInTeam(teamId, studentId) == false){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student not found in team.");
        }

        boolean success = teamStudentDAO.removeTeamStudent(teamId, studentId);

        if (!success) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to remove student from team.");
        }

        team = teamDAO.getTeamById(teamId);
        if (team.getTeamSize() == 0) {
            teamDAO.deleteTeam(teamId);
            return ResponseEntity.ok("Team is empty and has been deleted.");
        }
        return ResponseEntity.ok("Student removed from team.");
    }

    @GetMapping("/my")
    public ResponseEntity<List<TeamCourseDTO>> getTeamsByUser(
            HttpServletRequest request) {
        
        int userId = jwtUtil.getUserId(request);

        List<TeamCourseDTO> teams = teamStudentDAO.getTeamsByStudent(userId);
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/{teamId}/students")
    public ResponseEntity<List<TeamStudent>> getStudentsInTeam(@PathVariable int teamId) {
        return ResponseEntity.ok(teamStudentDAO.getStudentsInTeam(teamId));
    }
}
