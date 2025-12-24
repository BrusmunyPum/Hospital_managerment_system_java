package controllers;

import models.Room;
import models.Patient;
import db.dbConnecting;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomController {

    public boolean addRoom(Room room) {
        String sql = "INSERT INTO rooms (room_id, room_type) VALUES (?, ?)";
        try (Connection conn = dbConnecting.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, room.getRoomId());
            pstmt.setString(2, room.getRoomType());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateRoom(Room room) {
        String sql = "UPDATE rooms SET room_type=? WHERE room_id=?";
        try (Connection conn = dbConnecting.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, room.getRoomType());
            pstmt.setString(2, room.getRoomId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- NEW: Delete Room ---
    public boolean deleteRoom(String roomId) {
        // 1. Remove patients from this room
        String unassignSql = "UPDATE patients SET room_id = NULL WHERE room_id = ?";
        // 2. Delete the room
        String deleteSql = "DELETE FROM rooms WHERE room_id = ?";
        
        try (Connection conn = dbConnecting.getConnection()) {
            conn.setAutoCommit(false); // Transaction

            try (PreparedStatement pstmt1 = conn.prepareStatement(unassignSql)) {
                pstmt1.setString(1, roomId);
                pstmt1.executeUpdate();
            }

            try (PreparedStatement pstmt2 = conn.prepareStatement(deleteSql)) {
                pstmt2.setString(1, roomId);
                int rows = pstmt2.executeUpdate();
                conn.commit();
                return rows > 0;
            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Room> searchRooms(String query) {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT r.*, p.patient_id, p.name AS patient_name " +
                     "FROM rooms r " +
                     "LEFT JOIN patients p ON r.room_id = p.room_id " +
                     "WHERE r.room_id ILIKE ? OR r.room_type ILIKE ?";
                     
        try (Connection conn = dbConnecting.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String searchPattern = "%" + query + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Room r = new Room(rs.getString("room_id"), rs.getString("room_type"));
                String pid = rs.getString("patient_id");
                if (pid != null) {
                    Patient p = new Patient(pid, rs.getString("patient_name"), 0, "", "");
                    r.assignPatient(p);
                }
                rooms.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    public Room findRoomById(String roomId) {
        String sql = "SELECT r.*, p.patient_id, p.name AS patient_name " +
                     "FROM rooms r " +
                     "LEFT JOIN patients p ON r.room_id = p.room_id " +
                     "WHERE r.room_id = ?";
        try (Connection conn = dbConnecting.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, roomId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Room r = new Room(rs.getString("room_id"), rs.getString("room_type"));
                String pid = rs.getString("patient_id");
                if (pid != null) {
                    Patient p = new Patient(pid, rs.getString("patient_name"), 0, "", "");
                    r.assignPatient(p);
                }
                return r;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT r.*, p.patient_id, p.name AS patient_name " +
                     "FROM rooms r " +
                     "LEFT JOIN patients p ON r.room_id = p.room_id";
        try (Connection conn = dbConnecting.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Room r = new Room(rs.getString("room_id"), rs.getString("room_type"));
                String pid = rs.getString("patient_id");
                if (pid != null) {
                    Patient p = new Patient(pid, rs.getString("patient_name"), 0, "", "");
                    r.assignPatient(p);
                }
                rooms.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    public List<Room> getAvailableRooms() {
        List<Room> available = new ArrayList<>();
        String sql = "SELECT r.* FROM rooms r " +
                     "LEFT JOIN patients p ON r.room_id = p.room_id " +
                     "WHERE p.patient_id IS NULL";
        try (Connection conn = dbConnecting.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                available.add(new Room(rs.getString("room_id"), rs.getString("room_type")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return available;
    }

    public int getTotalRooms() {
        String sql = "SELECT COUNT(*) FROM rooms";
        try (Connection conn = dbConnecting.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getOccupiedRoomCount() {
        String sql = "SELECT COUNT(*) FROM patients WHERE room_id IS NOT NULL";
        try (Connection conn = dbConnecting.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}