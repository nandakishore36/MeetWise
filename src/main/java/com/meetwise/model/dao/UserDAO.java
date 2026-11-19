package com.meetwise.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

import com.meetwise.model.Data.User;
import com.meetwise.util.DBConnection;
import com.meetwise.util.PasswordUtil;

public class UserDAO {

    // Register a new user
    public boolean registerUser(User user) {

        String sql = "INSERT INTO users "
                   + "(name, email, password_hash, role, team_id) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String hashedPassword =
                    PasswordUtil.hashPassword(user.getPasswordHash());

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, hashedPassword);
            ps.setString(4, user.getRole());

            if (user.getTeamId() > 0) {
                ps.setInt(5, user.getTeamId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Login user
    public User loginUser(String email, String password) {

        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    String storedHash =
                            rs.getString("password_hash");

                    if (PasswordUtil.verifyPassword(
                            password, storedHash)) {

                        User user = new User();

                        user.setUserId(rs.getInt("user_id"));
                        user.setName(rs.getString("name"));
                        user.setEmail(rs.getString("email"));
                        user.setRole(rs.getString("role"));

                        int teamId = rs.getInt("team_id");

                        if (rs.wasNull()) {
                            user.setTeamId(0);
                        } else {
                            user.setTeamId(teamId);
                        }

                        return user;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // Get user by ID
    public User getUserById(int userId) {

        String sql = "SELECT * FROM users WHERE user_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    User user = new User();

                    user.setUserId(rs.getInt("user_id"));
                    user.setName(rs.getString("name"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(rs.getString("role"));

                    int teamId = rs.getInt("team_id");

                    if (rs.wasNull()) {
                        user.setTeamId(0);
                    } else {
                        user.setTeamId(teamId);
                    }

                    return user;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
