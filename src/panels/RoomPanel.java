package panels;

import controllers.HospitalManagementController;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import models.*;
import utils.IconUtils;
import utils.ModernUI;
import utils.DialogUtils;

public class RoomPanel extends JPanel {

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

    public RoomPanel(HospitalManagementController hmc, boolean isAdmin) {
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
        String[] columns = {"Room Details", "Room Type", "Status", "Occupant", "Rate/Day"};
        
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
        
        JLabel title = new JLabel("Room Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(textDark);
        
        JLabel subtitle = new JLabel("Monitor room availability, occupancy, and manage hospital accommodations.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(textGray);
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        textPanel.setOpaque(false);
        textPanel.add(title);
        textPanel.add(subtitle);
        
        panel.add(textPanel, BorderLayout.WEST);
        
        // "Add New Room" Button (Top Right) - Admin Only
        if (isAdmin) {
            JButton btnAdd = ModernUI.createPrimaryButton("+ Add New Room", null);
            btnAdd.setPreferredSize(new Dimension(180, 40));
            btnAdd.addActionListener(e -> showAddRoomDialog());
            panel.add(btnAdd, BorderLayout.EAST);
        }
        
        return panel;
    }

    private JPanel createStatsSection() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 20, 0));
        panel.setOpaque(false);
        
        // Calculate real stats
        List<Room> allRooms = hmc.getRoomCtrl().getAllRooms();
        int totalRooms = allRooms.size();
        int available = 0;
        int occupied = 0;
        int icu = 0;

        
        for (Room r : allRooms) {
            if (r.isOccupied()) occupied++;
            else available++;
            
            String type = r.getRoomType().toLowerCase();
            if (type.contains("icu")) icu++;
        }
        
        double occupancyRate = totalRooms > 0 ? (occupied * 100.0 / totalRooms) : 0;
        
        panel.add(ModernUI.createStatsCard("Total Rooms", String.valueOf(totalRooms), 
            "Facility", IconUtils.createIcon(IconUtils.ICON_HOSPITAL, 32, primaryBlue), 
            new Color(230, 240, 255), primaryBlue));
            
        panel.add(ModernUI.createStatsCard("Available", String.valueOf(available), 
            available > 10 ? "Capacity OK" : "Low", 
            IconUtils.createIcon(IconUtils.ICON_DOOR, 32, new Color(25, 135, 84)), 
            new Color(225, 255, 235), new Color(25, 135, 84)));
            
        panel.add(ModernUI.createStatsCard("Occupied", String.valueOf(occupied), 
            String.format("%.0f%%", occupancyRate), 
            IconUtils.createIcon(IconUtils.ICON_BED, 32, new Color(255, 193, 7)), 
            new Color(255, 245, 220), new Color(255, 193, 7)));
            
        panel.add(ModernUI.createStatsCard("ICU Rooms", String.valueOf(icu), 
            "Critical Care", 
            IconUtils.createIcon(IconUtils.ICON_ALERT, 32, new Color(220, 53, 69)), 
            new Color(255, 235, 235), new Color(220, 53, 69)));
        
        return panel;
    }

    private JPanel createToolbarSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        // Search Bar (Left)
        JPanel searchContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        searchContainer.setOpaque(false);
        
        txtSearch = ModernUI.createSearchField(" Search by room ID, type...");
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        searchContainer.add(txtSearch);
        
        // Filters (Right)
        JPanel filterContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterContainer.setOpaque(false);
        
        JButton btnFilterType = ModernUI.createOutlineButton("All Types ▼", null);
        btnFilterType.addActionListener(e -> showFilterMenu(btnFilterType));
        
        JButton btnFilterStatus = ModernUI.createOutlineButton("All Status ▼", null);
        btnFilterStatus.addActionListener(e -> showStatusFilterMenu(btnFilterStatus));
        
        // Show All / Refresh Button
        JButton btnRefresh = ModernUI.createOutlineButton("Refresh", IconUtils.createIcon(IconUtils.ICON_REFRESH, 18, textDark));
        btnRefresh.setPreferredSize(new Dimension(110, 35));
        btnRefresh.addActionListener(e -> { txtSearch.setText(""); refreshTable(null); });

        filterContainer.add(btnFilterType);
        filterContainer.add(btnFilterStatus);
        filterContainer.add(btnRefresh);
        
        panel.add(searchContainer, BorderLayout.WEST);
        panel.add(filterContainer, BorderLayout.EAST);
        
        return panel;
    }

    private void showFilterMenu(JButton source) {
        JPopupMenu menu = new JPopupMenu();
        String[] types = {"All Types", "General", "ICU", "Private"};
        for (String type : types) {
            JMenuItem item = new JMenuItem(type);
            item.addActionListener(e -> filterByType(type));
            menu.add(item);
        }
        menu.show(source, 0, source.getHeight());
    }

    private void showStatusFilterMenu(JButton source) {
        JPopupMenu menu = new JPopupMenu();
        String[] statuses = {"All Status", "Available", "Occupied"};
        for (String status : statuses) {
            JMenuItem item = new JMenuItem(status);
            item.addActionListener(e -> filterByStatus(status));
            menu.add(item);
        }
        menu.show(source, 0, source.getHeight());
    }

    private void filterByType(String type) {
        if (type.equals("All Types")) {
            refreshTable(null);
            return;
        }
        List<Room> filtered = hmc.getRoomCtrl().getAllRooms().stream()
            .filter(r -> r.getRoomType().equalsIgnoreCase(type))
            .collect(java.util.stream.Collectors.toList());
        refreshTable(filtered);
    }

    private void filterByStatus(String status) {
        if (status.equals("All Status")) {
            refreshTable(null);
            return;
        }
        List<Room> filtered = hmc.getRoomCtrl().getAllRooms().stream()
            .filter(r -> status.equals("Available") ? !r.isOccupied() : r.isOccupied())
            .collect(java.util.stream.Collectors.toList());
        refreshTable(filtered);
    }

    private JPanel createActionButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(bgLight);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        JButton btnEdit = ModernUI.createOutlineButton("Edit Room", IconUtils.createIcon(IconUtils.ICON_PENCIL, 16, textDark));
        
        JButton btnView = ModernUI.createOutlineButton("View Details", IconUtils.createIcon(IconUtils.ICON_EYE, 16, textDark));
        
        JButton btnClear = ModernUI.createWarningButton("Clear Room", IconUtils.createIcon(IconUtils.ICON_DOOR, 16, Color.BLACK));
        
        JButton btnDelete = ModernUI.createDangerButton("Delete", IconUtils.createIcon(IconUtils.ICON_TRASH, 16, Color.WHITE));
        
        btnEdit.addActionListener(e -> showEditRoomDialog());
        btnView.addActionListener(e -> showRoomDetailsDialog());
        btnClear.addActionListener(e -> performClearRoom());
        btnDelete.addActionListener(e -> performDelete());
        
        panel.add(btnView);
        panel.add(btnEdit);
        panel.add(btnClear);
        panel.add(btnDelete);
        
        return panel;
    }

    // ==========================================
    //           TABLE STYLING & RENDERERS
    // ==========================================

    private void setupTableStyle() {
        ModernUI.setupTableStyle(table);
        table.setRowHeight(60); // Keep taller rows for icons

        // Apply Custom Renderers
        table.getColumnModel().getColumn(0).setCellRenderer(new RoomInfoRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(new RoomTypeRenderer());
        table.getColumnModel().getColumn(2).setCellRenderer(new StatusRenderer());
        table.getColumnModel().getColumn(3).setCellRenderer(new OccupantRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new RateRenderer());
    }

    // 1. Room Info Renderer (Icon + Room ID)
    // 1. Room Info Renderer (Icon + Room ID)
    class RoomInfoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            // "justify-content: center" -> FlowLayout.CENTER
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 8)); 
            panel.setOpaque(true);
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            
            String roomId = (String) value;
            
            // Room Icon Background
            Color iconBg = new Color(225, 240, 255);
            Color iconFg = new Color(13, 110, 253);

            // Icon
            JLabel icon = new JLabel() {
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(iconBg);
                    g2.fillRoundRect(0, 0, 44, 44, 12, 12); 
                    
                    Icon symbolIcon;
                    if (roomId.contains("ICU")) symbolIcon = IconUtils.createIcon(IconUtils.ICON_ALERT, 24, iconFg);
                    else if (roomId.contains("VIP")) symbolIcon = IconUtils.createIcon(IconUtils.ICON_STAR, 24, iconFg);
                    else symbolIcon = IconUtils.createIcon(IconUtils.ICON_HOSPITAL, 24, iconFg);
                    
                    symbolIcon.paintIcon(this, g, (getWidth()-symbolIcon.getIconWidth())/2, (getHeight()-symbolIcon.getIconHeight())/2);
                }
            };
            icon.setPreferredSize(new Dimension(44, 44));
            
            // Text Panel
            JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 2)); 
            textPanel.setOpaque(false);
            
            JLabel lblRoom = new JLabel("Room " + roomId);
            lblRoom.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lblRoom.setForeground(isSelected ? table.getSelectionForeground() : textDark);
            
            JLabel lblFloor = new JLabel(getFloorInfo(roomId));
            lblFloor.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblFloor.setForeground(isSelected ? table.getSelectionForeground() : textGray);
            
            textPanel.add(lblRoom);
            textPanel.add(lblFloor);
            
            panel.add(icon);
            panel.add(textPanel);
            return panel;
        }
        
        private String getFloorInfo(String roomId) {
            if (roomId != null && roomId.length() >= 3 && Character.isDigit(roomId.charAt(0))) {
                 return "Building A • Floor " + roomId.charAt(0);
            }
            return "Building A";
        }
    }

    // 2. Room Type Renderer (Colored badge)
    class RoomTypeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            // "justify-content: center" -> FlowLayout.CENTER
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
            panel.setOpaque(true);
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            
            String type = (String) value;
            Color bg, fg;
            Icon icon;
            
            if (type.toLowerCase().contains("icu")) {
                bg = new Color(255, 235, 235);
                fg = new Color(220, 53, 69);
                icon = IconUtils.createIcon(IconUtils.ICON_ALERT, 14, fg);
            } else if (type.toLowerCase().contains("private")) {
                bg = new Color(240, 230, 255);
                fg = new Color(111, 66, 193);
                icon = IconUtils.createIcon(IconUtils.ICON_STAR, 14, fg);
            } else {
                bg = new Color(225, 240, 255);
                fg = new Color(13, 110, 253);
                icon = IconUtils.createIcon(IconUtils.ICON_HOSPITAL, 14, fg);
            }
            
            JLabel badge = new JLabel(type) {
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
            badge.setPreferredSize(new Dimension(120, 26));
            
            panel.add(badge);
            return panel;
        }
    }

    // 3. Status Renderer (Available/Occupied indicator)
    class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            // "justify-content: center" -> FlowLayout.CENTER
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
            panel.setOpaque(true);
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            
            String status = (String) value;
            Color dotColor = status.equals("Available") ? new Color(25, 135, 84) : new Color(220, 53, 69);
            Color bgColor = status.equals("Available") ? new Color(220, 255, 220) : new Color(255, 225, 225);
            Color fgColor = dotColor;
            
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
            badge.setForeground(fgColor);
            badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
            badge.setHorizontalAlignment(SwingConstants.CENTER);
            badge.setPreferredSize(new Dimension(90, 24));
            
            panel.add(badge);
            return panel;
        }
    }

    // 4. Occupant Renderer
    class OccupantRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String occupant = (String) value;
            
            if (occupant.equals("-") || occupant.equals("Empty")) {
                label.setForeground(Color.GRAY);
                label.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            } else {
                label.setForeground(textDark);
                label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            }
            
            label.setHorizontalAlignment(CENTER); // Center align to match header
            return label;
        }
    }

    // 5. Rate Renderer (Price per day)
    class RateRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setFont(new Font("Segoe UI", Font.BOLD, 14));
            label.setForeground(new Color(25, 135, 84));
            label.setHorizontalAlignment(CENTER);
            return label;
        }
    }

    // ==========================================
    //           LOGIC & DATA METHODS
    // ==========================================

    public void refreshTable(List<Room> data) {
        tableModel.setRowCount(0);

        SwingWorker<List<Room>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Room> doInBackground() throws Exception {
                if (data != null) return data;
                return hmc.getRoomCtrl().getAllRooms();
            }

            @Override
            protected void done() {
                try {
                    List<Room> rooms = get();
                    tableModel.setRowCount(0);

                    for (Room r : rooms) {
                        String status = r.isOccupied() ? "Occupied" : "Available";
                        String occupant = (r.getPatient() != null) ? r.getPatient().getName() : "Empty";
                        String rate = calculateRoomRate(r.getRoomType());
                        
                        tableModel.addRow(new Object[]{
                            r.getRoomId(),
                            r.getRoomType(),
                            status,
                            occupant,
                            rate
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(RoomPanel.this, "Error loading rooms: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private String calculateRoomRate(String roomType) {
        // Calculate daily rate based on room type
        if (roomType == null) return "$100";
        
        String type = roomType.toLowerCase();
        if (type.contains("icu")) return "$500";
        else if (type.contains("private")) return "$300";
        else return "$150"; // General
    }

    private void setupListeners() {
        txtSearch.addActionListener(e -> performSearch());
        
        if (isAdmin) {
            table.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) showEditRoomDialog();
                    if (SwingUtilities.isRightMouseButton(e)) showContextMenu(e);
                }
            });
        }
    }

    private void performSearch() {
        String query = txtSearch.getText().trim();
        if (!query.isEmpty() && !query.contains("Search")) {
            List<Room> results = hmc.getRoomCtrl().searchRooms(query);
            refreshTable(results);
        } else {
            refreshTable(null);
        }
    }
    
    private String getSelectedIdFromRow() {
        int row = table.getSelectedRow();
        if (row == -1) { 
            JOptionPane.showMessageDialog(this, "Please select a room first."); 
            return null; 
        }
        return (String) table.getValueAt(row, 0);
    }

    // ==========================================
    //           DIALOG METHODS
    // ==========================================

    private void showContextMenu(MouseEvent e) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem viewItem = new JMenuItem("View Details", IconUtils.createIcon(IconUtils.ICON_EYE, 16, Color.DARK_GRAY));
        JMenuItem editItem = new JMenuItem("Edit Room Type", IconUtils.createIcon(IconUtils.ICON_PENCIL, 16, Color.DARK_GRAY));
        JMenuItem clearItem = new JMenuItem("Clear Room", IconUtils.createIcon(IconUtils.ICON_DOOR, 16, Color.ORANGE));
        JMenuItem deleteItem = new JMenuItem("Delete Room", IconUtils.createIcon(IconUtils.ICON_TRASH, 16, Color.RED));
        
        viewItem.addActionListener(ev -> showRoomDetailsDialog());
        editItem.addActionListener(ev -> showEditRoomDialog());
        clearItem.addActionListener(ev -> performClearRoom());
        deleteItem.addActionListener(ev -> performDelete());
        
        menu.add(viewItem);
        menu.add(editItem);
        menu.add(clearItem);
        menu.addSeparator();
        menu.add(deleteItem);
        menu.show(e.getComponent(), e.getX(), e.getY());
    }

    private void performDelete() {
        String id = getSelectedIdFromRow();
        if (id == null) return;
        
        Room room = hmc.getRoomCtrl().findRoomById(id);
        
        String message = "Delete Room " + id + "?";
        if (room.isOccupied() && room.getPatient() != null) {
            message += "\n\nWarning: Room is currently occupied by " + room.getPatient().getName();
            message += "\nThe patient will be unassigned from this room.";
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, message, 
            "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
        if (confirm == JOptionPane.YES_OPTION) {
            if (hmc.getRoomCtrl().deleteRoom(id)) {
                JOptionPane.showMessageDialog(this, "Room deleted successfully.");
                refreshTable(null);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete room.");
            }
        }
    }

    private void performClearRoom() {
        String id = getSelectedIdFromRow();
        if (id == null) return;
        
        Room room = hmc.getRoomCtrl().findRoomById(id);
        
        if (!room.isOccupied()) {
            JOptionPane.showMessageDialog(this, "Room is already empty.");
            return;
        }
        
        String patientName = room.getPatient() != null ? room.getPatient().getName() : "Unknown";
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Remove " + patientName + " from Room " + id + "?",
            "Clear Room", JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            // Unassign patient from room
            if (room.getPatient() != null) {
                String patientId = room.getPatient().getPatientId();
                hmc.assignPatientToRoom(patientId, null); // Unassign
                JOptionPane.showMessageDialog(this, "Room cleared successfully.");
                refreshTable(null);
            }
        }
    }

    private void showAddRoomDialog() {
        JTextField idField = new JTextField();
        String[] types = {"General", "ICU", "Private"};
        JComboBox<String> typeBox = new JComboBox<>(types);
        
        JPanel panel = DialogUtils.createForm("Add New Room",
            "Room ID:", idField,
            "Room Type:", typeBox
        );
        
        if (JOptionPane.showConfirmDialog(this, panel, "Add New Room", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            
            if (idField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Room ID is required.");
                return;
            }
            
            Room r = new Room(idField.getText().trim(), (String)typeBox.getSelectedItem());
            
            if (hmc.getRoomCtrl().addRoom(r)) {
                JOptionPane.showMessageDialog(this, "Room added successfully!");
                refreshTable(null);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add room. ID may already exist.");
            }
        }
    }

    private void showEditRoomDialog() {
        String id = getSelectedIdFromRow();
        if (id == null) return;
        
        Room room = hmc.getRoomCtrl().findRoomById(id);
        if (room == null) return;
        
        String[] types = {"General", "ICU", "Private"};
        JComboBox<String> typeBox = new JComboBox<>(types);
        typeBox.setSelectedItem(room.getRoomType());
        
        JPanel panel = DialogUtils.createForm("Edit Room",
            "Room ID:", new JLabel(id),
            "Room Type:", typeBox
        );
        
        if (JOptionPane.showConfirmDialog(this, panel, "Edit Room", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            
            Room updatedRoom = new Room(id, (String)typeBox.getSelectedItem());
            
            if (hmc.getRoomCtrl().updateRoom(updatedRoom)) {
                JOptionPane.showMessageDialog(this, "Room updated successfully!");
                refreshTable(null);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update room.");
            }
        }
    }

    private void showRoomDetailsDialog() {
        String id = getSelectedIdFromRow();
        if (id == null) return;
        
        Room room = hmc.getRoomCtrl().findRoomById(id);
        if (room == null) return;
        
        StringBuilder details = new StringBuilder();
        details.append("═══════════════════════════════\n");
        details.append("       ROOM DETAILS\n");
        details.append("═══════════════════════════════\n\n");
        details.append("Room ID: ").append(room.getRoomId()).append("\n");
        details.append("Room Type: ").append(room.getRoomType()).append("\n");
        details.append("Status: ").append(room.isOccupied() ? "Occupied" : "Available").append("\n");
        details.append("Daily Rate: ").append(calculateRoomRate(room.getRoomType())).append("\n\n");
        
        details.append("───────────────────────────────\n");
        
        if (room.isOccupied() && room.getPatient() != null) {
            Patient p = room.getPatient();
            details.append("CURRENT OCCUPANT\n");
            details.append("───────────────────────────────\n\n");
            details.append("Patient ID: ").append(p.getPatientId()).append("\n");
            details.append("Name: ").append(p.getName()).append("\n");
            details.append("Age: ").append(p.getAge()).append("\n");
            details.append("Diagnosis: ").append(p.getMedicalHistory()).append("\n");
            
            if (p.getDoctor() != null) {
                details.append("\nAssigned Doctor: Dr. ").append(p.getDoctor().getName()).append("\n");
                details.append("Specialization: ").append(p.getDoctor().getSpecialization()).append("\n");
            }
        } else {
            details.append("OCCUPANCY STATUS\n");
            details.append("───────────────────────────────\n\n");
            details.append("Room is currently available\n");
            details.append("Ready for new patient admission\n");
        }
        
        JTextArea textArea = new JTextArea(details.toString());
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        textArea.setEditable(false);
        textArea.setBackground(new Color(250, 250, 250));
        textArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        
        JOptionPane.showMessageDialog(this, scrollPane, 
            "Room " + id + " Details", JOptionPane.INFORMATION_MESSAGE);
    }
}