package screens;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import model.Community;
import model.User;
import net.Api;
import ui.RoundedButton;
import ui.Theme;
import ui.UiHelper;

/*
 * The communities the user has joined. Just a grid of their communities with a
 * little prompt to go find more if they only have a few (or none).
 */
public class MyCommunitiesPanel extends JPanel implements CommunityCard.Listener {

    private MainWindow main;
    private User user;
    private Api api = Api.get();

    public MyCommunitiesPanel(MainWindow main, User user) {
        this.main = main;
        this.user = user;

        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(20, 28, 10, 28));

        add(buildHeader(), BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(buildBody());
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));

        ArrayList<Community> joined = api.joined(user.getUsername());
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(UiHelper.title("My Communities", 22));
        left.add(UiHelper.vgap(3));
        left.add(UiHelper.muted(joined.size() + " communities you're part of 💜", 13));
        header.add(left, BorderLayout.WEST);

        RoundedButton find = new RoundedButton("+ Find more", Theme.LILAC_100, Theme.LILAC_700);
        find.setPreferredSize(new java.awt.Dimension(120, 38));
        find.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                main.showDiscover();
            }
        });
        JPanel right = new JPanel(new java.awt.GridBagLayout());
        right.setOpaque(false);
        right.add(find);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 16));
        body.setOpaque(false);

        ArrayList<Community> joined = api.joined(user.getUsername());
        if (joined.isEmpty()) {
            JPanel empty = new JPanel();
            empty.setOpaque(false);
            empty.setLayout(new BoxLayout(empty, BoxLayout.Y_AXIS));
            JLabel l1 = UiHelper.title("No communities yet", 18);
            l1.setAlignmentX(Component.LEFT_ALIGNMENT);
            empty.add(l1);
            empty.add(UiHelper.vgap(6));
            JLabel l2 = UiHelper.muted("Head to Discover and join a few that match your vibe.", 14);
            l2.setAlignmentX(Component.LEFT_ALIGNMENT);
            empty.add(l2);
            body.add(empty);
            return body;
        }

        for (int i = 0; i < joined.size(); i++) {
            body.add(new CommunityCard(joined.get(i), true, false, this));
        }
        return body;
    }

    // ---- card callbacks ----

    public void open(Community c) {
        main.openCommunity(c);
    }

    public void join(Community c) {
        // not used here, everything in this list is already joined
    }
}
