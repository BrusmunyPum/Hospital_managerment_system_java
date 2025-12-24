package views;

import javax.swing.*;

import controllers.UserController;
import models.User;

import java.awt.*;

public class LoginView extends JFrame {

    private UserController userCtrl;

    public LoginView() {
        userCtrl = new UserController();

        setTitle("Hospital Login");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        // UI Components
        JLabel lblUser = new JLabel("Username:");
        JTextField txtUser = new JTextField(15);
        
        JLabel lblPass = new JLabel("Password:");
        JPasswordField txtPass = new JPasswordField(15);
        
        JButton btnLogin = new JButton("Login");

        // Layout Constraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Padding

        gbc.gridx = 0; gbc.gridy = 0;
        add(lblUser, gbc);
        gbc.gridx = 1;
        add(txtUser, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        add(lblPass, gbc);
        gbc.gridx = 1;
        add(txtPass, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        add(btnLogin, gbc);

        // Login Action
        btnLogin.addActionListener(e -> {
            String username = txtUser.getText();
            String password = new String(txtPass.getPassword());

            User user = userCtrl.authenticate(username, password);

            if (user != null) {
                // Login Success!
                JOptionPane.showMessageDialog(this, "Welcome " + user.getRole());
                dispose(); // Close Login Window
                
                // Open Dashboard with the logged-in user
                new HospitalDashboard(user).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Username or Password", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        // Allow pressing "Enter" to login
        getRootPane().setDefaultButton(btnLogin);
    }
}
