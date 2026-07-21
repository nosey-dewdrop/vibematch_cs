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
        String title = req.getString("title");
        String body = req.getString("body");
        if (title == null || title.trim().isEmpty()) {
            return Response.fail(req.id, "Post needs a title.");
        }
        if (body == null || body.trim().isEmpty()) {
            return Response.fail(req.id, "Post can't be empty.");
        }
        Post p = new Post(communityId, client.getUsername(),
                title.trim(), body.trim());
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
        String body = req.getString("body");
        if (body == null || body.trim().isEmpty()) {
            return Response.fail(req.id, "Comment can't be empty.");
        }
        Comment c = new Comment(postId, author,
                body.trim(), req.getInt("parentId"));
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

    public Response deletePost(Request req, ClientHandler client) {
        int postId = req.getInt("postId");
        Post post = postDao.findPost(postId);
        if (post == null) {
            return Response.fail(req.id, "Post not found.");
        }
        // only the author can delete their own post
        if (!client.getUsername().equals(post.getAuthor())) {
            return Response.fail(req.id, "You can only delete your own post.");
        }
        postDao.deletePost(postId);
        pushForumUpdate(post.getCommunityId());
        return Response.reply(req.id, "{\"ok\":true}");
    }

    public Response deleteComment(Request req, ClientHandler client) {
        int commentId = req.getInt("commentId");
        Comment c = postDao.findComment(commentId);
        if (c == null) {
            return Response.fail(req.id, "Comment not found.");
        }
        if (!client.getUsername().equals(c.getAuthor())) {
            return Response.fail(req.id, "You can only delete your own comment.");
        }
        postDao.deleteComment(commentId);
        Post post = postDao.findPost(c.getPostId());
        if (post != null) {
            pushForumUpdate(post.getCommunityId());
        }
        return Response.reply(req.id, "{\"ok\":true}");
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
