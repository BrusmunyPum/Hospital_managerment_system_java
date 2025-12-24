package views;

import controllers.HospitalManagementController;
import models.User;
import panels.*;

import javax.swing.*;
import java.awt.*;

public class HospitalDashboard extends JFrame {

    private HospitalManagementController hmc;
    private HomePanel homePanel; // NEW
    private PatientPanel patientPanel;
    private DoctorPanel doctorPanel;
    private RoomPanel roomPanel;
    private User currentUser;

    public HospitalDashboard(User user) {
        this.currentUser = user;
        
        hmc = new HospitalManagementController();

        setTitle("Hospital Management System - " + user.getRole() + " Mode");
        setSize(1100, 700); // Made it slightly bigger
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        setLayout(new BorderLayout());
        
        JLabel lblUser = new JLabel("Logged in as: " + user.getUsername() + " (" + user.getRole() + ")");
        lblUser.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(lblUser, BorderLayout.NORTH);

        // --- Create Panels ---
        boolean isAdmin = user.isAdmin();
        
        homePanel = new HomePanel(hmc); // NEW
        patientPanel = new PatientPanel(hmc); 
        doctorPanel = new DoctorPanel(hmc, isAdmin);
        roomPanel = new RoomPanel(hmc, isAdmin);

        // --- Create Tabs ---
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Home", homePanel); // NEW Tab
        tabbedPane.addTab("Patients", patientPanel);
        tabbedPane.addTab("Doctors", doctorPanel);
        tabbedPane.addTab("Rooms", roomPanel);

        tabbedPane.addChangeListener(e -> {
            Component selected = tabbedPane.getSelectedComponent();
            if (selected == homePanel) homePanel.refreshData(); // Refresh stats on click
            if (selected == patientPanel) patientPanel.refreshTable(null);
            if (selected == doctorPanel) doctorPanel.refreshTable(null);
            if (selected == roomPanel) roomPanel.refreshTable(null);
        });

        add(tabbedPane, BorderLayout.CENTER);
        
        // Logout Button
        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> {
            dispose();
            new LoginView().setVisible(true);
        });
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(btnLogout);
        add(bottomPanel, BorderLayout.SOUTH);
    }
}