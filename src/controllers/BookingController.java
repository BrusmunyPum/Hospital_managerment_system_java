package controllers;

import models.Booking;
import models.Patient;
import models.Doctor;
import models.Room;
import db.dbConnecting;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BookingController {

    private PatientController patientCtrl;
    private DoctorController doctorCtrl;
    private RoomController roomCtrl;

    public BookingController() {
        this.patientCtrl = new PatientController();
        this.doctorCtrl = new DoctorController();
        this.roomCtrl = new RoomController();
    }

    // --- CREATE BOOKING (Guest User) ---
    public boolean createBooking(Booking booking) {
        String sql = "INSERT INTO bookings (patient_name, age, gender, contact_number, symptoms, doctor_id, room_id, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDING')";
        
        try (Connection conn = dbConnecting.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, booking.getPatientName());
            pstmt.setInt(2, booking.getAge());
            pstmt.setString(3, booking.getGender());
            pstmt.setString(4, booking.getContactNumber());
            pstmt.setString(5, booking.getSymptoms());
            
            if (booking.getDoctorId() != null && !booking.getDoctorId().isEmpty())
                pstmt.setString(6, booking.getDoctorId());
            else
                pstmt.setNull(6, Types.VARCHAR);

            if (booking.getRoomId() != null && !booking.getRoomId().isEmpty())
                pstmt.setString(7, booking.getRoomId());
            else
                pstmt.setNull(7, Types.VARCHAR);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- GET PENDING BOOKINGS (Admin) ---
    public List<Booking> getPendingBookings() {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM bookings WHERE status = 'PENDING' ORDER BY booking_date DESC";
        
        try (Connection conn = dbConnecting.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                list.add(new Booking(
                    rs.getInt("booking_id"),
                    rs.getString("patient_name"),
                    rs.getInt("age"),
                    rs.getString("gender"),
                    rs.getString("contact_number"),
                    rs.getString("symptoms"),
                    rs.getString("doctor_id"),
                    rs.getString("room_id"),
                    rs.getString("status"),
                    rs.getTimestamp("booking_date")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // --- APPROVE BOOKING ---
    public boolean approveBooking(int bookingId) {
        Booking booking = getBookingById(bookingId);
        if (booking == null) return false;

        // 1. Generate Patient ID (P-timestamp-random)
        String patientId = "P-" + System.currentTimeMillis() % 100000 + "-" + new Random().nextInt(100);

        // 2. Prepare Patient Data
        // Combine gender/contact into medical history or similar since Patient model is simple
        String fullMedicalHistory = booking.getSymptoms() + " [Gender: " + booking.getGender() + ", Contact: " + booking.getContactNumber() + "]";
        
        Patient newPatient = new Patient(
            patientId,
            booking.getPatientName(),
            booking.getAge(),
            "Unknown Address", // Booking doesn't have address, could add to form later
            fullMedicalHistory
        );

        // 3. Insert Patient
        if (patientCtrl.addPatient(newPatient)) {
            
            // 4. Assign Relationships (Doctor/Room) if they exist
            if (booking.getDoctorId() != null) {
                Doctor d = doctorCtrl.findDoctorById(booking.getDoctorId());
                if (d != null) newPatient.assignDoctor(d);
            }
            if (booking.getRoomId() != null) {
                Room r = roomCtrl.findRoomById(booking.getRoomId());
                if (r != null) newPatient.assignRoom(r);
            }
            // Update DB relationships
            patientCtrl.updatePatientRelationships(newPatient);

            // 5. Update Booking Status
            updateBookingStatus(bookingId, "APPROVED");
            return true;
        }
        return false;
    }

    // --- REJECT BOOKING ---
    public boolean rejectBooking(int bookingId) {
        return updateBookingStatus(bookingId, "REJECTED");
    }

    private boolean updateBookingStatus(int bookingId, String status) {
        String sql = "UPDATE bookings SET status = ? WHERE booking_id = ?";
        try (Connection conn = dbConnecting.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, bookingId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Booking getBookingById(int bookingId) {
        String sql = "SELECT * FROM bookings WHERE booking_id = ?";
        try (Connection conn = dbConnecting.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookingId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Booking(
                    rs.getInt("booking_id"),
                    rs.getString("patient_name"),
                    rs.getInt("age"),
                    rs.getString("gender"),
                    rs.getString("contact_number"),
                    rs.getString("symptoms"),
                    rs.getString("doctor_id"),
                    rs.getString("room_id"),
                    rs.getString("status"),
                    rs.getTimestamp("booking_date")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
