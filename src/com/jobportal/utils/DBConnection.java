package com.jobportal.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    public static Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/JobPortal";
        String user = "root";
        String password = "krish1410";
        return DriverManager.getConnection(url, user, password);
    }
}
