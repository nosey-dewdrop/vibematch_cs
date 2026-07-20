package view;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

// screen 5
public class VibeProfilePanel extends JPanel {

    Frame frame;
    JLabel archetypeLabel;
    JLabel descLabel;
    JProgressBar energyBar;
    JProgressBar socialBar;
    JProgressBar creativityBar;
    JProgressBar chillBar;

    VibeProfilePanel(Frame frame){
        this.frame = frame;
        setBackground(new Color(245, 243, 250));
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new GridLayout(3, 1));
        topPanel.setBackground(new Color(245, 243, 250));
        JLabel title = new JLabel("Your Vibe Profile", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.PLAIN, 18));
        title.setForeground(Color.GRAY);

        archetypeLabel = new JLabel("???", SwingConstants.CENTER);
        archetypeLabel.setFont(new Font("Arial", Font.BOLD, 36));
        archetypeLabel.setForeground(new Color(103, 58, 183));

        descLabel = new JLabel("", SwingConstants.CENTER);
        descLabel.setFont(new Font("Arial", Font.PLAIN, 15));

        topPanel.add(title);
        topPanel.add(archetypeLabel);
        topPanel.add(descLabel);
        add(topPanel, BorderLayout.NORTH);


        // the trait bars 
        JPanel barsPanel = new JPanel(new GridLayout(4, 2, 10, 18));
        barsPanel.setBackground(new Color(245, 243, 250));

        energyBar = new JProgressBar(0, 100);
        socialBar = new JProgressBar(0, 100);
        creativityBar = new JProgressBar(0, 100);
        chillBar = new JProgressBar(0, 100);
        energyBar.setStringPainted(true);
        socialBar.setStringPainted(true);
        creativityBar.setStringPainted(true);
        chillBar.setStringPainted(true);

        barsPanel.add(new JLabel("Energy", SwingConstants.RIGHT));
        barsPanel.add(energyBar);
        barsPanel.add(new JLabel("Social", SwingConstants.RIGHT));
        barsPanel.add(socialBar);
        barsPanel.add(new JLabel("Creativity", SwingConstants.RIGHT));
        barsPanel.add(creativityBar);
        barsPanel.add(new JLabel("Chill", SwingConstants.RIGHT));
        barsPanel.add(chillBar);

        JPanel centerWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 40));
        centerWrap.setBackground(new Color(245, 243, 250));
        barsPanel.setPreferredSize(new Dimension(450, 220));
        centerWrap.add(barsPanel);
        add(centerWrap, BorderLayout.CENTER);


        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.setBackground(new Color(245, 243, 250));
        JButton goButton = new JButton("Lets Go!");
        goButton.setPreferredSize(new Dimension(180, 45));
        goButton.setFocusable(false);
        goButton.addActionListener(e -> frame.go_to("home"));
        bottomPanel.add(goButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void refresh(){
        archetypeLabel.setText(frame.personalityResult.getResultType());

        if (frame.personalityResult.getResultType().equals("The Explorer")){
            descLabel.setText(frame.personalityResult.getDescription());
        }else if (frame.personalityResult.getResultType().equals("The Harmonizer")){
            descLabel.setText(frame.personalityResult.getDescription());
        }else if (frame.personalityResult.getResultType().equals("The Thinker")){
            descLabel.setText(frame.personalityResult.getDescription());
        }else if (frame.personalityResult.getResultType().equals("The Spark")){
            descLabel.setText(frame.personalityResult.getDescription());
        }else{
            descLabel.setText("");
        }

        energyBar.setValue(frame.personalityResult.getEnergy());
        socialBar.setValue(frame.personalityResult.getSocial());
        creativityBar.setValue(frame.personalityResult.getCreativity());
        chillBar.setValue(frame.personalityResult.getChill());
    }

}
