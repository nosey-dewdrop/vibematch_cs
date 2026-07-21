package screens;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import net.Api;
import model.Interests;
import model.User;
import ui.Chip;
import ui.RoundedButton;
import ui.Theme;
import ui.UiHelper;

/*
 * Lets the user change their interests after onboarding. Same chips as the
 * picker, but pre selected with what they already have, and it just saves and
 * closes instead of moving on to the test.
 */
public class EditInterestsDialog extends JDialog {

    private Api api = Api.get();
    private ArrayList<Chip> chips = new ArrayList<Chip>();

    public EditInterestsDialog(Window owner, final User user, final Runnable onSaved) {
        super(owner, "Edit interests", ModalityType.APPLICATION_MODAL);
        setSize(560, 520);
        setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.WHITE);
        root.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));

        JLabel title = UiHelper.title("Your interests", 20);
        root.add(title, BorderLayout.NORTH);

        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 9));
        wrap.setBackground(Theme.WHITE);
        for (int i = 0; i < Interests.ALL.length; i++) {
            String name = Interests.ALL[i];
            Chip chip = new Chip(Interests.emojiFor(name) + " " + name);
            if (user.getInterests().contains(name)) {
                chip.setOn(true);
            }
            chips.add(chip);
            wrap.add(chip);
        }
        JScrollPane scroll = new JScrollPane(wrap);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        root.add(scroll, BorderLayout.CENTER);

        RoundedButton save = UiHelper.primaryButton("Save");
        save.setPreferredSize(new Dimension(10, 44));
        save.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ArrayList<String> picked = new ArrayList<String>();
                for (int i = 0; i < chips.size(); i++) {
                    if (chips.get(i).isOn()) {
                        picked.add(Interests.ALL[i]);
                    }
                }
                // keep any spotify-derived tags (music:*) -- they aren't chips
                // here, so saving the chip selection alone would silently wipe
                // the user's connected music taste.
                for (int i = 0; i < user.getInterests().size(); i++) {
                    String existing = user.getInterests().get(i);
                    if (existing.startsWith("music:") && !picked.contains(existing)) {
                        picked.add(existing);
                    }
                }
                try {
                    api.setInterests(user.getUsername(), picked);
                } catch (IllegalArgumentException ex) {
                    javax.swing.JOptionPane.showMessageDialog(EditInterestsDialog.this,
                            ex.getMessage() == null ? "Couldn't save. Try again." : ex.getMessage());
                    return;
                }
                user.setInterests(picked);
                dispose();
                if (onSaved != null) {
                    onSaved.run();
                }
            }
        });
        JPanel south = new JPanel(new BorderLayout());
        south.setBackground(Theme.WHITE);
        south.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        south.add(save, BorderLayout.CENTER);
        root.add(south, BorderLayout.SOUTH);

        setContentPane(root);
    }
}
