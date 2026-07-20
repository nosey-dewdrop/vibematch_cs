package server;

import com.google.gson.JsonObject;

import data.CommunityDao;
import data.PostDao;
import model.Comment;
import model.Post;
import protocol.Json;
import protocol.Request;
import protocol.Response;

import java.util.ArrayList;

/*
 * The reddit style forum: posts inside a community and their threaded comments.
 *
 * This is one of the two places real time matters. When someone posts or
 * comments, we push a little "forumUpdate" to everyone who is a member of that
 * community and online, so their screen can refresh itself instead of them
 * having to reload.
 */
public class ForumHandler {

    private PostDao postDao = new PostDao();
    private CommunityDao communityDao = new CommunityDao();
    private ChatServer server;

    public ForumHandler(ChatServer server) {
        this.server = server;
    }

    public Response posts(Request req) {
        ArrayList<Post> list = postDao.getPostsForCommunity(req.getInt("communityId"));
        Post[] array = new Post[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return Response.reply(req.id, Json.toJson(array));
    }

    public Response createPost(Request req, ClientHandler client) {
        int communityId = req.getInt("communityId");
        Post p = new Post(communityId, client.getUsername(),
                req.getString("title"), req.getString("body"));
        int newId = postDao.insertPost(p);
        p.setId(newId);

        // let the community know something new is up
        pushForumUpdate(communityId);

        return Response.reply(req.id, Json.toJson(p));
    }

    public Response comments(Request req) {
        ArrayList<Comment> list = postDao.getComments(req.getInt("postId"));
        Comment[] array = new Comment[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return Response.reply(req.id, Json.toJson(array));
    }

    public Response addComment(Request req, ClientHandler client) {
        int postId = req.getInt("postId");
        String author = client.getUsername();
        Comment c = new Comment(postId, author,
                req.getString("body"), req.getInt("parentId"));
        int newId = postDao.insertComment(c);
        c.setId(newId);

        // figure out which community this post belongs to so we can notify it
        Post post = postDao.findPost(postId);
        if (post != null) {
            pushForumUpdate(post.getCommunityId());
            // tell the post's author someone replied (unless they did it themself)
            if (post.getAuthor() != null && !post.getAuthor().equals(author)) {
                Notifier.notify(server, post.getAuthor(),
                        "reply", author + " commented on your post \"" + post.getTitle() + "\"");
            }
        }
        return Response.reply(req.id, Json.toJson(c));
    }

    // push a forumUpdate to every member of the community
    private void pushForumUpdate(int communityId) {
        JsonObject data = new JsonObject();
        data.addProperty("communityId", communityId);
        Response push = Response.push("forumUpdate", Json.toJson(data));

        ArrayList<String> members = communityDao.getMemberUsernames(communityId);
        server.pushToMany(members, push);
    }
}
