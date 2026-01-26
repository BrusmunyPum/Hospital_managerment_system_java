package views;

import controllers.UserController;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.net.URL;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import models.User;
import utils.*;

public class LoginView extends JFrame {

    private UserController userCtrl;
    private JPanel rightPanel; // Glass Card
    private JTextField txtUser;
    private JPasswordField txtPass;
    private JButton btnLogin;
    private JLabel lblSubtitle;

    public LoginView() {
        userCtrl = new UserController();
        setTitle("Hospital Management System");
        setSize(1100, 650); // Increased size slightly for better proportions
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false); 

        // Main Layered Pane for Background + Content
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(1100, 650));
        setContentPane(layeredPane);

        // ================= BACKGROUND =================
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                // Modern Gradient Background (Blue to Purple)
                GradientPaint gp = new GradientPaint(0, 0, new Color(52, 148, 230), 
                                                     getWidth(), getHeight(), new Color(142, 68, 173));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Optional: Overlay an image if available, else just symbols
                URL imageUrl = getClass().getResource("/imgs/BG.jpg"); 
                if (imageUrl != null) {
                    ImageIcon icon = new ImageIcon(imageUrl);
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f)); // Faint Overlay
                    g2d.drawImage(icon.getImage(), 0, 0, getWidth(), getHeight(), null);
                } else {
                     // Draw some abstract shapes for visual interest if no image
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
                    g2d.setColor(Color.WHITE);
                    g2d.fillOval(-100, -100, 400, 400);
                    g2d.fillOval(getWidth()-300, getHeight()-300, 500, 500);
                }
            }
        };
        backgroundPanel.setBounds(0, 0, 1100, 650);
        layeredPane.add(backgroundPanel, JLayeredPane.DEFAULT_LAYER);

        // ================= LEFT SIDE (Marketing/Title) =================
        JPanel leftTextPanel = new JPanel();
        leftTextPanel.setOpaque(false); 
        leftTextPanel.setLayout(new BoxLayout(leftTextPanel, BoxLayout.Y_AXIS));
        leftTextPanel.setBounds(100, 150, 500, 400); 

        JLabel lblWelcomeTitle = new JLabel("Welcome To MedCare");
        lblWelcomeTitle.setFont(new Font("Poppins", Font.BOLD, 16));
        lblWelcomeTitle.setForeground(new Color(255, 255, 255, 200)); 
        lblWelcomeTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel lblMainTitle = new JLabel("<html>Hospital<br>Management<br>System</html>");
        lblMainTitle.setFont(new Font("Poppins", Font.BOLD, 54));
        lblMainTitle.setForeground(Color.WHITE);
        lblMainTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel lblDesc = new JLabel("<html><div style='width:350px;'>Streamline your hospital operations, manage patients, doctors, and rooms efficiently with our modern platform.</div></html>");
        lblDesc.setFont(new Font("Poppins", Font.PLAIN, 14));
        lblDesc.setForeground(new Color(255, 255, 255, 180));
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftTextPanel.add(lblWelcomeTitle);
        leftTextPanel.add(Box.createVerticalStrut(10));
        leftTextPanel.add(lblMainTitle);
        leftTextPanel.add(Box.createVerticalStrut(20));
        leftTextPanel.add(lblDesc);
        
        layeredPane.add(leftTextPanel, JLayeredPane.PALETTE_LAYER);

        // ================= RIGHT PANEL (Glass Card) =================
        rightPanel = new GlassPanel(30); 
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBounds(680, 80, 340, 480); 
        rightPanel.setBorder(new EmptyBorder(40, 30, 40, 30));

        // -- Header --
        JLabel lblLoginIcon = new JLabel(IconUtils.createIcon(IconUtils.ICON_USER, 50, Color.WHITE));
        lblLoginIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblLoginTitle = new JLabel("Login");
        lblLoginTitle.setFont(new Font("Poppins", Font.BOLD, 28)); 
        lblLoginTitle.setForeground(Color.WHITE); 
        lblLoginTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        lblSubtitle = new JLabel("Sign in to your account");
        lblSubtitle.setFont(new Font("Poppins", Font.PLAIN, 13));
        lblSubtitle.setForeground(new Color(230, 230, 230));
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        rightPanel.add(lblLoginIcon);
        rightPanel.add(Box.createVerticalStrut(15));
        rightPanel.add(lblLoginTitle);
        rightPanel.add(Box.createVerticalStrut(5));
        rightPanel.add(lblSubtitle);
        rightPanel.add(Box.createVerticalStrut(40));

        // -- Inputs --
        txtUser = new PlaceholderFormattedTextField("Username");
        JPanel userField = createIconTextField(IconUtils.ICON_USER, txtUser);
        
        txtPass = new PlaceholderPasswordField("Password");
        JPanel passField = createIconTextField(IconUtils.ICON_LOCK, txtPass);

        rightPanel.add(userField);
        rightPanel.add(Box.createVerticalStrut(20));
        rightPanel.add(passField);
        rightPanel.add(Box.createVerticalStrut(30));

        // -- Buttons --
        btnLogin = new GradientButton("LOGIN", new Color(46, 204, 113), new Color(39, 174, 96));
        btnLogin.addActionListener(e -> performLogin());
        
        JButton btnBook = new GradientButton("Book Appointment", new Color(52, 152, 219), new Color(41, 128, 185));
        btnBook.addActionListener(e -> new BookingDialog(this).setVisible(true));
        
        rightPanel.add(btnLogin);
        rightPanel.add(Box.createVerticalStrut(15));
        // Divider
        JLabel lblOr = new JLabel("- OR -");
        lblOr.setForeground(new Color(255, 255, 255, 100));
        lblOr.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblOr.setFont(new Font("SansSerif", Font.PLAIN, 11));
        
        rightPanel.add(lblOr);
        rightPanel.add(Box.createVerticalStrut(15));
        rightPanel.add(btnBook);

        layeredPane.add(rightPanel, JLayeredPane.PALETTE_LAYER);
        getRootPane().setDefaultButton(btnLogin);
    }

    private void performLogin() {
        String username = txtUser.getText();
        String password = new String(txtPass.getPassword());
        User user = userCtrl.authenticate(username, password);

        if (user != null) {
            lblSubtitle.setText("Authenticating...");
            lblSubtitle.setForeground(new Color(46, 204, 113));
            
            // Simulate loading
            SwingUtilities.invokeLater(() -> {
                 try { Thread.sleep(500); } catch(Exception ignored){}
                 dispose();
                 new HospitalDashboard(user).setVisible(true);
            });
        } else {
            JOptionPane.showMessageDialog(this, "Invalid Username or Password", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Custom Glass Text Field with Icon
    private JPanel createIconTextField(String iconType, JTextField field) {
        // Use a custom panel for full control over rendering (Pill shape)
        JPanel panel = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // 1. Background (Solid White for Visibility)
                g2.setColor(Color.WHITE); 
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight()); // Full rounded pill
                
                // 2. Border (Blue on Focus, Gray on Idle)
                if (field.isFocusOwner()) {
                    g2.setColor(new Color(52, 152, 219));
                    g2.setStroke(new BasicStroke(2));
                } else {
                    g2.setColor(new Color(220, 220, 220));
                    g2.setStroke(new BasicStroke(1));
                }
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, getHeight(), getHeight()); 
                
                super.paintComponent(g);
            }
        };
        
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(300, 50)); 
        panel.setBorder(new EmptyBorder(5, 15, 5, 15)); 
        
        // Icon should be dark to match white background
        JLabel icon = new JLabel(IconUtils.createIcon(iconType, 20, new Color(100, 100, 100)));
        panel.add(icon, BorderLayout.WEST);
        
        // Custom painting for placeholder
        field.setOpaque(false);
        field.setForeground(new Color(50, 50, 50)); // Dark Text for visibility
        field.setCaretColor(new Color(50, 50, 50));
        field.setFont(new Font("Poppins", Font.PLAIN, 15));
        field.setBorder(null);

        // Explicitly set Echo Char if it's a Password Field
        if (field instanceof JPasswordField) {
            ((JPasswordField) field).setEchoChar('•');
        }

        panel.add(field, BorderLayout.CENTER);
        
        // Hover/Focus Effect Trigger
        FocusAdapter focusRepaint = new FocusAdapter() {
            public void focusGained(FocusEvent e) { panel.repaint(); }
            public void focusLost(FocusEvent e) { panel.repaint(); }
        };
        field.addFocusListener(focusRepaint);
        
        return panel;
    }

    // Glass Panel Component
    class GlassPanel extends JPanel {
        private int radius;
        public GlassPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Glass Background (Semi-transparent dark)
            g2.setColor(new Color(10, 10, 30, 100)); // Darker glass
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            
            // Border (Shine)
            g2.setColor(new Color(255, 255, 255, 50));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, radius, radius);
            
            super.paintComponent(g);
        }
    }

    // Gradient Button Component
    class GradientButton extends JButton {
        private Color c1, c2;
        public GradientButton(String text, Color c1, Color c2) {
            super(text);
            this.c1 = c1;
            this.c2 = c2;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(Color.WHITE);
            setFont(new Font("Poppins", Font.BOLD, 14));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setAlignmentX(Component.CENTER_ALIGNMENT);
            setMaximumSize(new Dimension(300, 45));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            GradientPaint gp = new GradientPaint(0, 0, c1, getWidth(), 0, c2);
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            
            super.paintComponent(g);
        }
    }
    
    // We will change the constructor to use this class
    private static class PlaceholderFormattedTextField extends JTextField {
        private String placeholder;
        public PlaceholderFormattedTextField(String p) { this.placeholder = p; }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
             if (getText().isEmpty() && !isFocusOwner()) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.GRAY); // Darker placeholder
                g2.setFont(getFont()); // Match font
                g2.drawString(placeholder, getInsets().left, getBaseline(getWidth(), getHeight()));
            }
        }
    }
    
     private static class PlaceholderPasswordField extends JPasswordField {
        private String placeholder;
        public PlaceholderPasswordField(String p) { this.placeholder = p; }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
             if (getPassword().length == 0 && !isFocusOwner()) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.GRAY); // Darker placeholder
                g2.setFont(getFont());
                g2.drawString(placeholder, getInsets().left, getBaseline(getWidth(), getHeight()));
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginView().setVisible(true));
    }
}