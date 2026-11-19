package com.meetwise.controller;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/sessionInfo")
public class SessionInfoController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Not logged in\"}");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        String userName = (String) session.getAttribute("userName");
        String userRole = (String) session.getAttribute("userRole");

        Object teamValue = session.getAttribute("teamId");
        int teamId = teamValue == null ? 0 : (Integer) teamValue;

        response.getWriter().write(
            "{"
            + "\"userId\":" + userId + ","
            + "\"userName\":\"" + escapeJson(userName) + "\","
            + "\"userRole\":\"" + escapeJson(userRole) + "\","
            + "\"teamId\":" + teamId
            + "}"
        );
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"");
    }
}