package com.careconnect.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages database connectivity to MySQL free_clinic database.
 * Supports configurable connection properties and connectivity testing.
 */
public class DBConnection {
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/free_clinic?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASS = "root";

    private static String url = DEFAULT_URL;
    private static String user = DEFAULT_USER;
    private static String pass = DEFAULT_PASS;
    private static Boolean connectionTested = null;

    public static void configure(String dbUrl, String dbUser, String dbPass) {
        url = dbUrl;
        user = dbUser;
        pass = dbPass;
        connectionTested = null;
    }

    public static boolean isAvailable() {
        if (connectionTested != null) {
            return connectionTested;
        }
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(url, user, pass)) {
                connectionTested = (conn != null && !conn.isClosed());
                return connectionTested;
            }
        } catch (Throwable t) {
            connectionTested = false;
            return false;
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found in classpath. Please add mysql-connector-j.", e);
        }
        return DriverManager.getConnection(url, user, pass);
    }
}
