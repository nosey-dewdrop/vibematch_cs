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
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import net.Api;
import ui.RoundedButton;
import ui.Theme;
import ui.UiHelper;

/*
 * Sign up screen. Collects name / username / email / password, creates the
 * (unverified) account, sends the email code and moves on to the verify screen.
 */
public class RegisterScreen extends JPanel {

    private AppFrame appFrame;
    private Api api = Api.get();

    private JTextField nameField = new JTextField();
    private JTextField userField = new JTextField();
    private JTextField emailField = new JTextField();
    private JPasswordField passField = new JPasswordField();
    private JLabel errorLabel = new JLabel(" ");

    public RegisterScreen(AppFrame appFrame) {
        this.appFrame = appFrame;

        JPanel form = new JPanel();
        form.setBackground(Theme.WHITE);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel title = UiHelper.title("Create your account", 26);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(title);
        form.add(UiHelper.vgap(6));

        JLabel sub = UiHelper.muted("Only Bilkent emails can join.", 14);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(sub);
        form.add(UiHelper.vgap(20));

        addField(form, "Full name", nameField);
        addField(form, "Username", userField);
        addField(form, "Bilkent email", emailField);
        addField(form, "Password", passField);

        errorLabel.setFont(Theme.body(12));
        errorLabel.setForeground(new Color(0xC0, 0x39, 0x4B));
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(errorLabel);
        form.add(UiHelper.vgap(6));

        RoundedButton btn = UiHelper.primaryButton("Send verification code");
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doRegister();
            }
        });
        form.add(btn);
        form.add(UiHelper.vgap(16));

        form.add(buildFooter());

        AuthShell shell = new AuthShell(
            "One campus.<br>Infinite circles.",
            "Verified students only. Takes a minute.",
            new Color(0xF5, 0x9E, 0x8B),
            form);
        setLayout(new java.awt.BorderLayout());
        add(shell);
    }

    private void addField(JPanel form, String label, JTextField field) {
        JLabel l = new JLabel(label.toUpperCase());
        l.setFont(Theme.bodyBold(11));
        l.setForeground(Theme.LILAC_500);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 2, 5, 0));
        form.add(l);

        JPanel box = UiHelper.field(field);
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(box);
        form.add(UiHelper.vgap(12));
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel();
        footer.setBackground(Theme.WHITE);
        footer.setLayout(new BoxLayout(footer, BoxLayout.X_AXIS));
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        footer.add(UiHelper.muted("Already have an account? ", 13));
        JLabel link = new JLabel("Log in");
        link.setFont(Theme.bodyBold(13));
        link.setForeground(Theme.LILAC_600);
        link.setCursor(new Cursor(Cursor.HAND_CURSOR));
        link.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                appFrame.showLogin();
            }
        });
        footer.add(link);
        return footer;
    }

    private void doRegister() {
        errorLabel.setText(" ");
        try {
            String name = nameField.getText();
            String username = userField.getText();
            String email = emailField.getText();
            String pass = new String(passField.getPassword());

            // the server creates the account, makes the code and tries to email
            // it. if email isnt set up it sends the code back so we can show it.
            Api.RegisterResult result = api.register(name, username, email, pass);
            if (!result.emailed) {
                JOptionPane.showMessageDialog(this,
                    "Email isn't set up, so here is your code:\n\n   " + result.code,
                    "Verification code",
                    JOptionPane.INFORMATION_MESSAGE);
            }
            appFrame.showVerify(result.username, result.email);
        } catch (IllegalArgumentException ex) {
            errorLabel.setText(ex.getMessage());
        }
    }
}
