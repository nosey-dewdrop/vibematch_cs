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
import java.text.SimpleDateFormat;
import java.util.Date;

// picks a joined community on the left, shows its message history on the right.
// this is the one screen that most directly maps to model/GroupChat.java +
// model/Message.java + model/ModerationFilter.java once those get implemented --
// see the TODO in send_message() below for exactly where that would plug in
public class CommunityChatsPanel extends JPanel {

    Frame frame;
    JList<String> chatList;
    DefaultListModel<String> chatListModel;
    JTextArea chatArea;
    JTextField inputField;
    JLabel chatTitle;
    Community open_chat = null;

    CommunityChatsPanel(Frame frame){
        this.frame = frame;
        setBackground(new Color(245, 243, 250));
        setLayout(new BorderLayout());

        // left side , list of my communities
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
            if (idx != -1 && idx < frame.my_communities.size()){
                open_chat = frame.my_communities.get(idx);
                load_chat();
            }
        });
        leftPanel.add(new JScrollPane(chatList), BorderLayout.CENTER);
        add(leftPanel, BorderLayout.WEST);


        // right side , the actual chat
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBackground(new Color(245, 243, 250));

        chatTitle = new JLabel("  pick a community from the left", SwingConstants.LEFT);
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
    }

    public void refresh(){
        chatListModel.clear();
        for (int i = 0; i < frame.my_communities.size(); i++) {
            chatListModel.addElement(frame.my_communities.get(i).get_name());
        }
        if (frame.my_communities.size() == 0){
            chatTitle.setText("  join a community to start chatting!");
            chatArea.setText("");
            open_chat = null;
        }
    }

    public void load_chat(){
        if (open_chat == null){
            return;
        }
        chatTitle.setText("  " + open_chat.get_name());
        chatArea.setText("");
        for (int i = 0; i < open_chat.get_messages().size(); i++) {
            Message m = open_chat.get_messages().get(i);
            chatArea.append(m.get_sender() + "  (" + m.get_time() + ")\n");
            chatArea.append("    " + m.get_content() + "\n\n");
        }
    }

    public void send_message(){
        if (open_chat == null){
            return;
        }
        String text = inputField.getText();
        if (text.length() == 0){
            return; // dont send empty stuff
        }

        // run through real moderation pipeline (ChatController -> GroupChat -> ModerationFilter)
        boolean allowed = frame.send_chat_message(open_chat, frame.user_name, text);
        if (!allowed){
            javax.swing.JOptionPane.showMessageDialog(this,
                "message blocked by moderation filter.",
                "blocked", javax.swing.JOptionPane.WARNING_MESSAGE);
            inputField.setText("");
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String time = sdf.format(new Date());
        open_chat.get_messages().add(new Message(frame.user_name, text, time));

        inputField.setText("");
        load_chat();
    }

}
