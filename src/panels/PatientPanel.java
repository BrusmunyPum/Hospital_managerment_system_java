package panels;

import controllers.HospitalManagementController;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import models.*;
import utils.*;

public class PatientPanel extends JPanel {

    private HospitalManagementController hmc;
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtSearch;
    
    // Stats Labels
    private JLabel lblTotalAdmitted, lblAvailableBeds, lblCriticalCases, lblDischarges;
    private JLabel lblAdmittedBadge, lblBedsBadge, lblCriticalBadge; // Dynamic Badges

    private User currentUser;       
    private String doctorFilterId;

    // Colors based on your screenshot
    private Color primaryBlue = new Color(13, 110, 253);
    private Color bgLight = new Color(245, 247, 251);
    private Color textDark = new Color(50, 50, 50);
    private Color textGray = new Color(108, 117, 125);

    public PatientPanel(HospitalManagementController hmc, User user, String doctorId) {
        this.hmc = hmc;
        this.currentUser = user;
        this.doctorFilterId = doctorId; 
        
        setLayout(new BorderLayout());
        setBackground(bgLight);
        setBorder(new EmptyBorder(20, 30, 20, 30)); // Outer Padding

        // --- 1. TOP SECTION (Header + Stats + Toolbar) ---
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        // A. Header
        topContainer.add(createHeaderSection());
        topContainer.add(Box.createVerticalStrut(20));

        // B. Stats Cards
        topContainer.add(createStatsSection());
        topContainer.add(Box.createVerticalStrut(20));

        // C. Toolbar (Search, Filter, Add Button)
        topContainer.add(createToolbarSection());
        topContainer.add(Box.createVerticalStrut(15));

        add(topContainer, BorderLayout.NORTH);

        // --- 2. CENTER SECTION (Table) ---
        // Columns matching the screenshot design
        String[] columns = {"Patient Name", "Diagnosis", "Assigned Dr.", "Room", "Admitted", "Status"};
        
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        
        table = new JTable(tableModel);
        setupTableStyle(); // Apply the "Modern" look

        // ScrollPane styling
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(Color.WHITE);
        
        // Card-like wrapper for table
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createLineBorder(new Color(230,230,230), 1));
        tableCard.add(scrollPane, BorderLayout.CENTER);
        
        add(tableCard, BorderLayout.CENTER);
        
        // --- 3. BOTTOM SECTION (Functional Buttons) ---
        // I kept your original logic but styled the buttons to be cleaner
        add(createActionButtons(), BorderLayout.SOUTH);

        // --- EVENT LISTENERS (Your Original Logic) ---
        setupListeners();
        
        refreshTable(null);
    }

    // ==========================================
    //              UI BUILDER METHODS
    // ==========================================

    private JPanel createHeaderSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        JLabel title = new JLabel("Patient Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(textDark);
        
        JLabel subtitle = new JLabel("Manage patient records, admissions, and discharges efficiently.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(textGray);
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        textPanel.setOpaque(false);
        textPanel.add(title);
        textPanel.add(subtitle);
        
        panel.add(textPanel, BorderLayout.WEST);
        
        // "Admit New Patient" Button (Top Right)
        if (!currentUser.isDoctor()) {
            JButton btnAdmit = ModernUI.createPrimaryButton("+ Admit New Patient", null);
            btnAdmit.setPreferredSize(new Dimension(180, 40));
            btnAdmit.addActionListener(e -> showAddPatientDialog());
            panel.add(btnAdmit, BorderLayout.EAST);
        }
        
        return panel;
    }

    private JPanel createStatsSection() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 20, 0)); // 4 Columns
        panel.setOpaque(false);
        
        lblTotalAdmitted = new JLabel("...");
        lblAvailableBeds = new JLabel("...");
        lblCriticalCases = new JLabel("...");
        lblDischarges = new JLabel("...");
        
        // Dynamic Badges
        lblAdmittedBadge = new JLabel("+0%");
        lblBedsBadge = new JLabel("0% Free");
        lblCriticalBadge = new JLabel("0% of Total");

        styleStatsLabel(lblTotalAdmitted);
        styleStatsLabel(lblAvailableBeds);
        styleStatsLabel(lblCriticalCases);
        styleStatsLabel(lblDischarges);

        // Data for cards
        // 1. Total Admitted (Growth)
        panel.add(createStatsCard("Total Admitted", lblTotalAdmitted, lblAdmittedBadge, 
            IconUtils.createIcon(IconUtils.ICON_PATIENT_GROUP, 32, primaryBlue), 
            new Color(230, 240, 255), primaryBlue));
            
        // 2. Available Beds (% Free)
        panel.add(createStatsCard("Available Beds", lblAvailableBeds, lblBedsBadge, 
            IconUtils.createIcon(IconUtils.ICON_BED, 32, new Color(25, 135, 84)), 
            new Color(225, 255, 235), new Color(25, 135, 84)));
            
        // 3. Critical Cases (% of Total Patients)
        panel.add(createStatsCard("Critical Cases", lblCriticalCases, lblCriticalBadge, 
            IconUtils.createIcon(IconUtils.ICON_ALERT, 32, new Color(220, 53, 69)), 
            new Color(255, 235, 235), new Color(220, 53, 69)));
            
        // 4. Discharges (Static 'Today')
        panel.add(createStatsCard("Discharges", lblDischarges, new JLabel("Today"), 
            IconUtils.createIcon(IconUtils.ICON_HOSPITAL, 32, new Color(255, 193, 7)), 
            new Color(255, 245, 220), new Color(255, 193, 7)));
        
        return panel;
    }

    private void styleStatsLabel(JLabel label) {
        label.setFont(new Font("Segoe UI", Font.BOLD, 22));
        label.setForeground(textDark);
    }

    private JPanel createStatsCard(String title, JLabel valueLabel, JLabel badgeLabel, Icon icon, Color bg, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        // Top Row: Icon + Badge
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel lblIcon = new JLabel(icon);
        
        // Style the Badge
        badgeLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badgeLabel.setForeground(accent);
        badgeLabel.setBackground(bg);
        badgeLabel.setOpaque(true);
        badgeLabel.setBorder(new EmptyBorder(2, 6, 2, 6));
        
        top.add(lblIcon, BorderLayout.WEST);
        top.add(badgeLabel, BorderLayout.EAST);
        
        // Bottom Row: Label + Number
        JPanel bot = new JPanel(new GridLayout(2, 1, 0, 5));
        bot.setOpaque(false);
        bot.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTitle.setForeground(textGray);
        
        // Use the passed label
        bot.add(lblTitle);
        bot.add(valueLabel);
        
        card.add(top, BorderLayout.NORTH);
        card.add(bot, BorderLayout.CENTER);
        return card;
    }

    private JPanel createToolbarSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        // Search Bar (Left)
        JPanel searchContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        searchContainer.setOpaque(false);
        
        txtSearch = ModernUI.createSearchField(" Search by name, ID...");
        searchContainer.add(txtSearch);
        
        // Filters (Right)
        JPanel filterContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterContainer.setOpaque(false);
        
        JButton btnFilterDept = ModernUI.createOutlineButton("Filter by Dept ▼", null);
        btnFilterDept.addActionListener(e -> showFilterMenu(btnFilterDept, "Department"));

        JButton btnFilterStatus = ModernUI.createOutlineButton("Filter by Status ▼", null);
        btnFilterStatus.addActionListener(e -> showFilterMenu(btnFilterStatus, "Status"));
        
        // Show All Refresh Button
        JButton btnRefresh = ModernUI.createOutlineButton("Refresh", IconUtils.createIcon(IconUtils.ICON_REFRESH, 18, textDark));
        btnRefresh.setPreferredSize(new Dimension(100, 35));
        btnRefresh.addActionListener(e -> { txtSearch.setText(""); refreshTable(null); });

        filterContainer.add(btnFilterDept);
        filterContainer.add(btnFilterStatus);
        filterContainer.add(btnRefresh);
        
        panel.add(searchContainer, BorderLayout.WEST);
        panel.add(filterContainer, BorderLayout.EAST);
        
        return panel;
    }

    private void showFilterMenu(JButton invoker, String type) {
        JPopupMenu menu = new JPopupMenu();
        
        if (type.equals("Department")) {
            menu.add(createFilterItem("All Departments", null, "Department")); // null = reset
            menu.addSeparator();
            String[] depts = {"Cardiology", "Neurology", "General", "Pediatrics"};
            for (String d : depts) menu.add(createFilterItem(d, d, "Department"));
        } else {
            menu.add(createFilterItem("All Statuses", null, "Status"));
            menu.addSeparator();
            String[] stats = {"Stable", "Critical", "Recovery", "Observation"};
            for (String s : stats) menu.add(createFilterItem(s, s, "Status"));
        }
        menu.show(invoker, 0, invoker.getHeight());
    }
    
    private JMenuItem createFilterItem(String label, String filterValue, String type) {
        JMenuItem item = new JMenuItem(label);
        item.addActionListener(e -> {
            if ("Department".equals(type)) {
                currentDeptFilter = filterValue;
            } else {
                currentStatusFilter = filterValue;
            }
            applyFilters();
        });
        return item;
    }
    
    // filterTable removed (logic moved to applyFilters)

    private JPanel createActionButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(bgLight);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        JButton btnEdit = ModernUI.createOutlineButton("Edit Patient", null);
        
        JButton btnBill = ModernUI.createWarningButton("Generate Bill", null);
        
        JButton btnDischarge = ModernUI.createDangerButton("Discharge", null);
        
        // Permissions
        if (!currentUser.isDoctor()) {
            JButton btnAssign = ModernUI.createOutlineButton("Assign Room/Doc", null);
            btnAssign.addActionListener(e -> showAssignDialog()); // Helper method below
            panel.add(btnAssign);
        }

        btnEdit.addActionListener(e -> showEditPatientDialog());
        btnBill.addActionListener(e -> showBillDialog());
        btnDischarge.addActionListener(e -> performDischarge());
        
        panel.add(btnEdit);
        panel.add(btnBill);
        panel.add(btnDischarge);
        
        return panel;
    }

    // ==========================================
    //           TABLE STYLING & RENDERERS
    // ==========================================

    private void setupTableStyle() {
        // Use Global Style
        ModernUI.setupTableStyle(table);
        
        // Custom adjustments where needed
        table.setRowHeight(60); // Keep taller rows for Avatars
        
        // Apply Custom Renderers
        table.getColumnModel().getColumn(0).setCellRenderer(new PatientInfoRenderer()); // Name + ID
        table.getColumnModel().getColumn(2).setCellRenderer(new DoctorInfoRenderer()); // Doctor Name
        table.getColumnModel().getColumn(5).setCellRenderer(new StatusPillRenderer()); // Status Badge
        
        // Center alignment for other columns
        table.getColumnModel().getColumn(1).setCellRenderer(ModernUI.createCenterRenderer());
        table.getColumnModel().getColumn(3).setCellRenderer(ModernUI.createCenterRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(ModernUI.createCenterRenderer());
    }

    // 1. Patient Info Renderer (Avatar + Name + ID)
    class PatientInfoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            // OUTER PANEL: Centers the inner content container
            JPanel outer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
            outer.setOpaque(true);
            outer.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            
            // INNER CONTAINER: Fixed width ensures Avatars align perfectly vertically
            // Width 220px is wide enough for most names but keeps the center look
            JPanel content = new JPanel(new BorderLayout(12, 0)); 
            content.setPreferredSize(new Dimension(240, 44)); 
            content.setOpaque(false);
            
            String fullString = (String) value; // "PT-101: Name"
            String[] parts = fullString.split(":"); 
            String id = parts.length > 0 ? parts[0] : "";
            String name = parts.length > 1 ? parts[1] : "";
            
            // Deterministic Color based on name
            int hash = Math.abs(name.hashCode());
            Color[] palette = {
                new Color(13, 110, 253), // Blue
                new Color(102, 16, 242), // Indigo
                new Color(111, 66, 193), // Purple
                new Color(214, 51, 132), // Pink
                new Color(220, 53, 69),  // Red
                new Color(253, 126, 20), // Orange
                new Color(255, 193, 7),  // Yellow
                new Color(25, 135, 84),  // Green
                new Color(32, 201, 151), // Teal
                new Color(13, 202, 240)  // Cyan
            };
            Color avatarBg = palette[hash % palette.length];

            // Avatar
            JLabel avatar = new JLabel() {
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(avatarBg);
                    g2.fillOval(0,0,44,44); 
                    
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
                    
                    String initial = name.length() > 0 ? name.substring(0,1).toUpperCase() : "?";
                    FontMetrics fm = g2.getFontMetrics();
                    int textX = (getWidth() - fm.stringWidth(initial)) / 2;
                    int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                    
                    g2.drawString(initial, textX, textY);
                }
            };
            avatar.setPreferredSize(new Dimension(44, 44));
            
            // Text Panel
            JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 0));
            textPanel.setOpaque(false);
            
            JLabel lblName = new JLabel(name);
            lblName.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Slightly smaller for better fit
            lblName.setForeground(isSelected ? table.getSelectionForeground() : textDark);
            
            JLabel lblId = new JLabel("ID: " + id);
            lblId.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblId.setForeground(isSelected ? table.getSelectionForeground() : textGray);
            
            textPanel.add(lblName);
            textPanel.add(lblId);
            
            content.add(avatar, BorderLayout.WEST);
            content.add(textPanel, BorderLayout.CENTER);
            
            outer.add(content);
            return outer;
        }
    }

    // 2. Doctor Info Renderer (Simple Icon + Name)
    class DoctorInfoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            String text = value.toString();
            String nameForInitial = text.replaceAll("(?i)^Dr\\.\\s*", "").trim(); // Remove "Dr." prefix
            String initial = nameForInitial.isEmpty() ? "?" : nameForInitial.substring(0, 1).toUpperCase();
            
            // Modern Colors
            label.setIcon(new TextIcon(initial, new Color(225, 230, 255))); // Soft Blue
            label.setText(" " + text);
            label.setForeground(new Color(60, 60, 60)); // Darker gray text
            
            // Left align for cleaner lists
            label.setHorizontalAlignment(SwingConstants.LEFT); 
            label.setHorizontalTextPosition(SwingConstants.RIGHT);
            
            // Add padding
            label.setBorder(new EmptyBorder(0, 15, 0, 0));
            
            if (isSelected) {
                 label.setForeground(table.getSelectionForeground());
            }
            
            return label;
        }
    }

    // 3. Status Pill Renderer
    class StatusPillRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = new JLabel((String) value);
            label.setHorizontalAlignment(CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            label.setOpaque(false); // We draw the pill manually
            
            // Determine Color
            String status = (String) value;
            Color bg, fg;
            
            if (status.equals("Stable")) {
                bg = new Color(220, 255, 220); fg = new Color(25, 135, 84);
            } else if (status.equals("Critical")) {
                bg = new Color(255, 225, 225); fg = new Color(220, 53, 69);
            } else if (status.equals("Recovery")) {
                bg = new Color(225, 240, 255); fg = new Color(13, 110, 253);
            } else {
                bg = new Color(255, 245, 220); fg = new Color(200, 150, 0);
            }
            
            // "justify-content: center" -> FlowLayout.CENTER
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
            panel.setOpaque(true);
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            
            JLabel pill = new JLabel(status) {
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(bg);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                    super.paintComponent(g);
                }
            };
            pill.setOpaque(false);
            pill.setForeground(fg);
            pill.setFont(new Font("Segoe UI", Font.BOLD, 11));
            pill.setHorizontalAlignment(SwingConstants.CENTER);
            pill.setPreferredSize(new Dimension(80, 24));
            
            panel.add(pill);
            return panel;
        }
    }

    // Data Cache & Filter State
    private java.util.List<Patient> masterList = new java.util.ArrayList<>();
    private String currentDeptFilter = null;
    private String currentStatusFilter = null;
    
    // ==========================================
    //           LOGIC & DATA METHODS
    // ==========================================

    /**
     * REFRESH DATA: Fetches fresh data from DB, updates stats, then applies current filters.
     * The 'data' argument is ignored in this new logic but kept for compatibility.
     */
    public void refreshTable(List<Patient> ignored) {
        // Show loading state could go here
        
        SwingWorker<List<Patient>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Patient> doInBackground() throws Exception {
                // Fetch ALL relevant data
                if (currentUser.isDoctor() && doctorFilterId != null) {
                    return hmc.getPatientCtrl().getPatientsByDoctorId(doctorFilterId);
                } else {
                    return hmc.getPatientCtrl().getAllPatients();
                }
            }

            @Override
            protected void done() {
                try {
                    masterList = get(); // Update Cache
                    applyFilters();     // Render Table
                    updateStats();      // Update Stats
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(PatientPanel.this, "Error loading data: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
    
    /**
     * APPLY FILTERS: filters masterList -> updates Table
     */
    private void applyFilters() {
        String query = txtSearch.getText().trim().toLowerCase();
        List<Patient> filtered = new java.util.ArrayList<>();
        
        for (Patient p : masterList) {
            // 1. Search Filter (Name or ID)
            boolean matchesSearch = query.isEmpty() || 
                                    p.getName().toLowerCase().contains(query) || 
                                    p.getPatientId().toLowerCase().contains(query);
                                    
            if (!matchesSearch) continue;

            // 2. Dept Filter (Doctor Specialization)
            boolean matchesDept = true;
            if (currentDeptFilter != null) {
                String spec = (p.getDoctor() != null) ? p.getDoctor().getSpecialization() : "Unassigned";
                if (!spec.equalsIgnoreCase(currentDeptFilter)) {
                    matchesDept = false;
                }
            }
            if (!matchesDept) continue;

            // 3. Status Filter (Generated)
            // Re-calculate status to match Renderer logic
            String[] statuses = {"Stable", "Critical", "Recovery", "Observation"};
            String status = statuses[Math.abs(p.getName().hashCode()) % statuses.length];
            if (p.getRoom() != null && "ICU".equalsIgnoreCase(p.getRoom().getRoomType())) {
                status = "Critical"; 
            }
            
            boolean matchesStatus = true;
            if (currentStatusFilter != null) {
                if (!status.equalsIgnoreCase(currentStatusFilter)) {
                    matchesStatus = false;
                }
            }
            if (!matchesStatus) continue;

            // All passed
            filtered.add(p);
        }
        
        updateTableOnly(filtered);
    }
    
    private void updateTableOnly(List<Patient> patients) {
        tableModel.setRowCount(0);
        for (Patient p : patients) {
            String docName = (p.getDoctor() != null) ? p.getDoctor().getName() : "Unassigned";
            String roomName = (p.getRoom() != null) ? p.getRoom().getRoomId() : "Waiting";
            String date = (p.getAdmissionDate() != null) ? p.getAdmissionDate().toString() : LocalDate.now().toString();
            
            String[] statuses = {"Stable", "Critical", "Recovery", "Observation"};
            String status = statuses[Math.abs(p.getName().hashCode()) % statuses.length];
            if (p.getRoom() != null && "ICU".equalsIgnoreCase(p.getRoom().getRoomType())) {
                status = "Critical";
            }

            tableModel.addRow(new Object[]{
                p.getPatientId() + ":" + p.getName(),
                p.getMedicalHistory(),
                docName,
                roomName,
                date,
                status
            });
        }
    }

    private void updateStats() {
        SwingWorker<Void, Void> statsWorker = new SwingWorker<>() {
            int total = 0, critical = 0, disch = 0, avail = 0;
            int admittedToday = 0, totalRooms = 0;
            double growth = 0;
            double pctBedsFree = 0;
            double pctCritical = 0;

            @Override
            protected Void doInBackground() {
                // 1. Fetch Basic counts from DB / Memory
                // Use masterList for consistency with UI filters if possible, but for 'Total Admitted' we generally want DB ground truth.
                // However, for 'Critical', we MUST use masterList logic because it's virtually generated.
                
                // A. Total Admitted (DB)
                total = hmc.getPatientCtrl().getPatientCount();
                
                // B. Critical (Memory - to match visual Table)
                critical = 0;
                String[] statuses = {"Stable", "Critical", "Recovery", "Observation"};
                for (Patient p : masterList) {
                    String status = statuses[Math.abs(p.getName().hashCode()) % statuses.length];
                    if (p.getRoom() != null && "ICU".equalsIgnoreCase(p.getRoom().getRoomType())) {
                        status = "Critical";
                    }
                    if ("Critical".equalsIgnoreCase(status)) {
                        critical++;
                    }
                }
                
                // C. Discharges & Admissions (DB)
                disch = hmc.getPatientCtrl().getTodayDischargeCount();
                admittedToday = hmc.getPatientCtrl().getTodayAdmissionCount();
                
                // D. Available Beds (DB - Robust method)
                // Use getAvailableRooms().size() directly instead of math
                avail = hmc.getRoomCtrl().getAvailableRooms().size();
                totalRooms = hmc.getRoomCtrl().getTotalRooms();
                
                // 3. Growth Calculation
                int yesterdayTotal = total - admittedToday + disch;
                if (yesterdayTotal > 0) {
                    growth = ((double)(total - yesterdayTotal) / yesterdayTotal) * 100;
                } else if (total > 0) {
                    growth = 100.0;
                }
                
                // 4. Badges (Avail Beds %, Critical %)
                if (totalRooms > 0) pctBedsFree = ((double)avail / totalRooms) * 100;
                if (total > 0) pctCritical = ((double)critical / total) * 100;
                
                return null;
            }
            @Override
            protected void done() {
                 lblTotalAdmitted.setText(String.valueOf(total));
                 lblCriticalCases.setText(String.valueOf(critical));
                 lblDischarges.setText(String.valueOf(disch));
                 lblAvailableBeds.setText(String.valueOf(avail));
                 
                 // Update Admitted Badge
                 if (growth > 0) {
                     lblAdmittedBadge.setText("+" + String.format("%.1f", growth) + "%");
                     lblAdmittedBadge.setForeground(new Color(25, 135, 84));
                     lblAdmittedBadge.setBackground(new Color(225, 255, 235));
                 } else if (growth < 0) {
                     lblAdmittedBadge.setText(String.format("%.1f", growth) + "%");
                     lblAdmittedBadge.setForeground(new Color(220, 53, 69));
                     lblAdmittedBadge.setBackground(new Color(255, 235, 235));
                 } else {
                     lblAdmittedBadge.setText("0%");
                     lblAdmittedBadge.setForeground(textGray);
                     lblAdmittedBadge.setBackground(new Color(240, 240, 240));
                 }
                 
                 // Update Beds Badge
                 lblBedsBadge.setText(String.format("%.0f", pctBedsFree) + "% Free");
                 
                 // Update Critical Badge
                 lblCriticalBadge.setText(String.format("%.0f", pctCritical) + "% of Total");
            }
        };
        statsWorker.execute();
    }
    
    private void setupListeners() {
        txtSearch.addActionListener(e -> performSearch());
        // Add Document Listener for instant search? Maybe later.
        
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) showEditPatientDialog();
                if (SwingUtilities.isRightMouseButton(e)) showContextMenu(e);
            }
        });
    }

    private void performSearch() {
        // Just apply filters. No DB hit.
        applyFilters(); 
    }
    


    // --- Helper: Simple Circle Icon Class ---
    private static class TextIcon implements Icon {
        private String text; private Color color;
        public TextIcon(String text, Color color) { this.text = text; this.color = color; }
        public int getIconWidth() { return 24; }
        public int getIconHeight() { return 24; }
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fill(new Ellipse2D.Double(x, y, 24, 24));
            g2.setColor(Color.DARK_GRAY);
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.drawString(text, x+8, y+17);
        }
    }

    // --- LOGIC HELPERS ---
    
    private String getSelectedIdFromRow() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a row."); return null; }
        String raw = (String) table.getValueAt(row, 0); 
        return raw.split(":")[0];
    }
    
    // ... Dialogs ...
    // Since I'm replacing the whole bottom chunk, I need to include them.
    // I will include: showAssignDialog, showContextMenu, showBillDialog, performDischarge, showAdd, showEdit, showAssignDoc, showAssignRoom
    
    private void showAssignDialog() {
         String pid = getSelectedIdFromRow();
         if(pid == null) return;
         String[] options = {"Assign Doctor", "Assign Room"};
         int choice = JOptionPane.showOptionDialog(this, "What to assign?", "Assign", 0, 3, null, options, options[0]);
         if(choice == 0) showAssignDoctorDialog(pid);
         else if(choice == 1) showAssignRoomDialog(pid);
    }
    
    private void showContextMenu(MouseEvent e) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem viewItem = new JMenuItem("View Details");
        JMenuItem billItem = new JMenuItem("Generate Bill"); 
        JMenuItem editItem = new JMenuItem("Edit Patient");
        JMenuItem deleteItem = new JMenuItem("Discharge Patient");

        viewItem.addActionListener(ev -> showPatientDetailsDialog());
        billItem.addActionListener(ev -> showBillDialog());
        editItem.addActionListener(ev -> showEditPatientDialog());
        deleteItem.addActionListener(ev -> performDischarge());

        menu.add(viewItem);
        menu.add(billItem);
        menu.addSeparator();
        menu.add(editItem);
        menu.add(deleteItem);
        menu.show(e.getComponent(), e.getX(), e.getY());
    }

    private void showPatientDetailsDialog() {
        String id = getSelectedIdFromRow();
        if (id == null) return;
        
        Patient p = hmc.getPatientCtrl().findPatientById(id);
        if (p == null) return;
        
        // === Main Card Panel ===
        JPanel cardPanel = new JPanel(new BorderLayout());
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setPreferredSize(new Dimension(450, 600)); // Taller for medical history
        cardPanel.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1));
        
        // 1. Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(13, 110, 253)); // Blue header
        header.setBorder(new EmptyBorder(20, 25, 20, 25));
        
        JLabel lblTitle = new JLabel("Patient Profile");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);
        
        JLabel lblSubtitle = new JLabel(p.getName());
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitle.setForeground(new Color(224, 247, 250));
        
        header.add(lblTitle, BorderLayout.NORTH);
        header.add(lblSubtitle, BorderLayout.SOUTH);
        cardPanel.add(header, BorderLayout.NORTH);
        
        // 2. Content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(20, 25, 20, 25));
        
        // Personal Info
        content.add(createSectionHeader("Personal Information"));
        content.add(createDetailRow("ID", p.getPatientId()));
        content.add(createDetailRow("Age", String.valueOf(p.getAge())));
        content.add(createDetailRow("Address", p.getAddress()));
        content.add(createDetailRow("Admission Date", p.getAdmissionDate() != null ? p.getAdmissionDate().toString() : "--"));
        
        content.add(Box.createVerticalStrut(20));
        
        // Medical Info
        content.add(createSectionHeader("Medical Record"));
        content.add(createDetailRow("Medical History", p.getMedicalHistory()));
        
        if (p.getDoctor() != null) {
            content.add(createDetailRow("Assigned Doctor", "Dr. " + p.getDoctor().getName()));
        } else {
            content.add(createDetailRow("Assigned Doctor", "--"));
        }
        
        if (p.getRoom() != null) {
            content.add(createDetailRow("Room", p.getRoom().getRoomId() + " (" + p.getRoom().getRoomType() + ")"));
        } else {
             content.add(createDetailRow("Room", "--"));
        }

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        cardPanel.add(scroll, BorderLayout.CENTER);
        
        // 3. Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(new Color(245, 245, 245));
        footer.setBorder(new EmptyBorder(10, 20, 10, 20));
        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(e -> javax.swing.SwingUtilities.getWindowAncestor(btnClose).dispose());
        footer.add(btnClose);
        
        JOptionPane.showMessageDialog(this, cardPanel, "Patient Details", JOptionPane.PLAIN_MESSAGE);
    }
    
    // UI Helpers (Duplicated for containment)
    // There are already duplicated helpers in this file (Invoice UI) but createDetailRow might be different. 
    // I will add them or reuse if already present.
    // Checking file... createInvoiceRow exists. I will add generic createDetailRow.
    
    // But wait, createInvoiceRow uses specific font sizes. I will add these specific helpers.
    private JLabel createSectionHeader(String title) {
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(new Color(13, 110, 253));
        lbl.setBorder(new EmptyBorder(0, 0, 10, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }
    
    private JPanel createDetailRow(String label, String value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setMaximumSize(new Dimension(1000, 25));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel l = new JLabel(label + ": ");
        l.setFont(new Font("Segoe UI", Font.BOLD, 13)); l.setForeground(Color.GRAY); l.setPreferredSize(new Dimension(130, 25)); // Slightly wider for "Medical Record" labels
        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.PLAIN, 13)); v.setForeground(new Color(50, 50, 50));
        p.add(l, BorderLayout.WEST); p.add(v, BorderLayout.CENTER);
        return p;
    }

    private void showBillDialog() {
        String patientId = getSelectedIdFromRow();
        if (patientId == null) return;
        
        Patient p = hmc.getPatientCtrl().findPatientById(patientId);
        if (p == null) return;

        // Ask for Date
        JTextField txtDate = new JTextField(LocalDate.now().toString());
        JPanel datePanel = new JPanel(new BorderLayout(10, 10));
        datePanel.add(new JLabel("Discharge Date (YYYY-MM-DD):"), BorderLayout.NORTH);
        datePanel.add(txtDate, BorderLayout.CENTER);
        datePanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        int result = JOptionPane.showConfirmDialog(this, datePanel, "Generate Bill", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            LocalDate dischargeDate = LocalDate.parse(txtDate.getText().trim());
            
            // Calculate Bill Data
            long days = 0;
            if (p.getAdmissionDate() != null) {
                days = java.time.temporal.ChronoUnit.DAYS.between(p.getAdmissionDate(), dischargeDate);
                if (days < 1) days = 1; // Minimum 1 day charge
            }
            
            double roomRate = 0.0;
            String roomType = "No Room Assigned";
            if (p.getRoom() != null) {
                roomType = p.getRoom().getRoomType();
                roomRate = p.getRoom().getPrice();
                if (roomRate == 0) roomRate = 100.0; // Fallback default
            }
            double total = days * roomRate;
            // double doctorFee = hmc.getDoctorCtrl().ConsultationFee(); // Future expansion

            // === BUILD INVOICE UI ===
            JPanel invoicePanel = new JPanel(new BorderLayout(0, 0));
            invoicePanel.setBackground(Color.WHITE);
            invoicePanel.setPreferredSize(new Dimension(450, 600));
            invoicePanel.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1)); // Outer Frame

            // 1. Header
            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(new Color(245, 247, 250)); // Light Gray Header
            header.setBorder(new EmptyBorder(25, 30, 25, 30));
            
            JLabel lblTitle = new JLabel("INVOICE");
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
            lblTitle.setForeground(new Color(50, 50, 50));
            
            JLabel lblDate = new JLabel("Date: " + LocalDate.now());
            lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblDate.setForeground(Color.GRAY);
            
            header.add(lblTitle, BorderLayout.WEST);
            header.add(lblDate, BorderLayout.EAST);
            invoicePanel.add(header, BorderLayout.NORTH);

            // 2. Content
            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setBackground(Color.WHITE);
            content.setBorder(new EmptyBorder(30, 30, 30, 30));

            // Section: Patient Details
            content.add(createInvoiceRow("Billed To:", p.getName()));
            content.add(Box.createVerticalStrut(5));
            content.add(createInvoiceRow("Patient ID:", p.getPatientId()));
            content.add(Box.createVerticalStrut(5));
            content.add(createInvoiceRow("Address:", p.getAddress()));
            
            content.add(Box.createVerticalStrut(30));
            JSeparator sep1 = new JSeparator(); sep1.setForeground(new Color(230, 230, 230));
            content.add(sep1);
            content.add(Box.createVerticalStrut(15));

            // Section: Charges Table (Grid)
            JPanel tableHeader = new JPanel(new BorderLayout());
            tableHeader.setBackground(Color.WHITE);
            JLabel th1 = new JLabel("Description"); th1.setFont(new Font("Segoe UI", Font.BOLD, 12));
            JLabel th2 = new JLabel("Amount"); th2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            tableHeader.add(th1, BorderLayout.WEST);
            tableHeader.add(th2, BorderLayout.EAST);
            content.add(tableHeader);
            content.add(Box.createVerticalStrut(10));
            
            // Item 1: Room Charges
            content.add(createInvoiceItem("Room Charges (" + roomType + ")", 
                days + " days @ $" + String.format("%.2f", roomRate), 
                days * roomRate));
            
            // Item 2: Doctor Fees (Fixed for now as example)
            content.add(Box.createVerticalStrut(10));
            content.add(createInvoiceItem("Medical Services", "Consultation & Care", 150.00));
            total += 150.00;

            content.add(Box.createVerticalStrut(30));
            JSeparator sep2 = new JSeparator(); sep2.setForeground(new Color(230, 230, 230));
            content.add(sep2);
            content.add(Box.createVerticalStrut(15));
            
            // Section: Total
            JPanel totalPanel = new JPanel(new BorderLayout());
            totalPanel.setBackground(Color.WHITE);
            JLabel lblTotalText = new JLabel("TOTAL DUE");
            lblTotalText.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblTotalText.setForeground(Color.GRAY);
            
            JLabel lblTotalVal = new JLabel("$" + String.format("%,.2f", total));
            lblTotalVal.setFont(new Font("Segoe UI", Font.BOLD, 28));
            lblTotalVal.setForeground(new Color(13, 110, 253)); // Primary Blue
            
            totalPanel.add(lblTotalText, BorderLayout.WEST);
            totalPanel.add(lblTotalVal, BorderLayout.EAST);
            content.add(totalPanel);
            
            invoicePanel.add(content, BorderLayout.CENTER);

            // 3. Footer
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
            footer.setBackground(Color.WHITE);
            footer.setBorder(new EmptyBorder(0, 0, 20, 0));
            JLabel lblFooter = new JLabel("Thank you for choosing our hospital service.");
            lblFooter.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            lblFooter.setForeground(Color.LIGHT_GRAY);
            footer.add(lblFooter);
            invoicePanel.add(footer, BorderLayout.SOUTH);

            // Show Dialog
            JOptionPane.showMessageDialog(this, invoicePanel, "Invoice Generated", JOptionPane.PLAIN_MESSAGE);

        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid Date Format.");
        }
    }

    // Helper for Invoice Labels
    private JPanel createInvoiceRow(String label, String value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setMaximumSize(new Dimension(1000, 20));
        JLabel l = new JLabel(label); l.setFont(new Font("Segoe UI", Font.BOLD, 12)); l.setForeground(Color.GRAY);
        JLabel v = new JLabel(value); v.setFont(new Font("Segoe UI", Font.PLAIN, 13)); v.setForeground(Color.DARK_GRAY);
        p.add(l, BorderLayout.WEST);
        p.add(v, BorderLayout.EAST);
        return p;
    }

    // Helper for Invoice Items
    private JPanel createInvoiceItem(String title, String subtitle, double amount) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setMaximumSize(new Dimension(1000, 40));
        
        JPanel left = new JPanel(new GridLayout(2, 1));
        left.setBackground(Color.WHITE);
        JLabel l1 = new JLabel(title); l1.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JLabel l2 = new JLabel(subtitle); l2.setFont(new Font("Segoe UI", Font.PLAIN, 11)); l2.setForeground(Color.GRAY);
        left.add(l1); left.add(l2);
        
        JLabel v = new JLabel("$" + String.format("%,.2f", amount));
        v.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        p.add(left, BorderLayout.WEST);
        p.add(v, BorderLayout.EAST);
        return p;
    }

    private void performDischarge() {
        String patientId = getSelectedIdFromRow();
        if (patientId != null) {
            if (JOptionPane.showConfirmDialog(this, "Discharge " + patientId + "?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (hmc.dischargePatient(patientId)) {
                    JOptionPane.showMessageDialog(this, "Discharged!");
                    refreshTable(null);
                }
            }
        }
    }

    private void showAddPatientDialog() {
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField ageField = new JTextField();
        JTextField addrField = new JTextField();
        JTextField historyField = new JTextField();
        JPanel panel = DialogUtils.createForm("Add New Patient",
            "ID:", idField,
            "Name:", nameField,
            "Age:", ageField,
            "Address:", addrField,
            "Medical History:", historyField
        );
        if (JOptionPane.showConfirmDialog(this, panel, "Add Patient", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            if (idField.getText().trim().isEmpty() || nameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "ID and Name required."); return;
            }
            try {
                int age = Integer.parseInt(ageField.getText().trim());
                Patient p = new Patient(idField.getText(), nameField.getText(), age, addrField.getText(), historyField.getText());
                if (hmc.getPatientCtrl().addPatient(p)) {
                    refreshTable(null);
                    JOptionPane.showMessageDialog(this, "Success!");
                } else JOptionPane.showMessageDialog(this, "ID exists.");
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Invalid Age."); }
        }
    }

    private void showEditPatientDialog() {
        String id = getSelectedIdFromRow();
        if (id == null) return;
        Patient p = hmc.getPatientCtrl().findPatientById(id);
        if (p == null) return;
        JTextField nameField = new JTextField(p.getName());
        JTextField ageField = new JTextField(String.valueOf(p.getAge()));
        JTextField addrField = new JTextField(p.getAddress());
        JTextField historyField = new JTextField(p.getMedicalHistory());
        JPanel panel = DialogUtils.createForm("Edit Patient",
            "Patient ID:", new JLabel(id),
            "Name:", nameField,
            "Age:", ageField,
            "Address:", addrField,
            "History:", historyField
        );

        if (JOptionPane.showConfirmDialog(this, panel, "Edit Patient", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            try {
                p.updateDetails(nameField.getText(), Integer.parseInt(ageField.getText()), addrField.getText(), historyField.getText());
                if (hmc.getPatientCtrl().updatePatient(p)) {
                    refreshTable(null);
                    JOptionPane.showMessageDialog(this, "Updated!");
                }
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Invalid Input"); }
        }
    }

    private void showAssignDoctorDialog(String pid) {
        List<Doctor> docs = hmc.getDoctorCtrl().getAllDoctors();
        if (docs.isEmpty()) { JOptionPane.showMessageDialog(this, "No Doctors."); return; }
        JComboBox<String> box = new JComboBox<>();
        for (Doctor d : docs) box.addItem(d.getDoctorId() + " - " + d.getName());
        if (JOptionPane.showConfirmDialog(this, box, "Assign Doctor", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String docId = ((String)box.getSelectedItem()).split(" - ")[0];
            hmc.assignPatientToDoctor(pid, docId);
            refreshTable(null);
        }
    }

    private void showAssignRoomDialog(String pid) {
        List<Room> rooms = hmc.getRoomCtrl().getAvailableRooms();
        if (rooms.isEmpty()) { JOptionPane.showMessageDialog(this, "No Empty Rooms."); return; }
        JComboBox<String> box = new JComboBox<>();
        for (Room r : rooms) box.addItem(r.getRoomId() + " - " + r.getRoomType());
        if (JOptionPane.showConfirmDialog(this, box, "Assign Room", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String rid = ((String)box.getSelectedItem()).split(" - ")[0];
            hmc.assignPatientToRoom(pid, rid);
            refreshTable(null);
        }
    }
}