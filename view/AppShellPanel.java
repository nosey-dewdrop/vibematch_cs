package view;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;

// this is the "logged in" part of the app -- sidebar on the left thats always
// there, plus a SECOND CardLayout on the right for the 5 pages you can navigate
// between (home/discover/detail/mycom/chats/profile). Frame's own CardLayout only
// ever needs to know about "onboarding screens" vs "this whole shell", it doesnt
// care which inner page is showing -- thats this class's job
public class AppShellPanel extends JPanel {

    Frame frame;
    CardLayout inner_cards;
    JPanel content;

    HomeFeedPanel home;
    DiscoverPanel discover;
    CommunityDetailPanel detail;
    MyCommunitiesEventsPanel mycom;
    CommunityChatsPanel chats;
    ProfileSettingsPanel profile;

    AppShellPanel(Frame frame){
        this.frame = frame;
        setLayout(new BorderLayout());

        Sidebar sidebar = new Sidebar(frame);
        add(sidebar, BorderLayout.WEST);

        inner_cards = new CardLayout();
        content = new JPanel(inner_cards);

        home = new HomeFeedPanel(frame);
        discover = new DiscoverPanel(frame);
        detail = new CommunityDetailPanel(frame);
        mycom = new MyCommunitiesEventsPanel(frame);
        chats = new CommunityChatsPanel(frame);
        profile = new ProfileSettingsPanel(frame);

        content.add(home, "home");
        content.add(discover, "discover");
        content.add(detail, "detail");
        content.add(mycom, "mycom");
        content.add(chats, "chats");
        content.add(profile, "profile");

        add(content, BorderLayout.CENTER);
    }

    public void show_page(String name){
        // refresh whatever page we are going to so it shows fresh data.
        // probably shouldve been else-if since only one of these can ever match,
        // but separate ifs work fine too, just does a couple extra useless checks
        if (name.equals("home")){
            home.refresh();
        }
        if (name.equals("discover")){
            discover.refresh();
        }
        if (name.equals("detail")){
            detail.refresh();
        }
        if (name.equals("mycom")){
            mycom.refresh();
        }
        if (name.equals("chats")){
            chats.refresh();
        }
        if (name.equals("profile")){
            profile.refresh();
        }
        inner_cards.show(content, name);
    }

}
