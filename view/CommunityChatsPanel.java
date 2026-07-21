package view;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.DefaultListModel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;

import model.Message;
import net.Api;
import net.PushListener;
import net.ServerClient;

// "Your Chats" -- Khalil's two-pane chat layout (list on the left, conversation
// on the right), wired to the real backend's 1:1 friend messaging. The backend
// has direct messages between friends (not group chat), so the left list is your
// friends and the right pane is your conversation with the selected one. New
// messages arrive live via the server push, no refresh.
public class CommunityChatsPanel extends JPanel implements PushListener {

    Frame frame;
    JList<String> chatList;
    DefaultListModel<String> chatListModel;
    JTextArea chatArea;
    JTextField inputField;
    JLabel chatTitle;
    JTextField addFriendField;
    ArrayList<String> friends = new ArrayList<>();
    String open_chat = null; // the friend's username we're talking to

    CommunityChatsPanel(Frame frame){
        this.frame = frame;
        setBackground(new Color(245, 243, 250));
        setLayout(new BorderLayout());

        // left side -- list of friends you can chat with
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(220, 500));
        JLabel lTitle = new JLabel("Your Chats", SwingConstants.CENTER);
        lTitle.setFont(new Font("Arial", Font.BOLD, 17));
        lTitle.setPreferredSize(new Dimension(220, 50));
        leftPanel.add(lTitle, BorderLayout.NORTH);

        chatListModel = new DefaultListModel<>();
        chatList = new JList<>(chatListModel);
        chatList.addListSelectionListener(e -> {
            int idx = chatList.getSelectedIndex();
            if (idx != -1 && idx < friends.size()){
                open_chat = friends.get(idx);
                load_chat();
            }
        });
        leftPanel.add(new JScrollPane(chatList), BorderLayout.CENTER);

        // bottom of the left column: add a friend + see incoming requests
        JPanel friendTools = new JPanel(new java.awt.GridLayout(3, 1, 0, 4));
        friendTools.setBackground(new Color(245, 243, 250));
        friendTools.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 6, 6, 6));

        addFriendField = new JTextField();
        addFriendField.setFont(new Font("Arial", Font.PLAIN, 13));
        JButton addFriendBtn = new JButton("Add friend");
        addFriendBtn.setFocusable(false);
        addFriendBtn.addActionListener(e -> addFriend());
        addFriendField.addActionListener(e -> addFriend()); // enter also sends

        JButton requestsBtn = new JButton("Friend requests");
        requestsBtn.setFocusable(false);
        requestsBtn.addActionListener(e -> showRequests());

        friendTools.add(addFriendField);
        friendTools.add(addFriendBtn);
        friendTools.add(requestsBtn);
        leftPanel.add(friendTools, BorderLayout.SOUTH);

        add(leftPanel, BorderLayout.WEST);

        // right side -- the actual conversation
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBackground(new Color(245, 243, 250));

        chatTitle = new JLabel("  pick a friend from the left", SwingConstants.LEFT);
        chatTitle.setFont(new Font("Arial", Font.BOLD, 18));
        chatTitle.setPreferredSize(new Dimension(400, 50));
        chatPanel.add(chatTitle, BorderLayout.NORTH);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 14));
        chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel inputRow = new JPanel(new BorderLayout());
        inputField = new JTextField();
        JButton sendButton = new JButton("Send");
        sendButton.setFocusable(false);
        sendButton.addActionListener(e -> send_message());
        inputField.addActionListener(e -> send_message()); // enter key also sends
        inputRow.add(inputField, BorderLayout.CENTER);
        inputRow.add(sendButton, BorderLayout.EAST);
        chatPanel.add(inputRow, BorderLayout.SOUTH);

        add(chatPanel, BorderLayout.CENTER);

        // listen for live incoming messages
        ServerClient.getInstance().addPushListener(this);
    }

    public void refresh(){
        // always clear the open conversation so nothing from a previous account
        // (or a previous friend) leaks in when this screen is re-shown.
        open_chat = null;
        chatArea.setText("");
        chatTitle.setText("  pick a friend from the left");

        chatListModel.clear();
        final String username = frame.username();
        new ui.BackgroundTask<ArrayList<String>>() {
            protected ArrayList<String> work(){
                return Api.get().friends(username);
            }
            protected void done(ArrayList<String> result){
                friends = result;
                chatListModel.clear();
                for (int i = 0; i < friends.size(); i++) {
                    chatListModel.addElement(friends.get(i));
                }
                if (friends.isEmpty()){
                    chatTitle.setText("  add a friend to start chatting!");
                }
            }
            protected void failed(Exception e){
                friends = new ArrayList<>();
            }
        }.start();
    }

    public void load_chat(){
        if (open_chat == null){
            return;
        }
        final String other = open_chat;
        final String me = frame.username();
        chatTitle.setText("  " + other);
        chatArea.setText("loading...");
        // fetch the conversation off the UI thread so the window never freezes
        new ui.BackgroundTask<ArrayList<Message>>() {
            protected ArrayList<Message> work(){
                return Api.get().conversation(me, other);
            }
            protected void done(ArrayList<Message> msgs){
                // if the user switched to a different friend meanwhile, ignore
                if (!other.equals(open_chat)){
                    return;
                }
                chatArea.setText("");
                for (int i = 0; i < msgs.size(); i++) {
                    Message m = msgs.get(i);
                    chatArea.append(m.getSender() + "\n");
                    chatArea.append("    " + m.getBody() + "\n\n");
                }
            }
            protected void failed(Exception e){
                if (other.equals(open_chat)){
                    chatArea.setText("couldn't load the conversation");
                }
            }
        }.start();
    }

    public void send_message(){
        if (open_chat == null){
            return;
        }
        String text = inputField.getText();
        if (text.trim().length() == 0){
            return; // don't send empty stuff
        }
        try {
            Api.get().sendMessage(frame.username(), open_chat, text.trim());
            inputField.setText("");
            load_chat();
        } catch (IllegalArgumentException ex){
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    // send a friend request to whoever the typed name/email resolves to
    private void addFriend(){
        String typed = addFriendField.getText().trim();
        if (typed.isEmpty()){
            return;
        }
        try {
            // resolve the typed name/email/username to the real account, then
            // send the request to that account's actual username.
            model.User target = Api.get().findUser(typed);
            String realUsername = target.getUsername();
            if (realUsername.equals(frame.username())){
                javax.swing.JOptionPane.showMessageDialog(this, "You can't add yourself.");
                return;
            }
            Api.get().sendFriendRequest(frame.username(), realUsername);
            addFriendField.setText("");
            javax.swing.JOptionPane.showMessageDialog(this,
                "Friend request sent to " + target.getDisplayName() + ".");
        } catch (IllegalArgumentException ex){
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    // show incoming friend requests and let the user accept/decline each
    private void showRequests(){
        ArrayList<String> reqs;
        try {
            reqs = Api.get().friendRequests(frame.username());
        } catch (IllegalArgumentException ex){
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage());
            return;
        }
        if (reqs.isEmpty()){
            javax.swing.JOptionPane.showMessageDialog(this, "No pending friend requests.");
            return;
        }
        for (int i = 0; i < reqs.size(); i++) {
            String from = reqs.get(i);
            int choice = javax.swing.JOptionPane.showConfirmDialog(this,
                from + " wants to be your friend. Accept?",
                "Friend request", javax.swing.JOptionPane.YES_NO_OPTION);
            try {
                Api.get().respondToRequest(frame.username(), from,
                        choice == javax.swing.JOptionPane.YES_OPTION);
            } catch (IllegalArgumentException ex){
                javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
        refresh(); // reload the friends list (newly accepted ones appear)
    }

    // a message pushed from the server -- if it's for the open conversation, reload
    public void onPush(String event, String dataJson){
        if (!"newMessage".equals(event)){
            return;
        }
        // reload the current conversation on the UI thread -- only if someone is
        // logged in and actually has a conversation open (guards stale pushes
        // after logout, and avoids pointless reloads on other screens).
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (frame.currentUser != null && open_chat != null){
                load_chat();
            }
        });
    }

}
