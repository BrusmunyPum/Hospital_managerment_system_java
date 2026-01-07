package panels;

import controllers.HospitalManagementController;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import utils.IconUtils;
import utils.ModernUI;

public class HomePanel extends JPanel {

    private HospitalManagementController hmc;
    private JLabel lblPatients, lblDoctors, lblRooms;
    private JProgressBar progressOccupancy;
    
    // Dynamic Components
    private DefaultTableModel recentTableModel;
    private JPanel deptLoadContent;
    private JPanel activeStaffContent;

    public HomePanel(HospitalManagementController hmc) {
        this.hmc = hmc;
        setLayout(new BorderLayout());
        setBackground(new Color(240, 242, 245));
        setBorder(new EmptyBorder(10, 30, 30, 30));

        // --- TOP STATS CARDS ---
        JPanel statsContainer = new JPanel(new GridLayout(1, 3, 30, 0));
        statsContainer.setOpaque(false);

        StatsCard patientCard = new StatsCard(new Color(66, 133, 244), "Total Patients", IconUtils.createIcon(IconUtils.ICON_PATIENT_GROUP, 24, new Color(66, 133, 244)));
        lblPatients = new JLabel("0");
        styleValueLabel(lblPatients);
        patientCard.addContent(lblPatients);

        StatsCard doctorCard = new StatsCard(new Color(15, 157, 88), "Active Doctors", IconUtils.createIcon(IconUtils.ICON_DOCTOR, 24, new Color(15, 157, 88)));
        lblDoctors = new JLabel("0");
        styleValueLabel(lblDoctors);
        doctorCard.addContent(lblDoctors);

        StatsCard roomCard = new StatsCard(new Color(244, 180, 0), "Room Occupancy", IconUtils.createIcon(IconUtils.ICON_BED, 24, new Color(244, 180, 0)));
        JPanel roomContent = new JPanel();
        roomContent.setLayout(new BoxLayout(roomContent, BoxLayout.Y_AXIS));
        roomContent.setOpaque(false);

        lblRooms = new JLabel("0 / 0");
        styleValueLabel(lblRooms);
        lblRooms.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblRooms.setAlignmentX(Component.CENTER_ALIGNMENT);

        progressOccupancy = new JProgressBar(0, 100);
        progressOccupancy.setPreferredSize(new Dimension(100, 10));
        progressOccupancy.setMaximumSize(new Dimension(100, 10));
        progressOccupancy.setForeground(new Color(244, 180, 0));
        progressOccupancy.setBackground(new Color(255, 245, 220));
        progressOccupancy.setBorderPainted(false);
        progressOccupancy.setAlignmentX(Component.CENTER_ALIGNMENT);

        roomContent.add(Box.createVerticalGlue());
        roomContent.add(lblRooms);
        roomContent.add(Box.createVerticalStrut(15));
        roomContent.add(progressOccupancy);
        roomContent.add(Box.createVerticalGlue());
        roomCard.addContent(roomContent);

        statsContainer.add(patientCard);
        statsContainer.add(doctorCard);
        statsContainer.add(roomCard);

        // --- MAIN CONTENT WRAPPER ---
        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setOpaque(false);
        contentWrapper.add(statsContainer, BorderLayout.NORTH);
        contentWrapper.add(createBottomSection(), BorderLayout.CENTER);

        add(contentWrapper, BorderLayout.CENTER);

        refreshData();
    }

    private JPanel createBottomSection() {
        JPanel container = new JPanel(new GridBagLayout());
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(20, 0, 0, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 20);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.65;
        gbc.weighty = 1.0;
        container.add(createRecentActivityPanel(), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.35;
        gbc.insets = new Insets(0, 0, 0, 0);
        container.add(createRightWidgetsPanel(), gbc);

        return container;
    }

    private JPanel createRightWidgetsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 20));
        panel.setOpaque(false);
        panel.add(createDepartmentLoadPanel());
        panel.add(createActiveStaffPanel());
        return panel;
    }

    private JPanel createDepartmentLoadPanel() {
        JPanel card = createBaseCard("Department Load");
        deptLoadContent = new JPanel();
        deptLoadContent.setLayout(new BoxLayout(deptLoadContent, BoxLayout.Y_AXIS));
        deptLoadContent.setOpaque(false);
        deptLoadContent.setBorder(new EmptyBorder(10, 0, 0, 0));

        // Initial Loading State
        JLabel loading = new JLabel("Loading stats...");
        loading.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        deptLoadContent.add(loading);

        card.add(deptLoadContent, BorderLayout.CENTER);
        return card;
    }

    private JPanel createProgressRow(String title, int value, Color color) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setOpaque(false);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel name = new JLabel(title);
        name.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JLabel percent = new JLabel(value + "%");
        percent.setFont(new Font("Segoe UI", Font.BOLD, 12));
        percent.setForeground(color);
        top.add(name, BorderLayout.WEST);
        top.add(percent, BorderLayout.EAST);

        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(value);
        bar.setForeground(color);
        bar.setBackground(new Color(240, 240, 240));
        bar.setBorderPainted(false);
        bar.setPreferredSize(new Dimension(100, 6));

        panel.add(top, BorderLayout.NORTH);
        panel.add(bar, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createActiveStaffPanel() {
        JPanel card = createBaseCard("Available Doctors");
        activeStaffContent = new JPanel();
        activeStaffContent.setLayout(new BoxLayout(activeStaffContent, BoxLayout.Y_AXIS));
        activeStaffContent.setOpaque(false);
        activeStaffContent.setBorder(new EmptyBorder(10, 0, 0, 0));

        // Initial Loading State
        JLabel loading = new JLabel("Loading staff...");
        loading.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        activeStaffContent.add(loading);

        card.add(activeStaffContent, BorderLayout.CENTER);
        return card;
    }

    private JPanel createStaffRow(String name, String role, String status, Color badgeBg, Color badgeFg) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(245, 245, 245)));
        panel.setPreferredSize(new Dimension(0, 45));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        left.setOpaque(false);

        JLabel avatar = new JLabel() {
            public void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(230, 230, 230));
                g2.fillOval(0, 0, 32, 32);
                g2.setColor(Color.GRAY);
                g2.setFont(new Font("SansSerif", Font.BOLD, 14));
                g2.drawString(name.substring(0, 1), 11, 21);
            }
        };
        avatar.setPreferredSize(new Dimension(32, 32));

        JPanel text = new JPanel(new GridLayout(2, 1));
        text.setOpaque(false);
        JLabel nLabel = new JLabel(name);
        nLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JLabel rLabel = new JLabel(role);
        rLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        rLabel.setForeground(Color.GRAY);
        text.add(nLabel);
        text.add(rLabel);

        left.add(avatar);
        left.add(text);

        JLabel badge = new JLabel(status);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badge.setForeground(badgeFg);
        badge.setBackground(badgeBg);
        badge.setOpaque(true);
        badge.setBorder(new EmptyBorder(4, 8, 4, 8));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 10));
        right.setOpaque(false);
        right.add(badge);

        panel.add(left, BorderLayout.WEST);
        panel.add(right, BorderLayout.EAST);
        return panel;
    }

    private JPanel createBaseCard(String titleText) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel title = new JLabel(titleText);
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(new Color(50, 50, 50));
        card.add(title, BorderLayout.NORTH);
        return card;
    }

    private JPanel createRecentActivityPanel() {
        JPanel card = createBaseCard("Recent Admissions");
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                new EmptyBorder(10, 10, 10, 10)
        ));

        String[] columns = {"Patient ID", "Name", "Assigned Doctor", "Date", "Status"};
        recentTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable table = new JTable(recentTableModel);
        ModernUI.setupTableStyle(table);
        table.setRowHeight(40); // Slightly compact for home dashboard
        
        table.getColumnModel().getColumn(4).setCellRenderer(new StatusColumnRenderer());
        // Center others
        table.getColumnModel().getColumn(0).setCellRenderer(ModernUI.createCenterRenderer());
        table.getColumnModel().getColumn(2).setCellRenderer(ModernUI.createCenterRenderer());
        table.getColumnModel().getColumn(3).setCellRenderer(ModernUI.createCenterRenderer());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        card.add(scrollPane, BorderLayout.CENTER);

        return card;
    }

    private void styleValueLabel(JLabel label) {
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setForeground(new Color(50, 50, 50));
        label.setHorizontalAlignment(SwingConstants.CENTER);
    }

    public void refreshData() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            int pCount, dCount, totalRooms, occupiedRooms;
            java.util.List<models.Patient> recentPatients;
            java.util.Map<String, Integer> specStats;
            java.util.List<models.Doctor> activeDoctors;

            @Override
            protected Void doInBackground() throws Exception {
                // 1. Fetch Counts
                pCount = hmc.getPatientCtrl().getPatientCount();
                dCount = hmc.getDoctorCtrl().getDoctorCount();
                totalRooms = hmc.getRoomCtrl().getTotalRooms();
                occupiedRooms = hmc.getRoomCtrl().getOccupiedRoomCount();

                // 2. Fetch Recent Patients (Limit 7)
                recentPatients = hmc.getPatientCtrl().getRecentPatients(7);

                // 3. Fetch Dept Load
                specStats = hmc.getDoctorCtrl().getSpecializationStats();

                // 4. Fetch Active/Available Doctors
                activeDoctors = hmc.getDoctorCtrl().getAvailableDoctors();

                return null;
            }

            @Override
            protected void done() {
                // Update Counts
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

                // Update Recent Patients Table
                recentTableModel.setRowCount(0);
                for (models.Patient p : recentPatients) {
                    String docName = (p.getDoctor() != null) ? p.getDoctor().getName() : "Unassigned";
                    // Infer Status from Room or Relation
                    String status = "Active"; 
                    if (p.getRoom() != null) status = "Admitted";
                    else if (docName.equals("Unassigned")) status = "Pending";
                    
                    recentTableModel.addRow(new Object[]{
                        p.getPatientId(),
                        p.getName(),
                        docName,
                        p.getAdmissionDate() != null ? p.getAdmissionDate().toString() : "N/A",
                        status
                    });
                }

                // Update Dept Load
                deptLoadContent.removeAll();
                int totalDocs = dCount > 0 ? dCount : 1;
                specStats.forEach((spec, count) -> {
                    int percent = (int)(((double)count / totalDocs) * 100);
                    Color barColor = new Color(13, 110, 253); // Default Blue
                    // Random-ish colors based on spec hash or name
                    if(spec.contains("Cardio")) barColor = new Color(220, 53, 69);
                    else if(spec.contains("Neuro")) barColor = new Color(111, 66, 193);
                    else if(spec.contains("Pediatr")) barColor = new Color(255, 193, 7);
                    
                    deptLoadContent.add(createProgressRow(spec, percent, barColor));
                    deptLoadContent.add(Box.createVerticalStrut(15));
                });
                deptLoadContent.revalidate();
                deptLoadContent.repaint();

                // Update Active Staff
                activeStaffContent.removeAll();
                for (models.Doctor d : activeDoctors) {
                     // Fake status for visual variety since we don't have it
                     String status = "On Duty";
                     Color bg = new Color(225, 255, 235);
                     Color fg = new Color(25, 135, 84);
                     
                     activeStaffContent.add(createStaffRow(d.getName(), d.getSpecialization(), status, bg, fg));
                     activeStaffContent.add(Box.createVerticalStrut(10));
                }
                activeStaffContent.revalidate();
                activeStaffContent.repaint();
            }
        };
        worker.execute();
    }

    // --- CARDS ---
    class StatsCard extends JPanel {
        private Color accentColor;
        private JPanel contentArea;

        public StatsCard(Color accentColor, String title, Icon icon) {
            this.accentColor = accentColor;
            setPreferredSize(new Dimension(0, 120));
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(new EmptyBorder(15, 15, 15, 15));

            JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            header.setOpaque(false);
            JLabel lblIcon = new JLabel(icon);
            lblIcon.setBorder(new EmptyBorder(0, 0, 0, 10)); // Add spacing
            // lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20)); // Removed
            JLabel lblTitle = new JLabel(title);
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblTitle.setForeground(new Color(100, 100, 100));
            header.add(lblIcon);
            header.add(lblTitle);
            add(header, BorderLayout.NORTH);

            contentArea = new JPanel(new GridBagLayout());
            contentArea.setOpaque(false);
            add(contentArea, BorderLayout.CENTER);
        }

        public void addContent(JComponent component) {
            contentArea.add(component, new GridBagConstraints());
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(220, 220, 220));
            g2.fillRoundRect(3, 3, getWidth() - 6, getHeight() - 6, 15, 15);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 15, 15);
            g2.setColor(accentColor);
            g2.fillRoundRect(0, 0, 6, getHeight() - 4, 15, 15);
        }
    }

    // --- STATUS COLUMN RENDERER WITH ROW SELECTION SUPPORT ---
    class StatusColumnRenderer extends DefaultTableCellRenderer {
        private Color bgColor = Color.WHITE;
        private Color fgColor = Color.BLACK;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            String status = (value != null) ? value.toString() : "";
            switch (status.toLowerCase()) {
                case "admitted", "active" -> { bgColor = new Color(220, 255, 220); fgColor = new Color(0, 100, 0); }
                case "pending", "in progress", "recovering" -> { bgColor = new Color(255, 248, 200); fgColor = new Color(180, 120, 0); }
                case "discharged" -> { bgColor = new Color(225, 240, 255); fgColor = new Color(0, 50, 150); }
                case "critical" -> { bgColor = new Color(255, 220, 220); fgColor = Color.RED; }
                default -> { bgColor = Color.WHITE; fgColor = Color.BLACK; }
            }

            setForeground(fgColor);
            setHorizontalAlignment(CENTER);

            if (isSelected) {
                bgColor = table.getSelectionBackground();
                setForeground(table.getSelectionForeground());
            }

            setOpaque(false);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(5, 4, getWidth() - 10, getHeight() - 8, 20, 20);
            super.paintComponent(g);
        }
    }
}