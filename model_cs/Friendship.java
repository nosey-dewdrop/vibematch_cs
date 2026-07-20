package model;

/*
 * MODEL -- Friendship
 * owner: Damla Su Bilge   (Table 5)
 *
 * NEW in the v2 design report -- wasnt in v1 at all. "A link between two
 * students. It starts as a pending request from one side and becomes a
 * friendship once the other accepts." (4.6)
 *
 * "We keep one row per pair, so once a request is accepted it stops
 * mattering who sent it." -- so status probably goes "pending" -> "accepted"
 * (or "declined"), and userA/userB dont mean "requester/receiver" anymore
 * once its accepted.
 *
 * HEADS UP: this class is described in section 4.6 of the report but it does
 * NOT appear in the actual UML diagram image (fig 2) -- that diagram still only
 * shows the original 12 classes from v1. probably just got missed when the
 * diagram was updated for v2. adding it here anyway since the text + Table 3
 * both clearly list it as a Model class, but flagging the mismatch in case
 * the diagram needs fixing before the report is final.
 *
 * status: first pass. went with pending/accepted/declined as the three states.
 */
public class Friendship {

    User userA;
    User userB;
    String status; // "pending" / "accepted" / "declined" probably, report doesnt give exact values
    String createdAt;

    // a Friendship object only makes sense between two actual people, so it takes
    // both users up front. requester is userA, receiver is userB -- that ordering
    // only matters while its still pending, once accepted the report says it stops
    // mattering who sent it
    public Friendship(User requester, User receiver){
        this.userA = requester;
        this.userB = receiver;
        this.status = "pending";
    }

    // fired when userA hits "add friend". the constructor already set pending, but
    // i kept request() as its own step so the createdAt timestamp gets stamped at
    // the moment its actually sent, not when the object happened to be built
    public void request(){
        this.status = "pending";
        this.createdAt = java.time.LocalDateTime.now().toString();
    }

    public void accept(){
        this.status = "accepted";
    }

    // declined instead of deleting the row -- the report wasnt sure, but keeping it
    // means userB doesnt get spammed with the same request over and over, we can see
    // it was already answered
    public void decline(){
        this.status = "declined";
    }

    public boolean areFriends(){
        return status.equals("accepted");
    }

}
