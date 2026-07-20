package screens;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JFrame;

import app.AppConstants;
import model.User;
import ui.Theme;

/*
 * The single main window. Instead of opening lots of frames we keep one frame
 * and swap whatever screen is inside it. Screens call the show... methods below
 * to move around the app.
 *
 * Some of these still show a Placeholder for now, they get filled in as we build
 * the onboarding and the main app.
 */
public class AppFrame extends JFrame {

    public AppFrame() {
        setTitle(AppConstants.APP_NAME);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1040, 680);
        setMinimumSize(new java.awt.Dimension(900, 600));
        setLocationRelativeTo(null); // center on screen
        getContentPane().setBackground(Theme.BG);
    }

    // put a screen into the window
    public void showScreen(JComponent screen) {
        getContentPane().removeAll();
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(screen, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    public void showLogin() {
        showScreen(new LoginScreen(this));
    }

    public void showRegister() {
        showScreen(new RegisterScreen(this));
    }

    public void showVerify(String username, String email) {
        showScreen(new VerifyScreen(this, username, email));
    }

    // after a successful login or verify, decide where to send them
    public void routeAfterLogin(User user) {
        if (!user.hasVibe()) {
            startOnboarding(user);
        } else {
            enterApp(user);
        }
    }

    public void startOnboarding(User user) {
        showScreen(new InterestPanel(this, user));
    }

    public void startMbti(User user) {
        showScreen(new MbtiTestPanel(this, user));
    }

    public void showVibeResult(User user, model.MbtiResult result) {
        showScreen(new VibeResultPanel(this, user, result));
    }

    public void enterApp(User user) {
        showScreen(new MainWindow(this, user));
    }
}
