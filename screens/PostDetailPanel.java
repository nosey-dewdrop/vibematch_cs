package screens;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import model.Comment;
import model.Community;
import model.Post;
import model.User;
import net.Api;
import protocol.Json;
import ui.RoundedButton;
import ui.RoundedPanel;
import ui.Theme;
import ui.UiHelper;

/*
 * Shows one post and its comments. Comments are threaded, replies sit indented
 * under their parent (the reddit style). You can add a top level comment at the
 * bottom or reply to any comment.
 */
public class PostDetailPanel extends JPanel implements net.PushListener {

    private MainWindow main;
    private User user;
    private Post post;
    private Community community;
    private Api api = Api.get();

    private JTextField commentField = new JTextField();

    public PostDetailPanel(MainWindow main, User user, Post post, Community community) {
        this.main = main;
        this.user = user;
        this.post = post;
        this.community = community;

        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(18, 28, 16, 28));

        add(buildBackRow(), BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(buildBody());
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        add(buildCommentBox(), BorderLayout.SOUTH);
    }

    private JPanel buildBackRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        JLabel back = new JLabel("←  Back to " + community.getName());
        back.setFont(Theme.bodyBold(13));
        back.setForeground(Theme.LILAC_600);
        back.setCursor(new Cursor(Cursor.HAND_CURSOR));
        back.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                main.openCommunity(community);
            }
        });
        row.add(back, BorderLayout.WEST);
        return row;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        // the post itself
        RoundedPanel card = new RoundedPanel(18, Theme.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = UiHelper.title(post.getTitle(), 20);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(UiHelper.vgap(4));
        JLabel by = UiHelper.muted("by " + post.getAuthor(), 12);
        by.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(by);
        card.add(UiHelper.vgap(12));
        JLabel bodyText = new JLabel("<html><div style='width:600px'>" + safe(post.getBody()) + "</div></html>");
        bodyText.setFont(Theme.body(14));
        bodyText.setForeground(Theme.INK);
        bodyText.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(bodyText);
        body.add(card);
        body.add(UiHelper.vgap(18));

        ArrayList<Comment> comments = api.comments(post.getId());
        JLabel header = UiHelper.title(comments.size() + " comments", 16);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(header);
        body.add(UiHelper.vgap(10));

        body.add(buildCommentTree(comments));
        return body;
    }

    // group comments by their parent, then render top level ones and recurse
    private JPanel buildCommentTree(ArrayList<Comment> comments) {
        JPanel holder = new JPanel();
        holder.setOpaque(false);
        holder.setLayout(new BoxLayout(holder, BoxLayout.Y_AXIS));
        holder.setAlignmentX(Component.LEFT_ALIGNMENT);

        HashMap<Integer, ArrayList<Comment>> byParent = new HashMap<Integer, ArrayList<Comment>>();
        for (int i = 0; i < comments.size(); i++) {
            Comment cm = comments.get(i);
            ArrayList<Comment> kids = byParent.get(cm.getParentId());
            if (kids == null) {
                kids = new ArrayList<Comment>();
                byParent.put(cm.getParentId(), kids);
            }
            kids.add(cm);
        }

        ArrayList<Comment> topLevel = byParent.get(0);
        if (topLevel == null || topLevel.isEmpty()) {
            holder.add(leftMuted("No comments yet. Say something nice :)"));
            return holder;
        }
        for (int i = 0; i < topLevel.size(); i++) {
            addComment(holder, topLevel.get(i), byParent, 0);
        }
        return holder;
    }

    private void addComment(JPanel holder, Comment cm, HashMap<Integer, ArrayList<Comment>> byParent, int depth) {
        holder.add(buildCommentRow(cm, depth));
        holder.add(UiHelper.vgap(8));
        ArrayList<Comment> kids = byParent.get(cm.getId());
        if (kids != null) {
            for (int i = 0; i < kids.size(); i++) {
                addComment(holder, kids.get(i), byParent, depth + 1);
            }
        }
    }

    private JPanel buildCommentRow(final Comment cm, int depth) {
        RoundedPanel row = new RoundedPanel(14, Theme.WHITE);
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        JLabel author = new JLabel(cm.getAuthor());
        author.setFont(Theme.bodyBold(13));
        author.setForeground(Theme.LILAC_700);
        row.add(author);
        row.add(UiHelper.vgap(3));
        JLabel text = new JLabel("<html><div style='width:520px'>" + safe(cm.getBody()) + "</div></html>");
        text.setFont(Theme.body(13));
        text.setForeground(Theme.INK);
        row.add(text);
        row.add(UiHelper.vgap(4));

        JLabel reply = new JLabel("Reply");
        reply.setFont(Theme.bodyBold(11));
        reply.setForeground(Theme.LILAC_500);
        reply.setCursor(new Cursor(Cursor.HAND_CURSOR));
        reply.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                replyTo(cm);
            }
        });
        row.add(reply);

        // indent replies under their parent
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrap.setBorder(BorderFactory.createEmptyBorder(0, depth * 28, 0, 0));
        wrap.add(row, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel buildCommentBox() {
        JPanel box = new JPanel(new BorderLayout(10, 0));
        box.setOpaque(false);
        box.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        commentField.setFont(Theme.body(14));
        JPanel fieldBox = UiHelper.field(commentField);
        box.add(fieldBox, BorderLayout.CENTER);

        RoundedButton send = new RoundedButton("Comment", Theme.LILAC_500, Color.WHITE);
        send.setPreferredSize(new Dimension(120, 46));
        send.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addTopLevelComment();
            }
        });
        box.add(send, BorderLayout.EAST);
        return box;
    }

    private void addTopLevelComment() {
        String text = commentField.getText().trim();
        if (text.isEmpty()) {
            return;
        }
        api.addComment(post.getId(), user.getUsername(), text, 0);
        commentField.setText("");
        main.openPost(post, community); // reload to show it
    }

    private void replyTo(Comment parent) {
        String text = JOptionPane.showInputDialog(this, "Reply to " + parent.getAuthor() + ":");
        if (text != null && !text.trim().isEmpty()) {
            api.addComment(post.getId(), user.getUsername(), text.trim(), parent.getId());
            main.openPost(post, community);
        }
    }

    // the server pushed a forum update. if it's for this community, reload so a
    // comment someone else just wrote shows up on its own, in real time.
    public void onPush(String event, String dataJson) {
        if (!event.equals("forumUpdate")) {
            return;
        }
        int communityId = Json.parse(dataJson).get("communityId").getAsInt();
        if (communityId == community.getId()) {
            main.openPost(post, community);
        }
    }

    private JLabel leftMuted(String text) {
        JLabel l = UiHelper.muted(text, 14);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    // posts/comments are plain text, escape the few html chars so the JLabel
    // html rendering doesnt break on them
    private String safe(String s) {
        if (s == null) {
            return "";
        }
        s = s.replace("&", "&amp;");
        s = s.replace("<", "&lt;");
        s = s.replace(">", "&gt;");
        return s;
    }
}
