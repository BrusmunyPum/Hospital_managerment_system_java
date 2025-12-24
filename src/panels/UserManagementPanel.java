package panels;

import controllers.UserController;
import models.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // --- Mouse Listener (Double Click to Edit) ---
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) showEditUserDialog();
            }
        });

        // --- Buttons ---
        JPanel btnPanel = new JPanel();
        JButton btnAdd = new JButton("Create User");
        JButton btnEdit = new JButton("Edit User"); // NEW
        JButton btnDelete = new JButton("Delete User");
        JButton btnRefresh = new JButton("Refresh");

        btnDelete.setForeground(Color.RED);

        btnPanel.add(btnAdd);
        btnPanel.add(btnEdit); // NEW
        btnPanel.add(btnDelete);
        btnPanel.add(btnRefresh);
        add(btnPanel, BorderLayout.SOUTH);

        // --- Actions ---
        btnAdd.addActionListener(e -> showCreateUserDialog());
        btnEdit.addActionListener(e -> showEditUserDialog()); // NEW
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

    // --- Create User Dialog ---
    private void showCreateUserDialog() {
        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        String[] roles = {"ADMIN", "DOCTOR", "STAFF"};
        JComboBox<String> roleBox = new JComboBox<>(roles);
        JTextField linkField = new JTextField();
        linkField.setToolTipText("Enter Doctor ID (e.g., DOC-001)");

        Object[] message = {
            "Username:", userField,
            "Password:", passField,
            "Role:", roleBox,
            "Linked ID (Optional):", linkField
        };

        if (JOptionPane.showConfirmDialog(this, message, "Create New User", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String u = userField.getText().trim();
            String p = new String(passField.getPassword());
            String r = (String) roleBox.getSelectedItem();
            String l = linkField.getText().trim();

            if (u.isEmpty() || p.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username and Password required."); return;
            }

            if (userCtrl.createUser(u, p, r, l)) {
                JOptionPane.showMessageDialog(this, "User Created!");
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed. Username might exist.");
            }
        }
    }

    // --- NEW: Edit User Dialog ---
    private void showEditUserDialog() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a user first."); return; }

        // Get current values from table
        String currentUsername = (String) table.getValueAt(row, 0);
        String currentRole = (String) table.getValueAt(row, 1);
        String currentLink = (String) table.getValueAt(row, 2);

        // Fields
        JTextField userField = new JTextField(currentUsername);
        userField.setEditable(false); // Cannot change Username
        JPasswordField passField = new JPasswordField();
        
        String[] roles = {"ADMIN", "DOCTOR", "STAFF"};
        JComboBox<String> roleBox = new JComboBox<>(roles);
        roleBox.setSelectedItem(currentRole);
        
        JTextField linkField = new JTextField(currentLink);

        Object[] message = {
            "Username (Cannot Change):", userField,
            "New Password (Leave blank to keep):", passField,
            "Role:", roleBox,
            "Linked ID:", linkField
        };

        if (JOptionPane.showConfirmDialog(this, message, "Edit User", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String newPass = new String(passField.getPassword());
            String newRole = (String) roleBox.getSelectedItem();
            String newLink = linkField.getText().trim();

            if (userCtrl.updateUser(currentUsername, newPass, newRole, newLink)) {
                JOptionPane.showMessageDialog(this, "User Updated!");
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this, "Update Failed.");
            }
        }
    }

    private void deleteSelectedUser() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a user."); return; }
        String username = (String) table.getValueAt(row, 0);
        
        if (username.equalsIgnoreCase("admin")) {
             JOptionPane.showMessageDialog(this, "Cannot delete default Admin."); return;
        }

        if (JOptionPane.showConfirmDialog(this, "Delete user " + username + "?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            userCtrl.deleteUser(username);
            refreshTable();
        }
    }
}