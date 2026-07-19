package model;

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
 * status: SCAFFOLD ONLY.
 */
public class Notification {

    String type; // "friend_request" / "friend_accepted" / "reply" -- report gives these as examples, not exact values
    String text;
    String createdAt;
    boolean isRead;

    public void insert(){
        // TODO: create a new notification row for whichever user this belongs to.
        // "When something happens elsewhere...we drop one in with insert()"
    }

    public int unreadCount(){
        // TODO: count how many of this user's notifications have isRead == false
        return 0;
    }

    public void markAllRead(){
        // TODO: flip isRead = true on everything for this user
    }

}
