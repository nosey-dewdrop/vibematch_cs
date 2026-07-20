package screens;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ui.Theme;
import ui.UiHelper;

/*
 * The split layout used by the login / register / verify screens: a colored
 * panel on the left with the brand and a friendly headline, and the actual form
 * on the right. Keeps the three auth screens looking the same.
 */
public class AuthShell extends JPanel {

    public AuthShell(String headline, String sub, Color leftColor, JPanel form) {
        setLayout(new BorderLayout());
        setBackground(Theme.WHITE);

        add(buildLeft(headline, sub, leftColor), BorderLayout.WEST);

        // form sits on the right with some breathing room
        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(Theme.WHITE);
        right.setBorder(BorderFactory.createEmptyBorder(50, 60, 50, 60));
        right.add(form, BorderLayout.CENTER);
        add(right, BorderLayout.CENTER);
    }

    private JPanel buildLeft(String headline, String sub, Color leftColor) {
        JPanel left = new JPanel();
        left.setBackground(leftColor);
        left.setPreferredSize(new Dimension(330, 10));
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBorder(BorderFactory.createEmptyBorder(40, 36, 40, 36));

        // brand row (a little v badge + the name)
        JLabel brand = new JLabel("v  vibematch");
        brand.setFont(Theme.heading(22));
        brand.setForeground(Theme.WHITE);
        left.add(brand);

        left.add(javax.swing.Box.createVerticalGlue()); // push the rest down

        JLabel head = new JLabel("<html>" + headline + "</html>");
        head.setFont(Theme.heading(30));
        head.setForeground(Theme.WHITE);
        left.add(head);

        left.add(UiHelper.vgap(12));

        JLabel subLabel = new JLabel("<html>" + sub + "</html>");
        subLabel.setFont(Theme.body(14));
        subLabel.setForeground(new Color(0xEC, 0xE4, 0xFB));
        left.add(subLabel);

        return left;
    }
}
