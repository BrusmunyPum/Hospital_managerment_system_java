package panels;

import controllers.HospitalManagementController;
import models.Doctor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

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
        String[] columns = {"ID", "Name", "Specialization"};
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
        JButton btnDelete = new JButton("Delete"); // NEW
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
            btnAdd.addActionListener(e -> showAddDoctorDialog());
            btnEdit.addActionListener(e -> showEditDoctorDialog());
            btnDelete.addActionListener(e -> performDelete()); // NEW
        }

        refreshTable(null);
    }
    
    // --- NEW: Delete Logic ---
    private void performDelete() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a doctor first."); return; }
        
        String id = (String) table.getValueAt(row, 0);
        String name = (String) table.getValueAt(row, 1);
        
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
        JMenuItem deleteItem = new JMenuItem("Delete Doctor"); // NEW
        
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
            tableModel.addRow(new Object[]{d.getDoctorId(), d.getName(), d.getSpecialization()});
        }
    }

    private void showAddDoctorDialog() {
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField specField = new JTextField();
        Object[] message = { "ID:", idField, "Name:", nameField, "Specialization:", specField };

        if (JOptionPane.showConfirmDialog(this, message, "Add Doctor", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            Doctor d = new Doctor(idField.getText(), nameField.getText(), specField.getText());
            if (hmc.getDoctorCtrl().addDoctor(d)) refreshTable(null);
            else JOptionPane.showMessageDialog(this, "Failed to add.");
        }
    }

    private void showEditDoctorDialog() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a doctor first."); return; }
        
        String id = (String) table.getValueAt(row, 0);
        Doctor d = hmc.getDoctorCtrl().findDoctorById(id);
        
        JTextField nameField = new JTextField(d.getName());
        JTextField specField = new JTextField(d.getSpecialization());
        
        Object[] message = { "ID: " + id, "Name:", nameField, "Specialization:", specField };
        
        if (JOptionPane.showConfirmDialog(this, message, "Edit Doctor", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            d.setName(nameField.getText());
            d.setSpecialization(specField.getText());
            if (hmc.getDoctorCtrl().updateDoctor(d)) {
                refreshTable(null);
                JOptionPane.showMessageDialog(this, "Updated!");
            }
        }
    }
}