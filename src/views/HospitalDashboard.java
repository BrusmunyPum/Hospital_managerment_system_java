package views;

import controllers.HospitalManagementController;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import models.User;
import panels.*;
import utils.ModernUI;

public class HospitalDashboard extends JFrame {

    private HospitalManagementController hmc;

    // Logic Variables

    private JPanel contentPanel;
    private CardLayout cardLayout;
    
    // --- SIZE SETTINGS (Modify these to change sizes) ---
    private int sidebarWidth = 260; // Width of the Left Panel
    private int logoutBtnWidth = 220; // Width of Logout Button
    private int logoutBtnHeight = 45; // Height of Logout Button
    
    // UI Colors
    private Color sidebarColor = Color.WHITE;
    private Color contentColor = new Color(245, 247, 251); 
    private Color activeMenuColor = new Color(225, 240, 255); 
    private Color activeTextColor = new Color(0, 102, 204);
    private Color inactiveTextColor = new Color(100, 100, 100);
    private Color borderColor = new Color(230, 230, 230);

    // Panels
    private HomePanel homePanel;
    private PatientPanel patientPanel;
    private DoctorPanel doctorPanel;
    private RoomPanel roomPanel;
    private UserManagementPanel userMgmtPanel;
    private HistoryPanel historyPanel;

    // Track active button
    private JButton currentActiveButton = null;
    
    // Header components
    private JLabel lblPageTitle;
    private JButton btnHome; 

    public HospitalDashboard(User user) {

        // 1. Initialize Controller
        hmc = new HospitalManagementController();

        // 2. Main Frame Setup
        setTitle("MediCare - " + user.getRole() + " Portal");
        setSize(1350, 800); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 3. Permission Logic
        boolean isAdmin = user.isAdmin();
        boolean isDoctor = user.isDoctor();
        String doctorFilterId = isDoctor ? user.getLinkedId() : null;

        // 4. Initialize Panels
        homePanel = new HomePanel(hmc);
        patientPanel = new PatientPanel(hmc, user, doctorFilterId);
        doctorPanel = new DoctorPanel(hmc, isAdmin);
        roomPanel = new RoomPanel(hmc, isAdmin);
        if (isAdmin) {
            userMgmtPanel = new UserManagementPanel();
        }
        historyPanel = new HistoryPanel(hmc);

        // =======================================================
        // UI IMPLEMENTATION
        // =======================================================

        // --- A. SIDEBAR (Left Panel) ---
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(sidebarWidth, 800)); // Uses size variable
        sidebar.setBackground(sidebarColor);
        sidebar.setLayout(new BorderLayout());
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, borderColor));

        // 1. Sidebar Logo
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 25));
        logoPanel.setBackground(sidebarColor);
        JLabel titleLabel = new JLabel("<html><b style='color:#0066cc; font-size:18px;'>Hospital</b><br><span style='font-size:11px; color:gray;'>Management System</span></html>");
        logoPanel.add(titleLabel);
        sidebar.add(logoPanel, BorderLayout.NORTH);

        // 2. Sidebar Menu Items
        JPanel menuContainer = new JPanel();
        menuContainer.setLayout(new BoxLayout(menuContainer, BoxLayout.Y_AXIS));
        menuContainer.setBackground(sidebarColor);
        menuContainer.setBorder(new EmptyBorder(20, 10, 10, 10));

        btnHome = createMenuButton("Home");
        JButton btnPatients = createMenuButton("Patients");
        
        menuContainer.add(btnHome);
        menuContainer.add(Box.createVerticalStrut(15));
        menuContainer.add(btnPatients);
        
        menuContainer.add(Box.createVerticalStrut(15));
        JButton btnHistory = createMenuButton("History");
        menuContainer.add(btnHistory);

        JButton btnDoctors = null;
        JButton btnRooms = null;
        if (!isDoctor) {
            menuContainer.add(Box.createVerticalStrut(15));
            btnDoctors = createMenuButton("Doctors");
            menuContainer.add(btnDoctors);
            
            menuContainer.add(Box.createVerticalStrut(15));
            btnRooms = createMenuButton("Rooms"); 
            menuContainer.add(btnRooms);
        }

        JButton btnUsers = null;
        if (isAdmin) {
            menuContainer.add(Box.createVerticalStrut(15));
            btnUsers = createMenuButton("Settings (Users)");
            menuContainer.add(btnUsers);
        }
        sidebar.add(menuContainer, BorderLayout.CENTER);
        
        // 3. LOGOUT BUTTON (Bottom of Sidebar)
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(sidebarColor);
        bottomPanel.setBorder(new EmptyBorder(20, 0, 20, 0)); // Padding bottom
        
        JButton btnLogout = ModernUI.createDangerButton("Logout", null);
        btnLogout.setPreferredSize(new Dimension(logoutBtnWidth, logoutBtnHeight));
        
        btnLogout.addActionListener(e -> {
            dispose();
            new LoginView().setVisible(true);
        });
        
        bottomPanel.add(btnLogout);
        sidebar.add(bottomPanel, BorderLayout.SOUTH);

        add(sidebar, BorderLayout.WEST);


        // --- B. MAIN CONTENT AREA (Center) ---
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(contentColor);

        // 1. Header (Top Bar)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(contentColor);
        headerPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        // Header LEFT: Page Title
        lblPageTitle = new JLabel("Dashboard Overview");
        lblPageTitle.setFont(new Font("SansSerif", Font.BOLD, 24));
        headerPanel.add(lblPageTitle, BorderLayout.WEST);
        
        // Header RIGHT: Search + User Profile ONLY (No Logout here)
        JPanel topRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        topRightPanel.setBackground(contentColor);

        // Search Field
        JTextField searchField = ModernUI.createSearchField(" Search records...");
        searchField.setPreferredSize(new Dimension(200, 35));
        topRightPanel.add(searchField);

        // User Profile Info
        JPanel userProfilePanel = new JPanel(new BorderLayout());
        userProfilePanel.setBackground(contentColor);
        
        JLabel lblUserName = new JLabel(user.getUsername());
        lblUserName.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblUserName.setHorizontalAlignment(SwingConstants.RIGHT);
        
        JLabel lblUserRole = new JLabel(user.getRole());
        lblUserRole.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblUserRole.setForeground(Color.GRAY);
        lblUserRole.setHorizontalAlignment(SwingConstants.RIGHT);
        
        userProfilePanel.add(lblUserName, BorderLayout.NORTH);
        userProfilePanel.add(lblUserRole, BorderLayout.SOUTH);
        topRightPanel.add(userProfilePanel);

        headerPanel.add(topRightPanel, BorderLayout.EAST);
        mainContainer.add(headerPanel, BorderLayout.NORTH);

        // 2. Card Panel (Views)
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(contentColor);
        contentPanel.setBorder(new EmptyBorder(0, 30, 30, 30));

        contentPanel.add(homePanel, "Home");
        contentPanel.add(patientPanel, "Patients");
        if (!isDoctor) {
            contentPanel.add(doctorPanel, "Doctors");
            contentPanel.add(roomPanel, "Rooms");
        }
        if (isAdmin) {
            contentPanel.add(userMgmtPanel, "Users");
        }
        contentPanel.add(historyPanel, "History");

        mainContainer.add(contentPanel, BorderLayout.CENTER);
        add(mainContainer, BorderLayout.CENTER);

        // =======================================================
        // EVENT LISTENERS
        // =======================================================
        
        btnHome.addActionListener(e -> {
            switchTab("Home", "Dashboard Overview", btnHome);
            homePanel.refreshData();
        });

        btnPatients.addActionListener(e -> {
            switchTab("Patients", "Patient Management", btnPatients);
            patientPanel.refreshTable(null);
        });

        btnHistory.addActionListener(e -> {
            switchTab("History", "Patient Discharge History", btnHistory);
            historyPanel.loadData();
        });

        if (btnDoctors != null) {
            JButton finalBtnDoctors = btnDoctors;
            btnDoctors.addActionListener(e -> {
                switchTab("Doctors", "Medical Staff", finalBtnDoctors);
                doctorPanel.refreshTable(null);
            });
        }

        if (btnRooms != null) {
            JButton finalBtnRooms = btnRooms;
            btnRooms.addActionListener(e -> {
                switchTab("Rooms", "Hospital Inventory & Rooms", finalBtnRooms);
                roomPanel.refreshTable(null);
            });
        }

        if (btnUsers != null) {
            JButton finalBtnUsers = btnUsers;
            btnUsers.addActionListener(e -> {
                switchTab("Users", "User Settings", finalBtnUsers);
            });
        }

        // Set Default View
        switchTab("Home", "Dashboard Overview", btnHome);
    }

    // --- HELPER METHODS ---

    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        btn.setForeground(inactiveTextColor);
        btn.setBackground(sidebarColor);
        btn.setBorder(new EmptyBorder(12, 20, 12, 20));
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(sidebarWidth, 45)); // Match Sidebar Width
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (btn != currentActiveButton) {
                    btn.setBackground(new Color(245, 245, 245));
                }
            }
            public void mouseExited(MouseEvent e) {
                if (btn != currentActiveButton) {
                    btn.setBackground(sidebarColor);
                }
            }
        });

        return btn;
    }
    
    private void switchTab(String cardName, String title, JButton activeBtn) {
        cardLayout.show(contentPanel, cardName);
        lblPageTitle.setText(title);

        if (currentActiveButton != null) {
            currentActiveButton.setBackground(sidebarColor);
            currentActiveButton.setForeground(inactiveTextColor);
            currentActiveButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        }

        currentActiveButton = activeBtn;
        currentActiveButton.setBackground(activeMenuColor);
        currentActiveButton.setForeground(activeTextColor);
        currentActiveButton.setFont(new Font("SansSerif", Font.BOLD, 14));
    }
}