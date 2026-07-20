package data;

import model.Community;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;

/*
 * Reads and writes communities, their tags and the membership table. The
 * memberCount on each Community is filled in here when we load it so the
 * screens dont have to count themselves.
 */
public class CommunityDao {

    // insert a community and return its new id
    public int insert(Community c) {
        String sql = "INSERT INTO communities (name, description, category, emoji, cover_color, created_by, created_at) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            Connection conn = Db.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, c.getName());
            ps.setString(2, c.getDescription());
            ps.setString(3, c.getCategory());
            ps.setString(4, c.getEmoji());
            ps.setString(5, c.getCoverColor());
            ps.setString(6, c.getCreatedBy());
            ps.setString(7, LocalDateTime.now().toString());
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
            throw new RuntimeException("could not insert community", e);
        }
    }

    public void addTag(int communityId, String tag) {
        String sql = "INSERT OR IGNORE INTO community_tags (community_id, tag) VALUES (?, ?)";
        try {
            Connection conn = Db.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, communityId);
            ps.setString(2, tag);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException("could not add tag", e);
        }
    }

    public ArrayList<String> getTags(int communityId) {
        ArrayList<String> tags = new ArrayList<String>();
        try {
            Connection conn = Db.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT tag FROM community_tags WHERE community_id = ?");
            ps.setInt(1, communityId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tags.add(rs.getString(1));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException("could not load tags", e);
        }
        return tags;
    }

    public Community findById(int id) {
        try {
            Connection conn = Db.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM communities WHERE id = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            Community c = null;
            if (rs.next()) {
                c = readRow(rs);
            }
            rs.close();
            ps.close();
            if (c != null) {
                fillDetails(c);
            }
            return c;
        } catch (SQLException e) {
            throw new RuntimeException("could not load community", e);
        }
    }

    public ArrayList<Community> getAll() {
        return query("SELECT * FROM communities ORDER BY name");
    }

    public ArrayList<Community> getByCategory(String category) {
        try {
            Connection conn = Db.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM communities WHERE category = ? ORDER BY name");
            ps.setString(1, category);
            return runAndRead(ps);
        } catch (SQLException e) {
            throw new RuntimeException("could not load by category", e);
        }
    }

    // simple search over name + description
    public ArrayList<Community> search(String text) {
        try {
            Connection conn = Db.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM communities WHERE name LIKE ? OR description LIKE ? ORDER BY name");
            String like = "%" + text + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            return runAndRead(ps);
        } catch (SQLException e) {
            throw new RuntimeException("could not search", e);
        }
    }

    // ---- membership ----

    public void join(String username, int communityId) {
        String sql = "INSERT OR IGNORE INTO memberships (username, community_id, joined_at) VALUES (?, ?, ?)";
        try {
            Connection conn = Db.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setInt(2, communityId);
            ps.setString(3, LocalDateTime.now().toString());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException("could not join", e);
        }
    }

    public void leave(String username, int communityId) {
        try {
            Connection conn = Db.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM memberships WHERE username = ? AND community_id = ?");
            ps.setString(1, username);
            ps.setInt(2, communityId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException("could not leave", e);
        }
    }

    public boolean isMember(String username, int communityId) {
        try {
            Connection conn = Db.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM memberships WHERE username = ? AND community_id = ?");
            ps.setString(1, username);
            ps.setInt(2, communityId);
            ResultSet rs = ps.executeQuery();
            boolean member = rs.next();
            rs.close();
            ps.close();
            return member;
        } catch (SQLException e) {
            throw new RuntimeException("could not check membership", e);
        }
    }

    public ArrayList<Community> getJoined(String username) {
        try {
            Connection conn = Db.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT c.* FROM communities c "
              + "JOIN memberships m ON m.community_id = c.id "
              + "WHERE m.username = ? ORDER BY c.name");
            ps.setString(1, username);
            return runAndRead(ps);
        } catch (SQLException e) {
            throw new RuntimeException("could not load joined communities", e);
        }
    }

    // the usernames of everyone in a community, used by the server to push
    // forum updates to them
    public ArrayList<String> getMemberUsernames(int communityId) {
        ArrayList<String> names = new ArrayList<String>();
        try {
            Connection conn = Db.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT username FROM memberships WHERE community_id = ?");
            ps.setInt(1, communityId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                names.add(rs.getString(1));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException("could not load member usernames", e);
        }
        return names;
    }

    public int memberCount(int communityId) {
        try {
            Connection conn = Db.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM memberships WHERE community_id = ?");
            ps.setInt(1, communityId);
            ResultSet rs = ps.executeQuery();
            int n = 0;
            if (rs.next()) {
                n = rs.getInt(1);
            }
            rs.close();
            ps.close();
            return n;
        } catch (SQLException e) {
            throw new RuntimeException("could not count members", e);
        }
    }

    public boolean isEmpty() {
        return getAll().isEmpty();
    }

    // ---- helpers ----

    private ArrayList<Community> query(String sql) {
        try {
            Connection conn = Db.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            return runAndRead(ps);
        } catch (SQLException e) {
            throw new RuntimeException("query failed", e);
        }
    }

    private ArrayList<Community> runAndRead(PreparedStatement ps) throws SQLException {
        ResultSet rs = ps.executeQuery();
        ArrayList<Community> list = new ArrayList<Community>();
        while (rs.next()) {
            list.add(readRow(rs));
        }
        rs.close();
        ps.close();
        // fill tags + member count after we closed the first query
        for (int i = 0; i < list.size(); i++) {
            fillDetails(list.get(i));
        }
        return list;
    }

    private void fillDetails(Community c) {
        c.setTags(getTags(c.getId()));
        c.setMemberCount(memberCount(c.getId()));
    }

    private Community readRow(ResultSet rs) throws SQLException {
        Community c = new Community();
        c.setId(rs.getInt("id"));
        c.setName(rs.getString("name"));
        c.setDescription(rs.getString("description"));
        c.setCategory(rs.getString("category"));
        c.setEmoji(rs.getString("emoji"));
        c.setCoverColor(rs.getString("cover_color"));
        c.setCreatedBy(rs.getString("created_by"));
        c.setCreatedAt(rs.getString("created_at"));
        return c;
    }
}
