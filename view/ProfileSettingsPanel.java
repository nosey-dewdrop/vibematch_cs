package view;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

// account info + the spotify connect button. save doesnt persist anywhere,
// its all still just fields on Frame for the current session
public class ProfileSettingsPanel extends JPanel {

    Frame frame;
    JLabel emailLabel;
    JLabel typeLabel;
    JLabel tagsLabel;
    JTextField nameField;
    JTextArea bioArea;

    ProfileSettingsPanel(Frame frame){
        this.frame = frame;
        setBackground(new Color(245, 243, 250));
        setLayout(new BorderLayout());

        JLabel title = new JLabel("  Profile & Settings", SwingConstants.LEFT);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setPreferredSize(new Dimension(400, 60));
        add(title, BorderLayout.NORTH);


        JPanel formPanel = new JPanel(new GridLayout(7, 1, 0, 5));
        formPanel.setBackground(new Color(245, 243, 250));

        emailLabel = new JLabel("Email: ", SwingConstants.CENTER);
        emailLabel.setForeground(Color.GRAY);

        typeLabel = new JLabel("Vibe type: ???", SwingConstants.CENTER);
        typeLabel.setFont(new Font("Arial", Font.BOLD, 15));
        typeLabel.setForeground(new Color(103, 58, 183));

        tagsLabel = new JLabel("Tags: none yet", SwingConstants.CENTER);

        JPanel nameRow = new JPanel(new FlowLayout());
        nameRow.setBackground(new Color(245, 243, 250));
        nameRow.add(new JLabel("Display name:"));
        nameField = new JTextField(15);
        nameRow.add(nameField);

        JPanel bioRow = new JPanel(new FlowLayout());
        bioRow.setBackground(new Color(245, 243, 250));
        bioRow.add(new JLabel("Bio:"));
        bioArea = new JTextArea(3, 20);
        bioArea.setLineWrap(true);
        bioRow.add(bioArea);

        JPanel spotifyRow = new JPanel(new FlowLayout());
        spotifyRow.setBackground(new Color(245, 243, 250));
        JButton spotifyButton = new JButton("Connect Spotify");
        spotifyButton.setBackground(new Color(30, 215, 96)); // spotify green
        spotifyButton.setFocusable(false);
        spotifyButton.addActionListener(e -> {
            // the whole OAuth thing is Ahmeds part now per the v2 report (see
            // model/SpotifyProfile.connect()) , this is just the button
            JOptionPane.showMessageDialog(this, "Spotify connection coming soon!\n(needs the OAuth stuff)");
        });
        spotifyRow.add(spotifyButton);

        JPanel saveRow = new JPanel(new FlowLayout());
        saveRow.setBackground(new Color(245, 243, 250));
        JButton saveButton = new JButton("Save Changes");
        saveButton.setPreferredSize(new Dimension(160, 40));
        saveButton.setFocusable(false);
        saveButton.addActionListener(e -> {
            if (nameField.getText().length() > 0){
                frame.user_name = nameField.getText();
            }
            // doesnt actually save anywhere , theres no database yet
            JOptionPane.showMessageDialog(this, "Saved!");
        });
        saveRow.add(saveButton);

        formPanel.add(emailLabel);
        formPanel.add(typeLabel);
        formPanel.add(tagsLabel);
        formPanel.add(nameRow);
        formPanel.add(bioRow);
        formPanel.add(spotifyRow);
        formPanel.add(saveRow);

        add(formPanel, BorderLayout.CENTER);
    }

    public void refresh(){
        emailLabel.setText("Email: " + frame.user_email);
        typeLabel.setText("Vibe type: " + frame.personalityResult.getResultType());
        nameField.setText(frame.user_name);

        if (frame.currentUser.getTags().size() == 0){
            tagsLabel.setText("Tags: none yet");
        }else{
            String s = "Tags: ";
            for (int i = 0; i < frame.currentUser.getTags().size(); i++) {
                s = s + "#" + frame.currentUser.getTags().get(i).getName() + " ";
            }
            tagsLabel.setText(s);
        }
    }

}
