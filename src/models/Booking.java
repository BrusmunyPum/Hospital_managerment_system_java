package models;

import java.sql.Timestamp;

public class Booking {
    private int bookingId;
    private String patientName;
    private int age;
    private String gender;
    private String contactNumber;
    private String symptoms;
    
    private String doctorId;
    private String roomId;
    private String status;
    private Timestamp bookingDate;

    // Full Constructor
    public Booking(int bookingId, String patientName, int age, String gender, String contactNumber, 
                   String symptoms, String doctorId, String roomId, String status, Timestamp bookingDate) {
        this.bookingId = bookingId;
        this.patientName = patientName;
        this.age = age;
        this.gender = gender;
        this.contactNumber = contactNumber;
        this.symptoms = symptoms;
        this.doctorId = doctorId;
        this.roomId = roomId;
        this.status = status;
        this.bookingDate = bookingDate;
    }

    // Constructor for creating a new booking (no ID/Timestamp yet)
    public Booking(String patientName, int age, String gender, String contactNumber, 
                   String symptoms, String doctorId, String roomId) {
        this.patientName = patientName;
        this.age = age;
        this.gender = gender;
        this.contactNumber = contactNumber;
        this.symptoms = symptoms;
        this.doctorId = doctorId;
        this.roomId = roomId;
        this.status = "PENDING";
    }

    // Getters
    public int getBookingId() { return bookingId; }
    public String getPatientName() { return patientName; }
    public int getAge() { return age; }
    public String getGender() { return gender; }
    public String getContactNumber() { return contactNumber; }
    public String getSymptoms() { return symptoms; }
    public String getDoctorId() { return doctorId; }
    public String getRoomId() { return roomId; }
    public String getStatus() { return status; }
    public Timestamp getBookingDate() { return bookingDate; }

    // Setters
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "Booking #" + bookingId + ": " + patientName + " (" + status + ")";
    }
}
