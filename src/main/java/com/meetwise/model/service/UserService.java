package com.meetwise.model.service;

import com.meetwise.model.Data.User;
import com.meetwise.model.dao.UserDAO;
import com.meetwise.model.design.UserDesign;

public class UserService implements UserDesign {

    private UserDAO userDAO = new UserDAO();
    private TeamService teamService = new TeamService();

    // Register user - Admin authorization required
    @Override
    public boolean registerUser(User user, int adminId) {

        if (user == null || adminId <= 0) {
            return false;
        }

        // Verify administrator
        User admin = userDAO.getUserById(adminId);

        if (admin == null || !"ADMIN".equals(admin.getRole())) {
            return false;
        }

        // Validate user details
        if (user.getName() == null || user.getName().trim().isEmpty()
                || user.getEmail() == null || user.getEmail().trim().isEmpty()
                || user.getPasswordHash() == null
                || user.getPasswordHash().length() < 8) {

            return false;
        }

        // Validate role
        if (!"ADMIN".equals(user.getRole())
                && !"TEAM_LEAD".equals(user.getRole())
                && !"EMPLOYEE".equals(user.getRole())) {

            return false;
        }

        // Team Lead and Employee must belong to a team
        if (!"ADMIN".equals(user.getRole()) && user.getTeamId() <= 0) {
            return false;
        }

        return userDAO.registerUser(user);
    }


    // Public Employee Registration
    public boolean registerEmployee(User user) {

        if (user == null) {
            return false;
        }

        // Validate name
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            return false;
        }

        // Validate email
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()
                || !user.getEmail().contains("@")) {
            return false;
        }

        // Validate password
        if (user.getPasswordHash() == null
                || user.getPasswordHash().length() < 8) {
            return false;
        }

        // Validate team selection
        if (user.getTeamId() <= 0
                || teamService.getTeamById(user.getTeamId()) == null) {
            return false;
        }

        // Force the role to EMPLOYEE
        user.setRole("EMPLOYEE");

        // Save employee using existing DAO and password hashing
        return userDAO.registerUser(user);
    }
    // Login user
    @Override
    public User loginUser(String email, String password) {

        if (email == null || email.trim().isEmpty()
                || password == null || password.isEmpty()) {

            return null;
        }

        return userDAO.loginUser(email.trim(), password);
    }


    // Get user by ID
    @Override
    public User getUserById(int userId) {

        if (userId <= 0) {
            return null;
        }

        return userDAO.getUserById(userId);
    }
}