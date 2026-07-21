package view;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.CardLayout;
import model.User;
import net.Api;
import net.ServerClient;

// The app's shell: one outer CardLayout, every screen panel made once, and the
// navigation spine (go_to). The look is Khalil's original team design; the data
// now comes from the real backend over the socket (net/Api) instead of the old
// in-memory controllers -- that's the "we wired the UI to a real client-server
// API" work.
public class Frame extends JFrame {

    CardLayout cards;
    JPanel root;

    // whoever is logged in right now (real backend user), null until login
    User currentUser;
    // carried from register/login into the verify screen
    String pending_username = "";
    // kept for panels that still show them
    String user_email = "";
    String user_name = "student";
    // the MBTI result from the quiz, shown on the vibe profile screen
    model.MbtiResult mbtiResult;

    LoginSignupPanel loginPanel;
    EmailVerificationPanel verifyPanel;
    InterestSelectionPanel interestPanel;
    PersonalityQuizPanel quizPanel;
    VibeProfilePanel vibePanel;
    AppShellPanel shell;
    AdminPanel adminPanel;

    Frame(){
        this("127.0.0.1");
    }

    Frame(String host){
        // connect to the server before anything touches the api
        boolean connected = ServerClient.getInstance().connect(host, 5050);
        if (!connected){
            javax.swing.JOptionPane.showMessageDialog(null,
                "Couldn't reach the VibeMatch server.\nStart it first with:  ./run-server.sh");
            System.exit(1);
        }

        this.setTitle("VibeMatch");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(1000, 650);
        this.setLocationRelativeTo(null);

        cards = new CardLayout();
        root = new JPanel(cards);

        loginPanel = new LoginSignupPanel(this);
        verifyPanel = new EmailVerificationPanel(this);
        interestPanel = new InterestSelectionPanel(this);
        quizPanel = new PersonalityQuizPanel(this);
        vibePanel = new VibeProfilePanel(this);
        shell = new AppShellPanel(this);
        adminPanel = new AdminPanel(this);

        root.add(loginPanel, "login");
        root.add(verifyPanel, "verify");
        root.add(interestPanel, "interests");
        root.add(quizPanel, "quiz");
        root.add(vibePanel, "vibe");
        root.add(shell, "app");
        root.add(adminPanel, "admin");

        this.add(root);
        this.setVisible(true);
    }

    // set who is logged in. panels read frame.currentUser after this.
    public void setCurrentUser(User u){
        this.currentUser = u;
        if (u != null){
            this.user_email = u.getEmail();
            this.user_name = u.getDisplayName();
        }
    }

    public String username(){
        return currentUser == null ? null : currentUser.getUsername();
    }

    // one method for all navigation so panels dont need to know about cards.
    // "name" doubles as an outer card (login/verify/...) or an inner app-shell
    // page (home/discover/...) -- unknown names fall through to the shell.
    public void go_to(String name){
        if (name.equals("login")){
            cards.show(root, "login");
        }else if (name.equals("verify")){
            verifyPanel.refresh();
            cards.show(root, "verify");
        }else if (name.equals("interests")){
            // already did onboarding? skip straight into the app
            if (currentUser != null && currentUser.hasVibe()){
                go_to("home");
                return;
            }
            cards.show(root, "interests");
        }else if (name.equals("quiz")){
            cards.show(root, "quiz");
        }else if (name.equals("vibe")){
            vibePanel.refresh();
            cards.show(root, "vibe");
        }else if (name.equals("admin")){
            adminPanel.refresh();
            cards.show(root, "admin");
        }else{
            // everything else lives inside the app shell (home, discover, ...)
            cards.show(root, "app");
            shell.show_page(name);
            updateNotificationBadge(); // keep the unread count fresh on every page
        }
    }

    public void open_community(model.Community c){
        shell.detail.set_community(c);
        go_to("detail");
    }

    // refresh the unread count shown on the sidebar's Notifications button
    public void updateNotificationBadge(){
        if (currentUser == null){
            return;
        }
        try {
            int n = Api.get().unreadCount(currentUser.getUsername());
            shell.sidebar.setNotifCount(n);
        } catch (Exception ignore) {}
    }

    // log out: drop the server session and return to login
    public void logout(){
        try { Api.get().logout(); } catch (Exception ignore) {}
        currentUser = null;
        go_to("login");
    }
}
