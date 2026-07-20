package model;

import java.util.List;
import java.util.ArrayList;

/*
 * MODEL -- Notification
 * owner: Damla Su Bilge   (Table 5)
 *
 * NEW in the v2 design report, same as Friendship. "A single alert on a
 * student's bell, like a friend request, an accepted request, or a reply
 * to one of their posts." (4.6) "the bell reads them back newest-first
 * with unreadCount() and clears them with markAllRead()."
 *
 * HEADS UP: same note as Friendship.java -- this class is in the report text
 * (Table 3 + section 4.6) but NOT in the actual UML diagram image (fig 2).
 * added anyway since the text is pretty explicit about it, but the diagram
 * probably needs to be updated to match before this report is final.
 *
 * status: first pass. in-memory only, no db yet -- same as User right now.
 */
public class Notification {

    String ownerId; // whose bell this belongs to
    String type; // "friend_request" / "friend_accepted" / "reply" -- report gives these as examples, not exact values
    String text;
    String createdAt;
    boolean isRead;

    // the report talks about unreadCount() and markAllRead() as things you do to
    // "everything for this user", which isnt something one notification row can
    // answer about itself. so the individual rows live in this one shared list and
    // the methods below work over it, filtered by ownerId. standing in for the db
    // until thats built, exactly like AuthController.usersByEmail does
    static List<Notification> all = new ArrayList<>();

    public Notification(String ownerId, String type, String text, String createdAt){
        this.ownerId = ownerId;
        this.type = type;
        this.text = text;
        this.createdAt = createdAt;
        this.isRead = false;
    }

    // "when something happens elsewhere we drop one in with insert()". so this is
    // the whole point of building the object -- you make one, then insert() it, and
    // now it shows up on that users bell
    public void insert(){
        all.add(this);
    }

    // counts this owners unread ones. walks the shared list, skips anything that
    // belongs to someone else or is already read
    public int unreadCount(){
        int count = 0;
        int i = 0;
        while (i < all.size()){
            Notification n = all.get(i);
            if (n.ownerId.equals(this.ownerId)){
                if (n.isRead == false){
                    count = count + 1;
                }
            }
            i = i + 1;
        }
        return count;
    }

    // clears this owners bell -- flips isRead on every row thats theirs
    public void markAllRead(){
        int i = 0;
        while (i < all.size()){
            Notification n = all.get(i);
            if (n.ownerId.equals(this.ownerId)){
                n.isRead = true;
            }
            i = i + 1;
        }
    }

}
