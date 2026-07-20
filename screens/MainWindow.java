package screens;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import model.Community;
import model.User;
import net.PushListener;
import net.ServerClient;
import ui.RoundedPanel;
import ui.Theme;
import ui.UiHelper;

/*
 * The main app once you are logged in. Left side is the sidebar with the nav,
 * the rest of the window is whatever screen is selected. The inner panels
 * (home, discover, ...) call back here to switch around.
 */
public class MainWindow extends JPanel implements PushListener {

    private AppFrame appFrame;
    private User user;

    private JPanel content;
    private JComponent currentPanel;
    private NavButton bellNav;
    private ArrayList<NavButton> navButtons = new ArrayList<NavButton>();

    public MainWindow(AppFrame appFrame, User user) {
        this.appFrame = appFrame;
        this.user = user;

        setLayout(new BorderLayout());
        setBackground(Theme.BG);

        add(buildSidebar(), BorderLayout.WEST);

        content = new JPanel(new BorderLayout());
        content.setBackground(Theme.BG);
        add(content, BorderLayout.CENTER);

        // one place listens for server pushes and forwards them to whatever
        // screen is open right now (if that screen cares). this way we never
        // pile up stale listeners as the user moves around.
        ServerClient.getInstance().addPushListener(this);

        showHome();
    }

    // a push arrived from the server. bump the bell if it's a notification, and
    // hand it to the open screen if that screen cares.
    public void onPush(String event, String dataJson) {
        if (event.equals("notification")) {
            updateBell();
        }
        if (currentPanel instanceof PushListener) {
            ((PushListener) currentPanel).onPush(event, dataJson);
        }
    }

    public User getUser() {
        return user;
    }

    public AppFrame getAppFrame() {
        return appFrame;
    }

    // stop listening for pushes (called on logout, before this window is dropped)
    public void detach() {
        ServerClient.getInstance().removePushListener(this);
    }

    // ---- sidebar ----

    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setBackground(Theme.LILAC_100);
        side.setPreferredSize(new Dimension(220, 10));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(BorderFactory.createEmptyBorder(22, 16, 18, 16));

        JLabel logo = new JLabel("v  vibematch");
        logo.setFont(Theme.heading(19));
        logo.setForeground(Theme.INK);
        logo.setAlignmentX(LEFT_ALIGNMENT);
        side.add(logo);
        side.add(UiHelper.vgap(22));

        addNav(side, "home", "🏠", "Home");
        addNav(side, "discover", "🔍", "Discover");
        addNav(side, "communities", "💜", "My Communities");
        addNav(side, "messages", "✉️", "Messages");
        bellNav = addNav(side, "notifications", "🔔", "Notifications");
        addNav(side, "profile", "🙂", "Profile");
        updateBell();

        side.add(javax.swing.Box.createVerticalGlue());
        side.add(buildUserChip());

        return side;
    }

    private NavButton addNav(JPanel side, final String key, String icon, String text) {
        NavButton nav = new NavButton(key, icon, text);
        nav.setAlignmentX(LEFT_ALIGNMENT);
        nav.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                navigate(key);
            }
        });
        navButtons.add(nav);
        side.add(nav);
        side.add(UiHelper.vgap(4));
        return nav;
    }

    // refresh the little unread count on the bell, in the background
    private void updateBell() {
        new ui.BackgroundTask<Integer>() {
            protected Integer work() {
                return Integer.valueOf(net.Api.get().unreadCount(user.getUsername()));
            }
            protected void done(Integer count) {
                if (bellNav != null) {
                    bellNav.setBadge(count.intValue());
                }
            }
        }.start();
    }

    private JPanel buildUserChip() {
        RoundedPanel chip = new RoundedPanel(14, Theme.WHITE);
        chip.setLayout(new BorderLayout(10, 0));
        chip.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        chip.setMaximumSize(new Dimension(200, 56));
        chip.setAlignmentX(LEFT_ALIGNMENT);

        JLabel avatar = new JLabel("🦊");
        avatar.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 22));
        chip.add(avatar, BorderLayout.WEST);

        JPanel txt = new JPanel();
        txt.setOpaque(false);
        txt.setLayout(new BoxLayout(txt, BoxLayout.Y_AXIS));
        JLabel name = new JLabel(user.getDisplayName());
        name.setFont(Theme.bodyBold(13));
        name.setForeground(Theme.INK);
        String type = user.getMbtiType() == null ? "" : user.getMbtiType();
        JLabel sub = UiHelper.muted(type, 11);
        txt.add(name);
        txt.add(sub);
        chip.add(txt, BorderLayout.CENTER);
        return chip;
    }

    // ---- navigation ----

    public void navigate(String key) {
        if (key.equals("home")) {
            showHome();
        } else if (key.equals("discover")) {
            showDiscover();
        } else if (key.equals("communities")) {
            showMyCommunities();
        } else if (key.equals("messages")) {
            showMessages();
        } else if (key.equals("notifications")) {
            showNotifications();
        } else if (key.equals("profile")) {
            showProfile();
        }
    }

    private void setActive(String key) {
        for (int i = 0; i < navButtons.size(); i++) {
            NavButton nb = navButtons.get(i);
            nb.setActive(nb.getKey().equals(key));
        }
    }

    public void setContent(JComponent panel) {
        currentPanel = panel;
        content.removeAll();
        content.add(panel, BorderLayout.CENTER);
        content.revalidate();
        content.repaint();
    }

    public void showHome() {
        setActive("home");
        setContent(new HomePanel(this, user));
    }

    public void showDiscover() {
        setActive("discover");
        setContent(new DiscoverPanel(this, user));
    }

    public void showMyCommunities() {
        setActive("communities");
        setContent(new MyCommunitiesPanel(this, user));
    }

    public void showMessages() {
        setActive("messages");
        setContent(new MessagesPanel(this, user));
    }

    public void showProfile() {
        setActive("profile");
        setContent(new ProfilePanel(this, user));
    }

    public void showNotifications() {
        setActive("notifications");
        setContent(new NotificationsPanel(this, user));
        // opening the list clears the unread ones
        net.Api.get().markNotificationsRead(user.getUsername());
        if (bellNav != null) {
            bellNav.setBadge(0);
        }
    }

    public void showSettings() {
        setActive("profile");
        setContent(new SettingsPanel(this, user));
    }

    // open a single community page
    public void openCommunity(Community c) {
        setActive("discover");
        setContent(new CommunityDetailPanel(this, user, c));
    }

    // open one post with its comments
    public void openPost(model.Post post, Community community) {
        setContent(new PostDetailPanel(this, user, post, community));
    }

    /*
     * One item in the sidebar. Paints a white rounded pill behind it when its
     * the active screen.
     */
    private class NavButton extends JPanel {
        private String key;
        private boolean active = false;
        private JLabel label;
        private JLabel badge;   // little unread count, hidden when zero
        private String baseText;

        NavButton(String key, String icon, String text) {
            this.key = key;
            this.baseText = icon + "   " + text;
            setOpaque(false);
            setLayout(new BorderLayout());
            setMaximumSize(new Dimension(200, 42));
            setBorder(BorderFactory.createEmptyBorder(2, 12, 2, 12));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            label = new JLabel(baseText);
            label.setFont(Theme.bodyBold(14));
            label.setForeground(Theme.INK_SOFT);
            add(label, BorderLayout.CENTER);

            badge = new JLabel("");
            badge.setFont(Theme.bodyBold(11));
            badge.setForeground(Color.WHITE);
            badge.setOpaque(true);
            badge.setBackground(new Color(0xE0, 0x7A, 0x8B));
            badge.setBorder(BorderFactory.createEmptyBorder(1, 7, 1, 7));
            badge.setVisible(false);
            add(badge, BorderLayout.EAST);
        }

        String getKey() {
            return key;
        }

        // show a red count on the right (used by the bell). 0 hides it.
        void setBadge(int count) {
            if (count > 0) {
                badge.setText(count > 9 ? "9+" : ("" + count));
                badge.setVisible(true);
            } else {
                badge.setVisible(false);
            }
            revalidate();
            repaint();
        }

        void setActive(boolean a) {
            this.active = a;
            label.setForeground(a ? Theme.LILAC_700 : Theme.INK_SOFT);
            repaint();
        }

        protected void paintComponent(java.awt.Graphics g) {
            if (active) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }
}
