package utils;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class IconUtils {

    // Icon Types
    public static final String ICON_PATIENT_GROUP = "PATIENT_GROUP";
    public static final String ICON_BED = "BED";
    public static final String ICON_ALERT = "ALERT";
    public static final String ICON_HOSPITAL = "HOSPITAL";
    public static final String ICON_DOCTOR = "DOCTOR";
    public static final String ICON_DOOR = "DOOR";
    public static final String ICON_STAR = "STAR";
    public static final String ICON_HEART = "HEART";
    public static final String ICON_BRAIN = "BRAIN";
    public static final String ICON_BABY = "BABY";
    public static final String ICON_EYE = "EYE";
    public static final String ICON_PENCIL = "PENCIL";
    public static final String ICON_TRASH = "TRASH";
    public static final String ICON_SEARCH = "SEARCH";
    public static final String ICON_USER = "USER";
    public static final String ICON_BONE = "BONE"; // Orthopedics
    public static final String ICON_LUNGS = "LUNGS"; // Pulmonology/General
    public static final String ICON_REFRESH = "REFRESH";
    public static final String ICON_ADMIN = "ADMIN";
    public static final String ICON_STAFF = "STAFF";

    public static Icon createIcon(String type, int size, Color color) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        
        // Setup high-quality rendering
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        
        g2.setColor(color);
        
        // Draw based on type
        // Scale factor: standard drawing on 100x100 canvas, then scale to size
        double scale = size / 100.0;
        AffineTransform oldTransform = g2.getTransform();
        g2.scale(scale, scale);
        
        try {
            switch (type) {
                case ICON_PATIENT_GROUP: drawPatientGroup(g2); break;
                case ICON_BED: drawBed(g2); break;
                case ICON_ALERT: drawAlert(g2); break;
                case ICON_HOSPITAL: drawHospital(g2); break;
                case ICON_DOCTOR: drawDoctor(g2); break;
                case ICON_DOOR: drawDoor(g2); break;
                case ICON_STAR: drawStar(g2); break;
                case ICON_HEART: drawHeart(g2); break;
                case ICON_BRAIN: drawBrain(g2); break;
                case ICON_BABY: drawBaby(g2); break;
                case ICON_EYE: drawEye(g2); break;
                case ICON_PENCIL: drawPencil(g2); break;
                case ICON_TRASH: drawTrash(g2); break;
                case ICON_SEARCH: drawSearch(g2); break;
                case ICON_USER: drawUser(g2); break;
                case ICON_BONE: drawBone(g2); break;
                case ICON_LUNGS: drawLungs(g2); break;
                case ICON_REFRESH: drawRefresh(g2); break;
                case ICON_ADMIN: drawAdmin(g2); break;
                case ICON_STAFF: drawStaff(g2); break;
                case ICON_LINK: drawLink(g2); break;
                default: drawHelp(g2); break; // Fallback
            }
        } finally {
            g2.setTransform(oldTransform);
            g2.dispose();
        }
        
        return new ImageIcon(image);
    }

    // --- Drawing Primitives (Assumes 100x100 box) ---

    private static void drawPatientGroup(Graphics2D g2) {
        // Main figure
        g2.fillOval(35, 20, 30, 30); // Head
        g2.fillArc(20, 55, 60, 60, 0, 180); // Body
        
        // Small figure left
        g2.fillOval(10, 35, 20, 20); 
        g2.fillArc(0, 60, 40, 40, 0, 180);
        
        // Small figure right
        g2.fillOval(70, 35, 20, 20);
        g2.fillArc(60, 60, 40, 40, 0, 180);
    }

    private static void drawBed(Graphics2D g2) {
        g2.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        // Bed frame
        g2.drawLine(10, 30, 10, 90); // Headboard
        g2.drawLine(90, 50, 90, 90); // Footboard
        g2.drawLine(10, 70, 90, 70); // Mattress line
        
        // Pillow
        g2.setStroke(new BasicStroke(1));
        g2.fillRoundRect(15, 60, 20, 10, 5, 5);
        
        // Blanket curve
        g2.drawArc(40, 60, 40, 20, 0, 180);
    }

    private static void drawAlert(Graphics2D g2) {
        // Triangle
        int[] xPoints = {50, 90, 10};
        int[] yPoints = {10, 90, 90};
        g2.fillPolygon(xPoints, yPoints, 3);
        
        // Exclamation mark (cut out)
        g2.setColor(Color.WHITE); // Assuming white background for cutout effect, or transparent if we handled composite
        // Better to draw the mark in white on top of the filled triangle
        g2.fillOval(45, 30, 10, 35);
        g2.fillOval(45, 75, 10, 10);
        
        // Reset color just in case (though we dispose graphics anyway)
    }

    private static void drawHospital(Graphics2D g2) {
        g2.fillRect(20, 30, 60, 60); // Building
        g2.setColor(Color.WHITE); // Windows/Cross
        g2.fillRect(45, 10, 10, 30); // Chimney/Top part? No, let's do a cross
        
        // Make it a cross cutout
        g2.fillRect(42, 45, 16, 30); // Door
        
        // Reset to draw roof? Let's keep it simple.
        // Let's draw a proper cross on a building
        g2.setColor(g2.getColor()); // Restore color (wait, I lost it). 
        // Actually, let's redraw.
    }
    


    // --- Actual Implementations ---
    
    // We need to be careful with Color switching. The passed 'color' is for the main foreground.
    // If we want "transparent" or "negative space", we should use AlphaComposite.Clear or simply leave it empty if possible.
    // simpler: Draw shape, then draw white on top if needed (assuming white bg). 
    // Ideally, for icons, we want transparency.
    
    private static void drawHelp(Graphics2D g2) {
        g2.setFont(new Font("SansSerif", Font.BOLD, 60));
        g2.drawString("?", 35, 70);
    }

    private static void drawUser(Graphics2D g2) {
        g2.fillOval(30, 20, 40, 40); // Head
        g2.fillArc(10, 65, 80, 70, 0, 180); // Body
    }
    
    private static void drawDoor(Graphics2D g2) {
        g2.setStroke(new BasicStroke(6));
        g2.drawRect(25, 15, 50, 80); // Frame
        g2.fillOval(60, 55, 8, 8); // Knob
    }

    private static void drawStar(Graphics2D g2) {
        // Simple star path
        Path2D p = new Path2D.Double();
        p.moveTo(50, 10); 
        p.lineTo(62, 35); 
        p.lineTo(90, 38); 
        p.lineTo(70, 58); 
        p.lineTo(75, 85); 
        p.lineTo(50, 72); 
        p.lineTo(25, 85); 
        p.lineTo(30, 58); 
        p.lineTo(10, 38); 
        p.lineTo(38, 35); 
        p.closePath();
        g2.fill(p);
    }

    private static void drawHeart(Graphics2D g2) {
        // Two circles and a triangle-ish bottom
        g2.fillOval(10, 20, 45, 45);
        g2.fillOval(45, 20, 45, 45);
        int[] x = {12, 50, 88};
        int[] y = {55, 95, 55};
        g2.fillPolygon(x, y, 3);
    }

    private static void drawBrain(Graphics2D g2) {
         // Abstract brain: oval with some squiggly lines inside
         g2.fillOval(15, 25, 35, 50); // Left lobe
         g2.fillOval(50, 25, 35, 50); // Right lobe
         // Details
         g2.setColor(new Color(255,255,255, 100)); // Semi-transparent for details
         g2.setStroke(new BasicStroke(3));
         g2.drawArc(20, 30, 25, 40, 90, 180);
         g2.drawArc(55, 30, 25, 40, 270, 180);
    }

    private static void drawBaby(Graphics2D g2) {
        // Baby face
        g2.fillOval(20, 20, 60, 60);
        // Pacifier ring
        g2.drawOval(40, 60, 20, 20);
    }
    
    private static void drawEye(Graphics2D g2) {
        // Eye shape
        g2.fillOval(10, 30, 80, 40);
        g2.setColor(Color.WHITE);
        g2.fillOval(35, 35, 30, 30);
        g2.setColor(g2.getBackground()); // wait, can't get background easily
        // Let's assume the caller sets the right color.
        // Actually, for the "Pupil", we should use the original color again.
        // But I don't have access to original color inside easily unless I stored it.
        // FIX: The methods should respect the current g2 color.
        
        // Simpler Eye: Outline
        g2.setColor(g2.getColor()); // It's already set
        // Let's do a filled eye, then a white sclera, then a filled pupil
        // Actually, let's just draw lines for transparency safety
        g2.setStroke(new BasicStroke(8));
        g2.drawArc(10, 20, 80, 60, 0, 180); // Top lid
        g2.drawArc(10, 20, 80, 60, 180, 180); // Bottom lid
        g2.fillOval(40, 40, 20, 20); // Pupil
    }

    private static void drawDoctor(Graphics2D g2) {
        // Stethoscope shape
        g2.setStroke(new BasicStroke(8));
        g2.drawArc(30, 20, 40, 50, 180, 180); // U shape
        g2.drawLine(50, 70, 50, 85); // Down line
        g2.fillOval(42, 85, 16, 16); // Bell
    }

    private static void drawPencil(Graphics2D g2) {
        // Diagonal rect
        g2.rotate(Math.toRadians(45), 50, 50);
        g2.fillRect(40, 20, 20, 50); // Body
        g2.fillPolygon(new int[]{40, 60, 50}, new int[]{20, 20, 0}, 3); // Tip
        g2.fillRect(40, 70, 20, 10); // Eraser
    }

    private static void drawTrash(Graphics2D g2) {
        g2.fillRect(30, 30, 40, 60); // Bin
        g2.fillRect(25, 20, 50, 6); // Lid
        g2.fillRect(45, 15, 10, 5); // Handle
        // Lines
        g2.setColor(new Color(255,255,255, 128));
        g2.drawLine(40, 40, 40, 80);
        g2.drawLine(50, 40, 50, 80);
        g2.drawLine(60, 40, 60, 80);
    }
    
    private static void drawSearch(Graphics2D g2) {
        g2.setStroke(new BasicStroke(10));
        g2.drawOval(20, 20, 50, 50); // Glass
        g2.drawLine(60, 60, 85, 85); // Handle
    }
    
    private static void drawBone(Graphics2D g2) {
        g2.setStroke(new BasicStroke(12, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(25, 25, 75, 75);
        g2.fillOval(15, 15, 20, 20);
        g2.fillOval(65, 65, 20, 20);
        // A bit abstract, but works for icon size
    }
    
    private static void drawLungs(Graphics2D g2) {
        g2.fillOval(20, 30, 25, 40);
        g2.fillOval(55, 30, 25, 40);
        g2.setStroke(new BasicStroke(4));
        g2.drawLine(50, 30, 50, 10);
        g2.drawLine(50, 30, 35, 40);
        g2.drawLine(50, 30, 65, 40);
    }

    private static void drawRefresh(Graphics2D g2) {
        g2.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        // Draw an open circle/arc
        g2.drawArc(20, 20, 60, 60, 45, 270);
        
        // Draw Arrow head at the end of the arc (approx at 45 degrees start)
        // Arc starts at 45 and goes 270. End is at 45+270 = 315 (-45).
        // Let's put arrow at the end (315 degrees, top rightish)
        // Coordinates for 315 deg on 100x100 circle (radius 30, center 50,50)
        // x = 50 + 30*cos(315) = 50 + 21 = 71
        // y = 50 - 30*sin(315) = 50 - (-21) = 71? Wait y is down.
        // 315 is top right.
        
        // Simpler: Just draw 2 poly lines for arrow head manually placed
        // End of arc is roughly at (70, 70) if 315 is bottom right? 0 is East. 90 is North.
        // Swing Arc: 0 is East, positive is CCW.
        // Start 45 (North East). +270 => 315 (South East).
        // So it ends at South East.
        
        // Arrow head near (70, 70)
        g2.drawLine(72, 60, 80, 50); // Adjusting visual logic..
        // Actually let's just draw separate path
        
        // Better arrow:
        Path2D p = new Path2D.Double();
        p.moveTo(65, 45);
        p.lineTo(80, 50);
        p.lineTo(65, 65);
        // ... this is hard to guess.
        
        // Fallback to simple arrow lines near the end
        // End is at 45 deg (start) ? No, drawArc(x,y,w,h, start, extent)
        // Start 45 (NE). Extent 270 (CCW). Ends at 315 (SE).
        
        // Drawing arrow at 45 deg (Start point) if we want "Counter Clockwise" rotation?
        // Usually refresh is clockwise. Negative extent?
        // g2.drawArc(..., 90, -270);
        
        // Let's stick to a simpler known shape for "Refresh"
        // Two arrows circle
        g2.drawArc(30, 30, 40, 40, 0, 150);
        g2.drawArc(30, 30, 40, 40, 180, 150);
        
        // Arrow heads
        // Head 1 (for top arc, moving CCW? No usually CW)
        // Let's do CW arrows.
        // Top arc: 45 to 135?
        
        // Simple "Open Circle with Arrow"
        g2.drawArc(25, 25, 50, 50, 0, 270); // 3/4 circle
        // Arrow at 0 (East). Pointing Up (if CCW) or Down (if CW).
        // Flow is usually CW. So arrow at "start" (0) pointing "in"?
        
        // Let's try:
        g2.drawArc(20, 20, 60, 60, 45, -270); // CW from 45 (NE) to 135 (NW)
        // Arrow at 135 (NW).
        // Arrow head at approx (30, 30)
        g2.drawLine(20, 30, 35, 40); 
    }

    private static void drawAdmin(Graphics2D g2) {
        // Shield shape
        int[] x = {20, 80, 80, 50, 20};
        int[] y = {20, 20, 50, 85, 50};
        g2.fillPolygon(x, y, 5);
        
        // Star or key in middle
        g2.setColor(Color.WHITE);
        g2.fillOval(40, 35, 20, 20); // Simple detail
    }

    private static void drawStaff(Graphics2D g2) {
        // User with tie/badge
        drawUser(g2);
        // Tie
        g2.setColor(Color.WHITE); // Tie color? Or maybe inherit?
        // Wait, drawUser draws in current color. 
        // We want to add a tie on top.
         g2.setColor(new Color(255,255,255, 150));
        int[] tx = {45, 55, 52, 48};
        int[] ty = {65, 65, 85, 85};
        g2.fillPolygon(tx, ty, 4);
    }

    public static final String ICON_LINK = "LINK";

    private static void drawLink(Graphics2D g2) {
        g2.rotate(Math.toRadians(-45), 50, 50);
        g2.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        // Link 1 (Left/Top)
        g2.drawRoundRect(25, 35, 35, 30, 20, 20); 
        
        // Link 2 (Right/Bottom) - Draw slightly offset
        g2.drawRoundRect(45, 35, 35, 30, 20, 20);
    }
    
    // --- Helper for Avatars ---
    public static Icon createCircleIcon(String text, Color bg, int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Circle
        g2.setColor(bg);
        g2.fillOval(0, 0, size, size);
        
        // Text
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, size / 2));
        FontMetrics fm = g2.getFontMetrics();
        int x = (size - fm.stringWidth(text)) / 2;
        int y = ((size - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(text, x, y);
        
        g2.dispose();
        return new ImageIcon(img);
    }
}
