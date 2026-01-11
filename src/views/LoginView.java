package views;

import controllers.UserController;
import java.awt.*;
import java.net.URL;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import models.User;

public class LoginView extends JFrame {

    private UserController userCtrl;
    private final Color GLASS_NAVY = new Color(13, 26, 64, 180); 
    private RoundedPanel rightPanel;
    private JPanel fieldsPanel;
    private JButton btnLogin;
    private JLabel lblSubtitle;

    public LoginView() {
        userCtrl = new UserController();
        setTitle("Hospital Management System");
        setSize(1000, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false); 

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(1000, 550));
        setContentPane(layeredPane);

        // ================= BACKGROUND IMAGE =================
        URL imageUrl = getClass().getResource("/imgs/BG.jpg");
        JLabel backgroundLabel;
        if (imageUrl != null) {
            ImageIcon originalIcon = new ImageIcon(imageUrl);
            Image scaledImage = originalIcon.getImage().getScaledInstance(1000, 550, Image.SCALE_SMOOTH);
            backgroundLabel = new JLabel(new ImageIcon(scaledImage));
        } else {
            backgroundLabel = new JLabel();
            backgroundLabel.setBackground(new Color(224, 242, 247));
            backgroundLabel.setOpaque(true);
        }
        backgroundLabel.setBounds(0, 0, 1000, 550);
        layeredPane.add(backgroundLabel, JLayeredPane.DEFAULT_LAYER);

        // ================= LEFT SIDE (Title Text) =================
        JPanel leftTextPanel = new JPanel();
        leftTextPanel.setOpaque(false); 
        leftTextPanel.setLayout(new BoxLayout(leftTextPanel, BoxLayout.Y_AXIS));
        leftTextPanel.setBounds(100, 40, 500, 300); 

        JLabel lblWelcomeTitle = new JLabel("Welcome To Our");
        lblWelcomeTitle.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        lblWelcomeTitle.setForeground(new Color(224, 247, 250)); 
        lblWelcomeTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel lblMainTitle = new JLabel("Hospital Management System");
        lblMainTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblMainTitle.setForeground(Color.WHITE);
        lblMainTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        leftTextPanel.add(lblWelcomeTitle);
        leftTextPanel.add(Box.createVerticalStrut(10)); // Top padding
        leftTextPanel.add(lblMainTitle);
        layeredPane.add(leftTextPanel, JLayeredPane.PALETTE_LAYER);

        // ================= RIGHT PANEL (Rounded Glass Card) =================
        rightPanel = new RoundedPanel(50, GLASS_NAVY); 
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBounds(650, 60, 300, 420); 
        rightPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel lblWelcome = new JLabel("Login");
        lblWelcome.setFont(new Font("Poppins", Font.BOLD, 26)); 
        lblWelcome.setForeground(Color.WHITE); 
        lblWelcome.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblSubtitle = new JLabel("Please sign in to continue");
        lblSubtitle.setFont(new Font("Poppins", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(200, 220, 255));
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ================= FIELDS PANEL =================
        fieldsPanel = new JPanel();
        fieldsPanel.setOpaque(false); 
        fieldsPanel.setLayout(new GridLayout(4, 1, 0, 5));
        fieldsPanel.setMaximumSize(new Dimension(240, 180));

        JLabel lblUser = new JLabel("Username:");
        lblUser.setForeground(Color.WHITE);
        JTextField txtUser = new JTextField();
        styleGlassInput(txtUser); 

        JLabel lblPass = new JLabel("Password:");
        lblPass.setForeground(Color.WHITE);
        JPasswordField txtPass = new JPasswordField();
        styleGlassInput(txtPass); 

        fieldsPanel.add(lblUser);
        fieldsPanel.add(txtUser);
        fieldsPanel.add(lblPass);
        fieldsPanel.add(txtPass);

        btnLogin = new JButton("LOGIN");
        btnLogin.setBackground(new Color(52, 152, 219)); 
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Poppins", Font.BOLD, 14));
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setOpaque(true);
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setMaximumSize(new Dimension(250, 45));
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));

        rightPanel.add(Box.createVerticalGlue());
        rightPanel.add(lblWelcome);
        rightPanel.add(Box.createVerticalStrut(5));
        rightPanel.add(lblSubtitle);
        rightPanel.add(Box.createVerticalStrut(30));
        rightPanel.add(fieldsPanel);
        rightPanel.add(Box.createVerticalStrut(25));
        rightPanel.add(btnLogin);
        rightPanel.add(Box.createVerticalGlue());

        layeredPane.add(rightPanel, JLayeredPane.PALETTE_LAYER);

        // ================= LOGIN LOGIC =================
        btnLogin.addActionListener(e -> {
            String username = txtUser.getText();
            String password = new String(txtPass.getPassword());
            User user = userCtrl.authenticate(username, password);

            if (user != null) {
                showLoadingInCard(user);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Credentials", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        getRootPane().setDefaultButton(btnLogin);
    }

    private void showLoadingInCard(User user) {
        // 1. Hide form elements
        fieldsPanel.setVisible(false);
        btnLogin.setVisible(false);
        lblSubtitle.setText("Authenticating...");

        // 2. Add Progress Bar to the existing rightPanel
        JProgressBar loader = new JProgressBar();
        loader.setIndeterminate(true);
        loader.setMaximumSize(new Dimension(200, 6));
        loader.setBackground(new Color(255, 255, 255, 30));
        loader.setForeground(new Color(52, 152, 219));
        loader.setBorderPainted(false);
        loader.setAlignmentX(Component.CENTER_ALIGNMENT);

        rightPanel.add(loader);
        rightPanel.revalidate();
        rightPanel.repaint();

        // 3. Background Task
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                Thread.sleep(1500); // 1.5 seconds loading
                return null;
            }
            @Override
            protected void done() {
                dispose();
                new HospitalDashboard(user).setVisible(true);
            }
        };
        worker.execute();
    }

    private void styleGlassInput(JTextField field) {
        field.setBackground(new Color(255, 255, 255, 30)); 
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setFont(new Font("Poppins", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
            new javax.swing.border.LineBorder(new Color(255, 255, 255, 60), 1, true), 
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setOpaque(false); 
    }

    class RoundedPanel extends JPanel {
        private int radius;
        private Color backgroundColor;
        public RoundedPanel(int radius, Color bgColor) {
            this.radius = radius;
            this.backgroundColor = bgColor;
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(backgroundColor);
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2d.setColor(new Color(255, 255, 255, 50));
            g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginView().setVisible(true));
    }
}