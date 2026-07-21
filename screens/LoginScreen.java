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
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import model.User;
import net.Api;
import ui.RoundedButton;
import ui.Session;
import ui.Theme;
import ui.UiHelper;

/*
 * The log in screen. Username or email + password. On success we figure out if
 * they still need to do onboarding or can go straight into the app.
 */
public class LoginScreen extends JPanel {

    private AppFrame appFrame;
    private Api api = Api.get();

    private JTextField userField = new JTextField();
    private JPasswordField passField = new JPasswordField();
    private JLabel errorLabel = new JLabel(" ");

    public LoginScreen(AppFrame appFrame) {
        this.appFrame = appFrame;

        JPanel form = new JPanel();
        form.setBackground(Theme.WHITE);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel title = UiHelper.title("Welcome back", 28);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(title);
        form.add(UiHelper.vgap(6));

        JLabel sub = UiHelper.muted("Log in to your communities.", 14);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(sub);
        form.add(UiHelper.vgap(24));

        form.add(fieldLabel("Username or Bilkent email"));
        JPanel userBox = UiHelper.field(userField);
        userBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(userBox);
        form.add(UiHelper.vgap(14));

        form.add(fieldLabel("Password"));
        JPanel passBox = UiHelper.field(passField);
        passBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(passBox);
        form.add(UiHelper.vgap(10));

        errorLabel.setFont(Theme.body(12));
        errorLabel.setForeground(new Color(0xC0, 0x39, 0x4B));
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(errorLabel);
        form.add(UiHelper.vgap(8));

        RoundedButton loginBtn = UiHelper.primaryButton("Log in");
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        loginBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doLogin();
            }
        });
        form.add(loginBtn);
        form.add(UiHelper.vgap(18));

        form.add(buildFooter());

        AuthShell shell = new AuthShell(
            "Your campus,<br>your circle.",
            "300+ communities matched to your vibe.",
            Theme.LILAC_600,
            form);
        setLayout(new java.awt.BorderLayout());
        add(shell);
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text.toUpperCase());
        l.setFont(Theme.bodyBold(11));
        l.setForeground(Theme.LILAC_500);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 2, 5, 0));
        return l;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel();
        footer.setBackground(Theme.WHITE);
        footer.setLayout(new BoxLayout(footer, BoxLayout.X_AXIS));
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);

        footer.add(UiHelper.muted("New here? ", 13));

        JLabel link = new JLabel("Create account");
        link.setFont(Theme.bodyBold(13));
        link.setForeground(Theme.LILAC_600);
        link.setCursor(new Cursor(Cursor.HAND_CURSOR));
        link.setHorizontalAlignment(SwingConstants.LEFT);
        link.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                appFrame.showRegister();
            }
        });
        footer.add(link);
        return footer;
    }

    private void doLogin() {
        errorLabel.setText(" ");
        String user = userField.getText();
        String pass = new String(passField.getPassword());
        try {
            User u = api.login(user, pass);
            Session.setUser(u);
            appFrame.routeAfterLogin(u);
        } catch (IllegalArgumentException ex) {
            String msg = ex.getMessage();
            // password was right but the account was never verified -- don't dead
            // end the user. send a fresh code and take them to the verify screen.
            if (msg != null && msg.toLowerCase().contains("verify")) {
                recoverUnverified(user);
                return;
            }
            errorLabel.setText(msg);
        }
    }

    // the account exists but isn't verified: resend a code and open verify.
    private void recoverUnverified(String usernameOrEmail) {
        try {
            Api.ResendResult r = api.resend(usernameOrEmail, "");
            if (!r.emailed && r.code != null) {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Email isn't set up, so here's your code: " + r.code);
            }
            appFrame.showVerify(r.username, r.email);
        } catch (IllegalArgumentException ex) {
            errorLabel.setText(ex.getMessage());
        }
    }
}
