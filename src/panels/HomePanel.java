package panels;

import controllers.HospitalManagementController;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class HomePanel extends JPanel {

    private HospitalManagementController hmc;
    private JLabel lblPatients, lblDoctors, lblRooms;
    private JProgressBar progressOccupancy;

    public HomePanel(HospitalManagementController hmc) {
        this.hmc = hmc;
        setLayout(new BorderLayout());
        setBackground(new Color(240, 242, 245)); 
        setBorder(new EmptyBorder(30, 30, 30, 30));

        // --- 1. Header ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel title = new JLabel("Dashboard Overview");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(50, 50, 50));
        
        JLabel subtitle = new JLabel("Welcome back to the Hospital Management System");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(100, 100, 100));

        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(subtitle, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // --- 2. Stats Grid ---
        JPanel statsContainer = new JPanel(new GridLayout(1, 3, 30, 0)); 
        statsContainer.setOpaque(false);
        statsContainer.setBorder(new EmptyBorder(30, 0, 0, 0)); 

        // --- Create Cards ---
        
        // Card 1: Patients (Blue)
        StatsCard patientCard = new StatsCard(new Color(66, 133, 244), "Total Patients", "🏥");
        lblPatients = new JLabel("0");
        styleValueLabel(lblPatients);
        patientCard.addContent(lblPatients);
        
        // Card 2: Doctors (Green)
        StatsCard doctorCard = new StatsCard(new Color(15, 157, 88), "Active Doctors", "👨‍⚕️");
        lblDoctors = new JLabel("0");
        styleValueLabel(lblDoctors);
        doctorCard.addContent(lblDoctors);

        // Card 3: Rooms (Yellow)
        StatsCard roomCard = new StatsCard(new Color(244, 180, 0), "Room Occupancy", "🛌");
        
        // Room Content Container (To hold Text + Progress Bar)
        JPanel roomContent = new JPanel();
        roomContent.setLayout(new BoxLayout(roomContent, BoxLayout.Y_AXIS));
        roomContent.setOpaque(false);
        
        lblRooms = new JLabel("0 / 0");
        styleValueLabel(lblRooms);
        lblRooms.setFont(new Font("Segoe UI", Font.BOLD, 32)); // Slightly smaller than others
        lblRooms.setAlignmentX(Component.CENTER_ALIGNMENT); // Center in BoxLayout
        
        progressOccupancy = new JProgressBar(0, 100);
        progressOccupancy.setPreferredSize(new Dimension(150, 10)); // Fixed width
        progressOccupancy.setMaximumSize(new Dimension(150, 10));  // Prevent stretching
        progressOccupancy.setForeground(new Color(244, 180, 0));
        progressOccupancy.setBackground(new Color(255, 245, 220));
        progressOccupancy.setBorderPainted(false);
        progressOccupancy.setAlignmentX(Component.CENTER_ALIGNMENT); // Center in BoxLayout
        
        // Add vertical spacing
        roomContent.add(Box.createVerticalGlue());
        roomContent.add(lblRooms);
        roomContent.add(Box.createVerticalStrut(10)); // Gap
        roomContent.add(progressOccupancy);
        roomContent.add(Box.createVerticalGlue());
        
        roomCard.addContent(roomContent);

        // Add to grid
        statsContainer.add(patientCard);
        statsContainer.add(doctorCard);
        statsContainer.add(roomCard);

        add(statsContainer, BorderLayout.CENTER);

        refreshData();
    }

    private void styleValueLabel(JLabel label) {
        label.setFont(new Font("Segoe UI", Font.BOLD, 48)); // Bigger font
        label.setForeground(new Color(50, 50, 50));
        label.setHorizontalAlignment(SwingConstants.CENTER);
    }

    public void refreshData() {
        SwingUtilities.invokeLater(() -> {
            int pCount = hmc.getPatientCtrl().getPatientCount();
            int dCount = hmc.getDoctorCtrl().getDoctorCount();
            int totalRooms = hmc.getRoomCtrl().getTotalRooms();
            int occupiedRooms = hmc.getRoomCtrl().getOccupiedRoomCount();

            lblPatients.setText(String.valueOf(pCount));
            lblDoctors.setText(String.valueOf(dCount));
            lblRooms.setText(occupiedRooms + " / " + totalRooms);

            if (totalRooms > 0) {
                int percent = (int) (((double) occupiedRooms / totalRooms) * 100);
                progressOccupancy.setValue(percent);
                progressOccupancy.setToolTipText(percent + "% Occupied");
            } else {
                progressOccupancy.setValue(0);
            }
        });
    }

    // ==========================================
    //       CUSTOM COMPONENT: Modern Card
    // ==========================================
    class StatsCard extends JPanel {
        private Color accentColor;
        private JPanel contentArea;

        public StatsCard(Color accentColor, String title, String icon) {
            this.accentColor = accentColor;
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(new EmptyBorder(20, 20, 20, 20)); 
            
            // Header
            JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            header.setOpaque(false);
            
            JLabel lblIcon = new JLabel(icon + "  ");
            lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
            
            JLabel lblTitle = new JLabel(title);
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblTitle.setForeground(new Color(100, 100, 100));
            
            header.add(lblIcon);
            header.add(lblTitle);
            add(header, BorderLayout.NORTH);
            
            // Content Area (Uses GridBagLayout for Perfect Centering)
            contentArea = new JPanel(new GridBagLayout()); 
            contentArea.setOpaque(false);
            add(contentArea, BorderLayout.CENTER);
        }

        public void addContent(JComponent component) {
            // Add component to center of GridBag
            contentArea.add(component, new GridBagConstraints());
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Shadow
            g2.setColor(new Color(220, 220, 220));
            g2.fillRoundRect(3, 3, getWidth() - 6, getHeight() - 6, 15, 15);

            // Card BG
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 15, 15);

            // Left Accent Bar
            g2.setColor(accentColor);
            g2.fillRoundRect(0, 0, 6, getHeight() - 4, 15, 15); 
            g2.fillRect(4, 0, 4, getHeight() - 4); 
        }
    }
}