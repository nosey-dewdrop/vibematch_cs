package model;

import java.util.List;
import java.util.ArrayList;

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
 * Once this is actually implemented, the view/ panels should switch over to using
 * this instead of their own placeholder -- thats a bigger change than "add scaffolding"
 * though so leaving that for later.
 *
 * status: SCAFFOLD ONLY, fields/methods match the report, bodies are TODO.
 */
public class Community {

    String communityId;
    String name;
    String description;

    // composition -- a GroupChat cant exist without its Community
    GroupChat groupChat;

    // TODO: this should probably be a List<User> once User has an id we can store here
    List<String> memberIds = new ArrayList<>();

    public void addMember(User user){
        // TODO: add user to the members list, probably memberIds.add(user.userId)
    }

    public void removeMember(User user){
        // TODO: opposite of addMember()
    }

    public int getMemberCount(){
        // TODO: this is probably fine as-is once memberIds is actually being filled
        return memberIds.size();
    }

}
