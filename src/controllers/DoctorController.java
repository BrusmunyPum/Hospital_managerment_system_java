package controllers;

import models.Doctor;
import db.dbConnecting;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DoctorController {

    public boolean addDoctor(Doctor doctor) {
        String sql = "INSERT INTO doctors (doctor_id, name, specialization) VALUES (?, ?, ?)";
        try (Connection conn = dbConnecting.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, doctor.getDoctorId());
            pstmt.setString(2, doctor.getName());
            pstmt.setString(3, doctor.getSpecialization());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateDoctor(Doctor doctor) {
        String sql = "UPDATE doctors SET name=?, specialization=? WHERE doctor_id=?";
        try (Connection conn = dbConnecting.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, doctor.getName());
            pstmt.setString(2, doctor.getSpecialization());
            pstmt.setString(3, doctor.getDoctorId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- NEW: Delete Doctor ---
    public boolean deleteDoctor(String doctorId) {
        // 1. Unassign patients first (Optional but recommended for safety)
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
                    rs.getString("specialization")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

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
                    rs.getString("specialization")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

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
                    rs.getString("specialization")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

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

    // --- DASHBOARD: DEPARTMENT LOAD ---
    // Returns Mapping of Specialization -> Count
    public Map<String, Integer> getSpecializationStats() {
        Map<String, Integer> stats = new HashMap<>();
        String sql = "SELECT specialization, COUNT(*) as count FROM doctors GROUP BY specialization";
        try (Connection conn = dbConnecting.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String spec = rs.getString("specialization");
                if(spec == null || spec.isEmpty()) spec = "General";
                stats.put(spec, rs.getInt("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    // --- DASHBOARD: AVAILABLE DOCTORS ---
    // Ideally this would check a 'status' or 'shift' column, but for now we fetch a random subset
    public List<Doctor> getAvailableDoctors() {
        List<Doctor> list = new ArrayList<>();
        // Fetch random 3 doctors
        String sql = "SELECT * FROM doctors ORDER BY RANDOM() LIMIT 3"; 
        // Note: RANDOM() works in Postgre/SQLite. For MySQL use RAND().
        // Assuming Standard SQL or simple limit for universal compat.
        // If not supported, just "ORDER BY doctor_id DESC LIMIT 3"
        
        // Let's check DB type? If unkown, use generic standard:
        // Actually, let's use a safe fallback if RANDOM() fails or just simply grab first 3.
        // "ORDER BY doctor_id" is safe.
        sql = "SELECT * FROM doctors LIMIT 3";
        
        try (Connection conn = dbConnecting.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Doctor(
                    rs.getString("doctor_id"),
                    rs.getString("name"),
                    rs.getString("specialization")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}