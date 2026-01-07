package utils;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ModernUI {

    // Modern Color Palette
    public static final Color PRIMARY_COLOR = new Color(13, 110, 253);
    public static final Color DANGER_COLOR = new Color(220, 53, 69); // Red
    public static final Color SUCCESS_COLOR = new Color(25, 135, 84); // Green
    public static final Color WARNING_COLOR = new Color(255, 193, 7); // Yellow/Orange
    public static final Color TEXT_DARK = new Color(50, 50, 50);
    public static final Color TEXT_GRAY = new Color(108, 117, 125);
    public static final Color CONTROL_BG = new Color(255, 255, 255);
    public static final Color BORDER_COLOR = new Color(220, 220, 220);

    // Font
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_PLAIN = new Font("Segoe UI", Font.PLAIN, 13);

    // ==========================================
    //              BUTTON FACTORY
    // ==========================================

    public static JButton createPrimaryButton(String text, Icon icon) {
        return createCustomButton(text, icon, PRIMARY_COLOR, Color.WHITE, true);
    }

    public static JButton createOutlineButton(String text, Icon icon) {
        return createCustomButton(text, icon, Color.WHITE, TEXT_DARK, false);
    }

    public static JButton createDangerButton(String text, Icon icon) {
        return createCustomButton(text, icon, DANGER_COLOR, Color.WHITE, true);
    }
    
    public static JButton createWarningButton(String text, Icon icon) {
        // Warning usually has black text on yellow/orange
        return createCustomButton(text, icon, WARNING_COLOR, Color.BLACK, true);
    }

    private static JButton createCustomButton(String text, Icon icon, Color bg, Color fg, boolean isFilled) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Determine background color based on state
                Color paintBg = getBackground();
                if (getModel().isPressed()) {
                    paintBg = isFilled ? bg.darker() : new Color(240, 240, 240);
                } else if (getModel().isRollover()) {
                    paintBg = isFilled ? bg.brighter() : new Color(248, 248, 248);
                }

                // Draw Background
                if (isFilled || getModel().isRollover()) {
                    g2.setColor(paintBg);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                } else if (!isFilled) {
                    // Start white/transparent
                    g2.setColor(getBackground());
                     g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }

                // Draw Border (for Outline or just for Definition)
                if (!isFilled) {
                    g2.setColor(BORDER_COLOR);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                }

                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setIcon(icon);
        btn.setFont(FONT_BOLD);
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Padding
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));

        // Hover Effect Logic implies internal repaint triggers
        // Swing handles rollover state if enabled
        btn.setRolloverEnabled(true);

        return btn;
    }

    // ==========================================
    //           TEXT FIELD FACTORY
    // ==========================================

    public static JTextField createSearchField(String placeholder) {
        JTextField txt = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);

                // Border
                g2.setColor(BORDER_COLOR);
                if (isFocusOwner()) {
                    g2.setColor(PRIMARY_COLOR);
                    g2.setStroke(new BasicStroke(2));
                }
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);

                g2.dispose();
                super.paintComponent(g);
                
                // Placeholder Text
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D gPlaceholder = (Graphics2D) g.create();
                    gPlaceholder.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    gPlaceholder.setColor(TEXT_GRAY);
                    gPlaceholder.setFont(getFont().deriveFont(Font.ITALIC));
                    int padding = 12;
                    int y = (getHeight() - gPlaceholder.getFontMetrics().getHeight()) / 2 + gPlaceholder.getFontMetrics().getAscent();
                    gPlaceholder.drawString(placeholder, padding, y);
                    gPlaceholder.dispose();
                }
            }
        };

        txt.setOpaque(false);
        txt.setBorder(new EmptyBorder(5, 12, 5, 12)); // Inner padding
        txt.setFont(FONT_PLAIN);
        txt.setPreferredSize(new Dimension(300, 40));
        
        return txt;
    }
    // ==========================================
    //           TABLE STYLING UTILS
    // ==========================================

    public static void setupTableStyle(JTable table) {
        table.setRowHeight(50); // Standard height
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(240, 240, 240));
        table.setSelectionBackground(new Color(240, 245, 255));
        table.setSelectionForeground(Color.BLACK);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        setupHeaderStyle(table);
    }

    public static void setupHeaderStyle(JTable table) {
        javax.swing.table.JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 50)); 
        header.setReorderingAllowed(false);
        
        header.setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                String title = value != null ? value.toString().toUpperCase() : "";
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, title, isSelected, hasFocus, row, column);
                
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                label.setBackground(new Color(248, 249, 250)); 
                label.setForeground(new Color(108, 120, 130));
                
                label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(230, 230, 230)),
                    new EmptyBorder(0, 5, 0, 5)
                ));
                
                label.setHorizontalAlignment(SwingConstants.CENTER);
                return label;
            }
        });
    }

    public static javax.swing.table.DefaultTableCellRenderer createCenterRenderer() {
        javax.swing.table.DefaultTableCellRenderer renderer = new javax.swing.table.DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        renderer.setBorder(new EmptyBorder(0, 5, 0, 5));
        return renderer;
    }

    public static Color getAvatarColor(String name) {
        if (name == null || name.isEmpty()) return TEXT_GRAY;
        int hash = Math.abs(name.hashCode());
        Color[] colors = {
            new Color(13, 110, 253), // Blue
            new Color(102, 16, 242), // Purple
            new Color(111, 66, 193), // Indigo
            new Color(214, 51, 132), // Pink
            new Color(220, 53, 69),  // Red
            new Color(253, 126, 20), // Orange
            new Color(255, 193, 7),  // Yellow
            new Color(25, 135, 84),  // Green
            new Color(32, 201, 151), // Teal
            new Color(13, 202, 240)  // Cyan
        };
        return colors[hash % colors.length];
    }

    // ==========================================
    //           WIDGET FACTORY
    // ==========================================

    public static JPanel createStatsCard(String title, String value, String badge, Icon icon, Color bg, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        // Top Row: Icon + Badge
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel lblIcon = new JLabel(icon);
        
        JLabel lblBadge = new JLabel(badge);
        lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblBadge.setForeground(accent);
        lblBadge.setBackground(bg);
        lblBadge.setOpaque(true);
        lblBadge.setBorder(new EmptyBorder(2, 6, 2, 6));
        
        top.add(lblIcon, BorderLayout.WEST);
        top.add(lblBadge, BorderLayout.EAST);
        
        // Bottom Row: Label + Number
        JPanel bot = new JPanel(new GridLayout(2, 1, 0, 5));
        bot.setOpaque(false);
        bot.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTitle.setForeground(TEXT_GRAY);
        
        JLabel lblVal = new JLabel(value);
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblVal.setForeground(TEXT_DARK);
        
        bot.add(lblTitle);
        bot.add(lblVal);
        
        card.add(top, BorderLayout.NORTH);
        card.add(bot, BorderLayout.CENTER);
        return card;
    }
}
