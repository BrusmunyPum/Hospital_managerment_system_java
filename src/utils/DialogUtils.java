package utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DialogUtils {

    /**
     * Creates a standardized form panel for dialogs.
     * Takes a variable number of arguments in pairs: "Label Text" (String), Component (JComponent).
     * Example: createForm("Name:", nameField, "Age:", ageField)
     */
    public static JPanel createForm(String title, Object... components) {
        if (components.length % 2 != 0) {
            throw new IllegalArgumentException("Arguments must be in pairs (Label String, Component)");
        }
        
        // 1. Main Container
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1));
        
        // 2. Header
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        header.setBackground(new Color(13, 110, 253)); // Primary Blue
        header.setOpaque(true);
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setIcon(IconUtils.createIcon(IconUtils.ICON_PENCIL, 20, Color.WHITE)); // Generic Edit Icon
        
        header.add(lblTitle);
        mainPanel.add(header, BorderLayout.NORTH);

        // 3. Form Content (GridBagLayout for full width)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        formPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5); // Spacing
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        for (int i = 0; i < components.length; i += 2) {
            Object labelObj = components[i];
            Object compObj = components[i + 1];

            if (!(labelObj instanceof String)) {
                throw new IllegalArgumentException("Even arguments must be label strings");
            }
            if (!(compObj instanceof JComponent)) {
                throw new IllegalArgumentException("Odd arguments must be JComponents");
            }

            // Label
            JLabel label = new JLabel((String) labelObj);
            label.setFont(new Font("Segoe UI", Font.BOLD, 13));
            label.setForeground(new Color(80, 80, 80));
            
            gbc.gridx = 0;
            gbc.gridy = i / 2;
            gbc.weightx = 0.3;
            formPanel.add(label, gbc);

            // Component
            JComponent comp = (JComponent) compObj;
            comp.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            comp.setPreferredSize(new Dimension(250, 35)); // Taller and wider default

            // Apply Modern Border if it's a Text Component
            if (comp instanceof javax.swing.text.JTextComponent) {
                comp.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)), 
                    new EmptyBorder(5, 8, 5, 8)
                ));
            }
            
            gbc.gridx = 1;
            gbc.weightx = 0.7;
            formPanel.add(comp, gbc);
        }
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // Remove outer border to look cleaner in JOptionPane
        mainPanel.setBorder(null);

        return mainPanel;
    }
}
