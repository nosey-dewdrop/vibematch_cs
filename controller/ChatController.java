package controller;

import model.GroupChat;
import model.Message;

/*
 * CONTROLLER -- ChatController
 * handles: group chat. per Table 5, Damla's classes (GroupChat, Message, ModerationFilter).
 *
 * this is the one the report actually walks through as an example (section 3.3):
 * "When a student sends a chat message, ChatController calls GroupChat.postMessage().
 * Before the message is stored or broadcast to other users, GroupChat passes it to
 * ModerationFilter to ensure it meets the platform's moderation rules."
 * so sendMessage() below should basically just be a thin pass-through to that.
 *
 * status: first pass, logic done. still NOT wired into the view/ package --
 * CommunityChatsPanel.java in view/ appends straight to a view.Message list with no
 * moderation at all right now (theres literally a TODO comment there saying so) --
 * this is what should replace it eventually.
 */
public class ChatController {

    // thin pass-through, exactly like the report describes it in 3.3. the controller
    // doesnt do moderation itself -- it just hands the message to the chat and lets
    // GroupChat.postMessage() run the filter. returns whether it went through so the
    // view can show the sender a "message blocked" note instead of silently eating it
    public boolean sendMessage(GroupChat chat, Message message){
        return chat.postMessage(message);
    }

}
