package ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

/*
 * A JPanel that paints itself as a rounded rectangle. Swing panels are square by
 * default and our whole design is soft rounded cards, so we use this all over
 * the place instead of a plain JPanel.
 */
public class RoundedPanel extends JPanel {

    private int radius;
    private Color fill;

    public RoundedPanel(int radius, Color fill) {
        this.radius = radius;
        this.fill = fill;
        setOpaque(false); // we draw our own background
    }

    public void setFill(Color fill) {
        this.fill = fill;
        repaint();
    }

    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        g2.dispose();
        super.paintComponent(g);
    }
}
