package panels;

import controllers.UserController;
import models.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UserManagementPanel extends JPanel {

    private UserController userCtrl;
    private DefaultTableModel tableModel;
    private JTable table;

    public UserManagementPanel() {
        this.userCtrl = new UserController();
        setLayout(new BorderLayout());

        // --- Header ---
        JLabel title = new JLabel("User Management (Admin Only)");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(title, BorderLayout.NORTH);

        // --- Table ---
        String[] columns = {"Username", "Role", "Linked ID"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // --- Buttons ---
        JPanel btnPanel = new JPanel();
        JButton btnAdd = new JButton("Create User");
        JButton btnDelete = new JButton("Delete User");
        JButton btnRefresh = new JButton("Refresh");

        btnPanel.add(btnAdd);
        btnPanel.add(btnDelete);
        btnPanel.add(btnRefresh);
        add(btnPanel, BorderLayout.SOUTH);

        // --- Actions ---
        btnAdd.addActionListener(e -> showCreateUserDialog());
        btnDelete.addActionListener(e -> deleteSelectedUser());
        btnRefresh.addActionListener(e -> refreshTable());

        refreshTable();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<User> users = userCtrl.getAllUsers();
        for (User u : users) {
            tableModel.addRow(new Object[]{u.getUsername(), u.getRole(), u.getLinkedId()});
        }
    }

    private void showCreateUserDialog() {
        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        String[] roles = {"ADMIN", "DOCTOR", "STAFF"};
        JComboBox<String> roleBox = new JComboBox<>(roles);
        JTextField linkField = new JTextField();
        linkField.setToolTipText("Enter Doctor ID (e.g., DOC-001) if role is DOCTOR");

        Object[] message = {
            "Username:", userField,
            "Password:", passField,
            "Role:", roleBox,
            "Linked ID (Optional):", linkField,
            "(Enter Doctor ID like 'DOC-001')"
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Create New User", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String u = userField.getText().trim();
            String p = new String(passField.getPassword());
            String r = (String) roleBox.getSelectedItem();
            String l = linkField.getText().trim();

            if (u.isEmpty() || p.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username and Password required.");
                return;
            }

            if (userCtrl.createUser(u, p, r, l)) {
                JOptionPane.showMessageDialog(this, "User Created!");
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed. Username might exist.");
            }
        }
    }

    private void deleteSelectedUser() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a user.");
            return;
        }
        String username = (String) table.getValueAt(row, 0);
        
        // Prevent deleting yourself (basic check)
        if (username.equalsIgnoreCase("admin")) {
             JOptionPane.showMessageDialog(this, "Cannot delete default Admin.");
             return;
        }

        if (JOptionPane.showConfirmDialog(this, "Delete user " + username + "?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            userCtrl.deleteUser(username);
            refreshTable();
        }
    }
}