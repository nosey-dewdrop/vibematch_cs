package controller;

import model.Administrator;
import model.Community;
import model.GroupChat;
import java.util.List;

/*
 * CONTROLLER -- AdminController
 * handles: the admin dashboard. per Table 5, Damla's class (Administrator),
 * plus Friendship/Notification if the admin panel ends up touching those too.
 *
 * "The admin panel calls the same backend as the student app, so a community
 * created or removed by staff behaves the same way for everyone." (2.3)
 *
 * status: SCAFFOLD ONLY, NOT wired into the view/ package. AdminPanel.java in
 * view/ currently just deletes straight out of Frame's in-memory list with no
 * login check at all -- this controller + a real Administrator login is meant
 * to replace that.
 */
public class AdminController {

    public Community createCommunity(Administrator admin, String name, String description){
        // TODO: return admin.createCommunity(name, description);
        return null;
    }

    public void deleteCommunity(Administrator admin, String communityId){
        // TODO: admin.deleteCommunity(communityId);
    }

    public List<GroupChat> viewAllChats(Administrator admin){
        // TODO: return admin.viewAllChats();
        return null;
    }

}
