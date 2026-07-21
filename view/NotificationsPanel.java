package view;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.DefaultListModel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;

import model.Notification;
import net.Api;

// "Notifications" -- the team's design didn't include this screen, but the real
// backend already produces notifications (a new message, a friend request, a
// friend accept). This lists them in Khalil's house style, and opening the page
// marks them read (which clears the sidebar badge).
public class NotificationsPanel extends JPanel {

    Frame frame;
    DefaultListModel<String> model;

    NotificationsPanel(Frame frame){
        this.frame = frame;
        setBackground(new Color(245, 243, 250));
        setLayout(new BorderLayout());

        JLabel title = new JLabel("  Notifications", SwingConstants.LEFT);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setPreferredSize(new Dimension(500, 60));
        add(title, BorderLayout.NORTH);

        model = new DefaultListModel<>();
        JList<String> list = new JList<>(model);
        list.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);
    }

    public void refresh(){
        model.clear();
        ArrayList<Notification> notes;
        try {
            notes = Api.get().notifications(frame.username());
        } catch (Exception ex){
            model.addElement("  couldn't load notifications: " + ex.getMessage());
            return;
        }
        if (notes.isEmpty()){
            model.addElement("  no notifications yet");
        } else {
            for (int i = 0; i < notes.size(); i++) {
                model.addElement("  🔔  " + notes.get(i).getText());
            }
        }
        // opening the page clears the unread badge
        try {
            Api.get().markNotificationsRead(frame.username());
        } catch (Exception ignore) {}
        frame.updateNotificationBadge();
    }

}
