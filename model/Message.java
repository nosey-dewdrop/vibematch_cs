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
 * status: first pass. fields + send() done, moderation wired in.
 */
public class Message {

    String messageId;
    String senderId;
    String content;
    String timestamp; // report says "DateTime", using String for now to keep this dependency-free
    boolean isBlocked;

    // gson needs a no-arg constructor to rebuild a Message off the wire, and the
    // sender side needs a real one, so theres two. isBlocked starts false -- a
    // message is innocent until the filter says otherwise
    public Message(){
    }

    public Message(String messageId, String senderId, String content, String timestamp){
        this.messageId = messageId;
        this.senderId = senderId;
        this.content = content;
        this.timestamp = timestamp;
        this.isBlocked = false;
    }

    // Table 3 says the Message "checks itself against the moderation filter before
    // it can appear", so send() does its own check instead of waiting for GroupChat
    // to do it. builds a filter, runs this message through it, and only leaves
    // isBlocked false if it came back clean. returns whether it may appear so the
    // caller (GroupChat.postMessage) knows if it can store/broadcast it
    public boolean send(){
        ModerationFilter filter = new ModerationFilter();
        boolean clean = filter.checkMessage(this);
        if (clean){
            this.isBlocked = false;
            return true;
        }
        filter.blockMessage(this);
        return false;
    }

    public String getContent(){
        return content;
    }

    public String getSenderId(){
        return senderId;
    }

    public boolean isBlocked(){
        return isBlocked;
    }

}
