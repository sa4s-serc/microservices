package com.teamsync.taskservice.dao;

import com.teamsync.taskservice.model.Task;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {
    private static final String URL = "jdbc:postgresql://postgres:5432/task_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "password";

    public void createTaskTable() {
        String sql = "CREATE TABLE IF NOT EXISTS tasks (" +
                "id SERIAL PRIMARY KEY," +
                "team_id INT NOT NULL," +
                "name VARCHAR(255) NOT NULL," +
                "description TEXT," +                
                "status BOOLEAN NOT NULL," +
                "assigned_to_id INT NOT NULL" +
                ")";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addTask(Task task) {
        String sql = "INSERT INTO tasks (team_id, name, description, status, assigned_to_id) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, task.getTeamId());
            preparedStatement.setString(2, task.getTitle());
            preparedStatement.setString(3, task.getDescription());
            preparedStatement.setBoolean(4, task.getCompleted());
            preparedStatement.setInt(5, task.getAssignedToId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateTask(Task task) {
        StringBuilder sql = new StringBuilder("UPDATE tasks SET ");
        List<Object> params = new ArrayList<>();
    
        if (task.getTitle() != null) {
            sql.append("name = ?, ");
            params.add(task.getTitle());
        }
        if (task.getDescription() != null) {
            sql.append("description = ?, ");
            params.add(task.getDescription());
        }
        if (task.getCompleted() != null) {
            sql.append("status = ?, ");
            params.add(task.getCompleted());
        }
        if (task.getAssignedToId() != null) {
            sql.append("assigned_to_id = ?, ");
            params.add(task.getAssignedToId());
        }
    
        if (params.isEmpty()) return; // nothing to update
        sql.setLength(sql.length() - 2);
    
        sql.append(" WHERE id = ?");
        params.add(task.getTaskId());
    
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(sql.toString())) {
    
            for (int i = 0; i < params.size(); i++) {
                preparedStatement.setObject(i + 1, params.get(i));
            }
    
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    

    public void deleteTask(int taskId) {
        String sql = "DELETE FROM tasks WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, taskId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Task> getTasksByTeamId(int teamId) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE team_id = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, teamId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Task task = new Task();
                task.setTaskId(resultSet.getInt("id"));
                task.setTitle(resultSet.getString("name"));
                task.setDescription(resultSet.getString("description"));
                task.setCompleted(resultSet.getBoolean("status"));
                task.setAssignedToId(resultSet.getInt("assigned_to_id"));
                tasks.add(task);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tasks;
    }

    public List<Task> getTasksByAssignedToId(int assignedToId) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE assigned_to_id = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, assignedToId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Task task = new Task();
                task.setTaskId(resultSet.getInt("id"));
                task.setTitle(resultSet.getString("name"));
                task.setDescription(resultSet.getString("description"));
                task.setCompleted(resultSet.getBoolean("status"));
                task.setAssignedToId(resultSet.getInt("assigned_to_id"));
                tasks.add(task);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tasks;
    }
}
