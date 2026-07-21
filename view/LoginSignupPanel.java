package view;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Arrays;
import model.User;

// screen 1 -- log in AND sign up share this one panel, just toggles which
// fields/labels show. this is what controller/AuthController.java is meant to
// sit behind eventually (see that file for what "real" would look like)
public class LoginSignupPanel extends JPanel {

    Frame frame;
    boolean signup_mode = false;
    JTextField email_field;
    JTextField name_field;
    JPasswordField pass_field;
    JButton mainButton;
    JButton modeButton;
    JLabel titleLabel;
    JPanel name_row;

    //main purple: new Color(103, 58, 183)
    //light bg: new Color(245, 243, 250)
    //dark side: new Color(40, 35, 60)

    LoginSignupPanel(Frame frame){
        this.frame = frame;
        setLayout(new BorderLayout());

        // left side , the branding part (split window like in the report)
        JPanel brandPanel = new JPanel();
        brandPanel.setBackground(new Color(40, 35, 60));
        brandPanel.setPreferredSize(new Dimension(380, 650));
        brandPanel.setLayout(new GridLayout(3, 1));

        JLabel logoLabel = new JLabel("VibeMatch", SwingConstants.CENTER);
        logoLabel.setForeground(new Color(103, 58, 183));
        logoLabel.setFont(new Font("Arial", Font.BOLD, 42));

        JLabel sloganLabel = new JLabel("find your people at Bilkent", SwingConstants.CENTER);
        sloganLabel.setForeground(Color.WHITE);
        sloganLabel.setFont(new Font("Arial", Font.PLAIN, 16));

        brandPanel.add(new JLabel(""));
        brandPanel.add(logoLabel);
        brandPanel.add(sloganLabel);

        add(brandPanel, BorderLayout.WEST);


        // right side , the actual form
        JPanel formPanel = new JPanel();
        formPanel.setBackground(new Color(245, 243, 250));
        formPanel.setLayout(new GridLayout(8, 1));

        titleLabel = new JLabel("Log In", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));

        name_row = new JPanel(new FlowLayout());
        name_row.setBackground(new Color(245, 243, 250));
        name_row.add(new JLabel("Display name:"));
        name_field = new JTextField(18);
        name_row.add(name_field);
        name_row.setVisible(false); // only for sign up

        JPanel email_row = new JPanel(new FlowLayout());
        email_row.setBackground(new Color(245, 243, 250));
        email_row.add(new JLabel("Bilkent email:"));
        email_field = new JTextField(18);
        email_row.add(email_field);

        JPanel pass_row = new JPanel(new FlowLayout());
        pass_row.setBackground(new Color(245, 243, 250));
        pass_row.add(new JLabel("Password:    "));
        pass_field = new JPasswordField(18);
        pass_row.add(pass_field);

        mainButton = new JButton("Log In");
        mainButton.setFocusable(false);
        mainButton.addActionListener(e -> submit());

        modeButton = new JButton("no account? Sign Up");
        modeButton.setFocusable(false);
        modeButton.setBorderPainted(false);
        modeButton.setContentAreaFilled(false);
        modeButton.setForeground(new Color(103, 58, 183));
        modeButton.addActionListener(e -> switche_mode());

        JButton adminButton = new JButton("admin login");
        adminButton.setFocusable(false);
        adminButton.setBorderPainted(false);
        adminButton.setContentAreaFilled(false);
        adminButton.setForeground(Color.GRAY);
        adminButton.addActionListener(e -> frame.go_to("admin")); // no real password check, its a placeholder

        JPanel buttonRow = new JPanel(new FlowLayout());
        buttonRow.setBackground(new Color(245, 243, 250));
        JPanel b_inner = new JPanel();
        b_inner.setBackground(new Color(245, 243, 250));
        mainButton.setPreferredSize(new Dimension(160, 40));
        b_inner.add(mainButton);
        buttonRow.add(b_inner);

        formPanel.add(new JLabel(""));
        formPanel.add(titleLabel);
        formPanel.add(name_row);
        formPanel.add(email_row);
        formPanel.add(pass_row);
        formPanel.add(buttonRow);
        formPanel.add(modeButton);
        formPanel.add(adminButton);

        add(formPanel, BorderLayout.CENTER);
    }

    // switches between log in and sign up , same screen tho
    public void switche_mode(){
        signup_mode = !signup_mode;
        if (signup_mode){
            titleLabel.setText("Sign Up");
            mainButton.setText("Sign Up");
            modeButton.setText("have an account? Log In");
            name_row.setVisible(true);
        }else{
            titleLabel.setText("Log In");
            mainButton.setText("Log In");
            modeButton.setText("no account? Sign Up");
            name_row.setVisible(false);
        }
    }

    public void submit(){
        String email = email_field.getText();
        //System.out.println(email);

        // this is the whole bilkent domain guard for now lol
        if (!email.endsWith("@ug.bilkent.edu.tr")){
            JOptionPane.showMessageDialog(this, "only Bilkent students can join!\n(use your @ug.bilkent.edu.tr mail)");
            return;
        }

        char[] pass_chars = pass_field.getPassword();
        String password = new String(pass_chars);
        if (password.length() == 0){
            JOptionPane.showMessageDialog(this, "password can't be empty!");
            return;
        }

        frame.user_email = email;
        if (name_field.getText().length() > 0){
            frame.user_name = name_field.getText();
        }

        // the backend keys on a username; Khalil's form only has an email, so we
        // derive the username from the email local-part (before the @). the
        // backend only allows letters/numbers/underscore, so any other character
        // (like the dot in "su.bilge") gets turned into an underscore.
        String username = email.substring(0, email.indexOf('@'))
                .replaceAll("[^a-zA-Z0-9_]", "_");

        if (signup_mode){
            String displayName = name_field.getText().trim().isEmpty()
                    ? username : name_field.getText().trim();
            try {
                net.Api.RegisterResult r = net.Api.get().register(displayName, username, email, password);
                Arrays.fill(pass_chars, ' ');
                frame.pending_username = r.username;
                frame.user_email = r.email;
                if (!r.emailed && r.code != null){
                    JOptionPane.showMessageDialog(this,
                        "Email isn't set up, so here's your code: " + r.code);
                }
                frame.go_to("verify");
            } catch (IllegalArgumentException ex){
                Arrays.fill(pass_chars, ' ');
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }else{
            try {
                User user = net.Api.get().login(email, password); // accepts email OR username
                Arrays.fill(pass_chars, ' ');
                frame.setCurrentUser(user);
                frame.go_to("interests"); // skips to home if onboarding already done
            } catch (IllegalArgumentException ex){
                Arrays.fill(pass_chars, ' ');
                String msg = ex.getMessage() == null ? "" : ex.getMessage();
                if (msg.toLowerCase().contains("verify")){
                    // unverified -> resend a code and go to the verify screen
                    try {
                        net.Api.ResendResult r = net.Api.get().resend(username, "");
                        frame.pending_username = r.username;
                        frame.user_email = r.email;
                        if (!r.emailed && r.code != null){
                            JOptionPane.showMessageDialog(this,
                                "Email isn't set up, so here's your code: " + r.code);
                        }
                        frame.go_to("verify");
                    } catch (IllegalArgumentException ex2){
                        JOptionPane.showMessageDialog(this, ex2.getMessage());
                    }
                } else {
                    JOptionPane.showMessageDialog(this, msg);
                }
            }
        }
    }

}
