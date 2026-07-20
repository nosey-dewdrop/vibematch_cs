package view;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.BorderFactory;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;

// the very first sidebar page -- one card per community, sorted by the fake match
// percent. controller/CommunityController.java + model/RecommendationEngine.java
// are meant to eventually replace sort_by_match_oder() below with the real ranking
public class HomeFeedPanel extends JPanel {

    Frame frame;
    JPanel feedPanel;

    HomeFeedPanel(Frame frame){
        this.frame = frame;
        setBackground(new Color(245, 243, 250));
        setLayout(new BorderLayout());

        JLabel title = new JLabel("  Home Feed - communities that match your vibe", SwingConstants.LEFT);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setPreferredSize(new Dimension(500, 60));
        add(title, BorderLayout.NORTH);

        feedPanel = new JPanel();
        feedPanel.setBackground(new Color(245, 243, 250));
        feedPanel.setLayout(new GridLayout(0, 1, 0, 10));

        JScrollPane scroll = new JScrollPane(feedPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    // sorts a copy of the communities by match percent , highest first
    // bubble sort , not the best way but it works and i understand it.
    // makes a copy first so we're not reordering frame.all_communities itself
    public ArrayList<Community> sort_by_match_oder(){
        ArrayList<Community> sorted = new ArrayList<>();
        for (int i = 0; i < frame.all_communities.size(); i++) {
            sorted.add(frame.all_communities.get(i));
        }

        for (int i = 0; i < sorted.size(); i++) {
            for (int j = 0; j < sorted.size()-1; j++) {
                if (sorted.get(j).get_match_percent() < sorted.get(j+1).get_match_percent()){
                    Community temp = sorted.get(j);
                    sorted.set(j, sorted.get(j+1));
                    sorted.set(j+1, temp);
                }
            }
        }
        return sorted;
    }

    public void refresh(){
        feedPanel.removeAll();

        ArrayList<Community> sorted = sort_by_match_oder();
        //System.out.println("feed size: " + sorted.size());

        for (int i = 0; i < sorted.size(); i++) {
            feedPanel.add(make_card(sorted.get(i)));
        }

        feedPanel.revalidate();
        feedPanel.repaint();
    }

    public JPanel make_card(Community c){
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(220, 215, 235)));
        card.setPreferredSize(new Dimension(600, 85));

        JPanel textPart = new JPanel(new GridLayout(3, 1));
        textPart.setBackground(Color.WHITE);
        JLabel nameLabel = new JLabel("  " + c.get_name());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 17));
        JLabel matchLabel = new JLabel("  " + frame.get_match_percent(c) + "% match");
        matchLabel.setForeground(new Color(56, 142, 60));
        matchLabel.setFont(new Font("Arial", Font.BOLD, 13));
        JLabel memberLabel = new JLabel("  " + c.get_member_count() + " members  ·  " + c.get_category());
        memberLabel.setForeground(Color.GRAY);
        textPart.add(nameLabel);
        textPart.add(matchLabel);
        textPart.add(memberLabel);
        card.add(textPart, BorderLayout.CENTER);

        JPanel buttonPart = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 25));
        buttonPart.setBackground(Color.WHITE);

        JButton viewButton = new JButton("View");
        viewButton.setFocusable(false);
        viewButton.addActionListener(e -> frame.open_community(c));

        JButton joinButton = new JButton("Join");
        joinButton.setFocusable(false);
        if (frame.my_communities.contains(c)){
            joinButton.setText("Joined");
            joinButton.setEnabled(false);
        }
        joinButton.addActionListener(e -> {
            frame.join_community(c);
            joinButton.setText("Joined");
            joinButton.setEnabled(false);
        });

        buttonPart.add(viewButton);
        buttonPart.add(joinButton);
        card.add(buttonPart, BorderLayout.EAST);

        return card;
    }

}
