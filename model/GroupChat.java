package model;

import java.util.List;
import java.util.ArrayList;

/*
 * MODEL -- GroupChat
 * owner: Damla Su Bilge   (Table 5)
 *
 * "The chat that belongs to one community." (4.3)
 * UML: composed by exactly 1 Community (filled diamond), composes many Messages
 * (0..* -- a GroupChat's Messages cant exist without it either).
 *
 * status: SCAFFOLD ONLY.
 * per section 3.3 of the report: when a student sends a chat message,
 * ChatController calls GroupChat.postMessage(), and BEFORE the message is stored
 * or broadcast, GroupChat has to pass it to ModerationFilter first. so postMessage()
 * is not just "add to the list" -- the moderation check has to happen inside it.
 *
 * status: first pass. postMessage runs the check + stores. broadcast over the
 * socket layer isnt here yet -- thats the net/ side (2.2 tools table), not mine.
 */
public class GroupChat {

    String chatId;
    String communityId;

    List<Message> messages = new ArrayList<>();

    public GroupChat(String chatId, String communityId){
        this.chatId = chatId;
        this.communityId = communityId;
    }

    // section 6.2 sequence: the message gets moderated BEFORE its stored. i let
    // Message.send() run the actual check (Table 3 says the message checks itself),
    // and only add it to the history if send() came back true. a blocked message
    // never makes it into the list, so getMessageHistory() stays clean by design.
    // returns whether it went through, so the controller can tell the sender
    public boolean postMessage(Message message){
        boolean allowed = message.send();
        if (allowed){
            messages.add(message);
            return true;
        }
        return false;
    }

    public List<Message> getMessageHistory(){
        return messages;
    }

}
