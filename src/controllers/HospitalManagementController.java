package controllers;

import models.*;

public class HospitalManagementController {
    
    private PatientController patientCtrl;
    private DoctorController doctorCtrl;
    private RoomController roomCtrl;

    public HospitalManagementController() {
        this.patientCtrl = new PatientController();
        this.doctorCtrl = new DoctorController();
        this.roomCtrl = new RoomController();
    }

    public PatientController getPatientCtrl() { return patientCtrl; }
    public DoctorController getDoctorCtrl() { return doctorCtrl; }
    public RoomController getRoomCtrl() { return roomCtrl; }

    // --- Complex Logic: Assign Patient to Room ---
    public boolean assignPatientToRoom(String patientId, String roomId) {
        // Fetch fresh data from DB
        Patient p = patientCtrl.findPatientById(patientId);
        Room r = roomCtrl.findRoomById(roomId);

        // Validation: exist, room empty, patient not already in room
        if (p != null && r != null && !r.isOccupied() && p.getRoom() == null) {
            
            // 1. Update In-Memory Models
            r.assignPatient(p); 
            p.assignRoom(r);    

            // 2. Persist to Database
            // We only need to update the patient, because the foreign key (room_id) is in the patient table
            patientCtrl.updatePatientRelationships(p);
            
            return true;
        }
        return false;
    }

    // --- Complex Logic: Assign Patient to Doctor ---
    public boolean assignPatientToDoctor(String patientId, String doctorId) {
        Patient p = patientCtrl.findPatientById(patientId);
        Doctor d = doctorCtrl.findDoctorById(doctorId);

        if (p != null && d != null) {
            // Logic to handle old doctor removal in memory
            if (p.getDoctor() != null) {
                p.getDoctor().removePatient(p);
            }
            d.assignPatient(p); 
            p.assignDoctor(d);  

            // Persist to Database (Foreign key doctor_id is in patient table)
            patientCtrl.updatePatientRelationships(p);

            return true;
        }
        return false;
    }

    // --- Complex Logic: Discharge Patient ---
    public boolean dischargePatient(String patientId) {
        Patient p = patientCtrl.findPatientById(patientId);
        
        if (p != null) {
            // Delegate to PatientController which handles History logic
            return patientCtrl.dischargePatient(patientId);
        }
        return false;
    }
}