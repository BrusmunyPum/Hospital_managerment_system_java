package panels;

import controllers.HospitalManagementController;
import models.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class PatientPanel extends JPanel {

    private HospitalManagementController hmc;
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtSearch;
    
    private User currentUser;       
    private String doctorFilterId;  // Stores the ID of the doctor (if logged in as doctor)

    // --- UPDATED CONSTRUCTOR ---
    // Accepts HMC, User, and the doctorId to filter by (or null)
    public PatientPanel(HospitalManagementController hmc, User user, String doctorId) {
        this.hmc = hmc;
        this.currentUser = user;
        this.doctorFilterId = doctorId; 
        
        setLayout(new BorderLayout());

        // --- TOP (Search) ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtSearch = new JTextField(20);
        JButton btnSearch = new JButton("Search");
        JButton btnShowAll = new JButton("Show All");
        topPanel.add(new JLabel("Search Patient:"));
        topPanel.add(txtSearch);
        topPanel.add(btnSearch);
        topPanel.add(btnShowAll);
        add(topPanel, BorderLayout.NORTH);

        // --- CENTER (Table) ---
        String[] columns = {"ID", "Name", "Age", "Address", "Doctor", "Room"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);
        
        // --- MOUSE LISTENERS ---
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) showEditPatientDialog();
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = table.rowAtPoint(e.getPoint());
                    if (row >= 0 && row < table.getRowCount()) {
                        table.setRowSelectionInterval(row, row);
                        showContextMenu(e);
                    }
                }
            }
        });

        // --- BOTTOM (Buttons) ---
        JPanel buttonPanel = new JPanel();
        JButton btnAdd = new JButton("Add");
        JButton btnEdit = new JButton("Edit"); 
        JButton btnAssignDoc = new JButton("Assign Dr.");
        JButton btnAssignRoom = new JButton("Assign Room");
        JButton btnBill = new JButton("Generate Bill");
        btnBill.setBackground(new Color(255, 204, 0)); 
        
        JButton btnDischarge = new JButton("Discharge");
        btnDischarge.setForeground(Color.RED);

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnAssignDoc);
        buttonPanel.add(btnAssignRoom);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(btnBill); 
        buttonPanel.add(btnDischarge);

        // --- PERMISSIONS LOGIC ---
        // If Doctor, disable administrative buttons
        if (currentUser.isDoctor()) {
            btnAdd.setEnabled(false); 
            btnAssignDoc.setEnabled(false); 
            btnAssignRoom.setEnabled(false);
        }

        add(buttonPanel, BorderLayout.SOUTH);

        // --- LISTENERS ---
        btnSearch.addActionListener(e -> performSearch());
        btnShowAll.addActionListener(e -> { txtSearch.setText(""); refreshTable(null); });
        txtSearch.addActionListener(e -> performSearch()); 

        // Only add admin listeners if NOT a doctor
        if (!currentUser.isDoctor()) {
            btnAdd.addActionListener(e -> showAddPatientDialog());
            btnAssignDoc.addActionListener(e -> {
                String pid = getSelectedId(0);
                if (pid != null) showAssignDoctorDialog(pid);
            });
            btnAssignRoom.addActionListener(e -> {
                String pid = getSelectedId(0);
                if (pid != null) showAssignRoomDialog(pid);
            });
        }
        
        btnEdit.addActionListener(e -> showEditPatientDialog());
        btnBill.addActionListener(e -> showBillDialog());
        btnDischarge.addActionListener(e -> performDischarge());

        refreshTable(null);
    }
    
    // --- UPDATED REFRESH LOGIC ---
    public void refreshTable(List<Patient> data) {
        tableModel.setRowCount(0);
        List<Patient> patients;

        if (data != null) {
            patients = data; // Use search results
        } else {
            // IF DOCTOR: Show ONLY my patients
            // We check if the user is a doctor AND we have a valid doctor ID filter
            if (currentUser.isDoctor() && doctorFilterId != null) {
                // Call the filtered method (Make sure this exists in PatientController!)
                patients = hmc.getPatientCtrl().getPatientsByDoctorId(doctorFilterId);
            } else {
                // IF ADMIN/STAFF: Show Everyone
                patients = hmc.getPatientCtrl().getAllPatients();
            }
        }
        
        for (Patient p : patients) {
            String docName = (p.getDoctor() != null) ? p.getDoctor().getName() : "None";
            String roomName = (p.getRoom() != null) ? p.getRoom().getRoomId() : "None";
            tableModel.addRow(new Object[]{
                p.getPatientId(), p.getName(), p.getAge(), p.getAddress(), docName, roomName
            });
        }
    }

    private void performSearch() {
        String query = txtSearch.getText().trim();
        if (!query.isEmpty()) {
            List<Patient> results = hmc.getPatientCtrl().searchPatients(query);
            // If doctor, we might want to filter search results too, but simple search is fine for now
            refreshTable(results);
        } else {
            refreshTable(null);
        }
    }

    // --- Helper Methods ---

    private String getSelectedId(int colIndex) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row first.");
            return null;
        }
        return (String) table.getValueAt(row, colIndex);
    }

    private void showContextMenu(MouseEvent e) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem billItem = new JMenuItem("Generate Bill"); 
        JMenuItem editItem = new JMenuItem("Edit Patient");
        JMenuItem deleteItem = new JMenuItem("Discharge Patient");

        billItem.addActionListener(ev -> showBillDialog());
        editItem.addActionListener(ev -> showEditPatientDialog());
        deleteItem.addActionListener(ev -> performDischarge());

        menu.add(billItem);
        menu.addSeparator();
        menu.add(editItem);
        
        // Only allow discharge if permitted (Admin/Staff/Doctor can all discharge usually)
        menu.add(deleteItem);
        
        menu.show(e.getComponent(), e.getX(), e.getY());
    }

    private void showBillDialog() {
        String patientId = getSelectedId(0);
        if (patientId == null) return;
        
        String inputDate = JOptionPane.showInputDialog(this, "Enter Discharge Date (YYYY-MM-DD):", LocalDate.now().toString());
        if (inputDate != null && !inputDate.trim().isEmpty()) {
            try {
                LocalDate dischargeDate = LocalDate.parse(inputDate);
                String invoice = hmc.getPatientCtrl().generateBill(patientId, dischargeDate);
                JTextArea textArea = new JTextArea(invoice);
                textArea.setFont(new Font("Monospaced", Font.BOLD, 14));
                textArea.setEditable(false);
                JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Hospital Invoice", JOptionPane.INFORMATION_MESSAGE);
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, "Invalid Date Format.");
            }
        }
    }

    private void performDischarge() {
        String patientId = getSelectedId(0);
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
        Object[] message = { "ID:", idField, "Name:", nameField, "Age:", ageField, "Address:", addrField, "Medical History:", historyField };
        if (JOptionPane.showConfirmDialog(this, message, "Add Patient", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
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
        String id = getSelectedId(0);
        if (id == null) return;
        Patient p = hmc.getPatientCtrl().findPatientById(id);
        if (p == null) return;
        JTextField nameField = new JTextField(p.getName());
        JTextField ageField = new JTextField(String.valueOf(p.getAge()));
        JTextField addrField = new JTextField(p.getAddress());
        JTextField historyField = new JTextField(p.getMedicalHistory());
        Object[] message = { "ID: " + id, "Name:", nameField, "Age:", ageField, "Address:", addrField, "History:", historyField };
        if (JOptionPane.showConfirmDialog(this, message, "Edit Patient", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
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