package screens;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import net.Api;
import model.Message;
import model.User;
import ui.RoundedButton;
import ui.RoundedPanel;
import ui.Theme;
import ui.UiHelper;

/*
 * Direct messages, 1 on 1, but only with your FRIENDS. The left side shows your
 * friends (people you can chat with) and, above them, any friend requests
 * waiting for you to accept or decline. The + button sends a friend request.
 *
 * Because of the friends rule, random people can't message you: you both have to
 * be friends first.
 */
public class MessagesPanel extends JPanel implements net.PushListener {

    private MainWindow main;
    private User user;
    private Api api = Api.get();

    private String selectedPartner = null;
    private JPanel centerHolder;
    private JPanel leftList;
    private JTextField input = new JTextField();

    public MessagesPanel(MainWindow main, User user) {
        this.main = main;
        this.user = user;

        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 16, 24));

        add(buildLeft(), BorderLayout.WEST);

        centerHolder = new JPanel(new BorderLayout());
        centerHolder.setOpaque(false);
        centerHolder.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 0));
        add(centerHolder, BorderLayout.CENTER);

        refreshLeft();
        showConversation();
    }

    private JPanel buildLeft() {
        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(240, 10));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        top.add(UiHelper.title("Messages", 20), BorderLayout.WEST);
        JLabel plus = new JLabel("＋");
        plus.setFont(Theme.heading(20));
        plus.setForeground(Theme.LILAC_600);
        plus.setCursor(new Cursor(Cursor.HAND_CURSOR));
        plus.setToolTipText("Add a friend");
        plus.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                addFriend();
            }
        });
        top.add(plus, BorderLayout.EAST);
        left.add(top, BorderLayout.NORTH);

        leftList = new JPanel();
        leftList.setOpaque(false);
        leftList.setLayout(new BoxLayout(leftList, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(leftList);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        left.add(scroll, BorderLayout.CENTER);
        return left;
    }

    // rebuild the left column: incoming requests first, then friends
    private void refreshLeft() {
        leftList.removeAll();

        ArrayList<String> requests = api.friendRequests(user.getUsername());
        if (!requests.isEmpty()) {
            leftList.add(sectionLabel("FRIEND REQUESTS"));
            for (int i = 0; i < requests.size(); i++) {
                leftList.add(buildRequestRow(requests.get(i)));
                leftList.add(UiHelper.vgap(6));
            }
            leftList.add(UiHelper.vgap(10));
        }

        leftList.add(sectionLabel("FRIENDS"));
        ArrayList<String> friends = api.friends(user.getUsername());
        if (friends.isEmpty()) {
            JLabel empty = UiHelper.muted("No friends yet. Tap + to add one.", 12);
            empty.setAlignmentX(LEFT_ALIGNMENT);
            leftList.add(empty);
        }
        for (int i = 0; i < friends.size(); i++) {
            leftList.add(buildFriendRow(friends.get(i)));
            leftList.add(UiHelper.vgap(6));
        }

        leftList.revalidate();
        leftList.repaint();
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.bodyBold(11));
        l.setForeground(Theme.LILAC_500);
        l.setAlignmentX(LEFT_ALIGNMENT);
        l.setBorder(BorderFactory.createEmptyBorder(0, 4, 6, 0));
        return l;
    }

    private JPanel buildFriendRow(final String friend) {
        boolean active = friend.equals(selectedPartner);
        RoundedPanel row = new RoundedPanel(12, active ? Theme.LILAC_100 : Theme.WHITE);
        row.setLayout(new BorderLayout(10, 0));
        row.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        row.setMaximumSize(new Dimension(220, 50));
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel avatar = new JLabel("🙂");
        row.add(avatar, BorderLayout.WEST);
        JLabel name = new JLabel(friend);
        name.setFont(Theme.bodyBold(13));
        name.setForeground(Theme.INK);
        row.add(name, BorderLayout.CENTER);

        row.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                selectedPartner = friend;
                refreshLeft();
                showConversation();
            }
        });
        return row;
    }

    // a pending request with accept / decline buttons
    private JPanel buildRequestRow(final String requester) {
        RoundedPanel row = new RoundedPanel(12, Theme.WHITE);
        row.setLayout(new BorderLayout(8, 4));
        row.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        row.setMaximumSize(new Dimension(220, 78));
        row.setAlignmentX(LEFT_ALIGNMENT);

        JLabel name = new JLabel(requester + " wants to be friends");
        name.setFont(Theme.body(12));
        name.setForeground(Theme.INK);
        row.add(name, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        buttons.setOpaque(false);
        RoundedButton accept = new RoundedButton("Accept", Theme.LILAC_500, Color.WHITE);
        accept.setPreferredSize(new Dimension(80, 30));
        accept.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                api.respondToRequest(user.getUsername(), requester, true);
                refreshLeft();
            }
        });
        RoundedButton decline = new RoundedButton("Decline", Theme.LILAC_100, Theme.LILAC_700);
        decline.setPreferredSize(new Dimension(80, 30));
        decline.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                api.respondToRequest(user.getUsername(), requester, false);
                refreshLeft();
            }
        });
        buttons.add(accept);
        buttons.add(decline);
        row.add(buttons, BorderLayout.SOUTH);
        return row;
    }

    private void showConversation() {
        centerHolder.removeAll();
        if (selectedPartner == null) {
            JPanel hint = new JPanel(new java.awt.GridBagLayout());
            hint.setOpaque(false);
            hint.add(UiHelper.muted("Pick a friend to start chatting.", 14));
            centerHolder.add(hint, BorderLayout.CENTER);
        } else {
            centerHolder.add(buildChatHeader(), BorderLayout.NORTH);
            centerHolder.add(buildBubbles(), BorderLayout.CENTER);
            centerHolder.add(buildInput(), BorderLayout.SOUTH);
        }
        centerHolder.revalidate();
        centerHolder.repaint();
    }

    private JPanel buildChatHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 4, 12, 0));
        JLabel name = UiHelper.title(selectedPartner, 18);
        header.add(name, BorderLayout.WEST);
        return header;
    }

    private JScrollPane buildBubbles() {
        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        ArrayList<Message> msgs = api.conversation(user.getUsername(), selectedPartner);
        for (int i = 0; i < msgs.size(); i++) {
            Message m = msgs.get(i);
            boolean mine = m.getSender().equals(user.getUsername());
            list.add(buildBubble(m.getBody(), mine));
            list.add(UiHelper.vgap(6));
        }

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JPanel buildBubble(String text, boolean mine) {
        JPanel rowWrap = new JPanel(new FlowLayout(mine ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        rowWrap.setOpaque(false);
        rowWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        RoundedPanel bubble = new RoundedPanel(14, mine ? Theme.LILAC_500 : Theme.WHITE);
        bubble.setLayout(new BorderLayout());
        bubble.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        JLabel label = new JLabel("<html><div style='width:320px'>" + safe(text) + "</div></html>");
        label.setFont(Theme.body(14));
        label.setForeground(mine ? Color.WHITE : Theme.INK);
        bubble.add(label, BorderLayout.CENTER);

        rowWrap.add(bubble);
        return rowWrap;
    }

    private JPanel buildInput() {
        JPanel box = new JPanel(new BorderLayout(10, 0));
        box.setOpaque(false);
        box.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        input.setText("");
        input.setFont(Theme.body(14));
        JPanel fieldBox = UiHelper.field(input);
        box.add(fieldBox, BorderLayout.CENTER);

        RoundedButton send = new RoundedButton("Send", Theme.LILAC_500, Color.WHITE);
        send.setPreferredSize(new Dimension(100, 46));
        send.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        input.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        box.add(send, BorderLayout.EAST);
        return box;
    }

    private void sendMessage() {
        String text = input.getText().trim();
        if (text.isEmpty() || selectedPartner == null) {
            return;
        }
        try {
            api.sendMessage(user.getUsername(), selectedPartner, text);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
            return;
        }
        showConversation();
    }

    private void addFriend() {
        String name = JOptionPane.showInputDialog(this, "Add a friend by username:");
        if (name == null) {
            return;
        }
        name = name.trim();
        if (name.isEmpty()) {
            return;
        }
        try {
            api.sendFriendRequest(user.getUsername(), name);
            JOptionPane.showMessageDialog(this, "Friend request sent to " + name + ".");
            refreshLeft();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    /*
     * Live updates from the server. A new message from the friend we're looking
     * at reloads the chat; a friend request or an accepted request refreshes the
     * left column so it shows up on its own.
     */
    public void onPush(String event, String dataJson) {
        if (event.equals("newMessage")) {
            Message m = protocol.Json.fromJson(dataJson, Message.class);
            if (selectedPartner != null && m.getSender().equals(selectedPartner)) {
                showConversation();
            }
        } else if (event.equals("friendRequest") || event.equals("friendAccepted")) {
            refreshLeft();
        }
    }

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
