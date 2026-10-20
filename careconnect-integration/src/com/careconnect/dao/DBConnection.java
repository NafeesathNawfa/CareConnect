package com.careconnect.dao;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Manages database connectivity to the MySQL free_clinic database.
 *
 * Connection settings are resolved in this order (first one found wins, per key):
 *   1. Environment variables:  CARECONNECT_DB_URL / CARECONNECT_DB_USER / CARECONNECT_DB_PASS
 *   2. A db.properties file in the working directory (keys: db.url, db.user, db.pass)
 *   3. Built-in defaults for a local MySQL server
 *
 * If the connection fails, the reason is kept in {@link #getLastError()} so the UI can show
 * WHY the app fell back to memory mode instead of failing silently.
 */
public class DBConnection {
    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/free_clinic?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASS = "root";
    private static final String CONFIG_FILE = "db.properties";

    private static String url;
    private static String user;
    private static String pass;
    private static Boolean connectionTested = null;
    private static String lastError = null;

    static {
        loadConfig();
    }

    private static void loadConfig() {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(CONFIG_FILE)) {
            props.load(in);
        } catch (IOException ignored) {
            // No db.properties file: fall through to env vars / defaults.
        }
        url  = firstNonEmpty(System.getenv("CARECONNECT_DB_URL"),  props.getProperty("db.url"),  DEFAULT_URL);
        user = firstNonEmpty(System.getenv("CARECONNECT_DB_USER"), props.getProperty("db.user"), DEFAULT_USER);
        pass = firstNonEmpty(System.getenv("CARECONNECT_DB_PASS"), props.getProperty("db.pass"), DEFAULT_PASS);
    }

    private static String firstNonEmpty(String... values) {
        for (String v : values) {
            if (v != null && !v.trim().isEmpty()) return v.trim();
        }
        return null;
    }

    /** Programmatic override (e.g. from a settings dialog). Resets the cached connectivity test. */
    public static void configure(String dbUrl, String dbUser, String dbPass) {
        url = dbUrl;
        user = dbUser;
        pass = dbPass;
        connectionTested = null;
        lastError = null;
    }

    public static boolean isAvailable() {
        if (connectionTested != null) {
            return connectionTested;
        }
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            return fail("MySQL JDBC driver not on classpath. Put mysql-connector-j-*.jar in the lib/ folder.");
        }
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            connectionTested = (conn != null && !conn.isClosed());
            lastError = null;
            return connectionTested;
        } catch (SQLException e) {
            return fail("Cannot connect to MySQL as '" + user + "': " + e.getMessage());
        } catch (Throwable t) {
            return fail("Unexpected database error: " + t);
        }
    }

    private static boolean fail(String message) {
        connectionTested = false;
        lastError = message;
        System.err.println("[CareConnect] " + message);
        return false;
    }

    /** Why the last connectivity test failed, or null if it succeeded / has not run. */
    public static String getLastError() {
        return lastError;
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
