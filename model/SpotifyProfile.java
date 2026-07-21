package model;

import java.util.ArrayList;

/*
 * A user's connected spotify account. topArtists is for showing off on the
 * profile; topGenres is what we actually feed into the match algorithm as
 * interests. Empty/absent means the user hasn't connected spotify.
 */
public class SpotifyProfile {

    private String username;
    private String displayName;              // their spotify display name
    private ArrayList<String> topArtists = new ArrayList<String>();
    private ArrayList<String> topGenres = new ArrayList<String>();
    private String connectedAt;

    public SpotifyProfile() {}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public ArrayList<String> getTopArtists() { return topArtists; }
    public void setTopArtists(ArrayList<String> topArtists) { this.topArtists = topArtists; }

    public ArrayList<String> getTopGenres() { return topGenres; }
    public void setTopGenres(ArrayList<String> topGenres) { this.topGenres = topGenres; }

    public String getConnectedAt() { return connectedAt; }
    public void setConnectedAt(String connectedAt) { this.connectedAt = connectedAt; }

    public boolean isConnected() {
        return username != null;
    }
}
