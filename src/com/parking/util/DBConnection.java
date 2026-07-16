package com.parking.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton DB connection utility.
 * Update DB_URL, DB_USER, DB_PASSWORD to match your MySQL setup.
 */
public class DBConnection {

    // ──────────────────────────────────────────────
    //  CHANGE THESE TO MATCH YOUR MYSQL SETUP
    // ──────────────────────────────────────────────
    private static final String DB_URL      = "jdbc:mysql://localhost:3306/parking_lot_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String DB_USER     = "root";
    private static final String DB_PASSWORD = "1234";   // ← your MySQL password
    // ──────────────────────────────────────────────

    private static Connection connection = null;

    private DBConnection() {}

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                System.out.println("[DB] Connected to parking_lot_db successfully.");
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver not found. Add mysql-connector-j JAR to /lib folder.", e);
            }
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("[DB] Connection closed.");
            } catch (SQLException e) {
                System.err.println("[DB] Error closing connection: " + e.getMessage());
            }
        }
    }
}
