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

import controller.ProfileController;
import model.PersonalityTest;
import model.PersonalityResult;
import java.util.ArrayList;
import java.util.List;

// screen 4 -- 3 sample questions
public class PersonalityQuizPanel extends JPanel {

    Frame frame;

    ProfileController controller = new ProfileController();

    PersonalityTest test = new PersonalityTest();

    ArrayList<ButtonGroup> groups = new ArrayList<>();

    // real quiz will have way more questions , 3 is enough for the walkthrough
    List<String> questions = test.getQuestions();
            
    PersonalityQuizPanel(Frame frame){
        this.frame = frame;
        setBackground(new Color(245, 243, 250));
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Quick Personality Check", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        add(title, BorderLayout.NORTH);

        JPanel qPanel = new JPanel(new GridLayout(questions.size(), 1, 5, 5));
        qPanel.setBackground(new Color(245, 243, 250));

        for (int i = 0; i < questions.size(); i++) {
            JPanel row = new JPanel(new GridLayout(2, 1));
            row.setBackground(new Color(245, 243, 250));

            JLabel qLabel = new JLabel((i+1) + ". " + questions.get(i), SwingConstants.CENTER);
            qLabel.setFont(new Font("Arial", Font.PLAIN, 16));

            JPanel answers = new JPanel(new FlowLayout());
            answers.setBackground(new Color(245, 243, 250));
            JRadioButton agree = new JRadioButton("agree");
            JRadioButton neutral = new JRadioButton("neutral");
            JRadioButton disagree = new JRadioButton("disagree");
            agree.setActionCommand("agree");
            neutral.setActionCommand("neutral");
            disagree.setActionCommand("disagree");
            agree.setBackground(new Color(245, 243, 250));
            neutral.setBackground(new Color(245, 243, 250));
            disagree.setBackground(new Color(245, 243, 250));
            ButtonGroup group = new ButtonGroup();
            group.add(agree);
            group.add(neutral);
            group.add(disagree);

            groups.add(group);

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

            List<String> answers = new ArrayList<>();

            // check every question
            for(ButtonGroup g : groups){

                if(g.getSelection() == null){
                    javax.swing.JOptionPane.showMessageDialog(this,"Please answer every question.");
                    return;
                }

            String selected = g.getSelection().getActionCommand();

            answers.add(selected);

            }

        PersonalityResult result = controller.submitQuiz(test, answers);

        frame.personalityResult = result;

        frame.go_to("vibe");

        });
        bottomPanel.add(finishButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

}
