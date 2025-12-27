package controllers;

import models.Doctor;
import db.dbConnecting;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DoctorController {

    // --- CRUD: ADD ---
    public boolean addDoctor(Doctor doctor) {
        String sql = "INSERT INTO doctors (doctor_id, name, specialization, image_path) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbConnecting.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, doctor.getDoctorId());
            pstmt.setString(2, doctor.getName());
            pstmt.setString(3, doctor.getSpecialization());
            pstmt.setString(4, doctor.getImagePath()); // Save Image Path
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- CRUD: UPDATE ---
    public boolean updateDoctor(Doctor doctor) {
        String sql = "UPDATE doctors SET name=?, specialization=?, image_path=? WHERE doctor_id=?";
        try (Connection conn = dbConnecting.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, doctor.getName());
            pstmt.setString(2, doctor.getSpecialization());
            pstmt.setString(3, doctor.getImagePath()); // Update Image Path
            pstmt.setString(4, doctor.getDoctorId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- CRUD: DELETE ---
    public boolean deleteDoctor(String doctorId) {
        // 1. Unassign patients first (Set doctor_id to NULL)
        String unassignSql = "UPDATE patients SET doctor_id = NULL WHERE doctor_id = ?";
        // 2. Delete the doctor
        String deleteSql = "DELETE FROM doctors WHERE doctor_id = ?";
        
        try (Connection conn = dbConnecting.getConnection()) {
            conn.setAutoCommit(false); // Start Transaction

            try (PreparedStatement pstmt1 = conn.prepareStatement(unassignSql)) {
                pstmt1.setString(1, doctorId);
                pstmt1.executeUpdate();
            }

            try (PreparedStatement pstmt2 = conn.prepareStatement(deleteSql)) {
                pstmt2.setString(1, doctorId);
                int rows = pstmt2.executeUpdate();
                
                conn.commit(); // Commit Transaction
                return rows > 0;
            } catch (SQLException ex) {
                conn.rollback(); // Undo if delete fails
                ex.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- SEARCH ---
    public List<Doctor> searchDoctors(String query) {
        List<Doctor> list = new ArrayList<>();
        String sql = "SELECT * FROM doctors WHERE name ILIKE ? OR specialization ILIKE ? OR doctor_id ILIKE ?";
        try (Connection conn = dbConnecting.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String searchPattern = "%" + query + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new Doctor(
                    rs.getString("doctor_id"),
                    rs.getString("name"),
                    rs.getString("specialization"),
                    rs.getString("image_path") // Retrieve Image Path
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // --- FIND SINGLE DOCTOR ---
    public Doctor findDoctorById(String doctorId) {
        String sql = "SELECT * FROM doctors WHERE doctor_id = ?";
        try (Connection conn = dbConnecting.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, doctorId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Doctor(
                    rs.getString("doctor_id"),
                    rs.getString("name"),
                    rs.getString("specialization"),
                    rs.getString("image_path") // Retrieve Image Path
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // --- GET ALL ---
    public List<Doctor> getAllDoctors() {
        List<Doctor> list = new ArrayList<>();
        String sql = "SELECT * FROM doctors";
        try (Connection conn = dbConnecting.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Doctor(
                    rs.getString("doctor_id"),
                    rs.getString("name"),
                    rs.getString("specialization"),
                    rs.getString("image_path") // Retrieve Image Path
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // --- STATS ---
    public int getDoctorCount() {
        String sql = "SELECT COUNT(*) FROM doctors";
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