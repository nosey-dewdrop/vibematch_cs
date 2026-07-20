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
 * status: first pass. keeps the community registry in a shared in-memory list,
 * same stopgap the rest of the app uses until the db is wired up.
 * design risk callout: "Admin/student privilege separation" -- check permissions
 * on every admin-facing call once this is real.
 */
public class Administrator {

    String adminId;
    String username;
    String passwordHash;

    // the report says admin actions "call the same backend as the student app", so
    // there has to be one shared place communities actually live. this stands in for
    // that until the db exists -- Mete's Community is the same object the student
    // side reads, an admin just creates/removes rows in here
    static List<Community> communities = new ArrayList<>();

    public Community createCommunity(String name, String description){
        Community community = new Community();
        community.name = name;
        community.description = description;
        communities.add(community);
        return community;
    }

    // finds the community by id and drops it. its GroupChat is a field on the
    // Community object itself, so removing the community takes the chat + its
    // messages with it, nothing orphaned left behind
    public void deleteCommunity(String communityId){
        int i = 0;
        while (i < communities.size()){
            Community c = communities.get(i);
            if (c.communityId != null && c.communityId.equals(communityId)){
                communities.remove(i);
            }
            i = i + 1;
        }
    }

    // the admin dashboard wants every chat in one place. walk the community list,
    // pull each ones groupChat out, skip any that dont have one yet
    public List<GroupChat> viewAllChats(){
        List<GroupChat> chats = new ArrayList<>();
        int i = 0;
        while (i < communities.size()){
            Community c = communities.get(i);
            if (c.groupChat != null){
                chats.add(c.groupChat);
            }
            i = i + 1;
        }
        return chats;
    }

}
