package com.teamsync.taskservice.model;

public class Task {
    private int taskId;

    private Integer teamId;
    private Integer assignedToId;

    private String title;
    private String description;

    private Boolean completed = false;

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public Integer getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public Integer getAssignedToId() {
        return assignedToId;
    }

    public void setAssignedToId(int assignedToId) {
        this.assignedToId = assignedToId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getCompleted(){
        return this.completed;
    }

    public void setCompleted(Boolean completed){
        this.completed = completed;
    }
}
