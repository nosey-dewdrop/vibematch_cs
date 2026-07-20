package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

/*
 * A bunch of little factory methods so building screens is less repetitive.
 * Everything visual that is shared (title labels, the lilac buttons, the
 * rounded input fields) comes from here.
 */
public class UiHelper {

    // big serif title
    public static JLabel title(String text, int size) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.heading(size));
        l.setForeground(Theme.INK);
        return l;
    }

    public static JLabel label(String text, int size, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.body(size));
        l.setForeground(color);
        return l;
    }

    // greyed out secondary text
    public static JLabel muted(String text, int size) {
        return label(text, size, Theme.INK_SOFT);
    }

    public static RoundedButton primaryButton(String text) {
        RoundedButton b = new RoundedButton(text, Theme.PRIMARY, Color.WHITE);
        b.setPreferredSize(new Dimension(120, 44));
        return b;
    }

    // lighter button for less important actions
    public static RoundedButton softButton(String text) {
        return new RoundedButton(text, Theme.LILAC_100, Theme.LILAC_700);
    }

    /*
     * Wrap a text field in a white rounded box so it matches the design. The
     * field itself stays see through and we put a bit of padding around it.
     */
    public static RoundedPanel field(JTextComponent input) {
        input.setOpaque(false);
        input.setBorder(BorderFactory.createEmptyBorder());
        input.setFont(Theme.body(15));
        input.setForeground(Theme.INK);

        RoundedPanel box = new RoundedPanel(14, Theme.WHITE);
        box.setLayout(new BorderLayout());
        box.setBorder(BorderFactory.createEmptyBorder(13, 16, 13, 16));
        box.add(input, BorderLayout.CENTER);
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        return box;
    }

    // a plain text field already styled (use with field(...) above)
    public static JTextField textField() {
        return new JTextField();
    }

    // fixed vertical gap, handy in BoxLayout columns
    public static Component vgap(int h) {
        return Box.createRigidArea(new Dimension(0, h));
    }

    private UiHelper() {
    }
}
