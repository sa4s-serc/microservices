package com.teamsync.taskservice.resource;

import com.teamsync.taskservice.model.Task;
import com.teamsync.taskservice.dao.TaskDAO;

import com.teamsync.taskservice.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;

import java.util.List;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskResource {

    private final TaskDAO taskDAO = new TaskDAO();
    private final JwtUtil jwtUtil = new JwtUtil();

    @PostMapping("/init")
    public ResponseEntity<String> createTable() {
        taskDAO.createTaskTable();
        return ResponseEntity.ok("Task table created successfully.");
    }

    @PostMapping
    public ResponseEntity<String> addTask(@RequestBody Task task) {
        taskDAO.addTask(task);
        return ResponseEntity.ok("Task added successfully.");
    }

    @PutMapping
    public ResponseEntity<String> updateTask(@RequestBody Task task) {
        taskDAO.updateTask(task);
        return ResponseEntity.ok("Task updated successfully.");
    }

    @GetMapping("/team/{teamId}")
    public ResponseEntity<List<Task>> getTasksByTeamId(@PathVariable int teamId) {
        List<Task> tasks = taskDAO.getTasksByTeamId(teamId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/my")
    public ResponseEntity<List<Task>> getTasksByAssignedToId(
            HttpServletRequest request ) {
        
        int assignedToId = jwtUtil.getUserId(request);

        List<Task> tasks = taskDAO.getTasksByAssignedToId(assignedToId);
        return ResponseEntity.ok(tasks);
    }

}
