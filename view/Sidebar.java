package view;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

// the always-visible nav bar on the left once youre past onboarding.
// just a bunch of buttons that call frame.go_to(...), doesnt hold any state itself
public class Sidebar extends JPanel {

    Frame frame;
    JButton notifButton; // kept so we can put an unread count on it
    JButton chatsButton; // kept so we can put an unread message count on it

    //main purple: new Color(103, 58, 183)
    //light bg: new Color(245, 243, 250)
    //dark side: new Color(40, 35, 60)

    Sidebar(Frame frame){
        this.frame = frame;
        setBackground(new Color(40, 35, 60));
        setPreferredSize(new Dimension(190, 650));
        setLayout(new BorderLayout());

        JLabel logo = new JLabel("VibeMatch", SwingConstants.CENTER);
        logo.setForeground(new Color(103, 58, 183));
        logo.setFont(new Font("Arial", Font.BOLD, 22));
        logo.setPreferredSize(new Dimension(190, 70));
        add(logo, BorderLayout.NORTH);

        // (6 rows but only 5 buttons get added -- leaves a little gap at the bottom, looks fine)
        JPanel buttontPanel = new JPanel(new GridLayout(6, 1, 0, 8));
        buttontPanel.setBackground(new Color(40, 35, 60));

        JButton homeButton = new JButton("Home");
        homeButton.setFocusable(false);
        homeButton.addActionListener(e -> frame.go_to("home"));

        JButton discoverButton = new JButton("Discover");
        discoverButton.setFocusable(false);
        discoverButton.addActionListener(e -> frame.go_to("discover"));

        JButton mycomButton = new JButton("My Communities");
        mycomButton.setFocusable(false);
        mycomButton.addActionListener(e -> frame.go_to("mycom"));

        chatsButton = new JButton("Chats");
        chatsButton.setFocusable(false);
        chatsButton.addActionListener(e -> frame.go_to("chats"));

        notifButton = new JButton("Notifications");
        notifButton.setFocusable(false);
        notifButton.addActionListener(e -> frame.go_to("notifications"));

        JButton profileButton = new JButton("Profile");
        profileButton.setFocusable(false);
        profileButton.addActionListener(e -> frame.go_to("profile"));


        buttontPanel.add(homeButton);
        buttontPanel.add(discoverButton);
        buttontPanel.add(mycomButton);
        buttontPanel.add(chatsButton);
        buttontPanel.add(notifButton);
        buttontPanel.add(profileButton);

        add(buttontPanel, BorderLayout.CENTER);

        JButton logoutButton = new JButton("Log Out");
        logoutButton.setFocusable(false);
        logoutButton.addActionListener(e -> frame.logout());
        JPanel bottom = new JPanel();
        bottom.setBackground(new Color(40, 35, 60));
        bottom.add(logoutButton);
        add(bottom, BorderLayout.SOUTH);
    }

    // put the unread count on the Notifications button (or clear it at 0)
    public void setNotifCount(int count){
        if (count > 0){
            notifButton.setText("Notifications (" + count + ")");
        } else {
            notifButton.setText("Notifications");
        }
    }

    // put the unread message count on the Chats button
    public void setChatCount(int count){
        if (count > 0){
            chatsButton.setText("Chats (" + count + ")");
        } else {
            chatsButton.setText("Chats");
        }
    }

}
