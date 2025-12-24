package models;

public class User {
    private String username;
    private String password; // In a real app, never store plain text!
    private String role;     // ADMIN, DOCTOR, STAFF
    private String linkedId; // Stores DoctorID or EmployeeID

    public User(String username, String password, String role, String linkedId) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.linkedId = linkedId;
    }

    // Getters
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public String getLinkedId() { return linkedId; }

    // Helpers
    public boolean isAdmin() { return "ADMIN".equalsIgnoreCase(role); }
    public boolean isDoctor() { return "DOCTOR".equalsIgnoreCase(role); }
    public boolean isStaff() { return "STAFF".equalsIgnoreCase(role); }
}
