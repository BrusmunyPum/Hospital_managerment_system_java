package models;

public class Room {
    private String roomId;
    private String roomType; // e.g., "ICU", "General", "Private"
    private double price;    // Price per night
    private Patient patient; // Null if empty

    public Room(String roomId, String roomType, double price) {
        this.roomId = roomId;
        this.roomType = roomType;
        this.price = price;
        this.patient = null;
    }
    
    // Legacy constructor for backward compatibility (defaults price based on type)
    public Room(String roomId, String roomType) {
        this(roomId, roomType, 0.0);
        // Auto-assign default if 0
        if (roomType.equalsIgnoreCase("Private")) this.price = 200.0;
        else if (roomType.equalsIgnoreCase("ICU")) this.price = 500.0;
        else this.price = 100.0;
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
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public Patient getPatient() { return patient; }

    // --- Utility for UI ---
    @Override
    public String toString() {
        return roomId + " [" + roomType + "]" + (isOccupied() ? " - Occupied" : " - Available");
    }
}