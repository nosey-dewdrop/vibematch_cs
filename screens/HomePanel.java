package screens;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import ui.BackgroundTask;
import ui.RoundedPanel;
import ui.Theme;
import ui.UiHelper;

/*
 * The home screen: a greeting, a banner telling you how many communities fit
 * your vibe, and a grid of your top matches. Clicking a card opens it, the join
 * button joins right away.
 */
public class HomePanel extends JPanel implements CommunityCard.Listener {

    private MainWindow main;
    private User user;
    private Api api = Api.get();
    private JPanel bodyHolder;

    public HomePanel(MainWindow main, User user) {
        this.main = main;
        this.user = user;

        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(20, 28, 10, 28));

        add(buildTopBar(), BorderLayout.NORTH);

        // show the frame right away with a gentle loading line, then fetch the
        // matches in the background so the window never freezes
        bodyHolder = new JPanel(new BorderLayout());
        bodyHolder.setOpaque(false);
        JLabel loading = UiHelper.muted("Finding your communities…", 14);
        loading.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        bodyHolder.add(loading, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(bodyHolder);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        loadMatches();
    }

    private void loadMatches() {
        new BackgroundTask<ArrayList<Community>>() {
            protected ArrayList<Community> work() {
                return api.homeMatches(user.getUsername());
            }
            protected void done(ArrayList<Community> top) {
                bodyHolder.removeAll();
                bodyHolder.add(buildBody(top), BorderLayout.CENTER);
                bodyHolder.revalidate();
                bodyHolder.repaint();
            }
        }.start();
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));

        JPanel greet = new JPanel();
        greet.setOpaque(false);
        greet.setLayout(new BoxLayout(greet, BoxLayout.Y_AXIS));
        greet.add(UiHelper.muted("Good to see you,", 12));
        JLabel name = UiHelper.title(firstName() + " ✨", 22);
        greet.add(name);
        bar.add(greet, BorderLayout.WEST);

        JLabel avatar = new JLabel("🦊");
        avatar.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 26));
        bar.add(avatar, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildBody(ArrayList<Community> top) {
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        body.add(buildHero(countGoodMatches(top)));
        body.add(UiHelper.vgap(20));

        JLabel section = UiHelper.title("Top picks for you", 17);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel sectionWrap = leftRow(section);
        body.add(sectionWrap);
        body.add(UiHelper.vgap(12));

        body.add(buildCardGrid(top));
        return body;
    }

    private int countGoodMatches(ArrayList<Community> list) {
        int n = 0;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getMatchPercent() >= 50) {
                n++;
            }
        }
        if (n == 0) {
            n = list.size(); // at least show something friendly
        }
        return n;
    }

    private JPanel buildHero(int count) {
        RoundedPanel hero = new RoundedPanel(20, Theme.LILAC_600);
        hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));
        hero.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));
        hero.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel kicker = new JLabel("MATCHED FOR YOU");
        kicker.setFont(Theme.bodyBold(11));
        kicker.setForeground(new Color(0xE2, 0xD5, 0xF6));
        hero.add(kicker);
        hero.add(UiHelper.vgap(4));

        JLabel big = new JLabel(count + " communities fit your vibe 🌿");
        big.setFont(Theme.heading(20));
        big.setForeground(Color.WHITE);
        hero.add(big);
        return hero;
    }

    private JPanel buildCardGrid(ArrayList<Community> list) {
        JPanel grid = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 16));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (int i = 0; i < list.size(); i++) {
            Community c = list.get(i);
            // these are all communities the user hasnt joined yet
            grid.add(new CommunityCard(c, false, true, this));
        }
        return grid;
    }

    private JPanel leftRow(Component c) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(c);
        return row;
    }

    private String firstName() {
        String dn = user.getDisplayName();
        if (dn == null || dn.isEmpty()) {
            return user.getUsername();
        }
        int space = dn.indexOf(' ');
        return space > 0 ? dn.substring(0, space) : dn;
    }

    // ---- card callbacks ----

    public void open(Community c) {
        main.openCommunity(c);
    }

    public void join(Community c) {
        api.join(user.getUsername(), c.getId());
        main.showHome(); // rebuild so the card flips to "Open"
    }
}
