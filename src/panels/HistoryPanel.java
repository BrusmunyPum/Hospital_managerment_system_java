package panels;

import controllers.HospitalManagementController;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import models.PatientHistory;
import utils.IconUtils;
import utils.ModernUI;

public class HistoryPanel extends JPanel {

    private HospitalManagementController hmc;
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtSearch;

    // Colors
    private Color bgLight = new Color(245, 247, 251);
    private Color textDark = new Color(50, 50, 50);
    private Color textGray = new Color(108, 117, 125);

    public HistoryPanel(HospitalManagementController hmc) {
        this.hmc = hmc;
        
        setLayout(new BorderLayout());
        setBackground(bgLight);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        // --- TOP SECTION ---
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Patient History");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(textDark);
        JLabel subtitle = new JLabel("Archive of discharged patients and medical records.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(textGray);
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        textPanel.setOpaque(false);
        textPanel.add(title);
        textPanel.add(subtitle);
        header.add(textPanel, BorderLayout.WEST);
        
        topContainer.add(header);
        topContainer.add(Box.createVerticalStrut(20));

        // Toolbar
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        
        txtSearch = ModernUI.createSearchField(" Search history...");
        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        searchBox.setOpaque(false);
        searchBox.add(txtSearch);
        
        // Buttons
        JButton btnReset = ModernUI.createDangerButton("Reset DB", null); 
        btnReset.setPreferredSize(new Dimension(110, 35));
        btnReset.addActionListener(e -> resetDatabase());

        JButton btnRefresh = ModernUI.createOutlineButton("Refresh", IconUtils.createIcon(IconUtils.ICON_REFRESH, 18, textDark));
        btnRefresh.setPreferredSize(new Dimension(110, 35));
        btnRefresh.addActionListener(e -> loadData());
        
        JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBox.setOpaque(false);
        rightBox.add(btnReset);
        rightBox.add(btnRefresh);
        
        toolbar.add(searchBox, BorderLayout.WEST);
        toolbar.add(rightBox, BorderLayout.EAST);
        
        topContainer.add(toolbar);
        topContainer.add(Box.createVerticalStrut(15));
        
        add(topContainer, BorderLayout.NORTH);

        // --- CENTER TABLE ---
        String[] columns = {"Patient", "Diagnosis", "Attending Doctor", "Admitted", "Discharged"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        
        table = new JTable(tableModel);
        setupTableStyle();
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createLineBorder(new Color(230,230,230), 1));
        tableCard.add(scrollPane, BorderLayout.CENTER);
        
        add(tableCard, BorderLayout.CENTER);
        
        refreshTable();
    }
    
    private void setupTableStyle() {
        ModernUI.setupTableStyle(table);
        
        // 1. Patient Info Column (Avatar + text)
        table.getColumnModel().getColumn(0).setCellRenderer(new HistoryPatientRenderer());
        table.getColumnModel().getColumn(0).setPreferredWidth(250);
        
        // 2. Center other columns: Diagnosis, Doctor, Admitted, Discharged
        for (int i = 1; i < table.getColumnCount(); i++) {
             table.getColumnModel().getColumn(i).setCellRenderer(ModernUI.createCenterRenderer());
        }
    }
    
    // --- CUSTOM RENDERER: Patient Info (Avatar + Name/ID) ---
    private class HistoryPatientRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            // Container
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5)); // Center align overall, with gap
            panel.setOpaque(true);
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            
            String labelText = (value != null) ? value.toString() : "";
            
            // Parse Name and ID from "Name (ID)" format
            String name = "Unknown";
            String id = "";
            if (labelText.contains("(") && labelText.contains(")")) {
                int start = labelText.lastIndexOf("(");
                int end = labelText.lastIndexOf(")");
                name = labelText.substring(0, start).trim();
                id = labelText.substring(start + 1, end).trim();
            } else {
                name = labelText;
            }
            
            // Create Avatar Icon
            Color avatarColor = ModernUI.getAvatarColor(name);
            String initial = (name.length() > 0) ? name.substring(0, 1).toUpperCase() : "?";
            Icon avatar = IconUtils.createCircleIcon(initial, avatarColor, 28);
            JLabel lblIcon = new JLabel(avatar);
            
            // Create Text Panel (Name top, ID bottom)
            JPanel textPanel = new JPanel(new GridLayout(2, 1));
            textPanel.setOpaque(false);
            
            JLabel lblName = new JLabel(name);
            lblName.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblName.setForeground(isSelected ? table.getSelectionForeground() : textDark);
            
            JLabel lblId = new JLabel("ID: " + id);
            lblId.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblId.setForeground(isSelected ? table.getSelectionForeground() : textGray);
            
            textPanel.add(lblName);
            textPanel.add(lblId);
            
            // Fix Width Container for "Smart Center" alignment
            // This ensures the avatar is always at the same X position relative to the center
            JPanel content = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            content.setOpaque(false);
            content.setPreferredSize(new Dimension(220, 35)); // Fixed width to ensure alignment
            content.add(lblIcon);
            content.add(Box.createHorizontalStrut(10));
            content.add(textPanel);
            
            panel.add(content);
            return panel;
        }
    }
    
    public void loadData() {
        refreshTable(); 
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        SwingWorker<List<PatientHistory>, Void> worker = new SwingWorker<>() {
            @Override protected List<PatientHistory> doInBackground() {
                return hmc.getPatientCtrl().getPatientHistory();
            }
            @Override protected void done() {
                try {
                    List<PatientHistory> list = get();
                    for (PatientHistory p : list) {
                        tableModel.addRow(new Object[]{
                            p.getName() + " (" + p.getPatientId() + ")",
                            p.getMedicalHistory(),
                            p.getDoctorName(),
                            p.getAdmissionDate(),
                            p.getDischargeDate()
                        });
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        };
        worker.execute();
    }

    private void resetDatabase() {
        int choice = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete ALL history and reset the database?\nThis action cannot be undone.",
            "Confirm Database Reset", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
        if (choice == JOptionPane.YES_OPTION) {
            boolean success = hmc.getPatientCtrl().resetDatabaseTables();
            if (success) {
                JOptionPane.showMessageDialog(this, "Database has been reset successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to reset database.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
