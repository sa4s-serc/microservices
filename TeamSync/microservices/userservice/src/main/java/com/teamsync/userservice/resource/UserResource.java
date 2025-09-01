package com.teamsync.userservice.resource; 

import com.teamsync.userservice.dao.UserDao;
import com.teamsync.userservice.model.User;
import com.teamsync.userservice.model.UserRole;
import com.teamsync.userservice.util.JwtUtil;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserResource {

    private UserDao userDao = new UserDao();

    private JwtUtil jwtUtil = new JwtUtil();

    @PostMapping("/init")
    public ResponseEntity<String> createTable() {
        userDao.createUserTable();
        return ResponseEntity.ok("User table created.");
    }
    // public ResponseEntity<String> createTable() {
    //     courseDAO.createCourseTable();
    //     courseStudentDAO.createCourseStudentTable();
    //     return ResponseEntity.ok("Course tables created.");
    // }

    // 1. checkUsername(): Check if a username already exists
    @GetMapping("/check-username/{username}")
    public ResponseEntity<?> checkUsername(@PathVariable String username) {
        User existingUser = userDao.getByUsername(username);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", existingUser != null);
        return ResponseEntity.ok(response);
    }

    // 2. register(): Register a new user
    @PutMapping
    public ResponseEntity<?> register(@RequestBody User user) {
        String username = user.getUsername();
        String password = user.getPassword();

        // Validate username and password
        if (username == null || username.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Username and password are required");
        }

        // Check if username already exists
        if (userDao.getByUsername(username) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
            .body("Username already exists");
        }

        // Create user
        User newUser = new User(username, password, UserRole.STUDENT);
        boolean iscreated = userDao.create(newUser);

        if (!iscreated) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Failed to create user");
        }
        return ResponseEntity.ok("User registered successfully");
    }

    // 3. login(): Authenticate a user
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        String username = user.getUsername();
        String password = user.getPassword();
        System.out.println("Username: " + username);

        if (username.equals("admin")) {
            System.out.println("Admin login");
            user.setRole(UserRole.ADMIN);
        } else {
            user.setRole(UserRole.STUDENT);
        }

        // Validate username and password
        if (username == null || username.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Username and password are required");
        }

        // Authenticate user
        boolean isAuthenticated = userDao.authenticate(username, password);
        if (!isAuthenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body("Invalid username or password");
        }

        // Generate JWT token
        String jwtToken = jwtUtil.generateToken(username, UserRole.STUDENT.toString());

        // Send response
        Map<String, Object> response = new HashMap<>();
        response.put("token", jwtToken);
        response.put("role", user.getRole());
        response.put("username", username);

        return ResponseEntity.ok(response);
    }

    // 4. logout(): Logout a user
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody User user) {
        String username = user.getUsername();

        // Validate username
        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Username is required");
        }
        
        // Perform logout logic (e.g., invalidate session, clear tokens) Later

        return ResponseEntity.ok("User logged out successfully");
    }
}
