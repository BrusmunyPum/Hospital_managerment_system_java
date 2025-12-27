package panels;

import controllers.HospitalManagementController;
import models.Room;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class RoomPanel extends JPanel {

    private HospitalManagementController hmc;
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtSearch;
    private boolean isAdmin;

    public RoomPanel(HospitalManagementController hmc, boolean isAdmin) {
        this.hmc = hmc;
        this.isAdmin = isAdmin;
        setLayout(new BorderLayout());

        // --- SEARCH ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtSearch = new JTextField(20);
        JButton btnSearch = new JButton("Search");
        JButton btnShowAll = new JButton("Show All");
        topPanel.add(new JLabel("Search Room:"));
        topPanel.add(txtSearch);
        topPanel.add(btnSearch);
        topPanel.add(btnShowAll);
        add(topPanel, BorderLayout.NORTH);

        // --- TABLE ---
        // Column 0 is "Badge" (The visual ID), Column 1 is "Type", etc.
        String[] columns = {"Room Badge", "Type", "Status", "Patient"};
        
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(50); // Taller rows for the badge
        table.setShowVerticalLines(false); // Cleaner look
        table.setIntercellSpacing(new Dimension(0, 5)); // Gap between rows

        // Apply Custom Renderer to Column 0
        table.getColumnModel().getColumn(0).setCellRenderer(new RoomBadgeRenderer());

        add(new JScrollPane(table), BorderLayout.CENTER);

        // --- MOUSE LISTENERS ---
        if (isAdmin) {
            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) showEditRoomDialog();
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
        JButton btnAdd = new JButton("Add Room");
        JButton btnEdit = new JButton("Edit Room"); 
        JButton btnDelete = new JButton("Delete Room");
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
            btnAdd.addActionListener(e -> showAddRoomDialog());
            btnEdit.addActionListener(e -> showEditRoomDialog());
            btnDelete.addActionListener(e -> performDelete());
        }

        refreshTable(null);
    }
    
    // ==========================================
    //       CUSTOM RENDERER: Room Badge
    // ==========================================
    class RoomBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            // Get the Room ID (text)
            String roomId = (String) value;
            
            // Get the Room Type (from the next column in the model) to decide color
            String type = (String) table.getModel().getValueAt(row, 1);
            
            // Create a custom panel to draw the badge
            JPanel panel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // 1. Determine Colors based on Type
                    Color bg, text;
                    if (type.equalsIgnoreCase("ICU")) {
                        bg = new Color(254, 226, 226); // Light Red (bg-red-100)
                        text = new Color(220, 38, 38); // Dark Red (text-red-600)
                    } else if (type.equalsIgnoreCase("Private")) {
                        bg = new Color(220, 252, 231); // Light Green (bg-green-100)
                        text = new Color(22, 163, 74); // Dark Green (text-green-600)
                    } else { // General
                        bg = new Color(219, 234, 254); // Light Blue (bg-blue-100)
                        text = new Color(37, 99, 235); // Dark Blue (text-blue-600)
                    }

                    // Selection override
                    if (isSelected) {
                        g2.setColor(table.getSelectionBackground());
                        g2.fillRect(0, 0, getWidth(), getHeight());
                    } else {
                        g2.setColor(Color.WHITE);
                        g2.fillRect(0, 0, getWidth(), getHeight());
                    }

                    // 2. Draw Rounded Badge (Centered)
                    int size = 40; // w-10 h-10 equivalent
                    int x = 10;    // Left padding
                    int y = (getHeight() - size) / 2;

                    g2.setColor(bg);
                    g2.fillRoundRect(x, y, size, size, 10, 10); // rounded-lg

                    // 3. Draw Text ID
                    g2.setColor(text);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    FontMetrics fm = g2.getFontMetrics();
                    int textX = x + (size - fm.stringWidth(roomId)) / 2;
                    int textY = y + ((size - fm.getHeight()) / 2) + fm.getAscent();
                    
                    g2.drawString(roomId, textX, textY);
                }
            };
            
            return panel;
        }
    }

    // --- Helper Methods ---

    private void performDelete() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a room first."); return; }
        
        // Note: Column 0 is the ID (Badge)
        String id = (String) table.getValueAt(row, 0);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Delete Room " + id + "?\nWarning: Any patient in this room will be unassigned.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            if (hmc.getRoomCtrl().deleteRoom(id)) {
                JOptionPane.showMessageDialog(this, "Room Deleted.");
                refreshTable(null);
            } else {
                JOptionPane.showMessageDialog(this, "Delete Failed.");
            }
        }
    }

    private void showContextMenu(MouseEvent e) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem editItem = new JMenuItem("Edit Room Type");
        JMenuItem deleteItem = new JMenuItem("Delete Room"); 
        
        editItem.addActionListener(ev -> showEditRoomDialog());
        deleteItem.addActionListener(ev -> performDelete());
        
        menu.add(editItem);
        menu.add(deleteItem);
        menu.show(e.getComponent(), e.getX(), e.getY());
    }

    private void performSearch() {
        String q = txtSearch.getText().trim();
        if (!q.isEmpty()) refreshTable(hmc.getRoomCtrl().searchRooms(q));
        else refreshTable(null);
    }

    public void refreshTable(List<Room> data) {
        tableModel.setRowCount(0);
        List<Room> rooms = (data == null) ? hmc.getRoomCtrl().getAllRooms() : data;
        for (Room r : rooms) {
            String status = r.isOccupied() ? "Occupied" : "Available";
            String patName = (r.getPatient() != null) ? r.getPatient().getName() : "-";
            // Column 0 is Room ID (Used by Renderer for Badge Text)
            // Column 1 is Type (Used by Renderer for Color)
            tableModel.addRow(new Object[]{r.getRoomId(), r.getRoomType(), status, patName});
        }
    }

    private void showAddRoomDialog() {
        JTextField idField = new JTextField();
        String[] types = {"General", "ICU", "Private"};
        JComboBox<String> typeBox = new JComboBox<>(types);
        Object[] message = { "Room ID:", idField, "Type:", typeBox };

        if (JOptionPane.showConfirmDialog(this, message, "Add Room", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            Room r = new Room(idField.getText(), (String)typeBox.getSelectedItem());
            if (hmc.getRoomCtrl().addRoom(r)) refreshTable(null);
            else JOptionPane.showMessageDialog(this, "Failed to add.");
        }
    }

    private void showEditRoomDialog() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a room first."); return; }
        
        String id = (String) table.getValueAt(row, 0);
        String currentType = (String) table.getValueAt(row, 1);
        
        String[] types = {"General", "ICU", "Private"};
        JComboBox<String> typeBox = new JComboBox<>(types);
        typeBox.setSelectedItem(currentType);
        
        Object[] message = { "ID: " + id, "Change Type:", typeBox };
        
        if (JOptionPane.showConfirmDialog(this, message, "Edit Room", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            Room r = new Room(id, (String)typeBox.getSelectedItem());
            if (hmc.getRoomCtrl().updateRoom(r)) {
                refreshTable(null);
                JOptionPane.showMessageDialog(this, "Updated!");
            }
        }
    }
}