package models;

import java.util.ArrayList;
import java.util.List;

public class Doctor {
    private String doctorId;
    private String name;
    private String specialization;
    private List<Patient> patients; // List of patients assigned to this doctor

    public Doctor(String doctorId, String name, String specialization) {
        this.doctorId = doctorId;
        this.name = name;
        this.specialization = specialization;
        this.patients = new ArrayList<>();
    }

    // --- Relationship Management ---
    public void assignPatient(Patient p) {
        if (!patients.contains(p)) {
            patients.add(p);
        }
    }

    public void removePatient(Patient p) {
        patients.remove(p);
    }

    public List<Patient> getAllPatients() {
        return new ArrayList<>(patients); // Return a copy to protect internal list
    }

    // --- Getters ---
    public String getDoctorId() { return doctorId; }
    public String getName() { return name; }
    public String getSpecialization() { return specialization; }

    // --- Setters (for updates) ---
    public void setName(String name) { this.name = name; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    // --- Utility for UI ---
    @Override
    public String toString() {
        return name + " (" + specialization + ")";
    }
}