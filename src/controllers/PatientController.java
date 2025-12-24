package controllers;

import models.Patient;
import models.Room;
import models.Doctor;
import db.dbConnecting;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class PatientController {

    private RoomController roomCtrl = new RoomController();
    private DoctorController doctorCtrl = new DoctorController();

    // --- CRUD: ADD ---
    public boolean addPatient(Patient patient) {
        // Updated to include admission_date
        String sql = "INSERT INTO patients (patient_id, name, age, address, medical_history, admission_date) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbConnecting.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, patient.getPatientId());
            pstmt.setString(2, patient.getName());
            pstmt.setInt(3, patient.getAge());
            pstmt.setString(4, patient.getAddress());
            pstmt.setString(5, patient.getMedicalHistory());
            pstmt.setDate(6, java.sql.Date.valueOf(patient.getAdmissionDate())); // Save Date
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- CRUD: UPDATE ---
    public boolean updatePatient(Patient p) {
        String sql = "UPDATE patients SET name=?, age=?, address=?, medical_history=? WHERE patient_id=?";
        try (Connection conn = dbConnecting.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, p.getName());
            pstmt.setInt(2, p.getAge());
            pstmt.setString(3, p.getAddress());
            pstmt.setString(4, p.getMedicalHistory());
            pstmt.setString(5, p.getPatientId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // --- CRUD: UPDATE RELATIONS ---
    public void updatePatientRelationships(Patient p) {
        String sql = "UPDATE patients SET room_id = ?, doctor_id = ? WHERE patient_id = ?";
        try (Connection conn = dbConnecting.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (p.getRoom() != null) pstmt.setString(1, p.getRoom().getRoomId());
            else pstmt.setNull(1, java.sql.Types.VARCHAR);

            if (p.getDoctor() != null) pstmt.setString(2, p.getDoctor().getDoctorId());
            else pstmt.setNull(2, java.sql.Types.VARCHAR);

            pstmt.setString(3, p.getPatientId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- CRUD: DELETE ---
    public boolean removePatient(String patientId) {
        String sql = "DELETE FROM patients WHERE patient_id = ?";
        try (Connection conn = dbConnecting.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, patientId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean dischargePatient(String patientId) {
        return removePatient(patientId);
    }
    
    // --- FIND SINGLE PATIENT ---
    public Patient findPatientById(String patientId) {
        String sql = "SELECT * FROM patients WHERE patient_id = ?";
        Patient p = null;
        try (Connection conn = dbConnecting.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, patientId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                p = new Patient(
                    rs.getString("patient_id"),
                    rs.getString("name"),
                    rs.getInt("age"),
                    rs.getString("address"),
                    rs.getString("medical_history")
                );
                
                // Load Date
                java.sql.Date dbDate = rs.getDate("admission_date");
                if (dbDate != null) p.setAdmissionDate(dbDate.toLocalDate());

                // Load Relations
                String roomId = rs.getString("room_id");
                if (roomId != null) {
                    Room r = roomCtrl.findRoomById(roomId);
                    if (r != null) { p.assignRoom(r); r.assignPatient(p); }
                }
                String doctorId = rs.getString("doctor_id");
                if (doctorId != null) {
                    Doctor d = doctorCtrl.findDoctorById(doctorId);
                    if (d != null) { p.assignDoctor(d); d.assignPatient(p); }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return p;
    }

    // --- OPTIMIZED: GET ALL (JOIN QUERY) ---
    public List<Patient> getAllPatients() {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT p.*, r.room_type, d.name AS doc_name, d.specialization " +
                     "FROM patients p " +
                     "LEFT JOIN rooms r ON p.room_id = r.room_id " +
                     "LEFT JOIN doctors d ON p.doctor_id = d.doctor_id";

        try (Connection conn = dbConnecting.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(extractPatientFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // --- NEW: FILTER BY DOCTOR (JOIN QUERY) ---
    public List<Patient> getPatientsByDoctorId(String doctorId) {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT p.*, r.room_type, d.name AS doc_name, d.specialization " +
                     "FROM patients p " +
                     "LEFT JOIN rooms r ON p.room_id = r.room_id " +
                     "LEFT JOIN doctors d ON p.doctor_id = d.doctor_id " +
                     "WHERE p.doctor_id = ?";

        try (Connection conn = dbConnecting.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, doctorId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(extractPatientFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // --- OPTIMIZED: SEARCH (JOIN QUERY) ---
    public List<Patient> searchPatients(String query) {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT p.*, r.room_type, d.name AS doc_name, d.specialization " +
                     "FROM patients p " +
                     "LEFT JOIN rooms r ON p.room_id = r.room_id " +
                     "LEFT JOIN doctors d ON p.doctor_id = d.doctor_id " +
                     "WHERE p.name ILIKE ? OR p.patient_id ILIKE ?";
                     
        try (Connection conn = dbConnecting.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String searchPattern = "%" + query + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(extractPatientFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    // Helper to extract patient data from JOIN results
    private Patient extractPatientFromResultSet(ResultSet rs) throws SQLException {
        Patient p = new Patient(
            rs.getString("patient_id"),
            rs.getString("name"),
            rs.getInt("age"),
            rs.getString("address"),
            rs.getString("medical_history")
        );
        
        java.sql.Date dbDate = rs.getDate("admission_date");
        if (dbDate != null) p.setAdmissionDate(dbDate.toLocalDate());

        String roomId = rs.getString("room_id");
        if (roomId != null) {
            Room r = new Room(roomId, rs.getString("room_type"));
            p.assignRoom(r);
            r.assignPatient(p); 
        }

        String doctorId = rs.getString("doctor_id");
        if (doctorId != null) {
            Doctor d = new Doctor(doctorId, rs.getString("doc_name"), rs.getString("specialization"));
            p.assignDoctor(d);
            d.assignPatient(p);
        }
        return p;
    }
    
    // --- DASHBOARD STATS ---
    public int getPatientCount() {
        String sql = "SELECT COUNT(*) FROM patients";
        try (Connection conn = dbConnecting.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // --- BILLING GENERATOR ---
    public String generateBill(String patientId, LocalDate dischargeDate) {
        Patient p = findPatientById(patientId);
        if (p == null) return centerText("Error: Patient not found.");
        
        if (p.getRoom() == null) {
            return centerText("Error: Patient is not assigned to a room.\nCannot calculate bill.");
        }

        LocalDate admissionDate = p.getAdmissionDate();
        if (admissionDate == null) admissionDate = LocalDate.now(); 

        long days = ChronoUnit.DAYS.between(admissionDate, dischargeDate);
        if (days < 0) return centerText("Error: Discharge date cannot be before admission.");
        if (days == 0) days = 1; 

        double dailyRate = 0;
        String roomType = p.getRoom().getRoomType();
        if (roomType.equalsIgnoreCase("ICU")) dailyRate = 200.0;
        else if (roomType.equalsIgnoreCase("Private")) dailyRate = 100.0;
        else dailyRate = 50.0;

        double totalBill = days * dailyRate;

        StringBuilder sb = new StringBuilder();
        sb.append(centerText("======================================")).append("\n");
        sb.append(centerText("       HOSPITAL INVOICE       ")).append("\n");
        sb.append(centerText("======================================")).append("\n\n");
        
        sb.append(centerText("Patient: " + p.getName())).append("\n");
        sb.append(centerText("ID: " + p.getPatientId())).append("\n");
        sb.append(centerText("Room: " + p.getRoom().getRoomId() + " (" + roomType + ")")).append("\n\n");
        
        sb.append(centerText("--------------------------------------")).append("\n");
        sb.append(centerText("Admission: " + admissionDate)).append("\n");
        sb.append(centerText("Discharge: " + dischargeDate)).append("\n");
        sb.append(centerText("Total Stay: " + days + " Day(s)")).append("\n");
        sb.append(centerText("Rate: $" + String.format("%.2f", dailyRate) + " / day")).append("\n");
        sb.append(centerText("--------------------------------------")).append("\n\n");
        
        sb.append(centerText("TOTAL DUE: $" + String.format("%.2f", totalBill))).append("\n");
        sb.append("\n").append(centerText("======================================"));

        return sb.toString();
    }

    private String centerText(String text) {
        int width = 50; 
        int padding = (width - text.length()) / 2;
        if (padding <= 0) return text;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < padding; i++) sb.append(" ");
        sb.append(text);
        return sb.toString();
    }
}