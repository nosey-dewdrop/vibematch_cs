package view;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

// screen 2 -- the 4 digit code entry. the countdown is just cosmetic (javax.swing.Timer
// ticking once a second), it doesnt actually gate anything -- Verify accepts any code
public class EmailVerificationPanel extends JPanel {

    Frame frame;
    JLabel infoLabel;
    JLabel timerLabel;
    JButton resendButton;
    JTextField code1;
    JTextField code2;
    JTextField code3;
    JTextField code4;
    Timer countdown;
    int seconds_left = 60;

    EmailVerificationPanel(Frame frame){
        this.frame = frame;
        setBackground(new Color(245, 243, 250));
        setLayout(new GridLayout(7, 1));

        // countdown timer done with javax.swing.Timer , got the idea from here:
        // https://stackoverflow.com/questions/10820033/make-a-simple-timer-in-java
        countdown = new Timer(1000, e -> tick());

        JLabel title = new JLabel("Check your email", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));

        infoLabel = new JLabel("we sent a 4 digit code to your mail", SwingConstants.CENTER);
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 15));


        JPanel codeRow = new JPanel(new FlowLayout());
        codeRow.setBackground(new Color(245, 243, 250));
        code1 = new JTextField(2);
        code2 = new JTextField(2);
        code3 = new JTextField(2);
        code4 = new JTextField(2);
        code1.setFont(new Font("Arial", Font.BOLD, 26));
        code2.setFont(new Font("Arial", Font.BOLD, 26));
        code3.setFont(new Font("Arial", Font.BOLD, 26));
        code4.setFont(new Font("Arial", Font.BOLD, 26));
        codeRow.add(code1);
        codeRow.add(code2);
        codeRow.add(code3);
        codeRow.add(code4);

        timerLabel = new JLabel("you can resend in 60s", SwingConstants.CENTER);
        timerLabel.setForeground(Color.GRAY);

        JPanel buttonRow = new JPanel(new FlowLayout());
        buttonRow.setBackground(new Color(245, 243, 250));

        JButton verifyButton = new JButton("Verify");
        verifyButton.setPreferredSize(new Dimension(140, 40));
        verifyButton.setFocusable(false);
        // TODO: actually check the code against VerificationService (Ahmeds part --
        // model/VerificationService.verifyToken() is the scaffolding for this)
        // right now literally any code works , even empty
        verifyButton.addActionListener(e -> {
            countdown.stop();
            frame.go_to("interests");
        });

        resendButton = new JButton("resend code");
        resendButton.setFocusable(false);
        resendButton.setEnabled(false);
        resendButton.addActionListener(e -> refresh());

        buttonRow.add(verifyButton);
        buttonRow.add(resendButton);

        add(new JLabel(""));
        add(title);
        add(infoLabel);
        add(codeRow);
        add(timerLabel);
        add(buttonRow);
        add(new JLabel(""));
    }

    // called every time we land on this screen (and on resend)
    public void refresh(){
        infoLabel.setText("we sent a 4 digit code to " + frame.user_email);
        code1.setText("");
        code2.setText("");
        code3.setText("");
        code4.setText("");
        seconds_left = 60;
        resendButton.setEnabled(false);
        timerLabel.setText("you can resend in 60s");
        countdown.restart();
    }

    // fires once a second from the countdown Timer above
    public void tick(){
        seconds_left--;
        //System.out.println(seconds_left);
        if (seconds_left <= 0){
            countdown.stop();
            timerLabel.setText("didnt get it? you can resend now");
            resendButton.setEnabled(true);
        }else{
            timerLabel.setText("you can resend in " + seconds_left + "s");
        }
    }

}
