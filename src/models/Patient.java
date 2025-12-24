package models;

import java.time.LocalDate; 

public class Patient {
    private String patientId;
    private String name;
    private int age;
    private String address;
    private String medicalHistory;
    
    // NEW FIELD: Stores when the patient was admitted
    private LocalDate admissionDate;
    
    // References to other objects
    private Room room; 
    private Doctor doctor; 

    public Patient(String patientId, String name, int age, String address, String medicalHistory) {
        this.patientId = patientId;
        this.name = name;
        this.age = age;
        this.address = address;
        this.medicalHistory = medicalHistory;
        
        // NEW: Default to today's date when creating a new patient object
        // This prevents "NullPointerException" when saving to the database
        this.admissionDate = LocalDate.now();
        
        this.room = null;
        this.doctor = null;
    }

    // --- Relationship Management ---
    public void assignRoom(Room r) {
        this.room = r;
    }

    public void assignDoctor(Doctor d) {
        this.doctor = d;
    }

    public void updateDetails(String name, int age, String address, String medicalHistory) {
        this.name = name;
        this.age = age;
        this.address = address;
        this.medicalHistory = medicalHistory;
    }

    // --- Getters ---
    public String getPatientId() { return patientId; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getAddress() { return address; }
    public String getMedicalHistory() { return medicalHistory; }
    public Room getRoom() { return room; }
    public Doctor getDoctor() { return doctor; }
    
    // NEW: Getter and Setter for Date
    // Without these, the controller cannot access the date!
    public LocalDate getAdmissionDate() { return admissionDate; }
    public void setAdmissionDate(LocalDate admissionDate) { this.admissionDate = admissionDate; }

    // --- Utility for UI ---
    @Override
    public String toString() {
        return name + " (ID: " + patientId + ")";
    }
}