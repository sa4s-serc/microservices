package com.teamsync.courseservice.model;

public class Course {
    private int courseId;
    private String courseName;
    private Integer teamSize;

    public Course(int courseId, String courseName, int teamSize) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.teamSize = teamSize;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public Integer getTeamSize() {
        return teamSize;
    }

    public void setTeamSize(int teamSize) {
        this.teamSize = teamSize;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
}
