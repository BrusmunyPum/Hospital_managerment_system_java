package panels;

import controllers.HospitalManagementController;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import models.*;

import utils.IconUtils;
import utils.ModernUI;
import utils.DialogUtils;

public class DoctorPanel extends JPanel {

    private HospitalManagementController hmc;
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtSearch;
    private boolean isAdmin;

    // Modern Color Palette
    private Color primaryBlue = new Color(13, 110, 253);
    private Color bgLight = new Color(245, 247, 251);
    private Color textDark = new Color(50, 50, 50);
    private Color textGray = new Color(108, 117, 125);

    public DoctorPanel(HospitalManagementController hmc, boolean isAdmin) {
        this.hmc = hmc;
        this.isAdmin = isAdmin;
        
        setLayout(new BorderLayout());
        setBackground(bgLight);
        setBorder(new EmptyBorder(20, 30, 20, 30));

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
        String[] columns = {"Doctor Name", "Specialization", "Patients", "Availability"};
        
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        
        table = new JTable(tableModel);
        setupTableStyle();

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
        
        // --- 3. BOTTOM SECTION (Action Buttons) ---
        if (isAdmin) {
            add(createActionButtons(), BorderLayout.SOUTH);
        }

        // --- EVENT LISTENERS ---
        setupListeners();
        
        refreshTable(null);
    }

    // ==========================================
    //              UI BUILDER METHODS
    // ==========================================

    private JPanel createHeaderSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        JLabel title = new JLabel("Doctor Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(textDark);
        
        JLabel subtitle = new JLabel("Manage medical staff, specializations, and assignments.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(textGray);
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        textPanel.setOpaque(false);
        textPanel.add(title);
        textPanel.add(subtitle);
        
        panel.add(textPanel, BorderLayout.WEST);
        
        // "Add New Doctor" Button (Top Right) - Admin Only
        if (isAdmin) {
            JButton btnAdd = ModernUI.createPrimaryButton("+ Add New Doctor", null);
            btnAdd.setPreferredSize(new Dimension(180, 40));
            btnAdd.addActionListener(e -> showAddDoctorDialog());
            panel.add(btnAdd, BorderLayout.EAST);
        }
        
        return panel;
    }

    private JPanel createStatsSection() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 20, 0));
        panel.setOpaque(false);
        
        // Calculate real stats
        int totalDoctors = hmc.getDoctorCtrl().getAllDoctors().size();
        
        // Count by specialization (example with common specializations)
        int cardiology = countBySpec("Cardiology");
        int neurology = countBySpec("Neurology");
        int pediatrics = countBySpec("Pediatrics");
        
        panel.add(ModernUI.createStatsCard("Total Doctors", String.valueOf(totalDoctors), "Active", 
            IconUtils.createIcon(IconUtils.ICON_DOCTOR, 32, primaryBlue),
            new Color(230, 240, 255), primaryBlue));
            
        panel.add(ModernUI.createStatsCard("Cardiology", String.valueOf(cardiology), "Specialists", 
            IconUtils.createIcon(IconUtils.ICON_HEART, 32, new Color(220, 53, 69)), 
            new Color(255, 235, 235), new Color(220, 53, 69)));
            
        panel.add(ModernUI.createStatsCard("Neurology", String.valueOf(neurology), "Specialists", 
            IconUtils.createIcon(IconUtils.ICON_BRAIN, 32, new Color(111, 66, 193)), 
            new Color(240, 230, 255), new Color(111, 66, 193)));
            
        panel.add(ModernUI.createStatsCard("Pediatrics", String.valueOf(pediatrics), "Specialists", 
            IconUtils.createIcon(IconUtils.ICON_BABY, 32, new Color(255, 193, 7)), 
            new Color(255, 245, 220), new Color(255, 193, 7)));
        
        return panel;
    }

    private int countBySpec(String spec) {
        int count = 0;
        for (Doctor d : hmc.getDoctorCtrl().getAllDoctors()) {
            if (d.getSpecialization() != null && 
                d.getSpecialization().toLowerCase().contains(spec.toLowerCase())) {
                count++;
            }
        }
        return count;
    }

    private JPanel createToolbarSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        // Search Bar (Left)
        JPanel searchContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        searchContainer.setOpaque(false);
        
        txtSearch = ModernUI.createSearchField(" Search by name, ID, specialization...");
        
        searchContainer.add(txtSearch);
        
        // Filters (Right)
        JPanel filterContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterContainer.setOpaque(false);
        
        JButton btnFilterSpec = ModernUI.createOutlineButton("All Specializations ▼", null);
        btnFilterSpec.addActionListener(e -> showSpecFilterMenu(btnFilterSpec));
        
        // Show All / Refresh Button
        JButton btnRefresh = ModernUI.createOutlineButton("Refresh", IconUtils.createIcon(IconUtils.ICON_REFRESH, 18, textDark));
        btnRefresh.setPreferredSize(new Dimension(110, 35));
        btnRefresh.addActionListener(e -> { 
             txtSearch.setText(""); 
             refreshTable(null); 
             btnFilterSpec.setText("All Specializations ▼");
        });
 
        filterContainer.add(btnFilterSpec);
        filterContainer.add(btnRefresh);
        
        panel.add(searchContainer, BorderLayout.WEST);
        panel.add(filterContainer, BorderLayout.EAST);
        
        return panel;
    }

    private JPanel createActionButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(bgLight);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        JButton btnEdit = ModernUI.createOutlineButton("Edit Doctor", IconUtils.createIcon(IconUtils.ICON_PENCIL, 16, textDark));
        
        JButton btnView = ModernUI.createOutlineButton("View Details", IconUtils.createIcon(IconUtils.ICON_EYE, 16, textDark));
        
        JButton btnDelete = ModernUI.createDangerButton("Delete", IconUtils.createIcon(IconUtils.ICON_TRASH, 16, Color.WHITE));
        
        btnEdit.addActionListener(e -> showEditDoctorDialog());
        btnView.addActionListener(e -> showDoctorDetailsDialog());
        btnDelete.addActionListener(e -> performDelete());
        
        panel.add(btnView);
        panel.add(btnEdit);
        panel.add(btnDelete);
        
        return panel;
    }

    private void showSpecFilterMenu(JButton source) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem allItem = new JMenuItem("All Specializations");
        allItem.addActionListener(e -> { 
            txtSearch.setText(""); 
            refreshTable(null); 
            source.setText("All Specializations ▼");
        });
        menu.add(allItem);
        menu.addSeparator();
        
        // Get unique specs
        Set<String> specs = new HashSet<>();
        List<Doctor> doctors = hmc.getDoctorCtrl().getAllDoctors();
        for(Doctor d : doctors) {
            if(d.getSpecialization() != null && !d.getSpecialization().isEmpty()) {
                specs.add(d.getSpecialization());
            }
        }
        
        for (String spec : specs) {
            JMenuItem item = new JMenuItem(spec);
            item.addActionListener(e -> {
                filterBySpec(spec);
                source.setText(spec + " ▼");
            });
            menu.add(item);
        }
        menu.show(source, 0, source.getHeight());
    }

    private void filterBySpec(String spec) {
        List<Doctor> all = hmc.getDoctorCtrl().getAllDoctors();
        List<Doctor> filtered = new ArrayList<>();
        for(Doctor d : all) {
            if(d.getSpecialization() != null && d.getSpecialization().equalsIgnoreCase(spec)) {
                filtered.add(d);
            }
        }
        refreshTable(filtered);
    }

    // ==========================================
    //           TABLE STYLING & RENDERERS
    // ==========================================

    private void setupTableStyle() {
        ModernUI.setupTableStyle(table);
        table.setRowHeight(60); 

        // Apply Custom Renderers
        table.getColumnModel().getColumn(0).setCellRenderer(new DoctorInfoRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(new SpecializationRenderer());
        table.getColumnModel().getColumn(2).setCellRenderer(new PatientCountRenderer());
        table.getColumnModel().getColumn(3).setCellRenderer(new AvailabilityRenderer());
    }

    // 1. Doctor Info Renderer (Avatar + Name + ID)
    class DoctorInfoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            // OUTER PANEL: Centers the inner content
            JPanel outer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
            outer.setOpaque(true);
            outer.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            
            // INNER CONTAINER: Fixed width for perfect vertical alignment
            JPanel content = new JPanel(new BorderLayout(10, 0)); 
            content.setPreferredSize(new Dimension(200, 40)); 
            content.setOpaque(false);
            
            String fullString = (String) value; // "DR-101:Name"
            String[] parts = fullString.split(":");
            String id = parts.length > 0 ? parts[0] : "";
            String name = parts.length > 1 ? parts[1] : "";
            
            // Avatar
            JLabel avatar = new JLabel() {
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(230, 240, 255));
                    g2.fillOval(0, 0, 40, 40);
                    g2.setColor(primaryBlue);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
                    String initial = name.length() > 1 ? name.substring(0, 1).toUpperCase() : "D";
                    g2.drawString(initial, 14, 26);
                }
            };
            avatar.setPreferredSize(new Dimension(40, 40));
            
            // Text Panel
            JPanel textPanel = new JPanel(new GridLayout(2, 1));
            textPanel.setOpaque(false);
            
            JLabel lblName = new JLabel("Dr. " + name);
            lblName.setFont(new Font("Segoe UI", Font.BOLD, 14));
            // Truncate long names
            if (name.length() > 15) {
                lblName.setText("Dr. " + name.substring(0, 12) + "...");
                lblName.setToolTipText("Dr. " + name);
            }
            
            JLabel lblId = new JLabel("ID: " + id);
            lblId.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblId.setForeground(Color.GRAY);
            
            textPanel.add(lblName);
            textPanel.add(lblId);
            
            content.add(avatar, BorderLayout.WEST);
            content.add(textPanel, BorderLayout.CENTER);
            
            outer.add(content);
            return outer;
        }
    }

    // 2. Specialization Renderer (Colored badge)
    class SpecializationRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            // "justify-content: center" -> FlowLayout.CENTER
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
            panel.setOpaque(true);
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            
            String spec = (String) value;
            Color bg, fg;
            
            // Assign colors based on specialization
            if (spec.toLowerCase().contains("cardio")) {
                bg = new Color(255, 235, 235); fg = new Color(220, 53, 69);
            } else if (spec.toLowerCase().contains("neuro")) {
                bg = new Color(240, 230, 255); fg = new Color(111, 66, 193);
            } else if (spec.toLowerCase().contains("ortho")) {
                bg = new Color(220, 240, 255); fg = new Color(13, 110, 253);
            } else if (spec.toLowerCase().contains("pediatr")) {
                bg = new Color(255, 245, 220); fg = new Color(255, 193, 7);
            } else {
                bg = new Color(240, 240, 240); fg = new Color(100, 100, 100);
            }
            
            JLabel badge = new JLabel(spec) {
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(bg);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                    super.paintComponent(g);
                }
            };
            badge.setOpaque(false);
            badge.setForeground(fg);
            badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
            badge.setHorizontalAlignment(SwingConstants.CENTER);
            badge.setPreferredSize(new Dimension(120, 26));
            
            panel.add(badge);
            return panel;
        }
    }

    // 3. Patient Count Renderer
    class PatientCountRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 14));
            label.setText(value + " Patients");
            return label;
        }
    }

    // 4. Availability Renderer (Status indicator)
    class AvailabilityRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            // "justify-content: center" -> FlowLayout.CENTER
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
            panel.setOpaque(true);
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            
            String status = (String) value;
            Color dotColor = status.equals("Available") ? new Color(25, 135, 84) : new Color(255, 193, 7);
            
            JLabel dot = new JLabel() {
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(dotColor);
                    g2.fillOval(0, 0, 10, 10);
                }
            };
            dot.setPreferredSize(new Dimension(10, 10));
            
            JLabel label = new JLabel(status);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            
            panel.add(dot);
            panel.add(label);
            return panel;
        }
    }

    // ==========================================
    //           LOGIC & DATA METHODS
    // ==========================================

    public void refreshTable(List<Doctor> data) {
        tableModel.setRowCount(0);
        
        // Use List<Object[]> to carry prepared row data
        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                List<Doctor> doctors;
                if (data != null) doctors = data;
                else doctors = hmc.getDoctorCtrl().getAllDoctors();
                
                // Get all patient counts in ONE query (Map<DoctorID, Count>)
                java.util.Map<String, Integer> counts = hmc.getPatientCtrl().getPatientCountsGroupedByDoctor();
                
                List<Object[]> rows = new java.util.ArrayList<>();
                
                for (Doctor d : doctors) {
                    // No more DB call here! Look up from map.
                    int patientCount = counts.getOrDefault(d.getDoctorId(), 0);
                    String availability = patientCount < 10 ? "Available" : "Busy";
                    
                    rows.add(new Object[]{
                        d.getDoctorId() + ":" + d.getName(),
                        d.getSpecialization(),
                        String.valueOf(patientCount),
                        availability
                    });
                }
                return rows;
            }

            @Override
            protected void done() {
                try {
                    List<Object[]> rows = get();
                    tableModel.setRowCount(0);
                    for (Object[] row : rows) {
                        tableModel.addRow(row);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(DoctorPanel.this, "Error loading doctors: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void setupListeners() {
        txtSearch.addActionListener(e -> performSearch());
        
        if (isAdmin) {
            table.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) showEditDoctorDialog();
                    if (SwingUtilities.isRightMouseButton(e)) showContextMenu(e);
                }
            });
        }
    }

        
    private void performSearch() {
        String query = txtSearch.getText().trim();
        if (!query.isEmpty() && !query.contains("Search")) {
            List<Doctor> results = hmc.getDoctorCtrl().searchDoctors(query);
            refreshTable(results);
        } else {
            refreshTable(null);
        }
    }
    
    private String getSelectedIdFromRow() {
        int row = table.getSelectedRow();
        if (row == -1) { 
            JOptionPane.showMessageDialog(this, "Please select a doctor first."); 
            return null; 
        }
        String raw = (String) table.getValueAt(row, 0);
        return raw.split(":")[0];
    }

    // ==========================================
    //           DIALOG METHODS
    // ==========================================

    private void showContextMenu(MouseEvent e) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem editItem = new JMenuItem("Edit Doctor", IconUtils.createIcon(IconUtils.ICON_PENCIL, 16, Color.DARK_GRAY));
        JMenuItem viewItem = new JMenuItem("View Details", IconUtils.createIcon(IconUtils.ICON_EYE, 16, Color.DARK_GRAY));
        JMenuItem deleteItem = new JMenuItem("Delete Doctor", IconUtils.createIcon(IconUtils.ICON_TRASH, 16, Color.RED));
        
        editItem.addActionListener(ev -> showEditDoctorDialog());
        viewItem.addActionListener(ev -> showDoctorDetailsDialog());
        deleteItem.addActionListener(ev -> performDelete());
        
        menu.add(viewItem);
        menu.add(editItem);
        menu.addSeparator();
        menu.add(deleteItem);
        menu.show(e.getComponent(), e.getX(), e.getY());
    }

    private void performDelete() {
        String id = getSelectedIdFromRow();
        if (id == null) return;
        
        Doctor d = hmc.getDoctorCtrl().findDoctorById(id);
        int patientCount = hmc.getPatientCtrl().getPatientsByDoctorId(id).size();
        
        String message = "Delete Dr. " + d.getName() + "?";
        if (patientCount > 0) {
            message += "\n\nWarning: " + patientCount + " patient(s) are currently assigned to this doctor.";
            message += "\nThey will be unassigned.";
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, message, 
            "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
        if (confirm == JOptionPane.YES_OPTION) {
            if (hmc.getDoctorCtrl().deleteDoctor(id)) {
                JOptionPane.showMessageDialog(this, "Doctor deleted successfully.");
                refreshTable(null);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete doctor.");
            }
        }
    }

    private void showAddDoctorDialog() {
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField specField = new JTextField();
        
        JPanel panel = DialogUtils.createForm("Add New Doctor",
            "Doctor ID:", idField,
            "Name:", nameField,
            "Specialization:", specField
        );
        
        if (JOptionPane.showConfirmDialog(this, panel, "Add New Doctor", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            
            if (idField.getText().trim().isEmpty() || nameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "ID and Name are required.");
                return;
            }
            
            Doctor d = new Doctor(idField.getText().trim(), nameField.getText().trim(), 
                                specField.getText().trim());
            
            if (hmc.getDoctorCtrl().addDoctor(d)) {
                JOptionPane.showMessageDialog(this, "Doctor added successfully!");
                refreshTable(null);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add doctor. ID may already exist.");
            }
        }
    }

    private void showEditDoctorDialog() {
        String id = getSelectedIdFromRow();
        if (id == null) return;
        
        Doctor d = hmc.getDoctorCtrl().findDoctorById(id);
        if (d == null) return;
        
        JTextField nameField = new JTextField(d.getName());
        JTextField specField = new JTextField(d.getSpecialization());
        
        JPanel panel = DialogUtils.createForm("Edit Doctor",
            "Doctor ID:", new JLabel(id),
            "Name:", nameField,
            "Specialization:", specField
        );
        
        if (JOptionPane.showConfirmDialog(this, panel, "Edit Doctor", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            
            d.setName(nameField.getText().trim());
            d.setSpecialization(specField.getText().trim());
            
            if (hmc.getDoctorCtrl().updateDoctor(d)) {
                JOptionPane.showMessageDialog(this, "Doctor updated successfully!");
                refreshTable(null);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update doctor.");
            }
        }
    }

    private void showDoctorDetailsDialog() {
        String id = getSelectedIdFromRow();
        if (id == null) return;
        
        Doctor d = hmc.getDoctorCtrl().findDoctorById(id);
        if (d == null) return;
        
        List<Patient> patients = hmc.getPatientCtrl().getPatientsByDoctorId(id);
        
        // === Main Card Panel ===
        JPanel cardPanel = new JPanel(new BorderLayout());
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setPreferredSize(new Dimension(450, 500));
        cardPanel.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1));
        
        // 1. Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(13, 110, 253)); // Blue header
        header.setBorder(new EmptyBorder(20, 25, 20, 25));
        
        JLabel lblTitle = new JLabel("Dr. " + d.getName());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);
        
        JLabel lblSubtitle = new JLabel(d.getSpecialization());
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
        
        // Doctor Info
        content.add(createSectionHeader("Profile"));
        content.add(createDetailRow("Doctor ID", d.getDoctorId()));
        content.add(createDetailRow("Specialization", d.getSpecialization()));
        content.add(createDetailRow("Total Patients", String.valueOf(patients.size())));
        
        content.add(Box.createVerticalStrut(20));
        
        // Assigned Patients
        content.add(createSectionHeader("Assigned Patients"));
        if (patients.isEmpty()) {
             JLabel lblFree = new JLabel("No active patients assigned.");
             lblFree.setFont(new Font("Segoe UI", Font.ITALIC, 13));
             lblFree.setForeground(Color.GRAY);
             content.add(lblFree);
        } else {
             for (Patient p : patients) {
                 JPanel pRow = new JPanel(new BorderLayout());
                 pRow.setBackground(new Color(248, 249, 250));
                 pRow.setBorder(new EmptyBorder(8, 10, 8, 10));
                 pRow.setMaximumSize(new Dimension(1000, 40));
                 pRow.setAlignmentX(Component.LEFT_ALIGNMENT);
                 
                 JLabel name = new JLabel(p.getName());
                 name.setFont(new Font("Segoe UI", Font.BOLD, 13));
                 
                 JLabel sub = new JLabel("ID: " + p.getPatientId() + " • " + (p.getRoom()!=null ? p.getRoom().getRoomId() : "No Room"));
                 sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                 sub.setForeground(Color.GRAY);
                 
                 JPanel text = new JPanel(new GridLayout(2, 1));
                 text.setBackground(new Color(248, 249, 250));
                 text.add(name); text.add(sub);
                 
                 pRow.add(text, BorderLayout.CENTER);
                 content.add(pRow);
                 content.add(Box.createVerticalStrut(5));
             }
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
        
        JOptionPane.showMessageDialog(this, cardPanel, "Doctor Details", JOptionPane.PLAIN_MESSAGE);
    }
    
    // UI Helpers (Duplicated for containment)
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
        l.setFont(new Font("Segoe UI", Font.BOLD, 13)); l.setForeground(Color.GRAY); l.setPreferredSize(new Dimension(100, 25));
        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.PLAIN, 13)); v.setForeground(new Color(50, 50, 50));
        p.add(l, BorderLayout.WEST); p.add(v, BorderLayout.CENTER);
        return p;
    }
}