package models;

public class Room {
    private String roomId;
    private String roomType; // e.g., "ICU", "General", "Private"
    private Patient patient; // Null if empty

    public Room(String roomId, String roomType) {
        this.roomId = roomId;
        this.roomType = roomType;
        this.patient = null;
    }

    // --- Relationship Management ---
    public void assignPatient(Patient p) {
        this.patient = p;
    }

    public void removePatient() {
        this.patient = null;
    }

    public boolean isOccupied() {
        return patient != null;
    }

    // --- Getters ---
    public String getRoomId() { return roomId; }
    public String getRoomType() { return roomType; }
    public Patient getPatient() { return patient; }

    // --- Utility for UI ---
    @Override
    public String toString() {
        return roomId + " [" + roomType + "]" + (isOccupied() ? " - Occupied" : " - Available");
    }
}