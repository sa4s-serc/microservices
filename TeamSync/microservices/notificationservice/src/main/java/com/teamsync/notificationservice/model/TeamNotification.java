package com.teamsync.notificationservice.model;

public class TeamNotification {

    private int notificationId;
    private int teamId;
    private int fromStudentId;

    public TeamNotification(int notificationId, int teamId, int fromStudentId) {
        this.notificationId = notificationId;
        this.teamId = teamId;
        this.fromStudentId = fromStudentId;
    }

    // Getters and Setters
    public int getNotification() {
        return notificationId;
    }

    public void setNotification(int notificationId) {
        this.notificationId = notificationId;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public int getFromStudentId() {
        return fromStudentId;
    }

    public void setFromStudentId(int fromStudentId) {
        this.fromStudentId = fromStudentId;
    }
}