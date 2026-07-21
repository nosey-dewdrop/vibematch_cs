package view;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;

import model.Community;
import model.CommunityMessage;
import net.Api;
import net.PushListener;
import net.ServerClient;

// zoomed-in view of one community. Join/leave is backed by the real server, and
// the middle is a community "guestbook" -- a shared wall where every member can
// post a line and everyone sees it, updated live via server push.
public class CommunityDetailPanel extends JPanel implements PushListener {

    Frame frame;
    Community current = null;

    JLabel nameLabel;
    JLabel matchLabel;
    JLabel memberLabel;
    JLabel descLabel;
    JButton joinButton;
    JTextArea chatArea;
    JTextField chatInput;
    JButton sendButton;

    CommunityDetailPanel(Frame frame){
        this.frame = frame;
        setBackground(new Color(245, 243, 250));
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new GridLayout(4, 1));
        topPanel.setBackground(new Color(245, 243, 250));
        topPanel.setPreferredSize(new Dimension(600, 170));

        nameLabel = new JLabel("no community selected", SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 28));

        matchLabel = new JLabel("", SwingConstants.CENTER);
        matchLabel.setForeground(new Color(56, 142, 60));
        matchLabel.setFont(new Font("Arial", Font.BOLD, 15));

        memberLabel = new JLabel("", SwingConstants.CENTER);
        memberLabel.setForeground(Color.GRAY);

        descLabel = new JLabel("", SwingConstants.CENTER);
        descLabel.setFont(new Font("Arial", Font.PLAIN, 15));

        topPanel.add(nameLabel);
        topPanel.add(matchLabel);
        topPanel.add(memberLabel);
        topPanel.add(descLabel);
        add(topPanel, BorderLayout.NORTH);

        // community chat (guestbook) in the middle
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBackground(new Color(245, 243, 250));
        JLabel cLabel = new JLabel("Community Chat", SwingConstants.CENTER);
        cLabel.setFont(new Font("Arial", Font.BOLD, 17));
        cLabel.setPreferredSize(new Dimension(400, 40));
        chatPanel.add(cLabel, BorderLayout.NORTH);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 14));
        chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel inputRow = new JPanel(new BorderLayout());
        chatInput = new JTextField();
        sendButton = new JButton("Send");
        sendButton.setFocusable(false);
        sendButton.addActionListener(e -> send());
        chatInput.addActionListener(e -> send()); // enter also sends
        inputRow.add(chatInput, BorderLayout.CENTER);
        inputRow.add(sendButton, BorderLayout.EAST);
        chatPanel.add(inputRow, BorderLayout.SOUTH);
        add(chatPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.setBackground(new Color(245, 243, 250));

        joinButton = new JButton("Join");
        joinButton.setPreferredSize(new Dimension(150, 42));
        joinButton.setFocusable(false);
        joinButton.addActionListener(e -> join_or_leave());

        JButton backButton = new JButton("back to feed");
        backButton.setFocusable(false);
        backButton.addActionListener(e -> frame.go_to("home"));

        bottomPanel.add(joinButton);
        bottomPanel.add(backButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // listen for live community messages
        ServerClient.getInstance().addPushListener(this);
    }

    public void set_community(Community c){
        this.current = c;
    }

    public void join_or_leave(){
        if (current == null){
            return;
        }
        try {
            if (current.isMember()){
                Api.get().leave(frame.username(), current.getId());
            }else{
                Api.get().join(frame.username(), current.getId());
            }
            current = Api.get().scoreOne(frame.username(), current.getId());
        } catch (IllegalArgumentException ex){
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage());
        }
        refresh();
    }

    private void send(){
        if (current == null){
            return;
        }
        String text = chatInput.getText().trim();
        if (text.isEmpty()){
            return;
        }
        try {
            Api.get().communityChatPost(current.getId(), text);
            chatInput.setText("");
            loadChat();
        } catch (IllegalArgumentException ex){
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    public void refresh(){
        if (current == null){
            nameLabel.setText("no community selected");
            return;
        }
        nameLabel.setText(current.getName());
        matchLabel.setText(current.getMatchPercent() + "% match with you");
        String cat = current.getTags().isEmpty() ? current.getCategory() : current.getTags().get(0);
        memberLabel.setText(current.getMemberCount() + " members  ·  " + cat);
        descLabel.setText(current.getDescription());

        joinButton.setText(current.isMember() ? "Leave" : "Join");

        // only members can see/use the chat
        boolean member = current.isMember();
        chatInput.setEnabled(member);
        sendButton.setEnabled(member);
        if (member){
            loadChat();
        } else {
            chatArea.setText("Join the community to see and join the chat.");
        }
    }

    private void loadChat(){
        if (current == null){
            return;
        }
        try {
            ArrayList<CommunityMessage> msgs = Api.get().communityChatList(current.getId());
            chatArea.setText("");
            if (msgs.isEmpty()){
                chatArea.setText("No messages yet. Say hi to the community!");
            } else {
                for (int i = 0; i < msgs.size(); i++) {
                    CommunityMessage m = msgs.get(i);
                    chatArea.append(m.getSender() + ":  " + m.getBody() + "\n");
                }
            }
        } catch (Exception ex){
            chatArea.setText("couldn't load the chat");
        }
    }

    // a community message was pushed -- if it's for the community we're viewing,
    // reload the chat on the UI thread.
    public void onPush(String event, String dataJson){
        if (!"communityMessage".equals(event)){
            return;
        }
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                int cid = protocol.Json.parse(dataJson).get("communityId").getAsInt();
                if (current != null && current.getId() == cid && current.isMember()){
                    loadChat();
                }
            } catch (Exception ignore) {}
        });
    }

}
