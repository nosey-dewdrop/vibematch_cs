package model;

import java.util.List;
import java.util.ArrayList;

/*
 * MODEL -- Administrator
 * owner: Damla Su Bilge   (Table 5)
 *
 * "A staff/moderator account, kept entirely separate from student accounts." (4.5)
 * "Keeping it a standalone class instead of a special kind of User makes it much
 * harder to accidentally give a student account admin powers." -- so this should
 * NOT extend User, on purpose, even though it looks kind of similar.
 * UML: manages Community (association, "manages").
 *
 * status: SCAFFOLD ONLY.
 * design risk callout: "Admin/student privilege separation" -- check permissions
 * on every admin-facing call once this is real.
 */
public class Administrator {

    String adminId;
    String username;
    String passwordHash;

    public Community createCommunity(String name, String description){
        // TODO: make a new model.Community, save it, return it
        return null;
    }

    public void deleteCommunity(String communityId){
        // TODO: remove the community (and probably its GroupChat/messages too?)
    }

    public List<GroupChat> viewAllChats(){
        // TODO: pull every GroupChat across every community, for the admin dashboard
        return new ArrayList<>();
    }

}
