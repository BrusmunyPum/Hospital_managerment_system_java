package views;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Color;

public class MainApp {
    public static void main(String[] args) {
        try {
            // // use the built-in "Nimbus" which is better than default
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            
            // Customize Nimbus to be flatter and whiter
            UIManager.put("control", Color.WHITE); // Global background
            UIManager.put("info", Color.WHITE);    // Tooltip/Info bg
            UIManager.put("nimbusBase", new Color(240, 240, 240)); // Subtle distinction
            UIManager.put("nimbusBlueGrey", new Color(240, 240, 240)); // Header/Selection
            UIManager.put("nimbusLightBackground", Color.WHITE); // Text fields, etc.
            
            UIManager.put("OptionPane.background", Color.WHITE);
            UIManager.put("Panel.background", Color.WHITE);
            UIManager.put("Dialog.background", Color.WHITE); // Dialog frame bg
        } catch (Exception e) {
            System.out.println("Could not set theme");
        }

        SwingUtilities.invokeLater(() -> {
            try {
                LoginView login = new LoginView();
                login.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
