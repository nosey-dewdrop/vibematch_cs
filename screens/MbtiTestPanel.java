package screens;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import model.MbtiResult;
import model.User;
import net.Api;
import service.MbtiService;
import ui.RoundedButton;
import ui.RoundedPanel;
import ui.Session;
import ui.Theme;
import ui.UiHelper;

/*
 * The 16 question vibe test. Shows one question at a time with a little progress
 * bar and a back button. When the last one is answered we score it, save the
 * type and show the result.
 */
public class MbtiTestPanel extends JPanel {

    private AppFrame appFrame;
    private User user;
    private Api api = Api.get();
    private MbtiService mbti = new MbtiService();

    private ArrayList<MbtiService.Question> questions;
    private int[] answers;
    private int current = 0;

    // components we update as we move between questions
    private RoundedPanel progressFill;
    private JLabel counterLabel;
    private JLabel questionLabel;
    private RoundedButton optionA;
    private RoundedButton optionB;
    private JLabel backLink;

    public MbtiTestPanel(AppFrame appFrame, User user) {
        this.appFrame = appFrame;
        this.user = user;
        this.questions = mbti.getQuestions();
        this.answers = new int[questions.size()];

        setBackground(Theme.BG);
        setLayout(new java.awt.GridBagLayout());

        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setPreferredSize(new Dimension(520, 460));

        col.add(buildProgress());
        col.add(UiHelper.vgap(10));

        counterLabel = UiHelper.muted("", 12);
        counterLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        col.add(counterLabel);
        col.add(UiHelper.vgap(16));

        questionLabel = new JLabel("", SwingConstants.CENTER);
        questionLabel.setFont(Theme.heading(24));
        questionLabel.setForeground(Theme.INK);
        questionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        questionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        col.add(questionLabel);
        col.add(UiHelper.vgap(26));

        optionA = optionButton();
        optionA.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                answer(0);
            }
        });
        col.add(optionA);
        col.add(UiHelper.vgap(12));

        optionB = optionButton();
        optionB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                answer(1);
            }
        });
        col.add(optionB);
        col.add(UiHelper.vgap(20));

        backLink = new JLabel("← Back");
        backLink.setFont(Theme.bodyBold(13));
        backLink.setForeground(Theme.LILAC_600);
        backLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backLink.setAlignmentX(Component.CENTER_ALIGNMENT);
        backLink.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                goBack();
            }
        });
        col.add(backLink);

        add(col);
        render();
    }

    private JPanel buildProgress() {
        // a simple track with a fill we resize as they progress
        RoundedPanel track = new RoundedPanel(6, Theme.LILAC_100);
        track.setLayout(null);
        track.setPreferredSize(new Dimension(360, 8));
        track.setMaximumSize(new Dimension(360, 8));
        track.setAlignmentX(Component.CENTER_ALIGNMENT);

        progressFill = new RoundedPanel(6, Theme.LILAC_500);
        progressFill.setBounds(0, 0, 40, 8);
        track.add(progressFill);
        return track;
    }

    private RoundedButton optionButton() {
        RoundedButton b = new RoundedButton("", Theme.WHITE, Theme.INK);
        b.setFont(Theme.body(15));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 18));
        b.setPreferredSize(new Dimension(440, 60));
        b.setMaximumSize(new Dimension(440, 64));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        return b;
    }

    private void render() {
        MbtiService.Question q = questions.get(current);
        counterLabel.setText("Question " + (current + 1) + " of " + questions.size());
        questionLabel.setText("<html><div style='text-align:center;width:440px'>" + q.text + "</div></html>");
        optionA.setText("<html>" + q.optionA + "</html>");
        optionB.setText("<html>" + q.optionB + "</html>");
        backLink.setVisible(current > 0);

        // resize the progress fill (track is 360 wide)
        double frac = (double) (current + 1) / questions.size();
        progressFill.setBounds(0, 0, (int) (360 * frac), 8);
        progressFill.repaint();
    }

    private void answer(int choice) {
        answers[current] = choice;
        if (current < questions.size() - 1) {
            current++;
            render();
        } else {
            finish();
        }
    }

    private void goBack() {
        if (current > 0) {
            current--;
            render();
        }
    }

    private void finish() {
        // the server scores the answers and stores the type, so the result and
        // whats saved can never drift apart
        MbtiResult result = api.submitMbti(user.getUsername(), answers);
        user.setMbtiType(result.getType());
        Session.setUser(user);
        appFrame.showVibeResult(user, result);
    }
}
