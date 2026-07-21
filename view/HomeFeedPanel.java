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

import model.Community;
import net.Api;

// the very first sidebar page -- one card per community. The real backend already
// ranks these by match % (RecommendationEngine on the server), so we just render
// what Api.homeMatches returns.
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

    public void refresh(){
        feedPanel.removeAll();

        ArrayList<Community> list;
        try {
            // server returns communities the user hasn't joined, ranked by match %
            list = Api.get().homeMatches(frame.username());
        } catch (Exception ex){
            list = new ArrayList<>();
        }

        if (list.isEmpty()){
            JLabel empty = new JLabel("  You're all caught up! Check Discover for more.");
            empty.setForeground(Color.GRAY);
            empty.setFont(new Font("Arial", Font.PLAIN, 14));
            feedPanel.add(empty);
        } else {
            for (int i = 0; i < list.size(); i++) {
                feedPanel.add(make_card(list.get(i)));
            }
        }

        feedPanel.revalidate();
        feedPanel.repaint();
    }

    public JPanel make_card(final Community c){
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(220, 215, 235)));
        card.setPreferredSize(new Dimension(600, 85));

        JPanel textPart = new JPanel(new GridLayout(3, 1));
        textPart.setBackground(Color.WHITE);
        JLabel nameLabel = new JLabel("  " + c.getName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 17));
        JLabel matchLabel = new JLabel("  " + c.getMatchPercent() + "% match");
        matchLabel.setForeground(new Color(56, 142, 60));
        matchLabel.setFont(new Font("Arial", Font.BOLD, 13));
        String cat = c.getTags().isEmpty() ? c.getCategory() : c.getTags().get(0);
        JLabel memberLabel = new JLabel("  " + c.getMemberCount() + " members  ·  " + cat);
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
        if (c.isMember()){
            joinButton.setText("Joined");
            joinButton.setEnabled(false);
        }
        joinButton.addActionListener(e -> {
            try {
                Api.get().join(frame.username(), c.getId());
                joinButton.setText("Joined");
                joinButton.setEnabled(false);
            } catch (IllegalArgumentException ex){
                javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });

        buttonPart.add(viewButton);
        buttonPart.add(joinButton);
        card.add(buttonPart, BorderLayout.EAST);

        return card;
    }

}
