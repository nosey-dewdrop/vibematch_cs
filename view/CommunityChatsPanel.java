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
        chatListModel.clear();
        try {
            friends = Api.get().friends(frame.username());
        } catch (Exception ex){
            friends = new ArrayList<>();
        }
        for (int i = 0; i < friends.size(); i++) {
            chatListModel.addElement(friends.get(i));
        }
        if (friends.isEmpty()){
            chatTitle.setText("  add a friend to start chatting!");
            chatArea.setText("");
            open_chat = null;
        }
    }

    public void load_chat(){
        if (open_chat == null){
            return;
        }
        chatTitle.setText("  " + open_chat);
        chatArea.setText("");
        try {
            ArrayList<Message> msgs = Api.get().conversation(frame.username(), open_chat);
            for (int i = 0; i < msgs.size(); i++) {
                Message m = msgs.get(i);
                chatArea.append(m.getSender() + "\n");
                chatArea.append("    " + m.getBody() + "\n\n");
            }
        } catch (Exception ex){
            chatArea.setText("couldn't load the conversation");
        }
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

    // a message pushed from the server -- if it's for the open conversation, reload
    public void onPush(String event, String dataJson){
        if (!"newMessage".equals(event)){
            return;
        }
        // reload the current conversation on the UI thread
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (open_chat != null){
                load_chat();
            }
        });
    }

}
