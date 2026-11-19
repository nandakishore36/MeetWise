package com.meetwise.controller;

import java.io.IOException;

import com.meetwise.model.Data.User;
import com.meetwise.model.service.UserService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/login")
public class LoginController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final UserService userService = new UserService();

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || email.trim().isEmpty()
                || password == null || password.isEmpty()) {

            response.sendRedirect("login.html?error=empty");
            return;
        }

        User user = userService.loginUser(email.trim(), password);

        if (user == null) {
            response.sendRedirect("login.html?error=invalid");
            return;
        }

        // Prevent session fixation after successful login.
        HttpSession session = request.getSession(true);
        request.changeSessionId();

        session.setAttribute("userId", user.getUserId());
        session.setAttribute("userName", user.getName());
        session.setAttribute("userRole", user.getRole());
        session.setAttribute("teamId", user.getTeamId());

        if ("ADMIN".equals(user.getRole())) {
            response.sendRedirect("admin-dashboard.html");
        } else {
            response.sendRedirect("dashboard.html");
        }
    }
}