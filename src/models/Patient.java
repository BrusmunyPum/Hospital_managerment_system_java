package models;

import java.time.LocalDate;

public class Patient {
    private String patientId;
    private String name;
    private int age;
    private String address;
    private String medicalHistory;
    private LocalDate admissionDate;
    private String imagePath; // NEW
    
    private Room room; 
    private Doctor doctor; 

    // Updated Constructor
    public Patient(String patientId, String name, int age, String address, String medicalHistory, String imagePath) {
        this.patientId = patientId;
        this.name = name;
        this.age = age;
        this.address = address;
        this.medicalHistory = medicalHistory;
        this.imagePath = imagePath; // NEW
        this.admissionDate = LocalDate.now();
        this.room = null;
        this.doctor = null;
    }
    
    // Backwards compatibility constructor
    public Patient(String patientId, String name, int age, String address, String medicalHistory) {
        this(patientId, name, age, address, medicalHistory, null);
    }

    // ... (Keep existing relationship methods and Getters) ...
    public void assignRoom(Room r) { this.room = r; }
    public void assignDoctor(Doctor d) { this.doctor = d; }
    public void updateDetails(String name, int age, String address, String medicalHistory) {
        this.name = name;
        this.age = age;
        this.address = address;
        this.medicalHistory = medicalHistory;
    }

    // Getters
    public String getPatientId() { return patientId; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getAddress() { return address; }
    public String getMedicalHistory() { return medicalHistory; }
    public Room getRoom() { return room; }
    public Doctor getDoctor() { return doctor; }
    public LocalDate getAdmissionDate() { return admissionDate; }
    public void setAdmissionDate(LocalDate admissionDate) { this.admissionDate = admissionDate; }
    
    // New Getter/Setter for Image
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    @Override
    public String toString() { return name + " (ID: " + patientId + ")"; }
}