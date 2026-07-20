package ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

/*
 * A pill shaped toggle button used for interests. Click it and it fills in with
 * the lilac color to show its selected, click again to turn it off.
 */
public class Chip extends JButton {

    private boolean selected = false;

    public Chip(String text) {
        super(text);
        setFont(Theme.bodyBold(13));
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        updateColors();

        // toggle ourselves on click
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selected = !selected;
                updateColors();
                repaint();
            }
        });
    }

    public boolean isOn() {
        return selected;
    }

    public void setOn(boolean on) {
        this.selected = on;
        updateColors();
        repaint();
    }

    private void updateColors() {
        if (selected) {
            setForeground(Color.WHITE);
        } else {
            setForeground(Theme.INK_SOFT);
        }
    }

    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        // a bit of horizontal padding so the pill isnt cramped
        return new Dimension(d.width + 26, 34);
    }

    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (selected) {
            g2.setColor(Theme.LILAC_500);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
        } else {
            g2.setColor(Theme.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.setColor(Theme.LILAC_200);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
