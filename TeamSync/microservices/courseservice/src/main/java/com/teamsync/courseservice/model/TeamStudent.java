package com.teamsync.courseservice.model;

public class TeamStudent {
    private int teamId;
    private int studentId;

    public TeamStudent(int teamId, int studentId) {
        this.teamId = teamId;
        this.studentId = studentId;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }
}
