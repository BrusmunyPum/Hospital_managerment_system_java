package models;

import java.util.ArrayList;
import java.util.List;

public class Doctor {
    private String doctorId;
    private String name;
    private String specialization;
    private List<Patient> patients;
    private String imagePath; // NEW: Path to profile image

    public Doctor(String doctorId, String name, String specialization, String imagePath) {
        this.doctorId = doctorId;
        this.name = name;
        this.specialization = specialization;
        this.imagePath = imagePath;
        this.patients = new ArrayList<>();
    }
    
    // Backwards compatibility constructor (defaults image to null)
    public Doctor(String doctorId, String name, String specialization) {
        this(doctorId, name, specialization, null);
    }

    // ... (Keep existing relationship methods) ...
    public void assignPatient(Patient p) { if (!patients.contains(p)) patients.add(p); }
    public void removePatient(Patient p) { patients.remove(p); }
    public List<Patient> getAllPatients() { return new ArrayList<>(patients); }

    // --- Getters & Setters ---
    public String getDoctorId() { return doctorId; }
    public String getName() { return name; }
    public String getSpecialization() { return specialization; }
    public String getImagePath() { return imagePath; } // NEW

    public void setName(String name) { this.name = name; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; } // NEW

    @Override
    public String toString() { return name + " (" + specialization + ")"; }
}