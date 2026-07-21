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

// reached from the small "admin login" link on the login screen, no real auth at
// all right now. stands in for model/Administrator.java + controller/AdminController.java
public class AdminPanel extends JPanel {

    Frame frame;
    JPanel communitiesPanel;

    // fake flagged messages , the real ones would come from ModerationFilter
    String[] flagged = {"[Football 5v5] user_burak: <censored> (blocked word)",
            "[Anime Watchers] user_ege: <censored> (blocked word)",
            "[Code & Coffee] user_can: <censored> (spam link)"};

    AdminPanel(Frame frame){
        this.frame = frame;
        setBackground(new Color(245, 243, 250));
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(40, 35, 60));
        topPanel.setPreferredSize(new Dimension(1000, 60));
        JLabel title = new JLabel("  Admin Panel", SwingConstants.LEFT);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        topPanel.add(title, BorderLayout.CENTER);

        JButton backButton = new JButton("back to login");
        backButton.setFocusable(false);
        backButton.addActionListener(e -> frame.go_to("login"));
        JPanel backWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        backWrap.setBackground(new Color(40, 35, 60));
        backWrap.add(backButton);
        topPanel.add(backWrap, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);


        JPanel split = new JPanel(new GridLayout(1, 2, 15, 0));
        split.setBackground(new Color(245, 243, 250));

        // left = manage communities
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(new Color(245, 243, 250));
        JLabel lLabel = new JLabel("Communities", SwingConstants.CENTER);
        lLabel.setFont(new Font("Arial", Font.BOLD, 16));
        lLabel.setPreferredSize(new Dimension(300, 40));
        leftPanel.add(lLabel, BorderLayout.NORTH);

        communitiesPanel = new JPanel(new GridLayout(0, 1, 0, 6));
        communitiesPanel.setBackground(new Color(245, 243, 250));
        JScrollPane scroll = new JScrollPane(communitiesPanel);
        scroll.setBorder(null);
        leftPanel.add(scroll, BorderLayout.CENTER);

        // right = flagged messages (read only for now)
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(new Color(245, 243, 250));
        JLabel rLabel = new JLabel("Flagged Messages", SwingConstants.CENTER);
        rLabel.setFont(new Font("Arial", Font.BOLD, 16));
        rLabel.setPreferredSize(new Dimension(300, 40));
        rightPanel.add(rLabel, BorderLayout.NORTH);

        DefaultListModel<String> flaggedModel = new DefaultListModel<>();
        for (int i = 0; i < flagged.length; i++) {
            flaggedModel.addElement(flagged[i]);
        }
        JList<String> flaggedList = new JList<>(flaggedModel);
        rightPanel.add(new JScrollPane(flaggedList), BorderLayout.CENTER);

        split.add(leftPanel);
        split.add(rightPanel);
        add(split, BorderLayout.CENTER);

        JLabel note = new JLabel("logged in as admin (no real login yet , its a placeholder)", SwingConstants.CENTER);
        note.setForeground(Color.GRAY);
        note.setPreferredSize(new Dimension(1000, 35));
        add(note, BorderLayout.SOUTH);
    }

    public void refresh(){
        communitiesPanel.removeAll();

        java.util.ArrayList<model.Community> all;
        try {
            all = net.Api.get().listCommunities(frame.username());
        } catch (Exception ex){
            all = new java.util.ArrayList<>();
        }

        for (int i = 0; i < all.size(); i++) {
            model.Community c = all.get(i);

            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
            row.setBackground(Color.WHITE);

            JLabel name = new JLabel(c.getName() + "  (" + c.getMemberCount() + " members)");
            name.setPreferredSize(new Dimension(280, 25));

            // the backend has no community-delete route, so this is read-only.
            JButton delet_button = new JButton("delete");
            delet_button.setForeground(Color.GRAY);
            delet_button.setFocusable(false);
            delet_button.setEnabled(false);
            delet_button.setToolTipText("community deletion isn't exposed by the server");

            row.add(name);
            row.add(delet_button);
            communitiesPanel.add(row);
        }

        communitiesPanel.revalidate();
        communitiesPanel.repaint();
    }

}
