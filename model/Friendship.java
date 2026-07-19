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
 * status: SCAFFOLD ONLY.
 */
public class Friendship {

    User userA;
    User userB;
    String status; // "pending" / "accepted" / "declined" probably, report doesnt give exact values
    String createdAt;

    public void request(){
        // TODO: create the pending row, userA = requester, userB = receiver
    }

    public void accept(){
        // TODO: status = "accepted"
    }

    public void decline(){
        // TODO: status = "declined" (or just delete the row? report doesnt say)
    }

    public boolean areFriends(){
        // TODO: return status.equals("accepted") once status is actually a real thing
        return false;
    }

}
