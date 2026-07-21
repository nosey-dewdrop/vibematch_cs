package screens;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import model.User;
import ui.RoundedPanel;
import ui.Session;
import ui.Theme;
import ui.UiHelper;

/*
 * Settings screen. Account info, an about box and the log out button. Kept
 * simple on purpose, the important one here is log out.
 */
public class SettingsPanel extends JPanel {

    private MainWindow main;
    private User user;

    public SettingsPanel(MainWindow main, User user) {
        this.main = main;
        this.user = user;

        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(20, 28, 16, 28));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        header.add(UiHelper.title("Settings", 22), BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));

        col.add(row("👤  Account", Theme.INK, new Runnable() {
            public void run() {
                JOptionPane.showMessageDialog(SettingsPanel.this,
                    "Signed in as " + user.getDisplayName()
                    + "\nUsername: " + user.getUsername()
                    + "\nEmail: " + user.getEmail(),
                    "Account", JOptionPane.INFORMATION_MESSAGE);
            }
        }));
        col.add(UiHelper.vgap(10));

        col.add(row("ℹ️  About vibematch", Theme.INK, new Runnable() {
            public void run() {
                JOptionPane.showMessageDialog(SettingsPanel.this,
                    "vibematch\nFind your people at Bilkent.\n\nA student community app.",
                    "About", JOptionPane.INFORMATION_MESSAGE);
            }
        }));
        col.add(UiHelper.vgap(10));

        col.add(row("↪  Log out", new java.awt.Color(0xE0, 0x7A, 0x8B), new Runnable() {
            public void run() {
                logout();
            }
        }));

        add(col, BorderLayout.CENTER);
    }

    private JPanel row(String text, java.awt.Color textColor, final Runnable onClick) {
        RoundedPanel row = new RoundedPanel(14, Theme.WHITE);
        row.setLayout(new BorderLayout());
        row.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel label = new JLabel(text);
        label.setFont(Theme.bodyBold(14));
        label.setForeground(textColor);
        row.add(label, BorderLayout.WEST);

        JLabel chevron = new JLabel("›");
        chevron.setFont(Theme.body(18));
        chevron.setForeground(Theme.INK_SOFT);
        row.add(chevron, BorderLayout.EAST);

        row.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                onClick.run();
            }
        });
        return row;
    }

    private void logout() {
        int choice = JOptionPane.showConfirmDialog(this,
            "Log out of vibematch?", "Log out",
            JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            net.Api.get().logout(); // tell the server to drop this socket's user
            main.detach(); // stop listening for pushes for this session
            Session.clear();
            main.getAppFrame().showLogin();
        }
    }
}
