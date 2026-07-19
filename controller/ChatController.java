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
 * status: SCAFFOLD ONLY, NOT wired into the view/ package. CommunityChatsPanel.java
 * in view/ appends straight to a view.Message list with no moderation at all right
 * now (theres literally a TODO comment there saying so) -- this is what should
 * replace it eventually.
 */
public class ChatController {

    public void sendMessage(GroupChat chat, Message message){
        // TODO: chat.postMessage(message); -- moderation happens inside postMessage(), see GroupChat.java
    }

}
