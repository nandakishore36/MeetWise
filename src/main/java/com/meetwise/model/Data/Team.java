package com.meetwise.model.Data;

public class Team {

    private int teamId;
    private String teamName;
    private String department;

    public Team() {

    }

    public Team(int teamId, String teamName, String department) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.department = department;
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

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}