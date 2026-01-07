package controllers;

import models.User;
import db.dbConnecting;
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
                    rs.getString("role"),
                    rs.getString("linked_id")
                );
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean createUser(String username, String password, String role, String linkedId) {
        String sql = "INSERT INTO users (username, password, role, linked_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbConnecting.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role);
            if (linkedId != null && !linkedId.isEmpty()) pstmt.setString(4, linkedId);
            else pstmt.setNull(4, Types.VARCHAR);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // --- NEW: Update User ---
    public boolean updateUser(String username, String newPassword, String role, String linkedId) {
        // Logic: If newPassword is empty, don't update the password column
        StringBuilder sql = new StringBuilder("UPDATE users SET role = ?, linked_id = ?");
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            sql.append(", password = ?");
        }
        sql.append(" WHERE username = ?");

        try (Connection conn = dbConnecting.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            pstmt.setString(1, role);
            
            if (linkedId != null && !linkedId.isEmpty()) pstmt.setString(2, linkedId);
            else pstmt.setNull(2, Types.VARCHAR);

            int paramIndex = 3;
            // Only set password parameter if we are updating it
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                pstmt.setString(paramIndex++, newPassword);
            }
            
            pstmt.setString(paramIndex, username); // Where clause

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean deleteUser(String username) {
        String sql = "DELETE FROM users WHERE username = ?";
        try (Connection conn = dbConnecting.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection conn = dbConnecting.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new User(
                    rs.getString("username"),
                    rs.getString("role"),
                    rs.getString("linked_id")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = dbConnecting.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(
                    rs.getString("username"),
                    rs.getString("role"),
                    rs.getString("linked_id")
                );
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
}