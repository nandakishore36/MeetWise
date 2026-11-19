package com.meetwise.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.meetwise.model.Data.Team;
import com.meetwise.model.service.TeamService;

@WebServlet("/publicTeams")
public class PublicTeamsController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private TeamService teamService = new TeamService();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            List<Team> teams = teamService.getAllTeams();

            StringBuilder json = new StringBuilder("[");
            boolean first = true;

            for (Team team : teams) {

                if (!first) {
                    json.append(",");
                }

                json.append("{")
                    .append("\"teamId\":").append(team.getTeamId()).append(",")
                    .append("\"teamName\":\"")
                    .append(escapeJson(team.getTeamName())).append("\"")
                    .append("}");

                first = false;
            }

            json.append("]");

            response.getWriter().write(json.toString());

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            response.getWriter().write(
                "{\"error\":\"Unable to load teams\"}"
            );
        }
    }

    private String escapeJson(String value) {

        if (value == null) {
            return "";
        }

        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");
    }
}
