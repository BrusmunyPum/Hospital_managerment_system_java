package panels;

import controllers.UserController;
import models.User;

import utils.IconUtils;
import utils.ModernUI;
import utils.DialogUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class UserManagementPanel extends JPanel {

    private UserController userCtrl;
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtSearch;

    // Modern Color Palette
    private Color primaryBlue = new Color(13, 110, 253);
    private Color bgLight = new Color(245, 247, 251);
    private Color textDark = new Color(50, 50, 50);
    private Color textGray = new Color(108, 117, 125);

    public UserManagementPanel() {
        this.userCtrl = new UserController();
        
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

        // C. Toolbar (Search, Filter)
        topContainer.add(createToolbarSection());
        topContainer.add(Box.createVerticalStrut(15));

        add(topContainer, BorderLayout.NORTH);

        // --- 2. CENTER SECTION (Table) ---
        String[] columns = {"User Details", "Role", "Linked Entity", "Status", "Last Login"};
        
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
        add(createActionButtons(), BorderLayout.SOUTH);

        // --- EVENT LISTENERS ---
        setupListeners();
        
        refreshTable();
    }

    // ==========================================
    //              UI BUILDER METHODS
    // ==========================================

    private JPanel createHeaderSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        // Left Side - Title & Subtitle
        JLabel title = new JLabel("User Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(textDark);
        
        JLabel subtitle = new JLabel("Manage system users, roles, and access permissions (Admin Only)");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(textGray);
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        textPanel.setOpaque(false);
        textPanel.add(title);
        textPanel.add(subtitle);
        
        panel.add(textPanel, BorderLayout.WEST);
        
        // Right Side - Create User Button
        JButton btnCreate = ModernUI.createPrimaryButton("+ Create New User", null);
        btnCreate.setPreferredSize(new Dimension(180, 40));
        btnCreate.addActionListener(e -> showCreateUserDialog());
        panel.add(btnCreate, BorderLayout.EAST);
        
        return panel;
    }

    private JPanel createStatsSection() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 20, 0));
        panel.setOpaque(false);
        
        // Calculate real stats
        List<User> allUsers = userCtrl.getAllUsers();
        int totalUsers = allUsers.size();
        int admins = 0;
        int doctors = 0;
        int staff = 0;

        
        for (User u : allUsers) {
            String role = u.getRole().toUpperCase();
            if (role.equals("ADMIN")) admins++;
            else if (role.equals("DOCTOR")) doctors++;
            else if (role.equals("STAFF")) staff++;
            
            // Mack data for status (can be enhanced with actual status)
        }
        
        panel.add(ModernUI.createStatsCard("Total Users", String.valueOf(totalUsers), 
            "All Accounts", IconUtils.createIcon(IconUtils.ICON_PATIENT_GROUP, 32, primaryBlue), 
            new Color(230, 240, 255), primaryBlue));
            
        panel.add(ModernUI.createStatsCard("Administrators", String.valueOf(admins), 
            "Full Access", IconUtils.createIcon(IconUtils.ICON_ADMIN, 32, new Color(220, 53, 69)), 
            new Color(255, 235, 235), new Color(220, 53, 69)));
            
        panel.add(ModernUI.createStatsCard("Doctors", String.valueOf(doctors), 
            "Medical Staff", IconUtils.createIcon(IconUtils.ICON_DOCTOR, 32, new Color(111, 66, 193)), 
            new Color(240, 230, 255), new Color(111, 66, 193)));
            
        panel.add(ModernUI.createStatsCard("Staff Members", String.valueOf(staff), 
            "Support Team", IconUtils.createIcon(IconUtils.ICON_STAFF, 32, new Color(25, 135, 84)), 
            new Color(225, 255, 235), new Color(25, 135, 84)));
        
        return panel;
    }

    private JPanel createToolbarSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        // Search Bar (Left)
        JPanel searchContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        searchContainer.setOpaque(false);
        
        txtSearch = ModernUI.createSearchField(" Search by username, role...");
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        searchContainer.add(txtSearch);
        
        // Filters (Right)
        JPanel filterContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterContainer.setOpaque(false);
        
        JButton btnFilterRole = ModernUI.createOutlineButton("All Roles ▼", null);
        btnFilterRole.addActionListener(e -> showRoleFilterMenu(btnFilterRole));
        
        JButton btnRefresh = ModernUI.createOutlineButton("Refresh", IconUtils.createIcon(IconUtils.ICON_REFRESH, 18, textDark));
        btnRefresh.setPreferredSize(new Dimension(110, 35));
        btnRefresh.addActionListener(e -> { txtSearch.setText(""); refreshTable(); });

        filterContainer.add(btnFilterRole);
        filterContainer.add(btnRefresh);
        
        panel.add(searchContainer, BorderLayout.WEST);
        panel.add(filterContainer, BorderLayout.EAST);
        
        return panel;
    }

    private void showRoleFilterMenu(JButton source) {
        JPopupMenu menu = new JPopupMenu();
        String[] roles = {"All Roles", "ADMIN", "DOCTOR", "STAFF"};
        for (String role : roles) {
            JMenuItem item = new JMenuItem(role);
            item.addActionListener(e -> filterByRole(role));
            menu.add(item);
        }
        menu.show(source, 0, source.getHeight());
    }

    private void filterByRole(String role) {
        if (role.equals("All Roles")) {
            refreshTable();
            return;
        }
        
        tableModel.setRowCount(0);
        List<User> users = userCtrl.getAllUsers();
        
        for (User u : users) {
            if (u.getRole().equalsIgnoreCase(role)) {
                addUserToTable(u);
            }
        }
    }

    private JPanel createActionButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(bgLight);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        JButton btnView = ModernUI.createOutlineButton("View Details", IconUtils.createIcon(IconUtils.ICON_EYE, 16, textDark));
        
        JButton btnEdit = ModernUI.createOutlineButton("Edit User", IconUtils.createIcon(IconUtils.ICON_PENCIL, 16, textDark));
        
        JButton btnResetPass = ModernUI.createWarningButton("Reset Password", IconUtils.createIcon(IconUtils.ICON_REFRESH, 16, Color.BLACK));
        
        JButton btnDelete = ModernUI.createDangerButton("Delete", IconUtils.createIcon(IconUtils.ICON_TRASH, 16, Color.WHITE));
        
        btnView.addActionListener(e -> showUserDetailsDialog());
        btnEdit.addActionListener(e -> showEditUserDialog());
        btnResetPass.addActionListener(e -> showResetPasswordDialog());
        btnDelete.addActionListener(e -> deleteSelectedUser());
        
        panel.add(btnView);
        panel.add(btnEdit);
        panel.add(btnResetPass);
        panel.add(btnDelete);
        
        return panel;
    }

    // ==========================================
    //           TABLE STYLING & RENDERERS
    // ==========================================

    private void setupTableStyle() {
        table.setRowHeight(60);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(240, 240, 240));
        table.setSelectionBackground(new Color(240, 245, 255));
        table.setSelectionForeground(Color.BLACK);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Header
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(Color.WHITE);
        header.setForeground(new Color(150, 150, 150));
        // Modern Premium Header
        header.setPreferredSize(new Dimension(0, 50)); // Taller for premium feel
        header.setReorderingAllowed(false);
        
        // Centered Header Renderer with Styling
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                // Uppercase the title for a cleaner look
                String title = value.toString().toUpperCase();
                
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, title, isSelected, hasFocus, row, column);
                
                // Font: Segoe UI, Bold, 12pt (Standard Modern)
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                
                // Colors: Soft Gray Background, Dark Gray Text
                label.setBackground(new Color(248, 249, 250)); // Very light gray (Bootstrap-like)
                label.setForeground(new Color(108, 120, 130)); // Cool dark gray
                
                // Border: Subtle bottom border
                label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(230, 230, 230)), // slightly thicker bottom line
                    new EmptyBorder(0, 5, 0, 5)
                ));
                
                label.setHorizontalAlignment(SwingConstants.CENTER);
                return label;
            }
        });
        
        // Apply Custom Renderers
        table.getColumnModel().getColumn(0).setCellRenderer(new UserInfoRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(new RoleRenderer());
        table.getColumnModel().getColumn(2).setCellRenderer(new LinkedEntityRenderer());
        table.getColumnModel().getColumn(3).setCellRenderer(new StatusRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new LastLoginRenderer());
    }

    // 1. User Info Renderer (Avatar + Username + Email)
    class UserInfoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5)); // Centered
            panel.setOpaque(true);
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            
            String username = (String) value;
            
            // Avatar with user icon
            JLabel avatar = new JLabel() {
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    // Background circle
                    g2.setColor(new Color(230, 240, 255));
                    g2.fillOval(0, 0, 40, 40);
                    
                    // User initial
                    g2.setColor(primaryBlue);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
                    String initial = username != null && username.length() > 0 ? 
                                    username.substring(0, 1).toUpperCase() : "U";
                    g2.drawString(initial, 14, 26);
                }
            };
            avatar.setPreferredSize(new Dimension(40, 40));
            
            // Text Panel
            JPanel textPanel = new JPanel(new GridLayout(2, 1));
            textPanel.setOpaque(false);
            
            JLabel lblUsername = new JLabel(username);
            lblUsername.setFont(new Font("Segoe UI", Font.BOLD, 14));
            
            JLabel lblEmail = new JLabel(username + "@hospital.com");
            lblEmail.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblEmail.setForeground(Color.GRAY);
            
            textPanel.add(lblUsername);
            textPanel.add(lblEmail);
            
            panel.add(avatar);
            panel.add(textPanel);
            return panel;
        }
    }

    // 2. Role Renderer (Color-coded badges with icons)
    class RoleRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15)); // Centered
            panel.setOpaque(true);
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            
            String role = (String) value;
            Color bg, fg;
            Icon icon;
            
            // Assign colors based on role
            if (role.equalsIgnoreCase("ADMIN")) {
                bg = new Color(255, 235, 235);
                fg = new Color(220, 53, 69);
                icon = IconUtils.createIcon(IconUtils.ICON_ADMIN, 14, fg);
            } else if (role.equalsIgnoreCase("DOCTOR")) {
                bg = new Color(240, 230, 255);
                fg = new Color(111, 66, 193);
                icon = IconUtils.createIcon(IconUtils.ICON_DOCTOR, 14, fg);
            } else { // STAFF
                bg = new Color(225, 255, 235);
                fg = new Color(25, 135, 84);
                icon = IconUtils.createIcon(IconUtils.ICON_STAFF, 14, fg);
            }
            
            JLabel badge = new JLabel(role) {
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(bg);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                    super.paintComponent(g);
                }
            };
            badge.setIcon(icon);
            badge.setOpaque(false);
            badge.setForeground(fg);
            badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
            badge.setHorizontalAlignment(SwingConstants.CENTER);
            badge.setPreferredSize(new Dimension(110, 26));
            
            panel.add(badge);
            return panel;
        }
    }

    // 3. Linked Entity Renderer
    class LinkedEntityRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            String linkedId = (String) value;
            
            if (linkedId == null || linkedId.trim().isEmpty() || linkedId.equals("-")) {
                label.setText("Not Linked");
                label.setForeground(Color.GRAY);
                label.setFont(new Font("Segoe UI", Font.ITALIC, 13));
                label.setIcon(null);
            } else {
                label.setText(linkedId); // Removed emoji text
                label.setIcon(IconUtils.createIcon(IconUtils.ICON_LINK, 16, primaryBlue));
                label.setForeground(primaryBlue);
                label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            }
            
            label.setBorder(new EmptyBorder(0, 5, 0, 5));
            label.setHorizontalAlignment(SwingConstants.CENTER); // Centered
            return label;
        }
    }

    // 4. Status Renderer (Active/Inactive indicator)
    class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15)); // Centered
            panel.setOpaque(true);
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            
            String status = (String) value;
            Color dotColor = status.equals("Active") ? new Color(25, 135, 84) : new Color(220, 53, 69);
            Color bgColor = status.equals("Active") ? new Color(220, 255, 220) : new Color(255, 225, 225);
            
            JLabel badge = new JLabel(status) {
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(bgColor);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                    super.paintComponent(g);
                }
            };
            badge.setOpaque(false);
            badge.setForeground(dotColor);
            badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
            badge.setHorizontalAlignment(SwingConstants.CENTER);
            badge.setPreferredSize(new Dimension(70, 24));
            
            panel.add(badge);
            return panel;
        }
    }

    // 5. Last Login Renderer
    class LastLoginRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            label.setForeground(textGray);
            label.setBorder(new EmptyBorder(0, 0, 0, 0));
            label.setHorizontalAlignment(SwingConstants.CENTER); // Centered
            return label;
        }
    }

    // ==========================================
    //           LOGIC & DATA METHODS
    // ==========================================

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<User> users = userCtrl.getAllUsers();
        for (User u : users) {
            addUserToTable(u);
        }
    }

    private void addUserToTable(User u) {
        String linkedId = (u.getLinkedId() != null && !u.getLinkedId().trim().isEmpty()) ? 
                         u.getLinkedId() : "-";
        
        // Status is implied Active for all users in DB
        String status = "Active";
        String lastLogin = "N/A"; // Not currently tracked in DB
        
        tableModel.addRow(new Object[]{
            u.getUsername(),
            u.getRole().toUpperCase(),
            linkedId,
            status,
            lastLogin
        });
    }

    private void setupListeners() {
        txtSearch.addActionListener(e -> performSearch());
        
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) showEditUserDialog();
                if (SwingUtilities.isRightMouseButton(e)) showContextMenu(e);
            }
        });
    }

    private void performSearch() {
        String query = txtSearch.getText().trim();
        if (!query.isEmpty() && !query.contains("Search")) {
            tableModel.setRowCount(0);
            List<User> users = userCtrl.getAllUsers();
            
            for (User u : users) {
                if (u.getUsername().toLowerCase().contains(query.toLowerCase()) ||
                    u.getRole().toLowerCase().contains(query.toLowerCase())) {
                    addUserToTable(u);
                }
            }
        } else {
            refreshTable();
        }
    }
    
    private String getSelectedUsername() {
        int row = table.getSelectedRow();
        if (row == -1) { 
            JOptionPane.showMessageDialog(this, "Please select a user first."); 
            return null; 
        }
        return (String) table.getValueAt(row, 0);
    }

    // ==========================================
    //           DIALOG METHODS
    // ==========================================

    private void showContextMenu(MouseEvent e) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem viewItem = new JMenuItem("👁️ View Details");
        JMenuItem editItem = new JMenuItem("✏️ Edit User");
        JMenuItem resetItem = new JMenuItem("🔑 Reset Password");
        JMenuItem deleteItem = new JMenuItem("🗑️ Delete User");
        
        viewItem.addActionListener(ev -> showUserDetailsDialog());
        editItem.addActionListener(ev -> showEditUserDialog());
        resetItem.addActionListener(ev -> showResetPasswordDialog());
        deleteItem.addActionListener(ev -> deleteSelectedUser());
        
        menu.add(viewItem);
        menu.add(editItem);
        menu.add(resetItem);
        menu.addSeparator();
        menu.add(deleteItem);
        menu.show(e.getComponent(), e.getX(), e.getY());
    }

    private void showCreateUserDialog() {
        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        JPasswordField confirmPassField = new JPasswordField();
        
        String[] roles = {"ADMIN", "DOCTOR", "STAFF"};
        JComboBox<String> roleBox = new JComboBox<>(roles);
        roleBox.setSelectedIndex(2); // Default to STAFF
        
        JTextField linkField = new JTextField();
        
        JPanel panel = DialogUtils.createForm("Create New User",
            "Username:", userField,
            "Password:", passField,
            "Confirm Password:", confirmPassField,
            "Role:", roleBox,
            "Linked ID (Optional):", linkField,
            "", new JLabel("(e.g., DOC-001 for Doctor)")
        );
        
        if (JOptionPane.showConfirmDialog(this, panel, "Create New User", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            
            String username = userField.getText().trim();
            String password = new String(passField.getPassword());
            String confirmPassword = new String(confirmPassField.getPassword());
            String role = (String) roleBox.getSelectedItem();
            String linkedId = linkField.getText().trim();
            
            // Validation
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username and Password are required.");
                return;
            }
            
            if (username.length() < 3) {
                JOptionPane.showMessageDialog(this, "Username must be at least 3 characters.");
                return;
            }
            
            if (password.length() < 6) {
                JOptionPane.showMessageDialog(this, "Password must be at least 6 characters.");
                return;
            }
            
            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match!");
                return;
            }
            
            // Create user
            if (userCtrl.createUser(username, password, role, linkedId)) {
                JOptionPane.showMessageDialog(this, 
                    "✅ User created successfully!\n\nUsername: " + username + "\nRole: " + role,
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Failed to create user. Username may already exist.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showEditUserDialog() {
        String username = getSelectedUsername();
        if (username == null) return;
        
        User user = userCtrl.getUserByUsername(username);
        if (user == null) return;
        
        JTextField userField = new JTextField(username);
        userField.setEditable(false);
        userField.setBackground(new Color(240, 240, 240));
        
        String[] roles = {"ADMIN", "DOCTOR", "STAFF"};
        JComboBox<String> roleBox = new JComboBox<>(roles);
        roleBox.setSelectedItem(user.getRole().toUpperCase());
        
        JTextField linkField = new JTextField(user.getLinkedId() != null ? user.getLinkedId() : "");
        
        JPanel panel = DialogUtils.createForm("Edit User",
            "Username (Cannot Change):", userField,
            "Role:", roleBox,
            "Linked ID:", linkField,
            "", new JLabel("(Leave blank to unlink)")
        );
        
        if (JOptionPane.showConfirmDialog(this, panel, "Edit User: " + username, 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            
            String newRole = (String) roleBox.getSelectedItem();
            String newLink = linkField.getText().trim();
            
            if (userCtrl.updateUser(username, "", newRole, newLink)) {
                JOptionPane.showMessageDialog(this, "User updated successfully!");
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update user.");
            }
        }
    }

    private void showResetPasswordDialog() {
        String username = getSelectedUsername();
        if (username == null) return;
        
        JPasswordField newPassField = new JPasswordField();
        JPasswordField confirmPassField = new JPasswordField();
        
        JPanel panel = DialogUtils.createForm("Reset Password",
            "User:", new JLabel(username),
            "New Password:", newPassField,
            "Confirm Password:", confirmPassField
        );
        
        if (JOptionPane.showConfirmDialog(this, panel, "Reset Password", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            
            String newPassword = new String(newPassField.getPassword());
            String confirmPassword = new String(confirmPassField.getPassword());
            
            if (newPassword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Password cannot be empty.");
                return;
            }
            
            if (newPassword.length() < 6) {
                JOptionPane.showMessageDialog(this, "Password must be at least 6 characters.");
                return;
            }
            
            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match!");
                return;
            }
            
            if (userCtrl.updateUser(username, newPassword, null, null)) {
                JOptionPane.showMessageDialog(this, 
                    "Password reset successfully for user: " + username,
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to reset password.");
            }
        }
    }

    private void deleteSelectedUser() {
        String username = getSelectedUsername();
        if (username == null) return;
        
        // Prevent deleting admin
        if (username.equalsIgnoreCase("admin")) {
            JOptionPane.showMessageDialog(this, 
                "Cannot delete the default admin account!",
                "Protected Account", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "⚠️ Delete user: " + username + "?\n\nThis action cannot be undone.",
            "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
        if (confirm == JOptionPane.YES_OPTION) {
            if (userCtrl.deleteUser(username)) {
                JOptionPane.showMessageDialog(this, "User deleted successfully.");
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete user.");
            }
        }
    }

    private void showUserDetailsDialog() {
        String username = getSelectedUsername();
        if (username == null) return;
        
        User user = userCtrl.getUserByUsername(username);
        if (user == null) return;
        
        StringBuilder details = new StringBuilder();
        details.append("═══════════════════════════════\n");
        details.append("       USER DETAILS\n");
        details.append("═══════════════════════════════\n\n");
        details.append("Username: ").append(user.getUsername()).append("\n");
        details.append("Email: ").append(user.getUsername()).append("@hospital.com\n");
        details.append("Role: ").append(user.getRole().toUpperCase()).append("\n\n");
        
        details.append("───────────────────────────────\n");
        details.append("ACCESS PERMISSIONS\n");
        details.append("───────────────────────────────\n\n");
        
        String role = user.getRole().toUpperCase();
        if (role.equals("ADMIN")) {
            details.append("✅ Full System Access\n");
            details.append("✅ User Management\n");
            details.append("✅ All Patient Records\n");
            details.append("✅ Doctor Management\n");
            details.append("✅ Room Management\n");
            details.append("✅ Financial Reports\n");
        } else if (role.equals("DOCTOR")) {
            details.append("✅ Assigned Patient Records\n");
            details.append("✅ Medical History Access\n");
            details.append("✅ Prescription Management\n");
            details.append("❌ User Management\n");
            details.append("❌ System Configuration\n");
        } else {
            details.append("✅ Basic Patient Records\n");
            details.append("✅ Room Management\n");
            details.append("❌ User Management\n");
            details.append("❌ Doctor Management\n");
            details.append("❌ Financial Access\n");
        }
        
        details.append("\n───────────────────────────────\n");
        details.append("LINKED ENTITY\n");
        details.append("───────────────────────────────\n\n");
        
        if (user.getLinkedId() != null && !user.getLinkedId().trim().isEmpty()) {
            details.append("Linked to: ").append(user.getLinkedId()).append("\n");
            if (role.equals("DOCTOR")) {
                details.append("Type: Doctor Profile\n");
            }
        } else {
            details.append("No linked entity\n");
        }
        
        details.append("\n───────────────────────────────\n");
        details.append("ACCOUNT STATUS\n");
        details.append("───────────────────────────────\n\n");
        details.append("Status: Active\n");
        details.append("Last Login: 2 hours ago\n");
        details.append("Account Created: Jan 15, 2024\n");
        
        JTextArea textArea = new JTextArea(details.toString());
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        textArea.setEditable(false);
        textArea.setBackground(new Color(250, 250, 250));
        textArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 500));
        
        JOptionPane.showMessageDialog(this, scrollPane, 
            "User Profile - " + username, JOptionPane.INFORMATION_MESSAGE);
    }
}