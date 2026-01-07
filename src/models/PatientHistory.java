package models;

public class PatientHistory {
    private String patientId;
    private String name;
    private int age;
    private String address;
    private String medicalHistory;
    private String doctorName; // Snapshot of doctor at discharge
    private String admissionDate;
    private String dischargeDate;

    public PatientHistory(String patientId, String name, int age, String address, 
                          String medicalHistory, String doctorName, 
                          String admissionDate, String dischargeDate) {
        this.patientId = patientId;
        this.name = name;
        this.age = age;
        this.address = address;
        this.medicalHistory = medicalHistory;
        this.doctorName = doctorName;
        this.admissionDate = admissionDate;
        this.dischargeDate = dischargeDate;
    }

    public String getPatientId() { return patientId; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getAddress() { return address; }
    public String getMedicalHistory() { return medicalHistory; }
    public String getDoctorName() { return doctorName; }
    public String getAdmissionDate() { return admissionDate; }
    public String getDischargeDate() { return dischargeDate; }
}
