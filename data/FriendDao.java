package data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;

/*
 * Friendships. A friendship starts as a request (status 'pending') from one
 * person to another, and becomes 'accepted' once the other says yes. We store
 * one row: user_a is the person who sent the request, user_b the one who got it.
 *
 * To check if two people are friends we look both directions, since it doesnt
 * matter who sent the request once its accepted.
 */
public class FriendDao {

    // send a friend request. does nothing if there is already a row either way.
    public void request(String from, String to) {
        if (relationExists(from, to)) {
            return;
        }
        String sql = "INSERT INTO friendships (user_a, user_b, status, created_at) VALUES (?, ?, 'pending', ?)";
        try {
            Connection c = Db.getConnection();
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setString(1, from);
            ps.setString(2, to);
            ps.setString(3, LocalDateTime.now().toString());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException("could not send friend request", e);
        }
    }

    // the target accepts a request that from -> me sent
    public void accept(String requester, String target) {
        setStatus(requester, target, "accepted");
    }

    // the target says no, we just remove the row
    public void decline(String requester, String target) {
        try {
            Connection c = Db.getConnection();
            PreparedStatement ps = c.prepareStatement(
                "DELETE FROM friendships WHERE user_a = ? AND user_b = ?");
            ps.setString(1, requester);
            ps.setString(2, target);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException("could not decline request", e);
        }
    }

    public boolean areFriends(String x, String y) {
        return statusBetween(x, y).equals("accepted");
    }

    /*
     * The relationship between two people from x's point of view:
     *   "none"     -> nothing yet
     *   "accepted" -> they are friends
     *   "outgoing" -> x sent y a request, waiting
     *   "incoming" -> y sent x a request, x can accept
     */
    public String relationFor(String me, String other) {
        String direct = rawStatus(me, other);   // me -> other
        String reverse = rawStatus(other, me);   // other -> me
        if (direct.equals("accepted") || reverse.equals("accepted")) {
            return "accepted";
        }
        if (direct.equals("pending")) {
            return "outgoing";
        }
        if (reverse.equals("pending")) {
            return "incoming";
        }
        return "none";
    }

    // everyone this user is actually friends with
    public ArrayList<String> getFriends(String username) {
        ArrayList<String> friends = new ArrayList<String>();
        String sql = "SELECT CASE WHEN user_a = ? THEN user_b ELSE user_a END AS friend "
                   + "FROM friendships WHERE status = 'accepted' AND (user_a = ? OR user_b = ?)";
        try {
            Connection c = Db.getConnection();
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, username);
            ps.setString(3, username);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                friends.add(rs.getString("friend"));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException("could not load friends", e);
        }
        return friends;
    }

    // requests waiting for this user to accept (people who sent TO them)
    public ArrayList<String> getIncomingRequests(String username) {
        ArrayList<String> list = new ArrayList<String>();
        try {
            Connection c = Db.getConnection();
            PreparedStatement ps = c.prepareStatement(
                "SELECT user_a FROM friendships WHERE user_b = ? AND status = 'pending'");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("user_a"));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException("could not load requests", e);
        }
        return list;
    }

    // ---- helpers ----

    private boolean relationExists(String x, String y) {
        return !rawStatus(x, y).equals("none") || !rawStatus(y, x).equals("none");
    }

    private String statusBetween(String x, String y) {
        String a = rawStatus(x, y);
        String b = rawStatus(y, x);
        if (a.equals("accepted") || b.equals("accepted")) {
            return "accepted";
        }
        if (a.equals("pending") || b.equals("pending")) {
            return "pending";
        }
        return "none";
    }

    // the stored status of the row from -> to, or "none" if there isnt one
    private String rawStatus(String from, String to) {
        try {
            Connection c = Db.getConnection();
            PreparedStatement ps = c.prepareStatement(
                "SELECT status FROM friendships WHERE user_a = ? AND user_b = ?");
            ps.setString(1, from);
            ps.setString(2, to);
            ResultSet rs = ps.executeQuery();
            String status = "none";
            if (rs.next()) {
                status = rs.getString("status");
            }
            rs.close();
            ps.close();
            return status;
        } catch (SQLException e) {
            throw new RuntimeException("could not read friendship", e);
        }
    }

    private void setStatus(String from, String to, String status) {
        try {
            Connection c = Db.getConnection();
            PreparedStatement ps = c.prepareStatement(
                "UPDATE friendships SET status = ? WHERE user_a = ? AND user_b = ?");
            ps.setString(1, status);
            ps.setString(2, from);
            ps.setString(3, to);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException("could not update friendship", e);
        }
    }
}
