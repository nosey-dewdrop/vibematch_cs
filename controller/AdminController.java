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
 * status: first pass, logic done. still NOT wired into the view/ package --
 * AdminPanel.java in view/ currently just deletes straight out of Frame's in-memory
 * list with no login check at all -- this controller + a real Administrator login is
 * meant to replace that.
 */
public class AdminController {

    // every method takes the Administrator doing the action instead of pulling one
    // from somewhere global. thats the privilege-separation risk row in the report --
    // an action only happens if theres a real admin passed in to do it, a student
    // account cant reach these because it isnt an Administrator in the first place
    public Community createCommunity(Administrator admin, String name, String description){
        if (admin == null){
            return null;
        }
        return admin.createCommunity(name, description);
    }

    public void deleteCommunity(Administrator admin, String communityId){
        if (admin == null){
            return;
        }
        admin.deleteCommunity(communityId);
    }

    public List<GroupChat> viewAllChats(Administrator admin){
        if (admin == null){
            return null;
        }
        return admin.viewAllChats();
    }

}
