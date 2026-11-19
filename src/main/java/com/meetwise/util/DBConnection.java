package com.meetwise.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DBConnection {

    public static Connection getConnection() throws Exception {

        // Explicitly load MySQL JDBC driver
        Class.forName("com.mysql.cj.jdbc.Driver");

        Properties props = new Properties();

        try (InputStream input = DBConnection.class
                .getClassLoader()
                .getResourceAsStream("connection.properties")) {

            if (input == null) {
                throw new Exception("connection.properties not found");
            }

            props.load(input);
        }

        String url = props.getProperty("db.url");
        String username = props.getProperty("db.username");
        String password = props.getProperty("db.password");

        return DriverManager.getConnection(url, username, password);
    }
}