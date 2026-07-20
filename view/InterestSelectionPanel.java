package view;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;

import controller.ProfileController;

// screen 3 -- the chip picker. each JToggleButton is a "chip", selected ones get
// saved into frame.my_tags. this is where model/Tag.java would eventually come in
// (right now its just plain strings, not real Tag objects)
public class InterestSelectionPanel extends JPanel {

    Frame frame;
    ProfileController profileController = new ProfileController();
    ArrayList<JToggleButton> chips = new ArrayList<>();
    

    String[] hobbies = {"chess", "hiking", "jazz", "board games", "animal welfare",
            "gaming", "anime", "football", "photography", "cooking", "coding",
            "movies", "reading", "volleyball", "painting", "kpop"};

    // chip colors just cycle through these
    Color[] chip_colors = {new Color(255, 205, 210), new Color(200, 230, 201),
            new Color(187, 222, 251), new Color(255, 224, 178), new Color(225, 190, 231)};

    InterestSelectionPanel(Frame frame){
        this.frame = frame;
        setBackground(new Color(245, 243, 250));
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(245, 243, 250));
        JLabel title = new JLabel("Pick Your Interests", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        JLabel sub = new JLabel("pick as many as you want , these become your tags", SwingConstants.CENTER);
        sub.setForeground(Color.GRAY);
        topPanel.setLayout(new BorderLayout());
        topPanel.add(title, BorderLayout.NORTH);
        topPanel.add(sub, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);


        JPanel chipPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 15));
        chipPanel.setBackground(new Color(245, 243, 250));

        for (int i = 0; i < hobbies.length; i++) {
            JToggleButton chip = new JToggleButton("#" + hobbies[i]);
            chip.setBackground(chip_colors[i % chip_colors.length]);
            chip.setFocusable(false);
            chip.setPreferredSize(new Dimension(140, 40));
            chips.add(chip);
            chipPanel.add(chip);
        }
        add(chipPanel, BorderLayout.CENTER);


        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.setBackground(new Color(245, 243, 250));
        JButton continueButton = new JButton("Continue");
        continueButton.setPreferredSize(new Dimension(180, 45));
        continueButton.setFocusable(false);
        continueButton.addActionListener(e -> save_tags());
        bottomPanel.add(continueButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void save_tags(){
        for (int i = 0; i < chips.size(); i++) {

            if (chips.get(i).isSelected()){
                profileController.addTag(frame.currentUser,hobbies[i]);
            }
        }

    frame.go_to("quiz");
    }

}
