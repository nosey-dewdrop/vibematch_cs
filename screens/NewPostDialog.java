package screens;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.Api;
import model.Post;
import ui.RoundedButton;
import ui.Theme;
import ui.UiHelper;

/*
 * Little popup for writing a new forum post. Takes a title and a body, saves it
 * and then runs the callback so the community screen can refresh its list.
 */
public class NewPostDialog extends JDialog {

    private Api api = Api.get();
    private JTextField titleField = new JTextField();
    private JTextArea bodyArea = new JTextArea(5, 20);
    private JLabel errorLabel = new JLabel(" ");

    public NewPostDialog(Window owner, final int communityId, final String author, final Runnable onPosted) {
        super(owner, "New post", ModalityType.APPLICATION_MODAL);
        setSize(440, 380);
        setLocationRelativeTo(owner);

        JPanel root = new JPanel();
        root.setBackground(Theme.WHITE);
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));

        JLabel title = UiHelper.title("Start a discussion", 20);
        title.setAlignmentX(LEFT_ALIGNMENT);
        root.add(title);
        root.add(UiHelper.vgap(16));

        root.add(fieldLabel("Title"));
        JPanel titleBox = UiHelper.field(titleField);
        titleBox.setAlignmentX(LEFT_ALIGNMENT);
        root.add(titleBox);
        root.add(UiHelper.vgap(12));

        root.add(fieldLabel("What's on your mind?"));
        bodyArea.setLineWrap(true);
        bodyArea.setWrapStyleWord(true);
        bodyArea.setFont(Theme.body(14));
        bodyArea.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        JScrollPane bodyScroll = new JScrollPane(bodyArea);
        bodyScroll.setAlignmentX(LEFT_ALIGNMENT);
        bodyScroll.setBorder(BorderFactory.createLineBorder(Theme.LILAC_200, 1, true));
        bodyScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        root.add(bodyScroll);
        root.add(UiHelper.vgap(8));

        errorLabel.setFont(Theme.body(12));
        errorLabel.setForeground(new Color(0xC0, 0x39, 0x4B));
        errorLabel.setAlignmentX(LEFT_ALIGNMENT);
        root.add(errorLabel);
        root.add(UiHelper.vgap(6));

        RoundedButton postBtn = UiHelper.primaryButton("Post");
        postBtn.setAlignmentX(LEFT_ALIGNMENT);
        postBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        postBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String t = titleField.getText().trim();
                if (t.isEmpty()) {
                    errorLabel.setText("Give your post a title.");
                    return;
                }
                api.createPost(communityId, author, t, bodyArea.getText().trim());
                dispose();
                if (onPosted != null) {
                    onPosted.run();
                }
            }
        });
        root.add(postBtn);

        setContentPane(root);
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text.toUpperCase());
        l.setFont(Theme.bodyBold(11));
        l.setForeground(Theme.LILAC_500);
        l.setAlignmentX(LEFT_ALIGNMENT);
        l.setBorder(BorderFactory.createEmptyBorder(0, 2, 5, 0));
        return l;
    }
}
