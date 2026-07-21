package view;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.CardLayout;

import net.PushListener;
import net.ServerClient;

// this is the "logged in" part of the app -- sidebar on the left thats always
// there, plus a SECOND CardLayout on the right for the pages you navigate between
// (home/discover/detail/mycom/chats/notifications/profile). It also listens for
// server pushes so the sidebar's unread badge updates live.
public class AppShellPanel extends JPanel implements PushListener {

    Frame frame;
    CardLayout inner_cards;
    JPanel content;
    Sidebar sidebar;

    HomeFeedPanel home;
    DiscoverPanel discover;
    CommunityDetailPanel detail;
    MyCommunitiesEventsPanel mycom;
    CommunityChatsPanel chats;
    ProfileSettingsPanel profile;
    NotificationsPanel notifications;

    AppShellPanel(Frame frame){
        this.frame = frame;
        setLayout(new BorderLayout());

        sidebar = new Sidebar(frame);
        add(sidebar, BorderLayout.WEST);

        inner_cards = new CardLayout();
        content = new JPanel(inner_cards);

        home = new HomeFeedPanel(frame);
        discover = new DiscoverPanel(frame);
        detail = new CommunityDetailPanel(frame);
        mycom = new MyCommunitiesEventsPanel(frame);
        chats = new CommunityChatsPanel(frame);
        profile = new ProfileSettingsPanel(frame);
        notifications = new NotificationsPanel(frame);

        content.add(home, "home");
        content.add(discover, "discover");
        content.add(detail, "detail");
        content.add(mycom, "mycom");
        content.add(chats, "chats");
        content.add(profile, "profile");
        content.add(notifications, "notifications");

        add(content, BorderLayout.CENTER);

        // listen for any server push so the notifications badge stays live
        ServerClient.getInstance().addPushListener(this);
    }

    // any push (new message, friend request, friend accept) may have created a
    // notification -- refresh the sidebar unread count.
    public void onPush(String event, String dataJson){
        SwingUtilities.invokeLater(() -> frame.updateNotificationBadge());
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
        if (name.equals("notifications")){
            notifications.refresh();
        }
        inner_cards.show(content, name);
    }

}
