package views;

import controllers.BookingController;
import controllers.DoctorController;
import controllers.RoomController;
import models.Booking;
import models.Doctor;
import models.Room;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class BookingDialog extends JDialog {

    private BookingController bookingCtrl;
    private DoctorController doctorCtrl;
    private RoomController roomCtrl;
    
    // UI Components
    private JTextField txtName, txtAge, txtContact;
    private JTextArea txtSymptoms;
    private JComboBox<String> cmbGender;
    private JComboBox<DoctorItem> cmbDoctor;
    private JComboBox<RoomItem> cmbRoom;
    
    // Modern color scheme
    private static final Color PRIMARY = new Color(99, 102, 241);      // Indigo
    private static final Color PRIMARY_DARK = new Color(79, 70, 229);
    private static final Color BACKGROUND = new Color(249, 250, 251);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(17, 24, 39);
    private static final Color TEXT_SECONDARY = new Color(107, 114, 128);
    private static final Color BORDER_COLOR = new Color(229, 231, 235);
    
    public BookingDialog(JFrame parent) {
        super(parent, "Book Appointment", true);
        bookingCtrl = new BookingController();
        doctorCtrl = new DoctorController();
        roomCtrl = new RoomController();

        setSize(550, 750);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND);

        // Main container with card design
        JPanel cardPanel = new JPanel(new BorderLayout());
        cardPanel.setBackground(CARD_BG);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(20, 20, 20, 20), // Reduced outer indent
            new LineBorder(BORDER_COLOR, 1, true)
        ));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(CARD_BG);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20)); // Reduced inner indent

        // Modern Header with icon
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(CARD_BG);
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT); // IMPORTANT: Align panel Left to match form
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        
        // Use a simple emoji or graphical character for icon if no image
        JLabel iconLabel = new JLabel("📋"); 
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Keep content centered
        
        JLabel lblTitle = new JLabel("New Appointment");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblSubtitle = new JLabel("Fill in the details to book your appointment");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitle.setForeground(TEXT_SECONDARY);
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        headerPanel.add(iconLabel);
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(lblTitle);
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(lblSubtitle);
        
        // Add Header
        mainPanel.add(headerPanel);
        mainPanel.add(Box.createVerticalStrut(30));

        // Form Fields (Left Aligned as requested)
        // Groups use LEFT_ALIGNMENT internally
        
        mainPanel.add(createFormGroup("Full Name *", txtName = createModernField()));
        mainPanel.add(createFormGroup("Age *", txtAge = createModernField()));
        
        cmbGender = createModernCombo(new String[]{"Male", "Female", "Other"});
        mainPanel.add(createFormGroup("Gender *", cmbGender));
        
        mainPanel.add(createFormGroup("Contact Number *", txtContact = createModernField()));
        
        txtSymptoms = createModernTextArea(3, 20);
        JScrollPane scrollSymptoms = new JScrollPane(txtSymptoms);
        scrollSymptoms.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        // Fix scroll pane alignment inside the form group to extend full width or align left properly
        scrollSymptoms.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(createFormGroup("Symptoms / Reason for Visit", scrollSymptoms));
        
        cmbDoctor = new JComboBox<>();
        loadDoctors();
        styleModernCombo(cmbDoctor);
        mainPanel.add(createFormGroup("Preferred Doctor (Optional)", cmbDoctor));
        
        cmbRoom = new JComboBox<>();
        loadRooms();
        styleModernCombo(cmbRoom);
        mainPanel.add(createFormGroup("Preferred Room (Optional)", cmbRoom));

        mainPanel.add(Box.createVerticalStrut(30));

        // Modern Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(CARD_BG);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT); // Main panel is aligned Left, so this should match flow

        
        JButton btnCancel = createModernButton("Cancel", false);
        btnCancel.addActionListener(e -> dispose());
        
        JButton btnSubmit = createModernButton("Booking", true);
        btnSubmit.addActionListener(e -> submitBooking());
        
        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSubmit);
        mainPanel.add(buttonPanel);

        // Scroll Pane for the whole form content
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER); // Prevent horizontal scroll
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        cardPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(cardPanel, BorderLayout.CENTER);
    }

    private JPanel createFormGroup(String labelText, JComponent field) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_BG);
        // Important: LEFT Alignment for the group itself
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));
        panel.setBorder(new EmptyBorder(0, 0, 15, 0));
        
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_PRIMARY);
        // Important: LEFT Alignment for Label
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(new EmptyBorder(0, 0, 8, 0));
        
        // Important: LEFT Alignment for Field
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panel.add(label);
        panel.add(field);
        
        return panel;
    }

    private JTextField createModernField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(0, 42));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));
        
        // Add focus effects
        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(PRIMARY, 2, true),
                    new EmptyBorder(8, 12, 8, 12)
                ));
            }
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(BORDER_COLOR, 1, true),
                    new EmptyBorder(8, 12, 8, 12)
                ));
            }
        });
        
        return field;
    }
    
    private JTextArea createModernTextArea(int rows, int cols) {
        JTextArea area = new JTextArea(rows, cols);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(new EmptyBorder(10, 12, 10, 12));
        return area;
    }
    
    private JComboBox<String> createModernCombo(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        styleModernCombo(combo);
        return combo;
    }
    
    private void styleModernCombo(JComboBox<?> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setBackground(Color.WHITE);
        combo.setPreferredSize(new Dimension(0, 42));
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        // Basic border fix
        combo.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(0, 5, 0, 5) 
        ));
    }
    
    private JButton createModernButton(String text, boolean isPrimary) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(140, 44));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        if (isPrimary) {
            btn.setBackground(PRIMARY);
            btn.setForeground(Color.WHITE);
            // Mouse Listener for Hover
            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { btn.setBackground(PRIMARY_DARK); }
                public void mouseExited(MouseEvent e) { btn.setBackground(PRIMARY); }
            });
        } else {
            btn.setBackground(BACKGROUND);
            btn.setForeground(TEXT_PRIMARY);
            btn.setBorder(new LineBorder(BORDER_COLOR, 1, true));
            
            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(243, 244, 246)); }
                public void mouseExited(MouseEvent e) { btn.setBackground(BACKGROUND); }
            });
        }
        
        return btn;
    }

    private void loadDoctors() {
        cmbDoctor.addItem(new DoctorItem("Any / No Preference", ""));
        List<Doctor> doctors = doctorCtrl.getAllDoctors();
        for (Doctor d : doctors) {
            cmbDoctor.addItem(new DoctorItem(d.getName() + " (" + d.getSpecialization() + ")", d.getDoctorId()));
        }
    }
    
    private void loadRooms() {
        cmbRoom.addItem(new RoomItem("Any / No Preference", ""));
        List<Room> rooms = roomCtrl.getAllRooms();
        for (Room r : rooms) {
            if (!r.isOccupied()) {
               cmbRoom.addItem(new RoomItem(r.getRoomId() + " (" + r.getRoomType() + ")", r.getRoomId()));
            }
        }
    }

    private void submitBooking() {
        String name = txtName.getText().trim();
        String ageStr = txtAge.getText().trim();
        String contact = txtContact.getText().trim();
        String symptoms = txtSymptoms.getText().trim();
        
        if (name.isEmpty() || ageStr.isEmpty() || contact.isEmpty()) {
            showModernMessage("Please fill in all required fields (Name, Age, Contact).", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
            if (age <= 0 || age > 150) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            showModernMessage("Please enter a valid age (1-150).", "Invalid Age", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String gender = (String) cmbGender.getSelectedItem();
        DoctorItem selectedDoc = (DoctorItem) cmbDoctor.getSelectedItem();
        RoomItem selectedRoom = (RoomItem) cmbRoom.getSelectedItem();
        
        String docId = (selectedDoc != null && !selectedDoc.id.isEmpty()) ? selectedDoc.id : null;
        String roomId = (selectedRoom != null && !selectedRoom.id.isEmpty()) ? selectedRoom.id : null;

        Booking booking = new Booking(name, age, gender, contact, symptoms, docId, roomId);

        if (bookingCtrl.createBooking(booking)) {
            showModernMessage("Booking submitted successfully!\nOur staff will review it shortly.", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            showModernMessage("Failed to submit booking. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showModernMessage(String message, String title, int messageType) {
        UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 13));
        UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 12));
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    // Inner classes for ComboBox items
    class DoctorItem {
        String label;
        String id;
        public DoctorItem(String label, String id) { 
            this.label = label; 
            this.id = id; 
        }
        public String toString() { return label; }
    }
    
    class RoomItem {
        String label;
        String id;
        public RoomItem(String label, String id) { 
            this.label = label; 
            this.id = id; 
        }
        public String toString() { return label; }
    }
}
