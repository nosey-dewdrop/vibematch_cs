package data;

import model.Notification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;

/*
 * Stores the notifications that show up on the bell. Nothing fancy: insert one
 * when something happens, read them back newest first, count the unread ones,
 * and mark them read when the user opens the list.
 */
public class NotificationDao {

    public void insert(String username, String type, String text) {
        String sql = "INSERT INTO notifications (username, type, text, created_at, is_read) VALUES (?, ?, ?, ?, 0)";
        try {
            Connection c = Db.getConnection();
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, type);
            ps.setString(3, text);
            ps.setString(4, LocalDateTime.now().toString());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException("could not save notification", e);
        }
    }

    public ArrayList<Notification> forUser(String username) {
        ArrayList<Notification> list = new ArrayList<Notification>();
        try {
            Connection c = Db.getConnection();
            PreparedStatement ps = c.prepareStatement(
                "SELECT * FROM notifications WHERE username = ? ORDER BY created_at DESC LIMIT 30");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Notification n = new Notification();
                n.setId(rs.getInt("id"));
                n.setUsername(rs.getString("username"));
                n.setType(rs.getString("type"));
                n.setText(rs.getString("text"));
                n.setCreatedAt(rs.getString("created_at"));
                n.setRead(rs.getInt("is_read") == 1);
                list.add(n);
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException("could not load notifications", e);
        }
        return list;
    }

    public int unreadCount(String username) {
        try {
            Connection c = Db.getConnection();
            PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT(*) FROM notifications WHERE username = ? AND is_read = 0");
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
            throw new RuntimeException("could not count notifications", e);
        }
    }

    public void markAllRead(String username) {
        try {
            Connection c = Db.getConnection();
            PreparedStatement ps = c.prepareStatement(
                "UPDATE notifications SET is_read = 1 WHERE username = ?");
            ps.setString(1, username);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException("could not mark notifications read", e);
        }
    }
}
