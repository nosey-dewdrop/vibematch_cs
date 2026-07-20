package model;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

/*
 * MODEL -- Community
 * owner: Mete Kemal Coskuner   (Table 5)
 *
 * "An interest-based group, e.g., Jazz Lovers or Board Game Club." (4.3)
 * tracks its own members through addMember()/removeMember() and owns exactly
 * one GroupChat. UML: composes 1 GroupChat (filled diamond), many-to-many
 * association with User through membership, managed by Administrator.
 *
 * NOTE: there is ALSO a view.Community class (the GUI-only placeholder used by
 * the click-through prototype -- it has demo fields like match_percent that
 * arent part of the real design). Theyre in different packages on purpose so
 * they dont collide. This one here is the real Model class from the UML diagram.
 *
 * status: real now. the memberIds list is in-memory only, same deal as
 * AuthController.usersByEmail -- its standing in for the database until
 * that gets built, so restart the app and every membership is gone.
 */
public class Community {

    String communityId;
    String name;
    String description;

    // composition -- a GroupChat cant exist without its Community, which is
    // exactly why its built right here in the constructor and theres no
    // setGroupChat() anywhere. nobody hands us a chat from outside.
    GroupChat groupChat;

    // went with ids instead of List<User> -- keeping whole User objects here
    // would mean every Community drags around password hashes and bios it has
    // no business holding. the id is all membership actually needs.
    List<String> memberIds = new ArrayList<>();

    // NOT in the report's field list, but RecommendationEngine needs SOMETHING
    // on the community side to match a student's Tags/genres against, otherwise
    // calculateMatchScore() has literally nothing to compare. so a community
    // carries its own interest tags ("jazz", "board games", ...) -- set by
    // whoever creates it (Administrator.createCommunity() on Damla's side).
    List<Tag> tags = new ArrayList<>();

    public Community(String name, String description){
        this.communityId = UUID.randomUUID().toString(); // same id scheme as User.register()
        this.name = name;
        this.description = description;
        this.groupChat = new GroupChat();
        // GroupChat's fields are package-private like everyone elses, so we can
        // stamp our id straight onto it -- same trick VerificationService pulls
        // when it flips user.isVerified
        this.groupChat.chatId = UUID.randomUUID().toString();
        this.groupChat.communityId = this.communityId;
    }

    // returns boolean instead of void btw -- the report doesnt specify, but the
    // view needs to know whether the join actually happened so the button can
    // flip to "Joined" or not (see HomeFeedPanel's single-click join)
    public boolean addMember(User user){
        if (user == null || user.userId == null){
            return false; // no account, or register() was never called -- nothing to store
        }
        if (memberIds.contains(user.userId)){
            return false; // already in, dont count anyone twice
        }
        memberIds.add(user.userId);
        return true;
    }

    public boolean removeMember(User user){
        if (user == null || user.userId == null){
            return false;
        }
        return memberIds.remove(user.userId);
    }

    public boolean isMember(User user){
        if (user == null || user.userId == null){
            return false;
        }
        return memberIds.contains(user.userId);
    }

    public int getMemberCount(){
        return memberIds.size();
    }

    public void addTag(Tag tag){
        if (tag == null || tag.getName() == null || tag.getName().isEmpty()){
            return;
        }
        tags.add(tag);
    }

    public String getCommunityId(){
        return communityId;
    }

    public String getName(){
        return name;
    }

    public String getDescription(){
        return description;
    }

    public GroupChat getGroupChat(){
        return groupChat;
    }

    public List<Tag> getTags(){
        return tags;
    }

}
