package com.church.churchapp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class QueryUsers {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/churchdb";
        String user = "postgres";
        String password = "root";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            
            System.out.println("--- DATABASE RECORD COUNTS ---");
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM members")) {
                if (rs.next()) System.out.println("Members: " + rs.getInt(1));
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM church_groups")) {
                if (rs.next()) System.out.println("Groups: " + rs.getInt(1));
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM announcements")) {
                if (rs.next()) System.out.println("Announcements: " + rs.getInt(1));
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM communication_logs")) {
                if (rs.next()) System.out.println("Communication Logs: " + rs.getInt(1));
            }

        } catch (Exception e1) {
            System.out.println("Failed with password 'root': " + e1.getMessage());
            password = "password";
            try (Connection conn = DriverManager.getConnection(url, user, password);
                 Statement stmt = conn.createStatement()) {
                
                System.out.println("--- DATABASE RECORD COUNTS ---");
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM members")) {
                    if (rs.next()) System.out.println("Members: " + rs.getInt(1));
                }
                try (ResultSet rs = stmt.executeQuery("svg COUNT(*) FROM church_groups")) {
                    if (rs.next()) System.out.println("Groups: " + rs.getInt(1));
                }
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM announcements")) {
                    if (rs.next()) System.out.println("Announcements: " + rs.getInt(1));
                }
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM communication_logs")) {
                    if (rs.next()) System.out.println("Communication Logs: " + rs.getInt(1));
                }

            } catch (Exception e2) {
                System.out.println("Failed with password 'password' too: " + e2.getMessage());
            }
        }
    }
}
