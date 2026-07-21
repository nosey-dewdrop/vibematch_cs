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
        spotifyButton.addActionListener(e -> connectSpotify());
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
            // display name / bio don't have a backend update route yet, so this
            // only updates the current session's shown name.
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

        String type = frame.currentUser == null ? null : frame.currentUser.getMbtiType();
        typeLabel.setText("Vibe type: " + (type == null ? "???" : type));
        nameField.setText(frame.user_name);

        // real interests from the server become the tags line
        java.util.ArrayList<String> interests = new java.util.ArrayList<>();
        try {
            model.User fresh = net.Api.get().getUser(frame.username());
            interests = fresh.getInterests();
        } catch (Exception ex){
            // fall back to whatever the session user has
            if (frame.currentUser != null) interests = frame.currentUser.getInterests();
        }
        if (interests == null || interests.isEmpty()){
            tagsLabel.setText("Tags: none yet");
        }else{
            String s = "Tags: ";
            for (int i = 0; i < interests.size(); i++) {
                s = s + "#" + interests.get(i) + " ";
            }
            tagsLabel.setText(s);
        }
    }

    private void connectSpotify(){
        if (!net.Api.get().spotifyAvailable()){
            JOptionPane.showMessageDialog(this, "Spotify isn't set up on this build.");
            return;
        }
        // OAuth opens the browser and blocks, so run it off the UI thread
        final JPanel self = this;
        new Thread(() -> {
            final String[] err = {null};
            try {
                net.Api.get().connectSpotify(frame.username());
            } catch (Exception ex){
                err[0] = ex.getMessage() == null ? "Spotify connect failed." : ex.getMessage();
            }
            javax.swing.SwingUtilities.invokeLater(() -> {
                if (err[0] != null){
                    JOptionPane.showMessageDialog(self, err[0]);
                } else {
                    JOptionPane.showMessageDialog(self, "Spotify connected! Your top artists now feed your matches.");
                    refresh();
                }
            });
        }).start();
    }

}
