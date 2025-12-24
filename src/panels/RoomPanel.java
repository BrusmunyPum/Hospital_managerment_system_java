package panels;

import controllers.HospitalManagementController;
import models.Room;

import javax.swing.*;
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
        String[] columns = {"ID", "Type", "Status", "Patient"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
        JButton btnDelete = new JButton("Delete Room"); // NEW
        btnDelete.setForeground(Color.RED);
        
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnDelete); // NEW
        
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
            btnDelete.addActionListener(e -> performDelete()); // NEW
        }

        refreshTable(null);
    }
    
    // --- NEW: Delete Logic ---
    private void performDelete() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a room first."); return; }
        
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
        JMenuItem deleteItem = new JMenuItem("Delete Room"); // NEW
        
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