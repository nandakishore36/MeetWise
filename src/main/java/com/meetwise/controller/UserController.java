package com.meetwise.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.meetwise.model.Data.User;
import com.meetwise.model.service.UserService;

@WebServlet("/users")
public class UserController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private UserService userService = new UserService();

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
                          throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Check Admin session
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(
                "{\"error\":\"Please login first.\"}"
            );
            return;
        }

        if (!"ADMIN".equals(session.getAttribute("userRole"))) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(
                "{\"error\":\"Admin access required.\"}"
            );
            return;
        }

        try {

            int adminId = (Integer) session.getAttribute("userId");

            String name = request.getParameter("name");
            String email = request.getParameter("email");
            String password = request.getParameter("password");
            String role = request.getParameter("role");
            String teamIdValue = request.getParameter("teamId");

            // Validate required fields
            if (name == null || email == null ||
                password == null || role == null) {

                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(
                    "{\"error\":\"Please provide all required details.\"}"
                );
                return;
            }

            // Create User object
            User user = new User();

            user.setName(name.trim());
            user.setEmail(email.trim());
            user.setPasswordHash(password);
            user.setRole(role.trim().toUpperCase());

            // Assign team to non-admin users
            if (!"ADMIN".equals(user.getRole())) {

                if (teamIdValue == null || teamIdValue.trim().isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write(
                        "{\"error\":\"Team ID is required.\"}"
                    );
                    return;
                }

                user.setTeamId(Integer.parseInt(teamIdValue));

            } else {
                user.setTeamId(0);
            }

            // Register user through service layer
            boolean result = userService.registerUser(user, adminId);

            if (result) {

                response.getWriter().write(
                    "{\"message\":\"User registered successfully.\"}"
                );

            } else {

                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(
                    "{\"error\":\"User registration failed. Check the details and ensure the email is not already registered.\"}"
                );
            }

        } catch (NumberFormatException e) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(
                "{\"error\":\"Invalid team ID.\"}"
            );

        } catch (Exception e) {

            response.setStatus(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            response.getWriter().write(
                "{\"error\":\"Unable to register user.\"}"
            );

            e.printStackTrace();
        }
    }
}
