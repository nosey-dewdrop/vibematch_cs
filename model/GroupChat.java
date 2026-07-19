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
 */
public class GroupChat {

    String chatId;
    String communityId;

    List<Message> messages = new ArrayList<>();

    public void postMessage(Message message){
        // TODO: this needs to run message through ModerationFilter.checkMessage() FIRST,
        // per the sequence diagram in section 6.2 of the report. something like:
        //   ModerationFilter filter = new ModerationFilter();
        //   if (!filter.checkMessage(message)) { filter.blockMessage(message); return; }
        //   messages.add(message);
        //   ... then broadcast it over the socket layer (see 2.2 tools table, java.net sockets)
    }

    public List<Message> getMessageHistory(){
        return messages;
    }

}
