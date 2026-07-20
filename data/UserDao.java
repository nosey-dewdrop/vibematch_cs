package data;

import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;

/*
 * Everything that reads or writes the users table (and user_interests) lives
 * here. The rest of the app talks to users through this class, not raw SQL.
 */
public class UserDao {

    // save a brand new user. they start unverified until they enter the code.
    public void insert(User u) {
        String sql = "INSERT INTO users (username, display_name, email, pass_hash, salt, bio, verified, mbti_type, created_at) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            Connection c = Db.getConnection();
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getDisplayName());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getPassHash());
            ps.setString(5, u.getSalt());
            ps.setString(6, u.getBio());
            ps.setInt(7, u.isVerified() ? 1 : 0);
            ps.setString(8, u.getMbtiType());
            ps.setString(9, LocalDateTime.now().toString());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException("could not insert user", e);
        }
    }

    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try {
            Connection c = Db.getConnection();
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            User u = null;
            if (rs.next()) {
                u = readRow(rs);
            }
            rs.close();
            ps.close();
            if (u != null) {
                u.setInterests(getInterests(u.getUsername()));
            }
            return u;
        } catch (SQLException e) {
            throw new RuntimeException("could not load user", e);
        }
    }

    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try {
            Connection c = Db.getConnection();
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            User u = null;
            if (rs.next()) {
                u = readRow(rs);
            }
            rs.close();
            ps.close();
            if (u != null) {
                u.setInterests(getInterests(u.getUsername()));
            }
            return u;
        } catch (SQLException e) {
            throw new RuntimeException("could not load user by email", e);
        }
    }

    public boolean usernameExists(String username) {
        return findByUsername(username) != null;
    }

    public boolean emailExists(String email) {
        return findByEmail(email) != null;
    }

    public void setVerified(String username, boolean verified) {
        runUpdate("UPDATE users SET verified = ? WHERE username = ?", verified ? "1" : "0", username);
    }

    public void updateMbti(String username, String mbtiType) {
        runUpdate("UPDATE users SET mbti_type = ? WHERE username = ?", mbtiType, username);
    }

    public void updateBio(String username, String bio) {
        runUpdate("UPDATE users SET bio = ? WHERE username = ?", bio, username);
    }

    // used when we re-hash an old account's password to the newer scheme
    public void updatePassword(String username, String passHash, String salt) {
        try {
            Connection c = Db.getConnection();
            PreparedStatement ps = c.prepareStatement(
                "UPDATE users SET pass_hash = ?, salt = ? WHERE username = ?");
            ps.setString(1, passHash);
            ps.setString(2, salt);
            ps.setString(3, username);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException("could not update password", e);
        }
    }

    // ---- interests ----

    public ArrayList<String> getInterests(String username) {
        ArrayList<String> list = new ArrayList<String>();
        String sql = "SELECT interest FROM user_interests WHERE username = ?";
        try {
            Connection c = Db.getConnection();
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rs.getString(1));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException("could not load interests", e);
        }
        return list;
    }

    // replace the whole interest set for a user (used by the picker)
    public void setInterests(String username, ArrayList<String> interests) {
        try {
            Connection c = Db.getConnection();
            PreparedStatement del = c.prepareStatement("DELETE FROM user_interests WHERE username = ?");
            del.setString(1, username);
            del.executeUpdate();
            del.close();

            PreparedStatement ins = c.prepareStatement(
                "INSERT INTO user_interests (username, interest) VALUES (?, ?)");
            for (int i = 0; i < interests.size(); i++) {
                ins.setString(1, username);
                ins.setString(2, interests.get(i));
                ins.executeUpdate();
            }
            ins.close();
        } catch (SQLException e) {
            throw new RuntimeException("could not save interests", e);
        }
    }

    // ---- helpers ----

    private void runUpdate(String sql, String value, String username) {
        try {
            Connection c = Db.getConnection();
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setString(1, value);
            ps.setString(2, username);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException("update failed", e);
        }
    }

    private User readRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUsername(rs.getString("username"));
        u.setDisplayName(rs.getString("display_name"));
        u.setEmail(rs.getString("email"));
        u.setPassHash(rs.getString("pass_hash"));
        u.setSalt(rs.getString("salt"));
        u.setBio(rs.getString("bio"));
        u.setVerified(rs.getInt("verified") == 1);
        u.setMbtiType(rs.getString("mbti_type"));
        u.setCreatedAt(rs.getString("created_at"));
        return u;
    }
}
