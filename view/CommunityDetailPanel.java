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

// zoomed-in view of one community, reached from a "View" button on a card
// elsewhere. frame.open_community(c) is what sets "current" below and switches here
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
        JLabel eLabel = new JLabel("Upcoming Events", SwingConstants.CENTER);
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

    // TODO: should be controller/CommunityController.joinCommunity()/leaveCommunity()
    // calling the real model.Community.addMember()/removeMember() eventually
    public void join_or_leave(){
        if (current == null){
            return;
        }
        if (frame.my_communities.contains(current)){
            frame.my_communities.remove(current);
            current.member_count = current.member_count - 1;
        }else{
            frame.my_communities.add(current);
            current.member_count = current.member_count + 1;
        }
        refresh();
    }

    public void refresh(){
        if (current == null){
            nameLabel.setText("no community selected");
            return;
        }
        nameLabel.setText(current.get_name());
        matchLabel.setText(current.get_match_percent() + "% match with you");
        memberLabel.setText(current.get_member_count() + " members  ·  " + current.get_category());
        descLabel.setText(current.get_description());

        eventsModel.clear();
        for (int i = 0; i < current.get_events().size(); i++) {
            eventsModel.addElement(current.get_events().get(i));
        }
        if (current.get_events().size() == 0){
            eventsModel.addElement("no upcoming events yet");
        }

        if (frame.my_communities.contains(current)){
            joinButton.setText("Leave");
        }else{
            joinButton.setText("Join");
        }
    }

}
