package view;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.DefaultListModel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;

import model.Community;
import model.Post;
import net.Api;

// zoomed-in view of one community, reached from a "View" button on a card
// elsewhere. frame.open_community(c) sets "current" and switches here. Join/leave
// and the posts list are backed by the real server now.
public class CommunityDetailPanel extends JPanel {

    Frame frame;
    Community current = null; // whichever community was last clicked, null before that ever happens

    JLabel nameLabel;
    JLabel matchLabel;
    JLabel memberLabel;
    JLabel descLabel;
    JButton joinButton;
    DefaultListModel<String> eventsModel;

    CommunityDetailPanel(Frame frame){
        this.frame = frame;
        setBackground(new Color(245, 243, 250));
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new GridLayout(4, 1));
        topPanel.setBackground(new Color(245, 243, 250));
        topPanel.setPreferredSize(new Dimension(600, 180));

        nameLabel = new JLabel("no community selected", SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 28));

        matchLabel = new JLabel("", SwingConstants.CENTER);
        matchLabel.setForeground(new Color(56, 142, 60));
        matchLabel.setFont(new Font("Arial", Font.BOLD, 15));

        memberLabel = new JLabel("", SwingConstants.CENTER);
        memberLabel.setForeground(Color.GRAY);

        descLabel = new JLabel("", SwingConstants.CENTER);
        descLabel.setFont(new Font("Arial", Font.PLAIN, 15));

        topPanel.add(nameLabel);
        topPanel.add(matchLabel);
        topPanel.add(memberLabel);
        topPanel.add(descLabel);
        add(topPanel, BorderLayout.NORTH);


        JPanel eventsPanel = new JPanel(new BorderLayout());
        eventsPanel.setBackground(new Color(245, 243, 250));
        JLabel eLabel = new JLabel("Posts", SwingConstants.CENTER);
        eLabel.setFont(new Font("Arial", Font.BOLD, 17));
        eLabel.setPreferredSize(new Dimension(400, 40));
        eventsPanel.add(eLabel, BorderLayout.NORTH);

        eventsModel = new DefaultListModel<>();
        JList<String> eventsList = new JList<>(eventsModel);
        JPanel listWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        listWrap.setBackground(new Color(245, 243, 250));
        JScrollPane sp = new JScrollPane(eventsList);
        sp.setPreferredSize(new Dimension(450, 150));
        listWrap.add(sp);
        eventsPanel.add(listWrap, BorderLayout.CENTER);
        add(eventsPanel, BorderLayout.CENTER);


        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.setBackground(new Color(245, 243, 250));

        joinButton = new JButton("Join");
        joinButton.setPreferredSize(new Dimension(150, 42));
        joinButton.setFocusable(false);
        joinButton.addActionListener(e -> join_or_leave());

        JButton backButton = new JButton("back to feed");
        backButton.setFocusable(false);
        backButton.addActionListener(e -> frame.go_to("home"));

        bottomPanel.add(joinButton);
        bottomPanel.add(backButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void set_community(Community c){
        this.current = c;
    }

    // join or leave through the real backend, then reload
    public void join_or_leave(){
        if (current == null){
            return;
        }
        try {
            if (current.isMember()){
                Api.get().leave(frame.username(), current.getId());
            }else{
                Api.get().join(frame.username(), current.getId());
            }
            // re-fetch so member state + count + match come back fresh from the server
            current = Api.get().scoreOne(frame.username(), current.getId());
        } catch (IllegalArgumentException ex){
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage());
        }
        refresh();
    }

    public void refresh(){
        if (current == null){
            nameLabel.setText("no community selected");
            return;
        }
        nameLabel.setText(current.getName());
        matchLabel.setText(current.getMatchPercent() + "% match with you");
        String cat = current.getTags().isEmpty() ? current.getCategory() : current.getTags().get(0);
        memberLabel.setText(current.getMemberCount() + " members  ·  " + cat);
        descLabel.setText(current.getDescription());

        // real forum posts for this community
        eventsModel.clear();
        try {
            ArrayList<Post> posts = Api.get().posts(current.getId());
            if (posts.isEmpty()){
                eventsModel.addElement("no posts yet");
            } else {
                for (int i = 0; i < posts.size(); i++) {
                    eventsModel.addElement(posts.get(i).getTitle() + "  —  " + posts.get(i).getAuthor());
                }
            }
        } catch (Exception ex){
            eventsModel.addElement("couldn't load posts");
        }

        joinButton.setText(current.isMember() ? "Leave" : "Join");
    }

}
