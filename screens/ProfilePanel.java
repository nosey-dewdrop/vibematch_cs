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
