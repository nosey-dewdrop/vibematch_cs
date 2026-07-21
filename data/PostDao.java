package data;

import model.Comment;
import model.Post;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;

/*
 * Handles the forum: posts inside a community and the comments on those posts.
 * Comments can be replies to other comments (parent_id), thats how the threads
 * work.
 */
public class PostDao {

    // ---- posts ----

    public int insertPost(Post p) {
        String sql = "INSERT INTO posts (community_id, author, title, body, created_at) VALUES (?, ?, ?, ?, ?)";
        try {
            Connection c = Db.getConnection();
            PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, p.getCommunityId());
            ps.setString(2, p.getAuthor());
            ps.setString(3, p.getTitle());
            ps.setString(4, p.getBody());
            ps.setString(5, LocalDateTime.now().toString());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            int id = -1;
            if (keys.next()) {
                id = keys.getInt(1);
            }
            keys.close();
            ps.close();
            return id;
        } catch (SQLException e) {
            throw new RuntimeException("could not insert post", e);
        }
    }

    public ArrayList<Post> getPostsForCommunity(int communityId) {
        ArrayList<Post> posts = new ArrayList<Post>();
        try {
            Connection c = Db.getConnection();
            PreparedStatement ps = c.prepareStatement(
                "SELECT * FROM posts WHERE community_id = ? ORDER BY created_at DESC");
            ps.setInt(1, communityId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                posts.add(readPost(rs));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException("could not load posts", e);
        }
        // count comments after, so we dont nest result sets
        for (int i = 0; i < posts.size(); i++) {
            posts.get(i).setCommentCount(commentCount(posts.get(i).getId()));
        }
        return posts;
    }

    public Post findPost(int id) {
        try {
            Connection c = Db.getConnection();
            PreparedStatement ps = c.prepareStatement("SELECT * FROM posts WHERE id = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            Post p = null;
            if (rs.next()) {
                p = readPost(rs);
            }
            rs.close();
            ps.close();
            if (p != null) {
                p.setCommentCount(commentCount(p.getId()));
            }
            return p;
        } catch (SQLException e) {
            throw new RuntimeException("could not load post", e);
        }
    }

    // ---- comments ----

    public int insertComment(Comment cm) {
        String sql = "INSERT INTO comments (post_id, author, body, parent_id, created_at) VALUES (?, ?, ?, ?, ?)";
        try {
            Connection c = Db.getConnection();
            PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, cm.getPostId());
            ps.setString(2, cm.getAuthor());
            ps.setString(3, cm.getBody());
            ps.setInt(4, cm.getParentId());
            ps.setString(5, LocalDateTime.now().toString());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            int id = -1;
            if (keys.next()) {
                id = keys.getInt(1);
            }
            keys.close();
            ps.close();
            return id;
        } catch (SQLException e) {
            throw new RuntimeException("could not insert comment", e);
        }
    }

    // all comments for a post, oldest first (we build the threads in the screen)
    public ArrayList<Comment> getComments(int postId) {
        ArrayList<Comment> list = new ArrayList<Comment>();
        try {
            Connection c = Db.getConnection();
            PreparedStatement ps = c.prepareStatement(
                "SELECT * FROM comments WHERE post_id = ? ORDER BY created_at ASC");
            ps.setInt(1, postId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Comment cm = new Comment();
                cm.setId(rs.getInt("id"));
                cm.setPostId(rs.getInt("post_id"));
                cm.setAuthor(rs.getString("author"));
                cm.setBody(rs.getString("body"));
                cm.setParentId(rs.getInt("parent_id"));
                cm.setCreatedAt(rs.getString("created_at"));
                list.add(cm);
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException("could not load comments", e);
        }
        return list;
    }

    public int commentCount(int postId) {
        try {
            Connection c = Db.getConnection();
            PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM comments WHERE post_id = ?");
            ps.setInt(1, postId);
            ResultSet rs = ps.executeQuery();
            int n = 0;
            if (rs.next()) {
                n = rs.getInt(1);
            }
            rs.close();
            ps.close();
            return n;
        } catch (SQLException e) {
            throw new RuntimeException("could not count comments", e);
        }
    }

    // find a single comment (used to check who owns it before deleting)
    public Comment findComment(int id) {
        try {
            Connection c = Db.getConnection();
            PreparedStatement ps = c.prepareStatement("SELECT * FROM comments WHERE id = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            Comment cm = null;
            if (rs.next()) {
                cm = new Comment();
                cm.setId(rs.getInt("id"));
                cm.setPostId(rs.getInt("post_id"));
                cm.setAuthor(rs.getString("author"));
                cm.setBody(rs.getString("body"));
                cm.setParentId(rs.getInt("parent_id"));
                cm.setCreatedAt(rs.getString("created_at"));
            }
            rs.close();
            ps.close();
            return cm;
        } catch (SQLException e) {
            throw new RuntimeException("could not load comment", e);
        }
    }

    // delete a post and all of its comments. there is no FK cascade so the
    // comments have to go first.
    public void deletePost(int postId) {
        try {
            Connection c = Db.getConnection();
            PreparedStatement wipe = c.prepareStatement("DELETE FROM comments WHERE post_id = ?");
            wipe.setInt(1, postId);
            wipe.executeUpdate();
            wipe.close();
            PreparedStatement ps = c.prepareStatement("DELETE FROM posts WHERE id = ?");
            ps.setInt(1, postId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException("could not delete post", e);
        }
    }

    // delete a comment and its WHOLE reply subtree (replies, replies-to-replies,
    // ...), so nothing is left orphaned and the "N comments" count stays right.
    public void deleteComment(int commentId) {
        try {
            Connection c = Db.getConnection();
            // walk the tree breadth-first collecting every descendant id
            ArrayList<Integer> toDelete = new ArrayList<Integer>();
            ArrayList<Integer> frontier = new ArrayList<Integer>();
            toDelete.add(commentId);
            frontier.add(commentId);
            while (!frontier.isEmpty()) {
                ArrayList<Integer> next = new ArrayList<Integer>();
                for (int i = 0; i < frontier.size(); i++) {
                    PreparedStatement q = c.prepareStatement(
                        "SELECT id FROM comments WHERE parent_id = ?");
                    q.setInt(1, frontier.get(i));
                    ResultSet rs = q.executeQuery();
                    while (rs.next()) {
                        int child = rs.getInt("id");
                        toDelete.add(child);
                        next.add(child);
                    }
                    rs.close();
                    q.close();
                }
                frontier = next;
            }
            PreparedStatement del = c.prepareStatement("DELETE FROM comments WHERE id = ?");
            for (int i = 0; i < toDelete.size(); i++) {
                del.setInt(1, toDelete.get(i));
                del.executeUpdate();
            }
            del.close();
        } catch (SQLException e) {
            throw new RuntimeException("could not delete comment", e);
        }
    }

    private Post readPost(ResultSet rs) throws SQLException {
        Post p = new Post();
        p.setId(rs.getInt("id"));
        p.setCommunityId(rs.getInt("community_id"));
        p.setAuthor(rs.getString("author"));
        p.setTitle(rs.getString("title"));
        p.setBody(rs.getString("body"));
        p.setCreatedAt(rs.getString("created_at"));
        return p;
    }
}
