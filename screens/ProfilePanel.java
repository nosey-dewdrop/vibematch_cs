package screens;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import model.User;
import net.Api;
import service.MbtiService;
import ui.RoundedButton;
import ui.RoundedPanel;
import ui.Theme;
import ui.UiHelper;

/*
 * The user's own profile. Shows their archetype, their interests (which they can
 * edit), how many communities they're in, and buttons to retake the test or go
 * to settings.
 */
public class ProfilePanel extends JPanel {

    private MainWindow main;
    private User user;
    private Api api = Api.get();
    private MbtiService mbti = new MbtiService();

    public ProfilePanel(MainWindow main, User user) {
        this.main = main;
        this.user = user;

        setLayout(new java.awt.GridBagLayout());
        setBackground(Theme.BG);

        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setPreferredSize(new Dimension(440, 560));

        col.add(buildCard());
        col.add(UiHelper.vgap(16));
        col.add(buildStats());
        col.add(UiHelper.vgap(16));
        col.add(buildSpotify());
        col.add(UiHelper.vgap(16));
        col.add(buildButtons());

        add(col);
    }

    private JPanel buildCard() {
        RoundedPanel card = new RoundedPanel(20, Theme.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel avatar = new JLabel("🦊");
        avatar.setFont(new Font("SansSerif", Font.PLAIN, 48));
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(avatar);
        card.add(UiHelper.vgap(8));

        JLabel name = UiHelper.title(user.getDisplayName(), 22);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(name);
        card.add(UiHelper.vgap(4));

        String type = user.getMbtiType();
        String vibe = type == null ? "no vibe yet" : (mbti.archetypeEmoji(type) + " " + type + " · " + mbti.archetypeName(type));
        JLabel sub = UiHelper.muted("@" + user.getUsername() + "  ·  " + vibe, 13);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(sub);
        card.add(UiHelper.vgap(16));

        card.add(buildInterestTags());
        card.add(UiHelper.vgap(16));

        RoundedButton edit = UiHelper.primaryButton("Edit interests");
        edit.setPreferredSize(new Dimension(10, 42));
        edit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        edit.setAlignmentX(Component.CENTER_ALIGNMENT);
        edit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openEditInterests();
            }
        });
        card.add(edit);
        return card;
    }

    private JPanel buildInterestTags() {
        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
        wrap.setOpaque(false);
        wrap.setAlignmentX(Component.CENTER_ALIGNMENT);
        ArrayList<String> list = user.getInterests();
        if (list.isEmpty()) {
            wrap.add(UiHelper.muted("No interests picked yet", 12));
        }
        for (int i = 0; i < list.size(); i++) {
            wrap.add(tag(list.get(i)));
        }
        return wrap;
    }

    private JLabel tag(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.bodyBold(11));
        l.setForeground(Theme.LILAC_700);
        l.setOpaque(true);
        l.setBackground(Theme.LILAC_100);
        l.setBorder(BorderFactory.createEmptyBorder(5, 11, 5, 11));
        return l;
    }

    private JPanel buildStats() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.CENTER_ALIGNMENT);
        int joined = api.joined(user.getUsername()).size();
        row.add(statTile("" + joined, "Communities"));
        String type = user.getMbtiType() == null ? "—" : user.getMbtiType();
        row.add(statTile(type, "Your type"));
        return row;
    }

    private JPanel statTile(String big, String label) {
        RoundedPanel tile = new RoundedPanel(16, Theme.WHITE);
        tile.setLayout(new BoxLayout(tile, BoxLayout.Y_AXIS));
        tile.setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 22));
        JLabel b = new JLabel(big);
        b.setFont(Theme.heading(22));
        b.setForeground(Theme.LILAC_600);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        tile.add(b);
        JLabel l = UiHelper.muted(label, 12);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        tile.add(l);
        return tile;
    }

    // spotify card: connect button when off, taste + disconnect when on
    private JPanel buildSpotify() {
        RoundedPanel card = new RoundedPanel(16, Theme.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        JLabel head = new JLabel("🎵  Spotify");
        head.setFont(Theme.bodyBold(14));
        head.setForeground(Theme.INK);
        head.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(head);
        card.add(UiHelper.vgap(8));

        // build had never fetched -> ask the server once
        model.SpotifyProfile sp = null;
        try {
            sp = api.getSpotify(user.getUsername());
        } catch (Exception e) {
            sp = null;
        }

        if (!api.spotifyAvailable()) {
            JLabel off = UiHelper.muted("Spotify isn't set up on this build.", 12);
            off.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(off);
            return card;
        }

        if (sp != null && sp.isConnected()) {
            String who = sp.getDisplayName() != null ? sp.getDisplayName() : "your account";
            JLabel connected = UiHelper.muted("Connected as " + who, 12);
            connected.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(connected);
            card.add(UiHelper.vgap(8));

            ArrayList<String> artists = sp.getTopArtists();
            if (artists != null && !artists.isEmpty()) {
                JLabel top = new JLabel("Top artists");
                top.setFont(Theme.bodyBold(12));
                top.setForeground(Theme.LILAC_700);
                top.setAlignmentX(Component.LEFT_ALIGNMENT);
                card.add(top);
                card.add(UiHelper.vgap(4));
                int show = Math.min(5, artists.size());
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < show; i++) {
                    if (i > 0) sb.append(" · ");
                    sb.append(artists.get(i));
                }
                JLabel list = new JLabel("<html><div style='width:340px'>" + sb + "</div></html>");
                list.setFont(Theme.body(12));
                list.setForeground(Theme.INK);
                list.setAlignmentX(Component.LEFT_ALIGNMENT);
                card.add(list);
                card.add(UiHelper.vgap(10));
            }

            RoundedButton disc = new RoundedButton("Disconnect Spotify", Theme.LILAC_100, Theme.LILAC_700);
            disc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            disc.setAlignmentX(Component.LEFT_ALIGNMENT);
            disc.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    disconnectSpotify();
                }
            });
            card.add(disc);
        } else {
            JLabel pitch = UiHelper.muted("Connect Spotify to match on your music taste.", 12);
            pitch.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(pitch);
            card.add(UiHelper.vgap(10));

            RoundedButton conn = new RoundedButton("Connect Spotify", new Color(0x1D, 0xB9, 0x54), Color.WHITE);
            conn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            conn.setAlignmentX(Component.LEFT_ALIGNMENT);
            conn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    conn.setEnabled(false); // block a second click starting a 2nd flow
                    connectSpotify();
                }
            });
            card.add(conn);
        }
        return card;
    }

    private void connectSpotify() {
        // the oauth flow blocks (browser + network), so run it off the EDT and
        // show a little wait dialog, then refresh the profile.
        final javax.swing.JDialog wait = new javax.swing.JDialog(
                SwingUtilities.getWindowAncestor(this), "Connecting Spotify");
        JLabel msg = new JLabel("  Approve in your browser, then come back…  ");
        msg.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        wait.add(msg);
        wait.pack();
        wait.setLocationRelativeTo(this);
        wait.setModal(false);

        new Thread(new Runnable() {
            public void run() {
                final String[] err = {null};
                try {
                    api.connectSpotify(user.getUsername());
                } catch (Exception ex) {
                    err[0] = ex.getMessage() == null ? "" : ex.getMessage();
                }
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        wait.dispose();
                        if (err[0] != null) {
                            javax.swing.JOptionPane.showMessageDialog(ProfilePanel.this,
                                    err[0].trim().isEmpty() ? "Spotify connect failed. Try again." : err[0]);
                        }
                        // refresh: interests may have changed too
                        try {
                            user.setInterests(api.getUser(user.getUsername()).getInterests());
                        } catch (Exception ignore) {}
                        main.showProfile();
                    }
                });
            }
        }).start();
        wait.setVisible(true);
    }

    private void disconnectSpotify() {
        try {
            api.disconnectSpotify(user.getUsername());
            user.setInterests(api.getUser(user.getUsername()).getInterests());
        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage());
        }
        main.showProfile();
    }

    private JPanel buildButtons() {
        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setAlignmentX(Component.CENTER_ALIGNMENT);

        RoundedButton retake = new RoundedButton("Retake vibe test", Theme.LILAC_100, Theme.LILAC_700);
        retake.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        retake.setAlignmentX(Component.CENTER_ALIGNMENT);
        retake.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                main.getAppFrame().startMbti(user);
            }
        });
        col.add(retake);
        col.add(UiHelper.vgap(8));

        RoundedButton settings = new RoundedButton("Settings", Theme.LILAC_100, Theme.LILAC_700);
        settings.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        settings.setAlignmentX(Component.CENTER_ALIGNMENT);
        settings.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                main.showSettings();
            }
        });
        col.add(settings);
        return col;
    }

    private void openEditInterests() {
        Window window = SwingUtilities.getWindowAncestor(this);
        EditInterestsDialog dialog = new EditInterestsDialog(window, user, new Runnable() {
            public void run() {
                main.showProfile(); // refresh tags
            }
        });
        dialog.setVisible(true);
    }
}
