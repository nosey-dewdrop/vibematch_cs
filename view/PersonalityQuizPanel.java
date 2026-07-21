package view;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.ButtonGroup;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;

import service.MbtiService;

// screen 4 -- the personality test. Khalil's look (purple bg, radio choices,
// "See My Vibe" button), but the real 16 backend questions, each a two-option
// A/B pick. Answers go to the server (Api.submitMbti) which scores the MBTI type.
public class PersonalityQuizPanel extends JPanel {

    Frame frame;
    ArrayList<ButtonGroup> groups = new ArrayList<>();
    ArrayList<MbtiService.Question> questions = new MbtiService().getQuestions();

    PersonalityQuizPanel(Frame frame){
        this.frame = frame;
        setBackground(new Color(245, 243, 250));
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Quick Personality Check", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        add(title, BorderLayout.NORTH);

        JPanel qPanel = new JPanel();
        qPanel.setLayout(new BoxLayout(qPanel, BoxLayout.Y_AXIS));
        qPanel.setBackground(new Color(245, 243, 250));

        for (int i = 0; i < questions.size(); i++) {
            MbtiService.Question q = questions.get(i);

            JPanel row = new JPanel(new GridLayout(2, 1));
            row.setBackground(new Color(245, 243, 250));
            row.setBorder(BorderFactory.createEmptyBorder(6, 20, 6, 20));

            JLabel qLabel = new JLabel((i+1) + ". " + q.text, SwingConstants.CENTER);
            qLabel.setFont(new Font("Arial", Font.BOLD, 15));

            JPanel answers = new JPanel(new FlowLayout());
            answers.setBackground(new Color(245, 243, 250));
            // optionA scores 0 (first pole), optionB scores 1 (second pole)
            JRadioButton a = new JRadioButton(q.optionA);
            JRadioButton b = new JRadioButton(q.optionB);
            a.setActionCommand("0");
            b.setActionCommand("1");
            a.setBackground(new Color(245, 243, 250));
            b.setBackground(new Color(245, 243, 250));
            a.setFont(new Font("Arial", Font.PLAIN, 13));
            b.setFont(new Font("Arial", Font.PLAIN, 13));
            ButtonGroup group = new ButtonGroup();
            group.add(a);
            group.add(b);
            groups.add(group);
            answers.add(a);
            answers.add(b);

            row.add(qLabel);
            row.add(answers);
            qPanel.add(row);
        }

        JScrollPane scroll = new JScrollPane(qPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.setBackground(new Color(245, 243, 250));
        JButton finishButton = new JButton("See My Vibe");
        finishButton.setPreferredSize(new Dimension(180, 45));
        finishButton.setFocusable(false);
        finishButton.addActionListener(e -> finish());
        bottomPanel.add(finishButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void finish(){
        int[] answers = new int[groups.size()];
        for (int i = 0; i < groups.size(); i++){
            ButtonGroup g = groups.get(i);
            if (g.getSelection() == null){
                javax.swing.JOptionPane.showMessageDialog(this, "Please answer every question.");
                return;
            }
            answers[i] = Integer.parseInt(g.getSelection().getActionCommand());
        }
        try {
            model.MbtiResult result = net.Api.get().submitMbti(frame.username(), answers);
            frame.mbtiResult = result;
            if (frame.currentUser != null){
                frame.currentUser.setMbtiType(result.getType());
            }
            frame.go_to("vibe");
        } catch (IllegalArgumentException ex){
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }
}
