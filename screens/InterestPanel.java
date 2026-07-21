package screens;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import model.Interests;
import model.User;
import net.Api;
import ui.Chip;
import ui.RoundedButton;
import ui.Session;
import ui.Theme;
import ui.UiHelper;

/*
 * First step of onboarding: pick what you are into. Need at least 3 so we have
 * something to match on. Selected interests get saved then we go to the test.
 */
public class InterestPanel extends JPanel {

    private static final int MIN_PICKS = 3;

    private AppFrame appFrame;
    private User user;
    private Api api = Api.get();

    private ArrayList<Chip> chips = new ArrayList<Chip>();
    private RoundedButton continueBtn;

    public InterestPanel(AppFrame appFrame, User user) {
        this.appFrame = appFrame;
        this.user = user;

        setBackground(Theme.BG);
        setLayout(new GridBagLayout()); // centers the column

        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setPreferredSize(new Dimension(680, 520));

        JLabel title = UiHelper.title("What are you into?", 26);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        col.add(title);
        col.add(UiHelper.vgap(6));

        JLabel sub = UiHelper.muted("Pick at least 3. This shapes your matches.", 14);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        col.add(sub);
        col.add(UiHelper.vgap(22));

        col.add(buildChips());
        col.add(UiHelper.vgap(26));

        continueBtn = UiHelper.primaryButton("Continue");
        continueBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        continueBtn.setPreferredSize(new Dimension(280, 46));
        continueBtn.setMaximumSize(new Dimension(280, 46));
        continueBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onContinue();
            }
        });
        col.add(continueBtn);

        add(col);
        refreshButton();
    }

    private JPanel buildChips() {
        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 9, 9));
        wrap.setOpaque(false);
        wrap.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        for (int i = 0; i < Interests.ALL.length; i++) {
            String name = Interests.ALL[i];
            Chip chip = new Chip(Interests.emojiFor(name) + " " + name);
            // when any chip is toggled, update the continue button count
            chip.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    refreshButton();
                }
            });
            chips.add(chip);
            wrap.add(chip);
        }
        return wrap;
    }

    private int countSelected() {
        int n = 0;
        for (int i = 0; i < chips.size(); i++) {
            if (chips.get(i).isOn()) {
                n++;
            }
        }
        return n;
    }

    private void refreshButton() {
        int n = countSelected();
        continueBtn.setText("Continue  ·  " + n + " picked");
        continueBtn.setEnabled(n >= MIN_PICKS);
    }

    private void onContinue() {
        if (countSelected() < MIN_PICKS) {
            return;
        }
        ArrayList<String> picked = new ArrayList<String>();
        for (int i = 0; i < chips.size(); i++) {
            if (chips.get(i).isOn()) {
                picked.add(Interests.ALL[i]);
            }
        }
        try {
            api.setInterests(user.getUsername(), picked);
        } catch (IllegalArgumentException ex) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    ex.getMessage() == null ? "Couldn't save. Try again." : ex.getMessage());
            return;
        }
        user.setInterests(picked);
        Session.setUser(user);
        appFrame.startMbti(user);
    }
}
