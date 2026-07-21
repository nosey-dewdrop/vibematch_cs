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

// left half = which communities you've joined, right half = recent posts across
// all of them, pulled together into one list. Backed by the real server.
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
        JLabel rLabel = new JLabel("Recent Posts", SwingConstants.CENTER);
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

    // holds everything the background thread gathered, so done() only touches UI
    private static class Loaded {
        ArrayList<Community> joined;
        ArrayList<String> postLines = new ArrayList<>();
    }

    public void refresh(){
        listPanel.removeAll();
        eventsModel.clear();
        JLabel loading = new JLabel("loading...", SwingConstants.CENTER);
        loading.setForeground(Color.GRAY);
        listPanel.add(loading);
        listPanel.revalidate();
        listPanel.repaint();

        final String username = frame.username();
        new ui.BackgroundTask<Loaded>() {
            protected Loaded work(){
                // all the server round-trips happen here, off the UI thread
                Loaded d = new Loaded();
                d.joined = Api.get().joined(username);
                for (int i = 0; i < d.joined.size(); i++) {
                    try {
                        ArrayList<Post> posts = Api.get().posts(d.joined.get(i).getId());
                        for (int j = 0; j < posts.size(); j++) {
                            d.postLines.add("[" + d.joined.get(i).getName() + "] " + posts.get(j).getTitle());
                        }
                    } catch (Exception ex){
                        // skip a community that fails to load
                    }
                }
                return d;
            }
            protected void done(Loaded d){
                listPanel.removeAll();
                if (d.joined.isEmpty()){
                    JLabel empty = new JLabel("you havent joined anything yet :(", SwingConstants.CENTER);
                    empty.setForeground(Color.GRAY);
                    listPanel.add(empty);
                }
                for (int i = 0; i < d.joined.size(); i++) {
                    final Community c = d.joined.get(i);
                    JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    row.setBackground(Color.WHITE);
                    JLabel name = new JLabel(c.getName() + "  (" + c.getMemberCount() + " members)");
                    name.setPreferredSize(new Dimension(250, 25));
                    JButton viewButton = new JButton("view");
                    viewButton.setFocusable(false);
                    viewButton.addActionListener(e -> frame.open_community(c));
                    row.add(name);
                    row.add(viewButton);
                    listPanel.add(row);
                }
                eventsModel.clear();
                for (int i = 0; i < d.postLines.size(); i++) {
                    eventsModel.addElement(d.postLines.get(i));
                }
                if (eventsModel.size() == 0){
                    eventsModel.addElement("no posts yet , join some communities first!");
                }
                listPanel.revalidate();
                listPanel.repaint();
            }
            protected void failed(Exception e){
                listPanel.removeAll();
                JLabel err = new JLabel("couldn't load: " + e.getMessage(), SwingConstants.CENTER);
                err.setForeground(new Color(180, 60, 60));
                listPanel.add(err);
                listPanel.revalidate();
                listPanel.repaint();
            }
        }.start();
    }

}
