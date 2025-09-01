package com.teamsync.courseservice.model;

public class Team {
    private int teamId;
    private Integer courseId;
    private String teamName;
    private Integer teamSize; 

    public Team(int teamId, int courseId, String teamName, int teamSize) {
        this.teamId = teamId;
        this.courseId = courseId;
        this.teamName = teamName;
        this.teamSize = teamSize;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public Integer getTeamSize() {
        return teamSize;
    }

    public void setTeamSize(int teamSize) {
        this.teamSize = teamSize;
    }
}
