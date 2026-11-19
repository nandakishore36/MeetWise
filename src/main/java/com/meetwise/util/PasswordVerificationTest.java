package com.meetwise.util;

import java.util.Scanner;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PasswordVerificationTest {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter Admin password: ");
        String password = sc.nextLine();

        String sql = "SELECT password_hash FROM users "
                   + "WHERE email = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "admin@meetwise.com");

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    String storedHash =
                            rs.getString("password_hash");

                    boolean valid =
                            PasswordUtil.verifyPassword(
                                    password, storedHash);

                    System.out.println(
                            "Password verification: " + valid);
                } else {
                    System.out.println("Admin account not found.");
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        sc.close();
    }
}
