package data;

import model.Message;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;

/*
 * Direct messages between two users. Pretty simple, we store every message and
 * read them back per conversation.
 */
public class MessageDao {

    public void send(Message m) {
        String sql = "INSERT INTO messages (sender, receiver, body, created_at, is_read) VALUES (?, ?, ?, ?, 0)";
        try {
            Connection c = Db.getConnection();
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setString(1, m.getSender());
            ps.setString(2, m.getReceiver());
            ps.setString(3, m.getBody());
            ps.setString(4, LocalDateTime.now().toString());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException("could not send message", e);
        }
    }

    // all messages between two people, oldest first
    public ArrayList<Message> getConversation(String a, String b) {
        ArrayList<Message> list = new ArrayList<Message>();
        String sql = "SELECT * FROM messages WHERE (sender = ? AND receiver = ?) OR (sender = ? AND receiver = ?) "
                   + "ORDER BY created_at ASC";
        try {
            Connection c = Db.getConnection();
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setString(1, a);
            ps.setString(2, b);
            ps.setString(3, b);
            ps.setString(4, a);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(readRow(rs));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException("could not load conversation", e);
        }
        return list;
    }

    // everyone this user has a conversation with
    public ArrayList<String> getPartners(String username) {
        ArrayList<String> partners = new ArrayList<String>();
        // the CASE picks the "other" person in each message
        String sql = "SELECT DISTINCT CASE WHEN sender = ? THEN receiver ELSE sender END AS partner "
                   + "FROM messages WHERE sender = ? OR receiver = ?";
        try {
            Connection c = Db.getConnection();
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, username);
            ps.setString(3, username);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                partners.add(rs.getString("partner"));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException("could not load conversations", e);
        }
        return partners;
    }

    // how many messages this user has received but not yet read
    public int unreadCount(String username) {
        String sql = "SELECT COUNT(*) FROM messages WHERE receiver = ? AND is_read = 0";
        try {
            Connection c = Db.getConnection();
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            int n = 0;
            if (rs.next()) {
                n = rs.getInt(1);
            }
            rs.close();
            ps.close();
            return n;
        } catch (SQLException e) {
            throw new RuntimeException("could not count unread messages", e);
        }
    }

    // mark every message from `other` to `me` as read (called when the user
    // opens that conversation).
    public void markRead(String me, String other) {
        String sql = "UPDATE messages SET is_read = 1 WHERE receiver = ? AND sender = ?";
        try {
            Connection c = Db.getConnection();
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setString(1, me);
            ps.setString(2, other);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException("could not mark messages read", e);
        }
    }

    private Message readRow(ResultSet rs) throws SQLException {
        Message m = new Message();
        m.setId(rs.getInt("id"));
        m.setSender(rs.getString("sender"));
        m.setReceiver(rs.getString("receiver"));
        m.setBody(rs.getString("body"));
        m.setCreatedAt(rs.getString("created_at"));
        m.setRead(rs.getInt("is_read") == 1);
        return m;
    }
}
