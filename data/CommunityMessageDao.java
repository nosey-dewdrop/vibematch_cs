package data;

import model.CommunityMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;

/*
 * The community guestbook: messages every member of a community can post and
 * read. Stored simply, read back in order per community.
 */
public class CommunityMessageDao {

    public void post(CommunityMessage m) {
        String sql = "INSERT INTO community_messages (community_id, sender, body, created_at) VALUES (?, ?, ?, ?)";
        try {
            Connection c = Db.getConnection();
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, m.getCommunityId());
            ps.setString(2, m.getSender());
            ps.setString(3, m.getBody());
            ps.setString(4, LocalDateTime.now().toString());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException("could not post community message", e);
        }
    }

    // all messages in a community, oldest first
    public ArrayList<CommunityMessage> list(int communityId) {
        ArrayList<CommunityMessage> out = new ArrayList<CommunityMessage>();
        String sql = "SELECT * FROM community_messages WHERE community_id = ? ORDER BY created_at ASC";
        try {
            Connection c = Db.getConnection();
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, communityId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                CommunityMessage m = new CommunityMessage();
                m.setId(rs.getInt("id"));
                m.setCommunityId(rs.getInt("community_id"));
                m.setSender(rs.getString("sender"));
                m.setBody(rs.getString("body"));
                m.setCreatedAt(rs.getString("created_at"));
                out.add(m);
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException("could not load community messages", e);
        }
        return out;
    }
}
