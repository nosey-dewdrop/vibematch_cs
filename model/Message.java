package model;

/*
 * MODEL -- Message
 * owner: Damla Su Bilge   (Table 5)
 *
 * "One chat message; checks itself against the moderation filter before it
 * can appear." (4.3) -- this is the class that actually calls send(), which
 * is a little different from GroupChat.postMessage() calling it FOR the message,
 * but the report describes it both ways in different sections (3.3 says GroupChat
 * does the moderation check, here it says the Message "checks itself"). going with
 * "Message.send() does its own moderation check" since thats what Table 3 says.
 *
 * UML: contained by GroupChat (0..* under a filled diamond), "checked by" ModerationFilter
 * (dashed arrow -- dependency, Message doesnt own the filter).
 *
 * NOTE: same deal as model.Community -- theres also a view.Message class used by the
 * click-through prototype's chat screen, its a different, simpler class in a different
 * package on purpose. this one is the real Model class.
 *
 * status: SCAFFOLD ONLY.
 */
public class Message {

    String messageId;
    String senderId;
    String content;
    String timestamp; // report says "DateTime", using String for now to keep this dependency-free
    boolean isBlocked;

    public void send(){
        // TODO: create a ModerationFilter, run checkMessage(this) on it, set isBlocked
        // accordingly, and only then let GroupChat store/broadcast this message
    }

}
