package net;

import java.util.ArrayList;

import com.google.gson.JsonObject;

import model.Comment;
import model.Community;
import model.MbtiResult;
import model.Message;
import model.Post;
import model.User;
import protocol.Json;
import protocol.Params;

/*
 * The friendly face the screens use. Instead of a screen building json and
 * calling the socket itself, it calls a method here like api.login(...) and
 * gets back a real object. Under the hood every method turns into a request to
 * the server and parses the reply.
 *
 * If the server says no, ServerClient throws IllegalArgumentException with the
 * message, which the screens already know how to show.
 */
public class Api {

    private static Api instance = new Api();

    public static Api get() {
        return instance;
    }

    private ServerClient client = ServerClient.getInstance();

    // ---- small result holders ----

    public static class RegisterResult {
        public String username;
        public String email;
        public boolean emailed;
        public String code;   // only set when email wasn't sent
    }

    public static class ResendResult {
        public boolean emailed;
        public String code;
    }

    // ---- auth ----

    public RegisterResult register(String name, String username, String email, String password) {
        String data = client.send("register", Params.of()
                .put("displayName", name).put("username", username)
                .put("email", email).put("password", password).json());
        return Json.fromJson(data, RegisterResult.class);
    }

    public User verify(String username, String code) {
        String data = client.send("verify", Params.of()
                .put("username", username).put("code", code).json());
        return Json.fromJson(data, User.class);
    }

    public ResendResult resend(String username, String email) {
        String data = client.send("resend", Params.of()
                .put("username", username).put("email", email).json());
        return Json.fromJson(data, ResendResult.class);
    }

    public User login(String usernameOrEmail, String password) {
        String data = client.send("login", Params.of()
                .put("usernameOrEmail", usernameOrEmail).put("password", password).json());
        return Json.fromJson(data, User.class);
    }

    // ---- profile / onboarding ----

    public void setInterests(String username, ArrayList<String> interests) {
        client.send("setInterests", Params.of()
                .put("username", username).putList("interests", interests).json());
    }

    public MbtiResult submitMbti(String username, int[] answers) {
        String data = client.send("submitMbti", Params.of()
                .put("username", username).putInts("answers", answers).json());
        return Json.fromJson(data, MbtiResult.class);
    }

    public User getUser(String username) {
        String data = client.send("getUser", Params.of().put("username", username).json());
        return Json.fromJson(data, User.class);
    }

    // ---- spotify ----

    private SpotifyAuth spotify = new SpotifyAuth();

    public boolean spotifyAvailable() {
        return SpotifyAuth.isConfigured();
    }

    /*
     * Runs the spotify oauth flow on THIS machine (opens the browser, catches
     * the redirect, reads the user's taste), then sends the result to the server
     * to store and fold the genres into their interests. Returns the profile.
     * Throws IllegalArgumentException with a friendly message on any failure.
     */
    public model.SpotifyProfile connectSpotify(String username) {
        model.SpotifyProfile p = spotify.connect(username); // browser + spotify api
        // hand the taste to the server to persist + merge into interests
        String data = client.send("spotify.save", Params.of()
                .put("username", username)
                .put("displayName", p.getDisplayName() == null ? "" : p.getDisplayName())
                .putList("artists", p.getTopArtists())
                .putList("genres", p.getTopGenres())
                .json());
        return Json.fromJson(data, model.SpotifyProfile.class);
    }

    public model.SpotifyProfile getSpotify(String username) {
        String data = client.send("spotify.get", Params.of().put("username", username).json());
        return Json.fromJson(data, model.SpotifyProfile.class);
    }

    public void disconnectSpotify(String username) {
        client.send("spotify.disconnect", Params.of().put("username", username).json());
    }

    // ---- communities ----

    // username lets the server fill in the match percent for each community
    public ArrayList<Community> listCommunities(String username) {
        return toList(client.send("communities.list", Params.of().put("username", username).json()));
    }

    public Community getCommunity(int id) {
        String data = client.send("communities.get", Params.of().put("id", id).json());
        return Json.fromJson(data, Community.class);
    }

    public ArrayList<Community> byCategory(String username, String category) {
        return toList(client.send("communities.byCategory", Params.of()
                .put("username", username).put("category", category).json()));
    }

    public ArrayList<Community> search(String username, String text) {
        return toList(client.send("communities.search", Params.of()
                .put("username", username).put("text", text).json()));
    }

    public ArrayList<Community> joined(String username) {
        return toList(client.send("communities.joined", Params.of().put("username", username).json()));
    }

    public boolean isMember(String username, int communityId) {
        String data = client.send("communities.isMember", Params.of()
                .put("username", username).put("communityId", communityId).json());
        JsonObject obj = Json.parse(data);
        return obj.get("member").getAsBoolean();
    }

    public void join(String username, int communityId) {
        client.send("communities.join", Params.of()
                .put("username", username).put("communityId", communityId).json());
    }

    public void leave(String username, int communityId) {
        client.send("communities.leave", Params.of()
                .put("username", username).put("communityId", communityId).json());
    }

    public ArrayList<Community> homeMatches(String username) {
        return toList(client.send("communities.homeMatches", Params.of().put("username", username).json()));
    }

    public Community scoreOne(String username, int communityId) {
        String data = client.send("communities.scoreOne", Params.of()
                .put("username", username).put("communityId", communityId).json());
        return Json.fromJson(data, Community.class);
    }

    // ---- forum ----

    public ArrayList<Post> posts(int communityId) {
        String data = client.send("forum.posts", Params.of().put("communityId", communityId).json());
        Post[] arr = Json.fromJson(data, Post[].class);
        return arrToList(arr);
    }

    public Post createPost(int communityId, String author, String title, String body) {
        String data = client.send("forum.createPost", Params.of()
                .put("communityId", communityId).put("author", author)
                .put("title", title).put("body", body).json());
        return Json.fromJson(data, Post.class);
    }

    public ArrayList<Comment> comments(int postId) {
        String data = client.send("forum.comments", Params.of().put("postId", postId).json());
        Comment[] arr = Json.fromJson(data, Comment[].class);
        ArrayList<Comment> list = new ArrayList<Comment>();
        for (int i = 0; i < arr.length; i++) {
            list.add(arr[i]);
        }
        return list;
    }

    public void addComment(int postId, String author, String body, int parentId) {
        client.send("forum.addComment", Params.of()
                .put("postId", postId).put("author", author)
                .put("body", body).put("parentId", parentId).json());
    }

    public void deletePost(int postId) {
        client.send("forum.deletePost", Params.of().put("postId", postId).json());
    }

    public void deleteComment(int commentId) {
        client.send("forum.deleteComment", Params.of().put("commentId", commentId).json());
    }

    // ---- notifications ----

    public ArrayList<model.Notification> notifications(String username) {
        String data = client.send("notifications.list", Params.of().put("username", username).json());
        model.Notification[] arr = Json.fromJson(data, model.Notification[].class);
        ArrayList<model.Notification> list = new ArrayList<model.Notification>();
        for (int i = 0; i < arr.length; i++) {
            list.add(arr[i]);
        }
        return list;
    }

    public int unreadCount(String username) {
        String data = client.send("notifications.unread", Params.of().put("username", username).json());
        return Json.parse(data).get("count").getAsInt();
    }

    public void markNotificationsRead(String username) {
        client.send("notifications.markRead", Params.of().put("username", username).json());
    }

    // ---- friends ----

    // send a friend request. throws with the reason if it can't be sent.
    public void sendFriendRequest(String from, String to) {
        client.send("friends.request", Params.of().put("from", from).put("to", to).json());
    }

    public void respondToRequest(String me, String requester, boolean accept) {
        client.send("friends.respond", Params.of()
                .put("me", me).put("requester", requester).put("accept", accept ? 1 : 0).json());
    }

    public ArrayList<String> friends(String username) {
        String data = client.send("friends.list", Params.of().put("username", username).json());
        return stringList(data);
    }

    public ArrayList<String> friendRequests(String username) {
        String data = client.send("friends.requests", Params.of().put("username", username).json());
        return stringList(data);
    }

    public String friendStatus(String me, String other) {
        String data = client.send("friends.status", Params.of().put("me", me).put("other", other).json());
        return Json.parse(data).get("relation").getAsString();
    }

    // ---- messages ----

    public ArrayList<String> partners(String username) {
        String data = client.send("messages.partners", Params.of().put("username", username).json());
        String[] arr = Json.fromJson(data, String[].class);
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < arr.length; i++) {
            list.add(arr[i]);
        }
        return list;
    }

    public ArrayList<Message> conversation(String me, String other) {
        String data = client.send("messages.conversation", Params.of()
                .put("me", me).put("other", other).json());
        Message[] arr = Json.fromJson(data, Message[].class);
        ArrayList<Message> list = new ArrayList<Message>();
        for (int i = 0; i < arr.length; i++) {
            list.add(arr[i]);
        }
        return list;
    }

    public void sendMessage(String sender, String receiver, String body) {
        client.send("messages.send", Params.of()
                .put("sender", sender).put("receiver", receiver).put("body", body).json());
    }

    public User findUser(String username) {
        String data = client.send("messages.findUser", Params.of().put("username", username).json());
        return Json.fromJson(data, User.class);
    }

    // ---- helpers ----

    private ArrayList<String> stringList(String data) {
        String[] arr = Json.fromJson(data, String[].class);
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < arr.length; i++) {
            list.add(arr[i]);
        }
        return list;
    }

    private ArrayList<Community> toList(String data) {
        Community[] arr = Json.fromJson(data, Community[].class);
        return arrToList(arr);
    }

    private ArrayList<Post> arrToList(Post[] arr) {
        ArrayList<Post> list = new ArrayList<Post>();
        for (int i = 0; i < arr.length; i++) {
            list.add(arr[i]);
        }
        return list;
    }

    private ArrayList<Community> arrToList(Community[] arr) {
        ArrayList<Community> list = new ArrayList<Community>();
        for (int i = 0; i < arr.length; i++) {
            list.add(arr[i]);
        }
        return list;
    }
}
