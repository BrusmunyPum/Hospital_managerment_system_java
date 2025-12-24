package controllers;

import models.*;
import db.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserController {

    public User authenticate(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        
        try (Connection conn = dbConnecting.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new User(
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("role"),
                    rs.getString("linked_id") // Fetch Linked ID
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // --- NEW: Create User (For Admin Panel) ---
    public boolean createUser(String username, String password, String role, String linkedId) {
        String sql = "INSERT INTO users (username, password, role, linked_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbConnecting.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role);
            if (linkedId != null && !linkedId.isEmpty()) {
                pstmt.setString(4, linkedId);
            } else {
                pstmt.setNull(4, Types.VARCHAR);
            }
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace(); // Handle duplicate username error
            return false;
        }
    }
    
    // --- NEW: Get All Users (For Admin Panel Table) ---
    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection conn = dbConnecting.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new User(
                    rs.getString("username"),
                    "*****", // Hide password
                    rs.getString("role"),
                    rs.getString("linked_id")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    // --- NEW: Delete User ---
    public boolean deleteUser(String username) {
        String sql = "DELETE FROM users WHERE username = ?";
        try (Connection conn = dbConnecting.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}