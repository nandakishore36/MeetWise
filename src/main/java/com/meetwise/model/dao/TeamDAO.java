package com.meetwise.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.meetwise.model.Data.Team;
import com.meetwise.util.DBConnection;

public class TeamDAO {

    // Add a new team
    public boolean addTeam(Team team) {

        String sql = "INSERT INTO teams (team_name, department) VALUES (?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, team.getTeamName());
            ps.setString(2, team.getDepartment());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get all teams
    public List<Team> getAllTeams() {

        List<Team> teams = new ArrayList<>();

        String sql = "SELECT * FROM teams ORDER BY team_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Team team = new Team();

                team.setTeamId(rs.getInt("team_id"));
                team.setTeamName(rs.getString("team_name"));
                team.setDepartment(rs.getString("department"));

                teams.add(team);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return teams;
    }

    // Get team by ID
    public Team getTeamById(int teamId) {

        String sql = "SELECT * FROM teams WHERE team_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, teamId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    Team team = new Team();

                    team.setTeamId(rs.getInt("team_id"));
                    team.setTeamName(rs.getString("team_name"));
                    team.setDepartment(rs.getString("department"));

                    return team;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
