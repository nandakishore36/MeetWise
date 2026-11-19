package com.meetwise.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.meetwise.model.Data.Team;
import com.meetwise.model.service.TeamService;

@WebServlet("/teams")
public class TeamController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private TeamService teamService = new TeamService();

    // View all teams
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
                         throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (!isAdmin(request, response)) {
            return;
        }

        try {
            List<Team> teams = teamService.getAllTeams();

            StringBuilder json = new StringBuilder();
            json.append("{\"teams\":[");

            for (int i = 0; i < teams.size(); i++) {

                Team team = teams.get(i);

                if (i > 0) {
                    json.append(",");
                }

                json.append("{");
                json.append("\"teamId\":").append(team.getTeamId()).append(",");
                json.append("\"teamName\":\"")
                    .append(escapeJson(team.getTeamName())).append("\",");
                json.append("\"department\":\"")
                    .append(escapeJson(team.getDepartment())).append("\"");
                json.append("}");
            }

            json.append("]}");

            response.getWriter().write(json.toString());

        } catch (Exception e) {

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(
                "{\"error\":\"Unable to retrieve teams.\"}"
            );

            e.printStackTrace();
        }
    }

    // Add a new team
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
                          throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (!isAdmin(request, response)) {
            return;
        }

        try {

            String teamName = request.getParameter("teamName");
            String department = request.getParameter("department");

            if (teamName == null || department == null ||
                teamName.trim().isEmpty() ||
                department.trim().isEmpty()) {

                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(
                    "{\"error\":\"Team name and department are required.\"}"
                );
                return;
            }

            Team team = new Team();

            team.setTeamName(teamName.trim());
            team.setDepartment(department.trim());

            boolean result = teamService.addTeam(team);

            if (result) {

                response.getWriter().write(
                    "{\"message\":\"Team added successfully.\"}"
                );

            } else {

                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(
                    "{\"error\":\"Unable to add team.\"}"
                );
            }

        } catch (Exception e) {

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(
                "{\"error\":\"Something went wrong while adding the team.\"}"
            );

            e.printStackTrace();
        }
    }

    // Check Admin access
    private boolean isAdmin(HttpServletRequest request,
                            HttpServletResponse response) throws IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(
                "{\"error\":\"Please login first.\"}"
            );

            return false;
        }

        if (!"ADMIN".equals(session.getAttribute("userRole"))) {

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(
                "{\"error\":\"Admin access required.\"}"
            );

            return false;
        }

        return true;
    }

    // Escape special characters for JSON
    private String escapeJson(String value) {

        if (value == null) {
            return "";
        }

        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
}
