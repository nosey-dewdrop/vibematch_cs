package screens;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import model.Community;
import model.Post;
import model.User;
import net.Api;
import ui.RoundedButton;
import ui.RoundedPanel;
import ui.Theme;
import ui.UiHelper;

/*
 * The page for a single community. Shows the cover, the join / leave button, the
 * description, and the forum discussions. Members can start a new post. Clicking
 * a post opens it with its comments.
 */
public class CommunityDetailPanel extends JPanel implements net.PushListener {

    private MainWindow main;
    private User user;
    private Community community;
    private Api api = Api.get();

    public CommunityDetailPanel(MainWindow main, User user, Community community) {
        this.main = main;
        this.user = user;
        // fetch the community once, scored and with membership filled in, so the
        // rest of the screen doesnt keep asking the server the same things
        this.community = api.scoreOne(user.getUsername(), community.getId());

        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(18, 28, 10, 28));

        add(buildBackRow(), BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(buildBody());
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildBackRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        JLabel back = new JLabel("←  Back to discover");
        back.setFont(Theme.bodyBold(13));
        back.setForeground(Theme.LILAC_600);
        back.setCursor(new Cursor(Cursor.HAND_CURSOR));
        back.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                main.showDiscover();
            }
        });
        row.add(back, BorderLayout.WEST);
        return row;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        body.add(buildCover());
        body.add(UiHelper.vgap(14));
        body.add(buildHeaderRow());
        body.add(UiHelper.vgap(10));
        body.add(leftLabel(description()));
        body.add(UiHelper.vgap(22));
        body.add(buildDiscussionsHeader());
        body.add(UiHelper.vgap(12));
        body.add(buildPostList());
        return body;
    }

    private JPanel buildCover() {
        RoundedPanel cover = new RoundedPanel(18, parseColor(community.getCoverColor()));
        cover.setLayout(new BorderLayout());
        cover.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));
        cover.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        cover.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel emoji = new JLabel(community.getEmoji());
        emoji.setFont(new Font("SansSerif", Font.PLAIN, 44));
        cover.add(emoji, BorderLayout.WEST);
        return cover;
    }

    private JPanel buildHeaderRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JLabel name = UiHelper.title(community.getName(), 24);
        left.add(name);
        left.add(UiHelper.vgap(4));
        String meta = community.getMemberCount() + " members  ·  " + community.getCategory()
                + "  ·  " + community.getMatchPercent() + "% match";
        left.add(UiHelper.muted(meta, 13));
        row.add(left, BorderLayout.WEST);

        row.add(buildJoinButton(), BorderLayout.EAST);
        return row;
    }

    private JPanel buildJoinButton() {
        JPanel wrap = new JPanel(new java.awt.GridBagLayout());
        wrap.setOpaque(false);
        if (community.isMember()) {
            RoundedButton leave = new RoundedButton("Leave", Theme.LILAC_100, Theme.LILAC_700);
            leave.setPreferredSize(new Dimension(120, 42));
            leave.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    api.leave(user.getUsername(), community.getId());
                    main.openCommunity(community);
                }
            });
            wrap.add(leave);
        } else {
            RoundedButton join = new RoundedButton("+ Join community", Theme.LILAC_500, Color.WHITE);
            join.setPreferredSize(new Dimension(160, 42));
            join.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    api.join(user.getUsername(), community.getId());
                    main.openCommunity(community);
                }
            });
            wrap.add(join);
        }
        return wrap;
    }

    private JPanel buildDiscussionsHeader() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(UiHelper.title("Discussions", 17), BorderLayout.WEST);

        RoundedButton newPost = new RoundedButton("+ New post", Theme.LILAC_500, Color.WHITE);
        newPost.setPreferredSize(new Dimension(130, 38));
        newPost.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openNewPost();
            }
        });
        JPanel right = new JPanel(new java.awt.GridBagLayout());
        right.setOpaque(false);
        right.add(newPost);
        row.add(right, BorderLayout.EAST);
        return row;
    }

    private JPanel buildPostList() {
        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setAlignmentX(Component.LEFT_ALIGNMENT);

        ArrayList<Post> posts = api.posts(community.getId());
        if (posts.isEmpty()) {
            JLabel empty = UiHelper.muted("No posts yet. Be the first to start a discussion!", 14);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            list.add(empty);
            return list;
        }
        for (int i = 0; i < posts.size(); i++) {
            list.add(buildPostRow(posts.get(i)));
            list.add(UiHelper.vgap(10));
        }
        return list;
    }

    private JPanel buildPostRow(final Post p) {
        RoundedPanel row = new RoundedPanel(16, Theme.WHITE);
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel title = new JLabel(p.getTitle());
        title.setFont(Theme.heading(15));
        title.setForeground(Theme.INK);
        row.add(title);
        row.add(UiHelper.vgap(4));
        row.add(UiHelper.muted("by " + p.getAuthor() + "  ·  " + p.getCommentCount() + " comments", 12));

        row.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                main.openPost(p, community);
            }
        });
        return row;
    }

    private void openNewPost() {
        if (!community.isMember()) {
            JOptionPane.showMessageDialog(this,
                "Join the community first to start a discussion.",
                "Join to post", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Window window = SwingUtilities.getWindowAncestor(this);
        NewPostDialog dialog = new NewPostDialog(window, community.getId(), user.getUsername(),
            new Runnable() {
                public void run() {
                    main.openCommunity(community); // refresh the list
                }
            });
        dialog.setVisible(true);
    }

    // ---- small helpers ----

    // someone posted or commented in this community, refresh the post list live
    public void onPush(String event, String dataJson) {
        if (!event.equals("forumUpdate")) {
            return;
        }
        int communityId = protocol.Json.parse(dataJson).get("communityId").getAsInt();
        if (communityId == community.getId()) {
            main.openCommunity(community);
        }
    }

    private String description() {
        return "<html><div style='width:620px'>" + community.getDescription() + "</div></html>";
    }

    private JLabel leftLabel(String html) {
        JLabel l = new JLabel(html);
        l.setFont(Theme.body(14));
        l.setForeground(Theme.INK_SOFT);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private Color parseColor(String hex) {
        try {
            if (hex != null && hex.length() == 6) {
                return Color.decode("#" + hex);
            }
        } catch (Exception e) {
            // ignore, use default
        }
        return Theme.LILAC_200;
    }
}
