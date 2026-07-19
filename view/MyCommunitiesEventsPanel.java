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

// left half = which communities youve joined, right half = every upcoming
// event across all of them, pulled together into one list
public class MyCommunitiesEventsPanel extends JPanel {

    Frame frame;
    JPanel listPanel;
    DefaultListModel<String> eventsModel;

    MyCommunitiesEventsPanel(Frame frame){
        this.frame = frame;
        setBackground(new Color(245, 243, 250));
        setLayout(new BorderLayout());

        JLabel title = new JLabel("  My Communities & Events", SwingConstants.LEFT);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setPreferredSize(new Dimension(500, 60));
        add(title, BorderLayout.NORTH);

        JPanel split = new JPanel(new GridLayout(1, 2, 15, 0));
        split.setBackground(new Color(245, 243, 250));

        // left = my communities
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(new Color(245, 243, 250));
        JLabel lLabel = new JLabel("Joined", SwingConstants.CENTER);
        lLabel.setFont(new Font("Arial", Font.BOLD, 16));
        lLabel.setPreferredSize(new Dimension(300, 35));
        leftPanel.add(lLabel, BorderLayout.NORTH);

        listPanel = new JPanel(new GridLayout(0, 1, 0, 8));
        listPanel.setBackground(new Color(245, 243, 250));
        JScrollPane leftScroll = new JScrollPane(listPanel);
        leftScroll.setBorder(null);
        leftPanel.add(leftScroll, BorderLayout.CENTER);

        // right = all upcoming events from my communities
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(new Color(245, 243, 250));
        JLabel rLabel = new JLabel("Upcoming Events", SwingConstants.CENTER);
        rLabel.setFont(new Font("Arial", Font.BOLD, 16));
        rLabel.setPreferredSize(new Dimension(300, 35));
        rightPanel.add(rLabel, BorderLayout.NORTH);

        eventsModel = new DefaultListModel<>();
        JList<String> eventsList = new JList<>(eventsModel);
        rightPanel.add(new JScrollPane(eventsList), BorderLayout.CENTER);

        split.add(leftPanel);
        split.add(rightPanel);
        add(split, BorderLayout.CENTER);
    }

    public void refresh(){
        listPanel.removeAll();

        if (frame.my_communities.size() == 0){
            JLabel empty = new JLabel("you havent joined anything yet :(", SwingConstants.CENTER);
            empty.setForeground(Color.GRAY);
            listPanel.add(empty);
        }

        for (int i = 0; i < frame.my_communities.size(); i++) {
            Community c = frame.my_communities.get(i);

            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
            row.setBackground(Color.WHITE);
            JLabel name = new JLabel(c.get_name() + "  (" + c.get_member_count() + " members)");
            name.setPreferredSize(new Dimension(250, 25));
            JButton viewButton = new JButton("view");
            viewButton.setFocusable(false);
            viewButton.addActionListener(e -> frame.open_community(c));
            row.add(name);
            row.add(viewButton);
            listPanel.add(row);
        }

        // collect events from every joined community , nested loop but its fine.
        // no model.Notification here yet either -- this is just the demo events list
        eventsModel.clear();
        for (int i = 0; i < frame.my_communities.size(); i++) {
            for (int j = 0; j < frame.my_communities.get(i).get_events().size(); j++) {
                eventsModel.addElement("[" + frame.my_communities.get(i).get_name() + "] " + frame.my_communities.get(i).get_events().get(j));
            }
        }
        if (eventsModel.size() == 0){
            eventsModel.addElement("no events , join some communities first!");
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

}
