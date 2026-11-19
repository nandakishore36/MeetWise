package com.meetwise.model.service;

import java.util.List;

import com.meetwise.model.Data.Team;
import com.meetwise.model.dao.TeamDAO;
import com.meetwise.model.design.TeamDesign;

public class TeamService implements TeamDesign {

    private TeamDAO teamDAO = new TeamDAO();

    // Add a new team
    @Override
    public boolean addTeam(Team team) {

        if (team == null) {
            return false;
        }

        if (team.getTeamName() == null ||
            team.getTeamName().trim().isEmpty()) {
            return false;
        }

        if (team.getDepartment() == null ||
            team.getDepartment().trim().isEmpty()) {
            return false;
        }

        team.setTeamName(team.getTeamName().trim());
        team.setDepartment(team.getDepartment().trim());

        return teamDAO.addTeam(team);
    }

    // Retrieve all teams
    @Override
    public List<Team> getAllTeams() {

        return teamDAO.getAllTeams();
    }

    // Retrieve a team by ID
    @Override
    public Team getTeamById(int teamId) {

        if (teamId <= 0) {
            return null;
        }

        return teamDAO.getTeamById(teamId);
    }
}
