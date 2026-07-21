package server;

import com.google.gson.JsonObject;

import data.Db;
import protocol.Json;
import protocol.Request;
import protocol.Response;

/*
 * Looks at a request's action and sends it to the right handler. This is the
 * one place that maps action names to actual work, so its a good map of
 * everything the server can do.
 *
 * The handlers are grouped: auth, profile (interests + mbti), communities,
 * forum, messages.
 */
public class RequestRouter {

    private ChatServer server;
    private AuthHandler authHandler = new AuthHandler();
    private ProfileHandler profileHandler = new ProfileHandler();
    private CommunityHandler communityHandler = new CommunityHandler();
    private ForumHandler forumHandler;
    private MessageHandler messageHandler;
    private FriendHandler friendHandler;
    private NotificationHandler notificationHandler = new NotificationHandler();

    public RequestRouter(ChatServer server) {
        this.server = server;
        this.forumHandler = new ForumHandler(server);
        this.messageHandler = new MessageHandler(server);
        this.friendHandler = new FriendHandler(server);
    }

    public Response handle(Request request, ClientHandler client) {
        String action = request.action;
        if (action == null) {
            return Response.fail(request.id, "no action given");
        }

        // ping is a pure connection check, it never touches the db, so keep it
        // out of the lock so a heartbeat can't be blocked behind a slow query.
        if (action.equals("ping")) {
            JsonObject data = new JsonObject();
            data.addProperty("message", "pong");
            return Response.reply(request.id, Json.toJson(data));
        }

        // everything else may hit the single shared sqlite connection, which is
        // not thread safe. serialize per request so two client threads never
        // use the connection at the same moment.
        Db.LOCK.lock();
        try {
            return route(action, request, client);
        } finally {
            Db.LOCK.unlock();
        }
    }

    private Response route(String action, Request request, ClientHandler client) {
        // auth: the only actions allowed before this connection has logged in
        if (action.equals("register")) {
            return authHandler.register(request);
        }
        if (action.equals("verify")) {
            return authHandler.verify(request, client, server);
        }
        if (action.equals("resend")) {
            return authHandler.resend(request);
        }
        if (action.equals("login")) {
            return authHandler.login(request, client, server);
        }

        // everything past this point acts on behalf of a user, so this
        // connection must have logged in first. we never trust a username from
        // the request body, only who this socket actually is.
        if (client.getUsername() == null) {
            return Response.fail(request.id, "Please log in first.");
        }

        // profile / onboarding
        if (action.equals("setInterests")) {
            return profileHandler.setInterests(request, client);
        }
        if (action.equals("submitMbti")) {
            return profileHandler.submitMbti(request, client);
        }
        if (action.equals("getUser")) {
            return profileHandler.getUser(request);
        }

        // communities
        if (action.equals("communities.list")) {
            return communityHandler.list(request, client);
        }
        if (action.equals("communities.get")) {
            return communityHandler.get(request);
        }
        if (action.equals("communities.byCategory")) {
            return communityHandler.byCategory(request, client);
        }
        if (action.equals("communities.search")) {
            return communityHandler.search(request, client);
        }
        if (action.equals("communities.joined")) {
            return communityHandler.joined(request, client);
        }
        if (action.equals("communities.isMember")) {
            return communityHandler.isMember(request, client);
        }
        if (action.equals("communities.join")) {
            return communityHandler.join(request, client);
        }
        if (action.equals("communities.leave")) {
            return communityHandler.leave(request, client);
        }
        if (action.equals("communities.homeMatches")) {
            return communityHandler.homeMatches(request, client);
        }
        if (action.equals("communities.scoreOne")) {
            return communityHandler.scoreOne(request, client);
        }

        // forum
        if (action.equals("forum.posts")) {
            return forumHandler.posts(request);
        }
        if (action.equals("forum.createPost")) {
            return forumHandler.createPost(request, client);
        }
        if (action.equals("forum.comments")) {
            return forumHandler.comments(request);
        }
        if (action.equals("forum.addComment")) {
            return forumHandler.addComment(request, client);
        }
        if (action.equals("forum.deletePost")) {
            return forumHandler.deletePost(request, client);
        }
        if (action.equals("forum.deleteComment")) {
            return forumHandler.deleteComment(request, client);
        }

        // friends
        if (action.equals("friends.request")) {
            return friendHandler.sendRequest(request, client);
        }
        if (action.equals("friends.respond")) {
            return friendHandler.respond(request, client);
        }
        if (action.equals("friends.list")) {
            return friendHandler.friends(request, client);
        }
        if (action.equals("friends.requests")) {
            return friendHandler.requests(request, client);
        }
        if (action.equals("friends.status")) {
            return friendHandler.status(request, client);
        }

        // notifications
        if (action.equals("notifications.list")) {
            return notificationHandler.list(request, client);
        }
        if (action.equals("notifications.unread")) {
            return notificationHandler.unread(request, client);
        }
        if (action.equals("notifications.markRead")) {
            return notificationHandler.markRead(request, client);
        }

        // messages
        if (action.equals("messages.partners")) {
            return messageHandler.partners(request, client);
        }
        if (action.equals("messages.conversation")) {
            return messageHandler.conversation(request, client);
        }
        if (action.equals("messages.send")) {
            return messageHandler.send(request, client);
        }
        if (action.equals("messages.findUser")) {
            return messageHandler.findUser(request);
        }

        return Response.fail(request.id, "unknown action: " + action);
    }
}
