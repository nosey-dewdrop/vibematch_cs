package view;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.CardLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import controller.AuthController;
import model.User;
//import java.awt.Color;

// this is basically the whole app's brain right now -- holds the outer CardLayout,
// makes every screen panel once in the constructor, and holds all the "session state"
// (which communities exist, which ones you joined, your fake archetype etc) as plain
// fields since theres no real backend/database yet. once model/ actually gets
// implemented, this state should probably move into a real model.User + friends
// instead of living here, but for now this is where it all lives
public class Frame extends JFrame {

    CardLayout cards;
    JPanel root;

    ArrayList<Community> all_communities = new ArrayList<>();
    ArrayList<Community> my_communities = new ArrayList<>();
    ArrayList<String> my_tags = new ArrayList<>();
    String user_email = "";
    String user_name = "student";
    String archetype = "???";

    // real auth now (see login_functionality.md) -- LoginSignupPanel and
    // EmailVerificationPanel actually call into this now instead of faking it
    AuthController authController = new AuthController();
    User currentUser; // whoever's actually logged in, null til that happens

    LoginSignupPanel loginPanel;
    EmailVerificationPanel verifyPanel;
    InterestSelectionPanel interestPanel;
    PersonalityQuizPanel quizPanel;
    VibeProfilePanel vibePanel;
    AppShellPanel shell;
    AdminPanel adminPanel;

    Frame(){
        make_communities();


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

    // one method for all the navigation so panels dont need to know about cards.
    // "name" doubles as both an outer card name (login/verify/...) AND an inner
    // app-shell page name (home/discover/...) -- if its not one of the outer ones
    // it falls through to the else and gets handed off to shell.show_page() instead
    public void go_to(String name){
        if (name.equals("login")){
            cards.show(root, "login");
        }else if (name.equals("verify")){
            verifyPanel.refresh();
            cards.show(root, "verify");
        }else if (name.equals("interests")){
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
            // everything else is inside the app shell (home, discover, detail...)
            cards.show(root, "app");
            shell.show_page(name);
        }
        //System.out.println("going to " + name);
    }

    public void open_community(Community c){
        shell.detail.set_community(c);
        go_to("detail");
    }


    public void pick_archetype(){
        // TODO: this should use the real quiz answers later (RecommendationEngine stuff).
        // once model/PersonalityTest.java + model/RecommendationEngine.java are actually
        // implemented this whole method should basically go away
        List<String> l = new ArrayList<>();
        Collections.shuffle(l);
        l.add("The Explorer");
        l.add("The Harmonizer");
        l.add("The Thinker");
        l.add("The Spark");
        Collections.shuffle(l); // had to shuffle again cuz it wasnt random ??
        this.archetype = l.get(0);
    }


    // hardcoded seed data so there's something to click through -- these are
    // view.Community objects (the demo one), not the real model.Community
    public void make_communities(){
        Random rand = new Random();

        Community c1 = new Community(1, "Jazz Lovers", "we listen to jazz and talk about jazz. thats it.", "Music", 47);
        c1.get_events().add("Listening night @ dorm 77 common room - friday 20:00");
        c1.messages.add(new Message("melis", "anyone going to the listening night??", "14:02"));
        c1.messages.add(new Message("arda", "yes!! bringing my mingus records", "14:05"));

        Community c2 = new Community(2, "Board Game Club", "catan, chess, and everything in between", "Games", 88);
        c2.get_events().add("Catan tournament - saturday 15:00 @ student center");
        c2.messages.add(new Message("deniz", "we need a 4th player for saturday", "09:30"));

        Community c3 = new Community(3, "Hiking Crew", "weekend hikes around ankara, all levels welcome", "Outdoor", 63);
        c3.get_events().add("Eymir lake walk - sunday 09:00");

        Community c4 = new Community(4, "Anime Watchers", "weekly watch parties + seasonal rankings", "Art & Media", 120);
        c4.messages.add(new Message("kaan", "this weeks episode was insane", "22:14"));

        Community c5 = new Community(5, "Bilkent Chess Society", "casual and rated games every week", "Games", 75);
        c5.get_events().add("Blitz night - wednesday 18:00 @ B building");

        Community c6 = new Community(6, "Indie Music Nights", "sharing playlists + going to local gigs together", "Music", 54);

        Community c7 = new Community(7, "Animal Welfare Volunteers", "helping the campus cats (and sometimes dogs)", "Volunteering", 96);
        c7.get_events().add("Food run for campus cats - everyday 17:00");

        Community c8 = new Community(8, "Football 5v5", "pickup games, dont have to be good just show up", "Sports", 110);

        Community c9 = new Community(9, "Photography Walks", "campus photo walks, phone cameras totally fine", "Art & Media", 41);

        Community c10 = new Community(10, "Code & Coffee", "side projects, leetcode crying sessions, coffee", "Tech", 67);
        c10.messages.add(new Message("zeynep", "cs102 project due next week... anyone else panicking", "11:45"));


        all_communities.add(c1);
        all_communities.add(c2);
        all_communities.add(c3);
        all_communities.add(c4);
        all_communities.add(c5);
        all_communities.add(c6);
        all_communities.add(c7);
        all_communities.add(c8);
        all_communities.add(c9);
        all_communities.add(c10);

        // fake match percents for now , real ones come from RecommendationEngine later
        for (int i = 0; i < all_communities.size(); i++) {
            all_communities.get(i).set_match_percent(rand.nextInt(45) + 55);
        }
    }


}
