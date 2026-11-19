package com.meetwise.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.meetwise.model.Data.User;
import com.meetwise.model.service.UserService;

@WebServlet("/register")
public class RegisterController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private UserService userService = new UserService();

    @Override
    protected void doPost(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String teamIdValue = request.getParameter("teamId");

        // Check required fields
        if (name == null || email == null || password == null
                || confirmPassword == null || teamIdValue == null
                || name.trim().isEmpty() || email.trim().isEmpty()
                || password.isEmpty() || confirmPassword.isEmpty()
                || teamIdValue.trim().isEmpty()) {

            response.sendRedirect("register.html?error=empty");
            return;
        }

        // Check password confirmation
        if (!password.equals(confirmPassword)) {
            response.sendRedirect("register.html?error=password");
            return;
        }

        int teamId;

        try {
            teamId = Integer.parseInt(teamIdValue);
        } catch (NumberFormatException e) {
            response.sendRedirect("register.html?error=team");
            return;
        }

        // Create Employee account
        User user = new User();

        user.setName(name.trim());
        user.setEmail(email.trim());
        user.setPasswordHash(password);
        user.setRole("EMPLOYEE");
        user.setTeamId(teamId);

        try {

            boolean registered = userService.registerEmployee(user);

            if (registered) {
                response.sendRedirect("login.html?registered=success");
            } else {
                response.sendRedirect("register.html?error=failed");
            }

        } catch (Exception e) {

            e.printStackTrace();

            response.sendRedirect("register.html?error=failed");
        }
    }
}