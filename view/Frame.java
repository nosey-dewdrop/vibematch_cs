package view;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.CardLayout;
import java.util.ArrayList;
import java.util.Random;
import controller.AuthController;
import controller.CommunityController;
import controller.ChatController;
import model.PersonalityResult;
import model.Tag;
import model.User;
//import java.awt.Color;

// this is basically the whole app's brain right now -- holds the outer CardLayout,
// makes every screen panel once in the constructor, and holds all the "session state"
// (which communities exist, which ones you joined, etc) as plain
// fields since theres no real backend/database yet. once model/ actually gets
// implemented, this state should probably move into a real model.User + friends
// instead of living here, but for now this is where it all lives
public class Frame extends JFrame {

    CardLayout cards;
    JPanel root;

    ArrayList<Community> all_communities = new ArrayList<>();
    ArrayList<Community> my_communities = new ArrayList<>();
    String user_email = "";
    String user_name = "student";
    PersonalityResult personalityResult;

    // real auth now (see login_functionality.md) -- LoginSignupPanel and
    // EmailVerificationPanel actually call into this now instead of faking it
    AuthController authController = new AuthController();
    CommunityController communityController = new CommunityController();
    ChatController chatController = new ChatController();
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

    // call after login + onboarding so the engine knows who we are. personalityResult
    // and interest tags come from the quiz/interest panels -- null is fine, engine
    // skips whatever it doesnt have (cold-start design, section 4.2)
    public void register_user_with_engine(){
        if (currentUser == null){
            return;
        }
        java.util.List<Tag> tags = new java.util.ArrayList<>();
        if (personalityResult != null){
            // archetype name doubles as a tag so communityHasTag() can match it
            tags.add(new Tag(personalityResult.getResultType()));
        }
        communityController.getRecommendationEngine().setUserProfile(
            currentUser, personalityResult, null, tags
        );
    }

    // get real match % from the engine for a view community by name.
    // falls back to the fake random one if the user isnt registered yet
    public int get_match_percent(Community viewCommunity){
        if (currentUser == null){
            return viewCommunity.get_match_percent();
        }
        java.util.List<model.Community> all = communityController.getAllCommunities();
        for (int i = 0; i < all.size(); i++){
            if (all.get(i).getName().equals(viewCommunity.get_name())){
                return communityController.getRecommendationEngine()
                    .getMatchPercent(currentUser, all.get(i));
            }
        }
        return viewCommunity.get_match_percent();
    }

    // join via the real model layer AND update the view layer so the card flips
    public void join_community(Community viewCommunity){
        if (!my_communities.contains(viewCommunity)){
            my_communities.add(viewCommunity);
            viewCommunity.member_count = viewCommunity.member_count + 1;
        }
        if (currentUser != null){
            java.util.List<model.Community> all = communityController.getAllCommunities();
            for (int i = 0; i < all.size(); i++){
                if (all.get(i).getName().equals(viewCommunity.get_name())){
                    communityController.joinCommunity(currentUser, all.get(i));
                    break;
                }
            }
        }
    }

    // send a message through the real moderation pipeline. returns false if blocked
    public boolean send_chat_message(Community viewCommunity, String senderName, String text){
        if (currentUser != null){
            java.util.List<model.Community> all = communityController.getAllCommunities();
            for (int i = 0; i < all.size(); i++){
                if (all.get(i).getName().equals(viewCommunity.get_name())){
                    model.GroupChat gc = all.get(i).getGroupChat();
                    model.Message msg = new model.Message(
                        java.util.UUID.randomUUID().toString(),
                        currentUser.getUserId() != null ? currentUser.getUserId() : senderName,
                        text,
                        java.time.LocalDateTime.now().toString()
                    );
                    return chatController.sendMessage(gc, msg);
                }
            }
        }
        // no model community found -- let it through unfiltered (view-only fallback)
        return true;
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

        // mirror each view community into the real model layer so the controllers
        // actually have something to work with. tag names match the category field
        // so RecommendationEngine can score them once the user has tags from onboarding
        String[][] seeds = {
            {"Jazz Lovers",              "we listen to jazz and talk about jazz. thats it.",   "Music"},
            {"Board Game Club",          "catan, chess, and everything in between",            "Games"},
            {"Hiking Crew",              "weekend hikes around ankara, all levels welcome",    "Outdoor"},
            {"Anime Watchers",           "weekly watch parties + seasonal rankings",           "Art & Media"},
            {"Bilkent Chess Society",    "casual and rated games every week",                  "Games"},
            {"Indie Music Nights",       "sharing playlists + going to local gigs together",   "Music"},
            {"Animal Welfare Volunteers","helping the campus cats (and sometimes dogs)",       "Volunteering"},
            {"Football 5v5",             "pickup games, dont have to be good just show up",    "Sports"},
            {"Photography Walks",        "campus photo walks, phone cameras totally fine",     "Art & Media"},
            {"Code & Coffee",            "side projects, leetcode crying sessions, coffee",    "Tech"}
        };
        for (int i = 0; i < seeds.length; i++) {
            model.Community mc = new model.Community(seeds[i][0], seeds[i][1]);
            mc.addTag(new Tag(seeds[i][2]));
            communityController.addCommunity(mc);
        }
    }


}
