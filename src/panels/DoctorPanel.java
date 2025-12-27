package panels;

import controllers.HospitalManagementController;
import models.Doctor;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.filechooser.FileNameExtensionFilter;

public class DoctorPanel extends JPanel {

    private HospitalManagementController hmc;
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtSearch;
    private boolean isAdmin;

    public DoctorPanel(HospitalManagementController hmc, boolean isAdmin) {
        this.hmc = hmc;
        this.isAdmin = isAdmin;
        setLayout(new BorderLayout());

        // --- SEARCH ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtSearch = new JTextField(20);
        JButton btnSearch = new JButton("Search");
        JButton btnShowAll = new JButton("Show All");
        topPanel.add(new JLabel("Search Doctor:"));
        topPanel.add(txtSearch);
        topPanel.add(btnSearch);
        topPanel.add(btnShowAll);
        add(topPanel, BorderLayout.NORTH);

        // --- TABLE ---
        String[] columns = {"Profile", "ID", "Name", "Specialization"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? ImageIcon.class : Object.class;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(70); // Increased height for better profile view
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 5));
        
        // Assign the stylish renderer
        table.getColumnModel().getColumn(0).setCellRenderer(new StylishImageRenderer());
        
        add(new JScrollPane(table), BorderLayout.CENTER);

        // --- MOUSE LISTENERS ---
        if (isAdmin) {
            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) showEditDoctorDialog();
                    if (SwingUtilities.isRightMouseButton(e)) {
                        int row = table.rowAtPoint(e.getPoint());
                        if (row >= 0 && row < table.getRowCount()) {
                            table.setRowSelectionInterval(row, row);
                            showContextMenu(e);
                        }
                    }
                }
            });
        }

        // --- BUTTONS ---
        JPanel buttonPanel = new JPanel();
        JButton btnAdd = new JButton("Add");
        JButton btnEdit = new JButton("Edit");
        JButton btnDelete = new JButton("Delete");
        btnDelete.setForeground(Color.RED);
        
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnDelete);
        
        if (!isAdmin) {
            btnAdd.setEnabled(false);
            btnEdit.setEnabled(false);
            btnDelete.setEnabled(false);
        }
        
        add(buttonPanel, BorderLayout.SOUTH);

        // --- LISTENERS ---
        btnSearch.addActionListener(e -> performSearch());
        btnShowAll.addActionListener(e -> { txtSearch.setText(""); refreshTable(null); });
        txtSearch.addActionListener(e -> performSearch());
        
        if (isAdmin) {
            btnAdd.addActionListener(e -> showAddDoctorDialog());
            btnEdit.addActionListener(e -> showEditDoctorDialog());
            btnDelete.addActionListener(e -> performDelete());
        }

        refreshTable(null);
    }
    
    // ==========================================
    //    CUSTOM RENDERER: Stylish Circular Image
    // ==========================================
    class StylishImageRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            // Background handling
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setText(""); 
            setHorizontalAlignment(JLabel.CENTER);
            
            // Get Name for Initials (Column 2)
            String name = (String) table.getModel().getValueAt(row, 2);
            String initials = getInitials(name);

            // Create a custom paint component
            JPanel panel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    // Background selection logic
                    if (isSelected) {
                        g2.setColor(table.getSelectionBackground());
                    } else {
                        g2.setColor(Color.WHITE);
                    }
                    g2.fillRect(0, 0, getWidth(), getHeight());

                    int size = 56; // Circle Diameter
                    int x = (getWidth() - size) / 2;
                    int y = (getHeight() - size) / 2;

                    if (value != null && !value.toString().equals("No Image") && new File(value.toString()).exists()) {
                        // --- DRAW IMAGE ---
                        try {
                            BufferedImage original = ImageIO.read(new File(value.toString()));
                            // Draw circular mask
                            g2.setClip(new Ellipse2D.Float(x, y, size, size));
                            g2.drawImage(original, x, y, size, size, null);
                            g2.setClip(null); // Reset clip
                            
                            // Draw Border
                            g2.setColor(new Color(200, 200, 200));
                            g2.setStroke(new BasicStroke(1f));
                            g2.drawOval(x, y, size, size);
                            
                        } catch (Exception e) {
                            drawPlaceholder(g2, x, y, size, initials);
                        }
                    } else {
                        // --- DRAW PLACEHOLDER ---
                        drawPlaceholder(g2, x, y, size, initials);
                    }
                }
                
                private void drawPlaceholder(Graphics2D g2, int x, int y, int size, String text) {
                    // Random-ish color based on name hash
                    int hash = name.hashCode();
                    Color bg = new Color((hash & 0xFF0000) >> 16, (hash & 0x00FF00) >> 8, hash & 0x0000FF).brighter();
                    // Ensure it's not too white
                    if (bg.getRed() > 200 && bg.getGreen() > 200 && bg.getBlue() > 200) bg = Color.GRAY;

                    g2.setColor(bg);
                    g2.fillOval(x, y, size, size);
                    
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
                    FontMetrics fm = g2.getFontMetrics();
                    int textX = x + (size - fm.stringWidth(text)) / 2;
                    int textY = y + ((size - fm.getHeight()) / 2) + fm.getAscent();
                    g2.drawString(text, textX, textY);
                }
            };
            return panel;
        }
    }
    
    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.split(" ");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
    
    // --- Helper: Pick Image ---
    private String pickImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Profile Image");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Images", "jpg", "png", "jpeg"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile().getAbsolutePath();
        }
        return null;
    }

    private void performDelete() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a doctor first."); return; }
        
        String id = (String) table.getValueAt(row, 1); 
        String name = (String) table.getValueAt(row, 2);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Delete Doctor " + name + "?\nWarning: Patients assigned to this doctor will be unassigned.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            if (hmc.getDoctorCtrl().deleteDoctor(id)) {
                JOptionPane.showMessageDialog(this, "Doctor Deleted.");
                refreshTable(null);
            } else {
                JOptionPane.showMessageDialog(this, "Delete Failed.");
            }
        }
    }

    private void showContextMenu(MouseEvent e) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem editItem = new JMenuItem("Edit Doctor");
        JMenuItem deleteItem = new JMenuItem("Delete Doctor"); 
        
        editItem.addActionListener(ev -> showEditDoctorDialog());
        deleteItem.addActionListener(ev -> performDelete());
        
        menu.add(editItem);
        menu.add(deleteItem);
        menu.show(e.getComponent(), e.getX(), e.getY());
    }

    private void performSearch() {
        String q = txtSearch.getText().trim();
        if (!q.isEmpty()) refreshTable(hmc.getDoctorCtrl().searchDoctors(q));
        else refreshTable(null);
    }

    public void refreshTable(List<Doctor> data) {
        tableModel.setRowCount(0);
        List<Doctor> doctors = (data == null) ? hmc.getDoctorCtrl().getAllDoctors() : data;
        for (Doctor d : doctors) {
            tableModel.addRow(new Object[]{
                d.getImagePath(), // Column 0: Image Path (Renderer will turn this into an icon)
                d.getDoctorId(),  // Column 1
                d.getName(),      // Column 2
                d.getSpecialization() // Column 3
            });
        }
    }

    private void showAddDoctorDialog() {
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField specField = new JTextField();
        
        // Image Button
        JButton btnImage = new JButton("Select Image");
        final String[] selectedPath = {null}; // Wrapper for listener
        
        btnImage.addActionListener(e -> {
            String path = pickImage();
            if (path != null) {
                selectedPath[0] = path;
                btnImage.setText("Image Selected!");
                btnImage.setBackground(Color.GREEN);
            }
        });

        Object[] message = { 
            "ID:", idField, 
            "Name:", nameField, 
            "Specialization:", specField,
            "Profile Photo:", btnImage
        };

        if (JOptionPane.showConfirmDialog(this, message, "Add Doctor", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            Doctor d = new Doctor(idField.getText(), nameField.getText(), specField.getText(), selectedPath[0]);
            if (hmc.getDoctorCtrl().addDoctor(d)) refreshTable(null);
            else JOptionPane.showMessageDialog(this, "Failed to add.");
        }
    }

    private void showEditDoctorDialog() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a doctor first."); return; }
        
        // Indices shifted by +1 because of Profile column at index 0
        String id = (String) table.getValueAt(row, 1);
        Doctor d = hmc.getDoctorCtrl().findDoctorById(id);
        
        JTextField nameField = new JTextField(d.getName());
        JTextField specField = new JTextField(d.getSpecialization());
        
        JButton btnImage = new JButton(d.getImagePath() != null ? "Change Image" : "Select Image");
        final String[] selectedPath = {d.getImagePath()}; 
        
        btnImage.addActionListener(e -> {
            String path = pickImage();
            if (path != null) {
                selectedPath[0] = path;
                btnImage.setText("Image Selected!");
            }
        });
        
        Object[] message = { 
            "ID: " + id, 
            "Name:", nameField, 
            "Specialization:", specField,
            "Profile Photo:", btnImage
        };
        
        if (JOptionPane.showConfirmDialog(this, message, "Edit Doctor", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            d.setName(nameField.getText());
            d.setSpecialization(specField.getText());
            d.setImagePath(selectedPath[0]); // Update Image Path
            
            if (hmc.getDoctorCtrl().updateDoctor(d)) {
                refreshTable(null);
                JOptionPane.showMessageDialog(this, "Updated!");
            }
        }
    }
}