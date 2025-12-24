package views;

import controllers.HospitalManagementController;
import models.User;
import panels.*;

import javax.swing.*;
import java.awt.*;
public class HospitalDashboard extends JFrame {

    private HospitalManagementController hmc;
    
    // Panels
    private HomePanel homePanel;
    private PatientPanel patientPanel;
    private DoctorPanel doctorPanel;
    private RoomPanel roomPanel;
    private UserManagementPanel userMgmtPanel; // Admin Only
    
    private User currentUser;

    public HospitalDashboard(User user) {
        this.currentUser = user;
        
        // 1. Initialize Main Controller
        hmc = new HospitalManagementController();

        // 2. Main Frame Setup
        setTitle("Hospital Management System - " + user.getRole() + " Mode");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        setLayout(new BorderLayout());
        
        // 3. User Info Header
        JLabel lblUser = new JLabel("Logged in as: " + user.getUsername() + " (" + user.getRole() + ")");
        lblUser.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(lblUser, BorderLayout.NORTH);

        // 4. Permission Logic
        boolean isAdmin = user.isAdmin();
        boolean isDoctor = user.isDoctor();
        
        String doctorFilterId = null;
        if (isDoctor) {
            // If Doctor, get the ID linked to this user (e.g., "DOC-001")
            doctorFilterId = user.getLinkedId(); 
        }

        // 5. Initialize Panels
        homePanel = new HomePanel(hmc);
        
        // Pass 'doctorFilterId' to PatientPanel. 
        // If null (Admin/Staff), it shows all patients. If set (Doctor), it filters.
        patientPanel = new PatientPanel(hmc, user, doctorFilterId); 
        
        doctorPanel = new DoctorPanel(hmc, isAdmin);
        roomPanel = new RoomPanel(hmc, isAdmin);
        
        if (isAdmin) {
            userMgmtPanel = new UserManagementPanel();
        }

        // 6. Create Tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Home", homePanel);
        tabbedPane.addTab("Patients", patientPanel);

        // Doctors do not see Management Tabs
        if (!isDoctor) {
            tabbedPane.addTab("Doctors", doctorPanel);
            tabbedPane.addTab("Rooms", roomPanel);
        }
        
        // Only Admin sees User Management
        if (isAdmin) {
            tabbedPane.addTab("Manage Users", userMgmtPanel);
        }

        // 7. Auto-Refresh Logic
        tabbedPane.addChangeListener(e -> {
            Component selected = tabbedPane.getSelectedComponent();
            if (selected == homePanel) homePanel.refreshData();
            if (selected == patientPanel) patientPanel.refreshTable(null);
            if (selected == doctorPanel) doctorPanel.refreshTable(null);
            if (selected == roomPanel) roomPanel.refreshTable(null);
        });

        add(tabbedPane, BorderLayout.CENTER);
        
        // 8. Logout Button
        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> {
            dispose(); // Close Dashboard
            new LoginView().setVisible(true); // Open Login Screen
        });
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(btnLogout);
        add(bottomPanel, BorderLayout.SOUTH);
    }
}