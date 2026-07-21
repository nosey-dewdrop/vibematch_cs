package server;

import com.google.gson.JsonArray;

import data.SpotifyDao;
import data.UserDao;
import model.SpotifyProfile;
import protocol.Json;
import protocol.Request;
import protocol.Response;

import java.util.ArrayList;

/*
 * Stores the spotify taste the client read (the oauth itself happens on the
 * client, since it needs the user's browser). On connect we also fold the top
 * genres into the user's interests so the match algorithm can use their music
 * taste; on disconnect we strip exactly those genres back out.
 */
public class SpotifyHandler {

    private SpotifyDao spotifyDao = new SpotifyDao();
    private UserDao userDao = new UserDao();

    public Response save(Request req, ClientHandler client) {
        String username = client.getUsername();

        SpotifyProfile p = new SpotifyProfile();
        p.setUsername(username);
        String dn = req.getString("displayName");
        p.setDisplayName(dn == null || dn.isEmpty() ? null : dn);
        p.setTopArtists(readList(req, "artists"));
        ArrayList<String> genres = readList(req, "genres");
        p.setTopGenres(genres);

        spotifyDao.save(p);
        // genres feed the match algorithm as interests
        if (!genres.isEmpty()) {
            userDao.addInterests(username, genres);
        }

        SpotifyProfile saved = spotifyDao.find(username);
        return Response.reply(req.id, Json.toJson(saved));
    }

    public Response get(Request req, ClientHandler client) {
        SpotifyProfile p = spotifyDao.find(client.getUsername());
        if (p == null) {
            // not connected -> return an empty profile the client can read as "off"
            return Response.reply(req.id, Json.toJson(new SpotifyProfile()));
        }
        return Response.reply(req.id, Json.toJson(p));
    }

    public Response disconnect(Request req, ClientHandler client) {
        String username = client.getUsername();
        SpotifyProfile p = spotifyDao.find(username);
        if (p != null && !p.getTopGenres().isEmpty()) {
            // strip out exactly the genres we added when connecting
            userDao.removeInterests(username, p.getTopGenres());
        }
        spotifyDao.delete(username);
        return Response.reply(req.id, "{\"ok\":true}");
    }

    private ArrayList<String> readList(Request req, String key) {
        ArrayList<String> list = new ArrayList<String>();
        if (req.data != null && req.data.has(key)) {
            JsonArray arr = req.data.getAsJsonArray(key);
            for (int i = 0; i < arr.size(); i++) {
                list.add(arr.get(i).getAsString());
            }
        }
        return list;
    }
}
