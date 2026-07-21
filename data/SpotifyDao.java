package data;

import model.SpotifyProfile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;

/*
 * Stores a user's connected spotify account. Artists and genres are kept as
 * simple comma separated strings in one row -- there's no need to query inside
 * them, we only ever load or wipe the whole thing, so a join table would be
 * overkill here.
 */
public class SpotifyDao {

    // connect or re-connect: one row per user, so upsert by deleting first
    public void save(SpotifyProfile p) {
        try {
            Connection c = Db.getConnection();
            PreparedStatement del = c.prepareStatement("DELETE FROM spotify_profiles WHERE username = ?");
            del.setString(1, p.getUsername());
            del.executeUpdate();
            del.close();

            PreparedStatement ins = c.prepareStatement(
                "INSERT INTO spotify_profiles (username, display_name, top_artists, top_genres, connected_at) " +
                "VALUES (?, ?, ?, ?, ?)");
            ins.setString(1, p.getUsername());
            ins.setString(2, p.getDisplayName());
            ins.setString(3, join(p.getTopArtists()));
            ins.setString(4, join(p.getTopGenres()));
            ins.setString(5, LocalDateTime.now().toString());
            ins.executeUpdate();
            ins.close();
        } catch (SQLException e) {
            throw new RuntimeException("could not save spotify profile", e);
        }
    }

    public SpotifyProfile find(String username) {
        try {
            Connection c = Db.getConnection();
            PreparedStatement ps = c.prepareStatement("SELECT * FROM spotify_profiles WHERE username = ?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            SpotifyProfile p = null;
            if (rs.next()) {
                p = new SpotifyProfile();
                p.setUsername(rs.getString("username"));
                p.setDisplayName(rs.getString("display_name"));
                p.setTopArtists(split(rs.getString("top_artists")));
                p.setTopGenres(split(rs.getString("top_genres")));
                p.setConnectedAt(rs.getString("connected_at"));
            }
            rs.close();
            ps.close();
            return p;
        } catch (SQLException e) {
            throw new RuntimeException("could not load spotify profile", e);
        }
    }

    public void delete(String username) {
        try {
            Connection c = Db.getConnection();
            PreparedStatement ps = c.prepareStatement("DELETE FROM spotify_profiles WHERE username = ?");
            ps.setString(1, username);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException("could not delete spotify profile", e);
        }
    }

    private String join(ArrayList<String> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(SEP);
            sb.append(list.get(i));
        }
        return sb.toString();
    }

    private ArrayList<String> split(String s) {
        ArrayList<String> list = new ArrayList<String>();
        if (s == null || s.trim().isEmpty()) {
            return list;
        }
        // split on the unit separator, NOT a comma -- artist names like
        // "Tyler, The Creator" contain commas and would be torn in two.
        String[] parts = s.split(java.util.regex.Pattern.quote(SEP));
        for (int i = 0; i < parts.length; i++) {
            String t = parts[i].trim();
            if (!t.isEmpty()) {
                list.add(t);
            }
        }
        return list;
    }

    // ASCII unit separator: can't appear in an artist or genre name
    private static final String SEP = "";
}
