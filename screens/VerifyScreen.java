package screens;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import model.User;
import net.Api;
import ui.RoundedButton;
import ui.Session;
import ui.Theme;
import ui.UiHelper;

/*
 * Where the user types the code we emailed them. Now the SERVER is the one that
 * knows the real code, so we just send what they typed and the server tells us
 * if it was right. On success we get the (now verified) user back and head into
 * onboarding.
 */
public class VerifyScreen extends JPanel {

    private AppFrame appFrame;
    private Api api = Api.get();
    private String username;
    private String email;

    private JTextField codeField = new JTextField();
    private JLabel errorLabel = new JLabel(" ");

    public VerifyScreen(AppFrame appFrame, String username, String email) {
        this.appFrame = appFrame;
        this.username = username;
        this.email = email;

        JPanel form = new JPanel();
        form.setBackground(Theme.WHITE);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel title = UiHelper.title("Enter your code", 26);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(title);
        form.add(UiHelper.vgap(6));

        JLabel sub = UiHelper.muted("We sent a 6 digit code to " + email, 13);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(sub);
        form.add(UiHelper.vgap(22));

        codeField.setFont(Theme.heading(26));
        codeField.setHorizontalAlignment(JTextField.CENTER);
        JPanel codeBox = UiHelper.field(codeField);
        codeBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        codeBox.setMaximumSize(new Dimension(220, 60));
        form.add(codeBox);
        form.add(UiHelper.vgap(10));

        errorLabel.setFont(Theme.body(12));
        errorLabel.setForeground(new Color(0xC0, 0x39, 0x4B));
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(errorLabel);
        form.add(UiHelper.vgap(8));

        RoundedButton btn = UiHelper.primaryButton("Verify and continue");
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doVerify();
            }
        });
        form.add(btn);
        form.add(UiHelper.vgap(16));

        form.add(buildResend());

        AuthShell shell = new AuthShell(
            "Check your<br>inbox 📬",
            "The code is on its way to your Bilkent mail.",
            new Color(0x5C, 0xC0, 0x9A),
            form);
        setLayout(new java.awt.BorderLayout());
        add(shell);
    }

    private JPanel buildResend() {
        JPanel row = new JPanel();
        row.setBackground(Theme.WHITE);
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(UiHelper.muted("Didn't get it? ", 13));
        JLabel link = new JLabel("Resend code");
        link.setFont(Theme.bodyBold(13));
        link.setForeground(Theme.LILAC_600);
        link.setCursor(new Cursor(Cursor.HAND_CURSOR));
        link.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                resend();
            }
        });
        row.add(link);
        return row;
    }

    private void doVerify() {
        errorLabel.setText(" ");
        String typed = codeField.getText().trim();
        try {
            User user = api.verify(username, typed);
            Session.setUser(user);
            appFrame.routeAfterLogin(user);
        } catch (IllegalArgumentException ex) {
            errorLabel.setText(ex.getMessage());
        }
    }

    private void resend() {
        try {
            Api.ResendResult result = api.resend(username, email);
            if (!result.emailed) {
                JOptionPane.showMessageDialog(this,
                    "Here is your new code:\n\n   " + result.code,
                    "Verification code",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }
}
