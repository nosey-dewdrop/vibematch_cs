package server;

import data.FriendDao;
import data.MessageDao;
import data.UserDao;
import model.Message;
import model.User;
import protocol.Json;
import protocol.Request;
import protocol.Response;

import java.util.ArrayList;

/*
 * Direct messages, 1 on 1. The other real time spot: when someone sends a
 * message we save it and, if the person they sent it to is online, push it
 * straight to them so it pops up right away.
 */
public class MessageHandler {

    private MessageDao messageDao = new MessageDao();
    private UserDao userDao = new UserDao();
    private FriendDao friendDao = new FriendDao();
    private ChatServer server;

    public MessageHandler(ChatServer server) {
        this.server = server;
    }

    public Response partners(Request req, ClientHandler client) {
        ArrayList<String> partners = messageDao.getPartners(client.getUsername());
        String[] array = new String[partners.size()];
        for (int i = 0; i < partners.size(); i++) {
            array[i] = partners.get(i);
        }
        return Response.reply(req.id, Json.toJson(array));
    }

    public Response conversation(Request req, ClientHandler client) {
        String me = client.getUsername();
        String other = req.getString("other");
        ArrayList<Message> list = messageDao.getConversation(me, other);
        // opening a conversation marks its incoming messages as read
        messageDao.markRead(me, other);
        Message[] array = new Message[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return Response.reply(req.id, Json.toJson(array));
    }

    // count of unread incoming messages, for the Chats badge
    public Response unread(Request req, ClientHandler client) {
        int n = messageDao.unreadCount(client.getUsername());
        com.google.gson.JsonObject data = new com.google.gson.JsonObject();
        data.addProperty("count", n);
        return Response.reply(req.id, Json.toJson(data));
    }

    public Response send(Request req, ClientHandler client) {
        String sender = client.getUsername();
        String receiver = req.getString("receiver");
        String body = req.getString("body");

        // a message needs actual text. null/blank would either hit the NOT NULL
        // column and blow up, or send an empty bubble -- reject it cleanly.
        if (body == null || body.trim().isEmpty()) {
            return Response.fail(req.id, "Message can't be empty.");
        }
        body = body.trim();

        // privacy: you can only message people you are friends with
        if (!friendDao.areFriends(sender, receiver)) {
            return Response.fail(req.id, "You can only message your friends.");
        }

        Message m = new Message(sender, receiver, body);
        messageDao.send(m);

        // if the receiver is online, push it to them now
        Response push = Response.push("newMessage", Json.toJson(m));
        server.pushTo(receiver, push);

        // and drop a notification on their bell
        Notifier.notify(server, receiver, "message", sender + " sent you a message");

        return Response.reply(req.id, Json.toJson(m));
    }

    // used when starting a new chat, to check the username exists
    public Response findUser(Request req) {
        // match by username, email or display name so people can be added by
        // the name they show, not just their exact username.
        User u = userDao.findByAny(req.getString("username"));
        if (u == null) {
            return Response.fail(req.id, "No user found with that name.");
        }
        return Response.reply(req.id, Json.toJson(Dto.safeUser(u)));
    }
}
