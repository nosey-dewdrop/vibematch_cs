package screens;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import model.Community;
import model.User;
import net.Api;
import ui.BackgroundTask;
import ui.RoundedButton;
import ui.Theme;
import ui.UiHelper;

/*
 * Discover screen. You can search communities by name, filter by category, or
 * just browse everything. Cards show your match percent so you can see whats
 * worth joining.
 */
public class DiscoverPanel extends JPanel implements CommunityCard.Listener {

    private MainWindow main;
    private User user;
    private Api api = Api.get();

    private JTextField searchField = new JTextField();
    private JPanel gridHolder;
    private String activeCategory = "All";

    public DiscoverPanel(MainWindow main, User user) {
        this.main = main;
        this.user = user;

        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(20, 28, 10, 28));

        add(buildHeader(), BorderLayout.NORTH);

        gridHolder = new JPanel(new BorderLayout());
        gridHolder.setOpaque(false);
        JScrollPane scroll = new JScrollPane(gridHolder);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        loadAll();
    }

    // load everything in the background so opening discover doesnt freeze
    private void loadAll() {
        showLoading();
        new BackgroundTask<ArrayList<Community>>() {
            protected ArrayList<Community> work() {
                return api.listCommunities(user.getUsername());
            }
            protected void done(ArrayList<Community> list) {
                showCommunities(list);
            }
        }.start();
    }

    private void loadCategory(final String category) {
        showLoading();
        new BackgroundTask<ArrayList<Community>>() {
            protected ArrayList<Community> work() {
                return api.byCategory(user.getUsername(), category);
            }
            protected void done(ArrayList<Community> list) {
                showCommunities(list);
            }
        }.start();
    }

    private void loadSearch(final String text) {
        showLoading();
        new BackgroundTask<ArrayList<Community>>() {
            protected ArrayList<Community> work() {
                return api.search(user.getUsername(), text);
            }
            protected void done(ArrayList<Community> list) {
                showCommunities(list);
            }
        }.start();
    }

    private void showLoading() {
        gridHolder.removeAll();
        gridHolder.add(UiHelper.muted("Loading…", 14), BorderLayout.NORTH);
        gridHolder.revalidate();
        gridHolder.repaint();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));

        JLabel title = UiHelper.title("Discover", 22);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(title);
        header.add(UiHelper.vgap(12));

        searchField.setFont(Theme.body(14));
        JPanel searchBox = UiHelper.field(searchField);
        searchBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        searchField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runSearch();
            }
        });
        header.add(searchBox);
        header.add(UiHelper.vgap(12));

        header.add(buildCategoryRow());
        return header;
    }

    private JPanel buildCategoryRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        addCategoryButton(row, "All");
        for (int i = 0; i < model.Categories.ALL.length; i++) {
            addCategoryButton(row, model.Categories.ALL[i]);
        }
        return row;
    }

    private void addCategoryButton(JPanel row, final String category) {
        boolean active = category.equals(activeCategory);
        Color bg = active ? Theme.LILAC_500 : Theme.WHITE;
        Color fg = active ? Color.WHITE : Theme.INK_SOFT;
        RoundedButton b = new RoundedButton(category, bg, fg);
        b.setPreferredSize(new Dimension(10, 34));
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                activeCategory = category;
                searchField.setText("");
                refilter();
            }
        });
        row.add(b);
    }

    private void refilter() {
        if (activeCategory.equals("All")) {
            loadAll();
        } else {
            loadCategory(activeCategory);
        }
        // rebuild header so the active category button updates its color
        removeAll();
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 28, 10, 28));
        add(buildHeader(), BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(gridHolder);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private void runSearch() {
        String text = searchField.getText().trim();
        activeCategory = "All";
        if (text.isEmpty()) {
            loadAll();
        } else {
            loadSearch(text);
        }
    }

    private void showCommunities(ArrayList<Community> list) {
        JPanel grid = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 16));
        grid.setOpaque(false);

        if (list.isEmpty()) {
            grid.add(UiHelper.muted("No communities found. Try another search.", 14));
        }
        for (int i = 0; i < list.size(); i++) {
            Community c = list.get(i);
            // membership already came back with the list, no extra round trip
            grid.add(new CommunityCard(c, c.isMember(), true, this));
        }

        gridHolder.removeAll();
        gridHolder.add(grid, BorderLayout.CENTER);
        gridHolder.revalidate();
        gridHolder.repaint();
    }

    // ---- card callbacks ----

    public void open(Community c) {
        main.openCommunity(c);
    }

    public void join(Community c) {
        try {
            api.join(user.getUsername(), c.getId());
        } catch (IllegalArgumentException ex) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    ex.getMessage() == null ? "Couldn't join. Try again." : ex.getMessage());
            return;
        }
        c.setMember(true);
        refilter();
    }
}
