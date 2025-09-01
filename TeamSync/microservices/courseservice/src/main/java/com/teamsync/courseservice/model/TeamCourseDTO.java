package com.teamsync.courseservice.model;

public class TeamCourseDTO {
    private int teamId;
    private String teamName;
    private int courseId;
    private String courseName;

    public TeamCourseDTO(int teamId, String teamName, int courseId, String courseName) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.courseId = courseId;
        this.courseName = courseName;
    }
    public int getTeamId() {
        return teamId;
    }
    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }
    public String getTeamName() {
        return teamName;
    }
    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }
    public int getCourseId() {
        return courseId;
    }
    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }
    public String getCourseName() {
        return courseName;
    }
    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

}
