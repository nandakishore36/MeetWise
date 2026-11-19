package com.meetwise.model.design;

import java.util.List;
import com.meetwise.model.Data.Team;

public interface TeamDesign {

    boolean addTeam(Team team);

    List<Team> getAllTeams();

    Team getTeamById(int teamId);
}
