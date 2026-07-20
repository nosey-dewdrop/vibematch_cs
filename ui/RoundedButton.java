package ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;

/*
 * A flat rounded button in our colors. It gets a little darker when you hover
 * so it feels clickable. We use this for the main actions (Log in, Join, etc).
 */
public class RoundedButton extends JButton {

    private Color baseColor;
    private Color hoverColor;
    private boolean hovering = false;
    private int radius = 16;

    public RoundedButton(String text, Color base, Color textColor) {
        super(text);
        this.baseColor = base;
        this.hoverColor = base.darker();
        setForeground(textColor);
        setFont(Theme.bodyBold(15));
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                hovering = true;
                repaint();
            }
            public void mouseExited(MouseEvent e) {
                hovering = false;
                repaint();
            }
        });
    }

    public void setRadius(int r) {
        this.radius = r;
    }

    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (hovering) {
            g2.setColor(hoverColor);
        } else {
            g2.setColor(baseColor);
        }
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        g2.dispose();
        super.paintComponent(g); // this draws the text on top
    }
}
