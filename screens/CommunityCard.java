package screens;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import model.Community;
import ui.RoundedButton;
import ui.RoundedPanel;
import ui.Theme;
import ui.UiHelper;

/*
 * A card for one community, reused on home / discover / my communities. It shows
 * the colored cover with the emoji, the name, a bit of info and a button. The
 * screen that uses it passes a Listener to find out when its clicked.
 */
public class CommunityCard extends RoundedPanel {

    public interface Listener {
        void open(Community c);
        void join(Community c);
    }

    public CommunityCard(Community c, boolean isMember, boolean showMatch, final Listener listener) {
        super(20, Theme.WHITE);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(300, 200));
        setMaximumSize(new Dimension(300, 200));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        add(buildCover(c, showMatch), BorderLayout.NORTH);
        add(buildBody(c), BorderLayout.CENTER);
        add(buildButton(c, isMember, listener), BorderLayout.SOUTH);

        // clicking anywhere on the card (not the button) opens it
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                listener.open(c);
            }
        });
    }

    private JPanel buildCover(Community c, boolean showMatch) {
        RoundedPanel cover = new RoundedPanel(16, parseColor(c.getCoverColor()));
        cover.setLayout(new BorderLayout());
        cover.setPreferredSize(new Dimension(10, 66));
        cover.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JLabel emoji = new JLabel(c.getEmoji());
        emoji.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 28));
        cover.add(emoji, BorderLayout.WEST);

        if (showMatch && c.getMatchPercent() > 0) {
            JLabel match = new JLabel(c.getMatchPercent() + "% match");
            match.setFont(Theme.bodyBold(11));
            match.setForeground(Theme.LILAC_700);
            match.setOpaque(true);
            match.setBackground(Theme.WHITE);
            match.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
            JPanel wrap = new JPanel(new BorderLayout());
            wrap.setOpaque(false);
            wrap.add(match, BorderLayout.NORTH);
            cover.add(wrap, BorderLayout.EAST);
        }
        return cover;
    }

    private JPanel buildBody(Community c) {
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new javax.swing.BoxLayout(body, javax.swing.BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(10, 14, 4, 14));

        JLabel name = new JLabel(c.getName());
        name.setFont(Theme.heading(15));
        name.setForeground(Theme.INK);
        body.add(name);
        body.add(UiHelper.vgap(3));

        String meta = c.getMemberCount() + " members";
        if (!c.getTags().isEmpty()) {
            meta = meta + " · " + c.getTags().get(0);
        }
        JLabel metaLabel = UiHelper.muted(meta, 12);
        body.add(metaLabel);
        return body;
    }

    private JPanel buildButton(Community c, boolean isMember, final Listener listener) {
        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.setBorder(BorderFactory.createEmptyBorder(0, 14, 14, 14));

        final Community community = c;
        if (isMember) {
            RoundedButton open = new RoundedButton("Open", Theme.LILAC_100, Theme.LILAC_700);
            open.setPreferredSize(new Dimension(10, 36));
            open.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    listener.open(community);
                }
            });
            south.add(open, BorderLayout.CENTER);
        } else {
            RoundedButton join = new RoundedButton("+ Join", Theme.LILAC_500, Color.WHITE);
            join.setPreferredSize(new Dimension(10, 36));
            join.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    listener.join(community);
                }
            });
            south.add(join, BorderLayout.CENTER);
        }
        return south;
    }

    // cover colors are stored as hex like "D6F0E4"
    private Color parseColor(String hex) {
        try {
            if (hex != null && hex.length() == 6) {
                return Color.decode("#" + hex);
            }
        } catch (Exception e) {
            // fall through to a default
        }
        return Theme.LILAC_200;
    }
}
