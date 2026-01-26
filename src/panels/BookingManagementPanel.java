package panels;

import controllers.BookingController;
import models.Booking;
import utils.IconUtils;
import utils.ModernUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class BookingManagementPanel extends JPanel {

    private BookingController bookingCtrl;
    private DefaultTableModel tableModel;
    private JTable table;
    private Color bgLight = new Color(245, 247, 251);

    public BookingManagementPanel() {
        this.bookingCtrl = new BookingController();

        setLayout(new BorderLayout());
        setBackground(bgLight);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        // --- TOP SECTION ---
        add(createHeaderSection(), BorderLayout.NORTH);

        // --- CENTER SECTION (Table) ---
        String[] columns = {"ID", "Date", "Patient Name", "Age/Gender", "Contact", "Requested Doc/Room", "Symptoms", "Status"};
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
        tableCard.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);

        // --- BOTTOM SECTION (Actions) ---
        add(createActionButtons(), BorderLayout.SOUTH);

        refreshTable();
    }

    private JPanel createHeaderSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel title = new JLabel("Booking Requests");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(50, 50, 50));

        JLabel subtitle = new JLabel("Review and approve patient appointment requests");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(108, 117, 125));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        textPanel.setOpaque(false);
        textPanel.add(title);
        textPanel.add(subtitle);

        panel.add(textPanel, BorderLayout.WEST);
        
        JButton btnRefresh = ModernUI.createOutlineButton("Refresh", IconUtils.createIcon(IconUtils.ICON_REFRESH, 18, Color.BLACK));
        btnRefresh.addActionListener(e -> refreshTable());
        panel.add(btnRefresh, BorderLayout.EAST);
        
        panel.setBorder(new EmptyBorder(0, 0, 20, 0));
        return panel;
    }

    private JPanel createActionButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(bgLight);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JButton btnApprove = ModernUI.createPrimaryButton("Approve Booking", IconUtils.createIcon(IconUtils.ICON_CHECK, 16, Color.WHITE));
        btnApprove.setBackground(new Color(40, 167, 69)); // Green
        
        JButton btnReject = ModernUI.createDangerButton("Reject", IconUtils.createIcon(IconUtils.ICON_TRASH, 16, Color.WHITE));

        btnApprove.addActionListener(e -> approveSelectedBooking());
        btnReject.addActionListener(e -> rejectSelectedBooking());

        panel.add(btnApprove);
        panel.add(btnReject);
        return panel;
    }

    private void setupTableStyle() {
        table.setRowHeight(50);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(240, 240, 240));
        table.setSelectionBackground(new Color(240, 245, 255));
        table.setSelectionForeground(Color.BLACK);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(Color.WHITE);
        header.setForeground(new Color(150, 150, 150));
        header.setPreferredSize(new Dimension(0, 40));
        header.setReorderingAllowed(false);
        
        // Hide ID column usually, but for now show it
        table.getColumnModel().getColumn(0).setPreferredWidth(40); // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(100); // Date
        table.getColumnModel().getColumn(2).setPreferredWidth(150); // Name
        table.getColumnModel().getColumn(6).setPreferredWidth(200); // Symptoms
        
        // Custom Renderer for Status (Column 7)
        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                 JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                 label.setForeground(new Color(255, 193, 7)); // Amber/Orange for Pending
                 label.setFont(label.getFont().deriveFont(Font.BOLD));
                 label.setHorizontalAlignment(SwingConstants.CENTER);
                 return label;
            }
        });
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<Booking> bookings = bookingCtrl.getPendingBookings();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        for (Booking b : bookings) {
            String docInfo = (b.getDoctorId() != null) ? b.getDoctorId() : "Any";
            String roomInfo = (b.getRoomId() != null) ? b.getRoomId() : "Any";
            
            tableModel.addRow(new Object[]{
                b.getBookingId(),
                (b.getBookingDate() != null) ? sdf.format(b.getBookingDate()) : "N/A",
                b.getPatientName(),
                b.getAge() + " / " + b.getGender(),
                b.getContactNumber(),
                "Doc: " + docInfo + ", Rm: " + roomInfo,
                b.getSymptoms(),
                b.getStatus()
            });
        }
    }

    private void approveSelectedBooking() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking to approve.");
            return;
        }

        int bookingId = (int) table.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Approve booking #" + bookingId + "?\nThis will create a new Patient record.", 
            "Confirm Approval", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (bookingCtrl.approveBooking(bookingId)) {
                JOptionPane.showMessageDialog(this, "Booking Approved! New Patient Created.");
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to approve booking.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void rejectSelectedBooking() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking to reject.");
            return;
        }

        int bookingId = (int) table.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Reject booking #" + bookingId + "?", 
            "Confirm Rejection", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (bookingCtrl.rejectBooking(bookingId)) {
                JOptionPane.showMessageDialog(this, "Booking Rejected.");
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to reject booking.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
