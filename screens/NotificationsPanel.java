package screens;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import model.Notification;
import model.User;
import net.Api;
import ui.BackgroundTask;
import ui.RoundedPanel;
import ui.Theme;
import ui.UiHelper;

/*
 * The list of notifications: friend requests, accepted requests, new messages,
 * replies to your posts. Loaded in the background. Opening this screen marks
 * them all as read (the caller does that so the bell clears).
 */
public class NotificationsPanel extends JPanel {

    private User user;
    private Api api = Api.get();
    private JPanel listHolder;

    public NotificationsPanel(MainWindow main, User user) {
        this.user = user;

        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(20, 28, 12, 28));

        JLabel title = UiHelper.title("Notifications", 22);
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        top.add(title, BorderLayout.WEST);
        add(top, BorderLayout.NORTH);

        listHolder = new JPanel();
        listHolder.setOpaque(false);
        listHolder.setLayout(new BoxLayout(listHolder, BoxLayout.Y_AXIS));
        listHolder.add(UiHelper.muted("Loading…", 14));

        JScrollPane scroll = new JScrollPane(listHolder);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        load();
    }

    private void load() {
        new BackgroundTask<ArrayList<Notification>>() {
            protected ArrayList<Notification> work() {
                return api.notifications(user.getUsername());
            }
            protected void done(ArrayList<Notification> list) {
                showList(list);
            }
        }.start();
    }

    private void showList(ArrayList<Notification> list) {
        listHolder.removeAll();
        if (list.isEmpty()) {
            listHolder.add(UiHelper.muted("Nothing here yet. We'll let you know when something happens.", 14));
        }
        for (int i = 0; i < list.size(); i++) {
            listHolder.add(buildRow(list.get(i)));
            listHolder.add(UiHelper.vgap(8));
        }
        listHolder.revalidate();
        listHolder.repaint();
    }

    private JPanel buildRow(Notification n) {
        RoundedPanel row = new RoundedPanel(14, n.isRead() ? Theme.WHITE : Theme.LILAC_100);
        row.setLayout(new BorderLayout(12, 0));
        row.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel icon = new JLabel(iconFor(n.getType()));
        icon.setFont(new Font("SansSerif", Font.PLAIN, 20));
        row.add(icon, BorderLayout.WEST);

        JLabel text = new JLabel(n.getText());
        text.setFont(Theme.body(14));
        text.setForeground(Theme.INK);
        row.add(text, BorderLayout.CENTER);
        return row;
    }

    private String iconFor(String type) {
        if (type.equals("friend_request")) return "👋";
        if (type.equals("friend_accepted")) return "🤝";
        if (type.equals("message")) return "✉️";
        if (type.equals("reply")) return "💬";
        return "🔔";
    }
}
