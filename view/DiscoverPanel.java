package view;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.DefaultListModel;
import javax.swing.SwingConstants;
import javax.swing.BorderFactory;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;

import model.Community;
import net.Api;

// browse-by-category page, plus a "trending" (most members) list on the right.
// The community data now comes from the real backend (Api.listCommunities);
// grouping and the trending sort stay client-side.
public class DiscoverPanel extends JPanel {

    Frame frame;
    JPanel shelvesPanel;
    JList<String> trendingList;
    DefaultListModel<String> trendingModel;
    ArrayList<Community> trending_communities = new ArrayList<>();

    DiscoverPanel(Frame frame){
        this.frame = frame;
        setBackground(new Color(245, 243, 250));
        setLayout(new BorderLayout());

        JLabel title = new JLabel("  Discover", SwingConstants.LEFT);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setPreferredSize(new Dimension(400, 60));
        add(title, BorderLayout.NORTH);

        shelvesPanel = new JPanel(new GridLayout(0, 1, 0, 5));
        shelvesPanel.setBackground(new Color(245, 243, 250));
        JScrollPane scroll = new JScrollPane(shelvesPanel);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        // trending part on the right
        JPanel trendingPanel = new JPanel(new BorderLayout());
        trendingPanel.setBackground(new Color(245, 243, 250));
        trendingPanel.setPreferredSize(new Dimension(230, 500));
        JLabel tLabel = new JLabel("Trending", SwingConstants.CENTER);
        tLabel.setFont(new Font("Arial", Font.BOLD, 17));
        tLabel.setPreferredSize(new Dimension(230, 40));
        trendingPanel.add(tLabel, BorderLayout.NORTH);

        trendingModel = new DefaultListModel<>();
        trendingList = new JList<>(trendingModel);
        trendingPanel.add(new JScrollPane(trendingList), BorderLayout.CENTER);

        JButton openTrending = new JButton("view selected");
        openTrending.setFocusable(false);
        openTrending.addActionListener(e -> {
            int idx = trendingList.getSelectedIndex();
            if (idx != -1){
                frame.open_community(trending_communities.get(idx));
            }
        });
        trendingPanel.add(openTrending, BorderLayout.SOUTH);

        add(trendingPanel, BorderLayout.EAST);
    }

    public void refresh(){
        shelvesPanel.removeAll();

        ArrayList<Community> all;
        try {
            all = Api.get().listCommunities(frame.username());
        } catch (Exception ex){
            all = new ArrayList<>();
            JLabel err = new JLabel("Couldn't load communities: " + ex.getMessage());
            err.setForeground(new Color(180, 60, 60));
            shelvesPanel.add(err);
        }

        // collect the unique categories
        ArrayList<String> categories = new ArrayList<>();
        for (int i = 0; i < all.size(); i++) {
            String cat = catOf(all.get(i));
            if (!categories.contains(cat)){
                categories.add(cat);
            }
        }

        // one shelf per category
        for (int i = 0; i < categories.size(); i++) {
            JPanel shelf = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
            shelf.setBackground(new Color(245, 243, 250));

            JLabel catLabel = new JLabel(categories.get(i) + ":");
            catLabel.setFont(new Font("Arial", Font.BOLD, 15));
            catLabel.setPreferredSize(new Dimension(120, 30));
            shelf.add(catLabel);

            for (int j = 0; j < all.size(); j++) {
                final Community c = all.get(j);
                if (catOf(c).equals(categories.get(i))){
                    JButton b = new JButton(c.getName());
                    b.setFocusable(false);
                    b.setBackground(Color.WHITE);
                    b.addActionListener(e -> frame.open_community(c));
                    shelf.add(b);
                }
            }
            shelvesPanel.add(shelf);
        }

        // trending = most members, top 5
        trending_communities.clear();
        for (int i = 0; i < all.size(); i++) {
            trending_communities.add(all.get(i));
        }
        for (int i = 0; i < trending_communities.size(); i++) {
            for (int j = 0; j < trending_communities.size()-1; j++) {
                if (trending_communities.get(j).getMemberCount() < trending_communities.get(j+1).getMemberCount()){
                    Community temp = trending_communities.get(j);
                    trending_communities.set(j, trending_communities.get(j+1));
                    trending_communities.set(j+1, temp);
                }
            }
        }
        while (trending_communities.size() > 5){
            trending_communities.remove(trending_communities.size()-1);
        }

        trendingModel.clear();
        for (int i = 0; i < trending_communities.size(); i++) {
            Community c = trending_communities.get(i);
            trendingModel.addElement((i+1) + ". " + c.getName() + " (" + c.getMemberCount() + ")");
        }

        shelvesPanel.revalidate();
        shelvesPanel.repaint();
    }

    // category label: prefer the first tag, fall back to the category field
    private String catOf(Community c){
        if (c.getTags() != null && !c.getTags().isEmpty()){
            return c.getTags().get(0);
        }
        String cat = c.getCategory();
        return cat == null || cat.isEmpty() ? "Other" : cat;
    }

}
