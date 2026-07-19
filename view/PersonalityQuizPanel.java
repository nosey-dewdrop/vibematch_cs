package view;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

// screen 4 -- 3 sample questions, radio buttons arent even read anywhere right now
// (see the comment on finishButton below). stands in for model/PersonalityTest.java
public class PersonalityQuizPanel extends JPanel {

    Frame frame;

    // real quiz will have way more questions , 3 is enough for the walkthrough
    String[] questions = {"I feel energized after spending time with a big group of people",
            "I prefer planning things out instead of going with the flow",
            "I would rather explore a new place than revisit a favorite one"};

    PersonalityQuizPanel(Frame frame){
        this.frame = frame;
        setBackground(new Color(245, 243, 250));
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Quick Personality Check", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        add(title, BorderLayout.NORTH);

        JPanel qPanel = new JPanel(new GridLayout(questions.length, 1, 5, 5));
        qPanel.setBackground(new Color(245, 243, 250));

        for (int i = 0; i < questions.length; i++) {
            JPanel row = new JPanel(new GridLayout(2, 1));
            row.setBackground(new Color(245, 243, 250));

            JLabel qLabel = new JLabel((i+1) + ". " + questions[i], SwingConstants.CENTER);
            qLabel.setFont(new Font("Arial", Font.PLAIN, 16));

            JPanel answers = new JPanel(new FlowLayout());
            answers.setBackground(new Color(245, 243, 250));
            JRadioButton agree = new JRadioButton("agree");
            JRadioButton neutral = new JRadioButton("neutral");
            JRadioButton disagree = new JRadioButton("disagree");
            agree.setBackground(new Color(245, 243, 250));
            neutral.setBackground(new Color(245, 243, 250));
            disagree.setBackground(new Color(245, 243, 250));
            ButtonGroup group = new ButtonGroup();
            group.add(agree);
            group.add(neutral);
            group.add(disagree);
            answers.add(agree);
            answers.add(neutral);
            answers.add(disagree);

            row.add(qLabel);
            row.add(answers);
            qPanel.add(row);
        }
        add(qPanel, BorderLayout.CENTER);


        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.setBackground(new Color(245, 243, 250));
        JButton finishButton = new JButton("See My Vibe");
        finishButton.setPreferredSize(new Dimension(180, 45));
        finishButton.setFocusable(false);
        finishButton.addActionListener(e -> {
            // answers dont actually do anything yet -- nothing reads the agree/neutral/disagree
            // groups above at all. (PersonalityTest is Yaras part, model/PersonalityTest.java)
            frame.pick_archetype();
            frame.go_to("vibe");
        });
        bottomPanel.add(finishButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

}
