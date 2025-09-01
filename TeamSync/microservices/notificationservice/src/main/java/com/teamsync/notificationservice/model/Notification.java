package com.teamsync.notificationservice.model;

public class Notification {

    private int id;  // Auto-generated primary key
    private int fromStudentId;  // Sender's student ID
    private int toTeamId;       // Target Team ID
    private NotificationType type;
    private String description;

    public Notification() {}

    public Notification(int fromStudentId, int toTeamId, NotificationType type, String description) {
        this.fromStudentId = fromStudentId;
        this.toTeamId = toTeamId;
        this.type = type;
        this.description = description;
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFromStudentId() {
        return fromStudentId;
    }

    public void setFromStudentId(int fromStudentId) {
        this.fromStudentId = fromStudentId;
    }

    public int getToTeamId() {
        return toTeamId;
    }

    public void setToTeamId(int toTeamId) {
        this.toTeamId = toTeamId;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
