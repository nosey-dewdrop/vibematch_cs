package net;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import model.SpotifyProfile;
import protocol.Json;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.Properties;

/*
 * NET -- SpotifyAuth
 *
 * Connects the user's real spotify account and reads their music taste, using
 * the Authorization Code + PKCE flow (the right flow for a desktop app: no
 * client secret has to sit on disk).
 *
 * The flow, all on the user's machine:
 *   1. open the system browser to spotify's consent page
 *   2. spotify redirects back to http://127.0.0.1:8888/callback with a code
 *   3. a tiny one-shot local server here catches that code
 *   4. exchange the code (+ our PKCE verifier) for an access token
 *   5. call /me and /me/top/artists to get their name, top artists and genres
 *
 * The client id comes from credentials.properties (spotify.client.id) or the
 * env var VIBEMATCH_SPOTIFY_CLIENT_ID -- same pattern as the email creds, so it
 * never has to be hardcoded in the repo.
 */
public class SpotifyAuth {

    private static final String REDIRECT_URI = "http://127.0.0.1:8888/callback";
    private static final int CALLBACK_PORT = 8888;
    private static final String SCOPE = "user-top-read";
    private static final String CREDS_FILE = "credentials.properties";

    private final HttpClient http = HttpClient.newHttpClient();

    public static boolean isConfigured() {
        return notBlank(clientId());
    }

    /*
     * Runs the whole connect flow and returns the filled-in SpotifyProfile.
     * Blocks while the user approves in their browser (with a timeout). Throws
     * IllegalArgumentException with a friendly message if anything goes wrong,
     * which the UI already knows how to show.
     */
    public SpotifyProfile connect(String username) {
        String cid = clientId();
        if (!notBlank(cid)) {
            throw new IllegalArgumentException("Spotify isn't set up on this build yet.");
        }

        String verifier = randomVerifier();
        String challenge = challengeFor(verifier);

        // step 1 + 2 + 3: open browser, catch the redirect, pull the code
        String code = authorizeAndCatchCode(cid, challenge);

        // step 4: code -> access token
        String accessToken = exchangeCodeForToken(cid, code, verifier);

        // step 5: read their taste
        SpotifyProfile profile = new SpotifyProfile();
        profile.setUsername(username);
        profile.setDisplayName(fetchDisplayName(accessToken));
        fillTopArtistsAndGenres(accessToken, profile);
        return profile;
    }

    // ---- step 1-3: browser consent + loopback capture ----

    private String authorizeAndCatchCode(String clientId, String challenge) {
        ServerSocket server;
        try {
            server = new ServerSocket();
            server.setReuseAddress(true);
            server.bind(new InetSocketAddress("127.0.0.1", CALLBACK_PORT));
            server.setSoTimeout(120000); // 2 min for the user to approve
        } catch (Exception e) {
            throw new IllegalArgumentException(
                "Couldn't start the Spotify sign-in (port 8888 busy?). Close other apps and try again.");
        }

        try {
            String authUrl = "https://accounts.spotify.com/authorize"
                    + "?client_id=" + enc(clientId)
                    + "&response_type=code"
                    + "&redirect_uri=" + enc(REDIRECT_URI)
                    + "&scope=" + enc(SCOPE)
                    + "&code_challenge_method=S256"
                    + "&code_challenge=" + enc(challenge);
            openBrowser(authUrl);

            // wait for spotify to redirect the browser back to us
            Socket client = server.accept();
            String code = readCodeFromRequest(client);
            writeBrowserResponse(client, code != null);
            client.close();

            if (code == null) {
                throw new IllegalArgumentException("Spotify sign-in was cancelled.");
            }
            return code;
        } catch (IllegalArgumentException ie) {
            throw ie;
        } catch (java.net.SocketTimeoutException te) {
            throw new IllegalArgumentException("Spotify sign-in timed out. Try again.");
        } catch (Exception e) {
            throw new IllegalArgumentException("Spotify sign-in failed: " + e.getMessage());
        } finally {
            try { server.close(); } catch (Exception ignore) {}
        }
    }

    // read the GET line and pull ?code= out of it (or ?error=)
    private String readCodeFromRequest(Socket client) throws Exception {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
        String line = in.readLine();
        if (line == null) {
            return null;
        }
        // line looks like: GET /callback?code=XXXX&... HTTP/1.1
        int q = line.indexOf('?');
        int sp = line.lastIndexOf(" HTTP");
        if (q < 0 || sp < 0 || sp <= q) {
            return null;
        }
        String query = line.substring(q + 1, sp);
        String[] parts = query.split("&");
        for (int i = 0; i < parts.length; i++) {
            String[] kv = parts[i].split("=", 2);
            if (kv.length == 2 && kv[0].equals("code")) {
                return java.net.URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    private void writeBrowserResponse(Socket client, boolean ok) throws Exception {
        String body = ok
                ? "<html><body style='font-family:sans-serif;text-align:center;margin-top:60px'>"
                  + "<h2>vibematch is connected to Spotify ✓</h2>"
                  + "<p>You can close this tab and go back to the app.</p></body></html>"
                : "<html><body style='font-family:sans-serif;text-align:center;margin-top:60px'>"
                  + "<h2>Sign-in cancelled</h2><p>You can close this tab.</p></body></html>";
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        OutputStream out = client.getOutputStream();
        String header = "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/html; charset=utf-8\r\n"
                + "Content-Length: " + bytes.length + "\r\n"
                + "Connection: close\r\n\r\n";
        out.write(header.getBytes(StandardCharsets.UTF_8));
        out.write(bytes);
        out.flush();
    }

    // ---- step 4: token exchange ----

    private String exchangeCodeForToken(String clientId, String code, String verifier) {
        String form = "grant_type=authorization_code"
                + "&code=" + enc(code)
                + "&redirect_uri=" + enc(REDIRECT_URI)
                + "&client_id=" + enc(clientId)
                + "&code_verifier=" + enc(verifier);
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://accounts.spotify.com/api/token"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(form))
                    .build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) {
                throw new IllegalArgumentException("Spotify rejected the sign-in. Try again.");
            }
            JsonObject obj = Json.parse(res.body());
            JsonElement tok = obj.get("access_token");
            if (tok == null) {
                throw new IllegalArgumentException("Spotify didn't return a token. Try again.");
            }
            return tok.getAsString();
        } catch (IllegalArgumentException ie) {
            throw ie;
        } catch (Exception e) {
            throw new IllegalArgumentException("Couldn't reach Spotify: " + e.getMessage());
        }
    }

    // ---- step 5: read taste ----

    private String fetchDisplayName(String accessToken) {
        try {
            JsonObject me = getJson("https://api.spotify.com/v1/me", accessToken);
            JsonElement dn = me.get("display_name");
            return dn != null && !dn.isJsonNull() ? dn.getAsString() : null;
        } catch (Exception e) {
            return null; // name is cosmetic, don't fail the whole connect over it
        }
    }

    private void fillTopArtistsAndGenres(String accessToken, SpotifyProfile profile) {
        try {
            JsonObject top = getJson(
                    "https://api.spotify.com/v1/me/top/artists?limit=10&time_range=medium_term",
                    accessToken);
            JsonArray items = top.getAsJsonArray("items");
            ArrayList<String> artists = new ArrayList<String>();
            ArrayList<String> ids = new ArrayList<String>();
            // keep genre order stable and unique
            LinkedHashSet<String> genres = new LinkedHashSet<String>();
            if (items != null) {
                for (int i = 0; i < items.size(); i++) {
                    JsonObject a = items.get(i).getAsJsonObject();
                    if (a.has("name")) {
                        artists.add(a.get("name").getAsString());
                    }
                    if (a.has("id")) {
                        ids.add(a.get("id").getAsString());
                    }
                    // genres are sometimes present inline
                    JsonArray gs = a.getAsJsonArray("genres");
                    if (gs != null) {
                        for (int j = 0; j < gs.size(); j++) {
                            genres.add(gs.get(j).getAsString());
                        }
                    }
                }
            }
            profile.setTopArtists(artists);

            // spotify often returns empty genres inline on /me/top/artists now,
            // so if we came up short, try the batch /artists call. NOTE: apps
            // created after nov 2024 get 403 on that catalog endpoint, so this
            // may come back empty -- that's a spotify restriction, not our bug.
            if (genres.size() < 3 && !ids.isEmpty()) {
                genres.addAll(fetchGenresByArtistIds(ids, accessToken));
            }

            // what we feed the match algorithm: real genres if spotify gave us
            // any, otherwise fall back to the top artists themselves (the one
            // piece of taste data user-top-read always returns). tagged so we
            // can tell them apart from hand-picked interests and strip them on
            // disconnect.
            ArrayList<String> tasteTags = new ArrayList<String>();
            if (!genres.isEmpty()) {
                ArrayList<String> genreList = new ArrayList<String>(genres);
                int cap = Math.min(8, genreList.size());
                for (int i = 0; i < cap; i++) {
                    tasteTags.add("music:" + genreList.get(i).toLowerCase());
                }
            } else {
                int cap = Math.min(8, artists.size());
                for (int i = 0; i < cap; i++) {
                    tasteTags.add("music:" + artists.get(i).toLowerCase());
                }
            }
            profile.setTopGenres(tasteTags);
        } catch (Exception e) {
            // an empty account is fine -- connect still succeeds, just no taste yet
            profile.setTopArtists(new ArrayList<String>());
            profile.setTopGenres(new ArrayList<String>());
        }
    }

    // batch-fetch full artist objects (up to 50 ids in one call) and collect
    // their genres. this is where spotify actually fills genres in reliably.
    private LinkedHashSet<String> fetchGenresByArtistIds(ArrayList<String> ids, String accessToken) {
        LinkedHashSet<String> genres = new LinkedHashSet<String>();
        try {
            StringBuilder joined = new StringBuilder();
            for (int i = 0; i < ids.size() && i < 50; i++) {
                if (i > 0) joined.append(",");
                joined.append(ids.get(i));
            }
            // NOTE: the comma-separated ids must NOT be url-encoded (encoding the
            // commas to %2C makes spotify treat it as one bad id -> all null).
            JsonObject res = getJson(
                    "https://api.spotify.com/v1/artists?ids=" + joined.toString(),
                    accessToken);
            JsonArray arr = res.getAsJsonArray("artists");
            if (arr != null) {
                for (int i = 0; i < arr.size(); i++) {
                    JsonElement el = arr.get(i);
                    if (el == null || el.isJsonNull()) continue;
                    JsonArray gs = el.getAsJsonObject().getAsJsonArray("genres");
                    if (gs != null) {
                        for (int j = 0; j < gs.size(); j++) {
                            genres.add(gs.get(j).getAsString());
                        }
                    }
                }
            }
        } catch (Exception e) {
            // best effort -- newer spotify apps get 403 on the catalog /artists
            // endpoint, so genres may be unavailable; we fall back to artists.
        }
        return genres;
    }

    private JsonObject getJson(String url, String accessToken) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new IllegalArgumentException("Spotify request failed (" + res.statusCode() + ").");
        }
        return Json.parse(res.body());
    }

    // ---- PKCE helpers ----

    private String randomVerifier() {
        byte[] bytes = new byte[64];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String challengeFor(String verifier) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(verifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new IllegalArgumentException("Couldn't prepare Spotify sign-in.");
        }
    }

    // ---- misc helpers ----

    private void openBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(url));
                return;
            }
        } catch (Exception ignore) {
        }
        // fallback for macos / linux where Desktop may be flaky
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("mac")) {
                new ProcessBuilder("open", url).start();
            } else if (os.contains("nix") || os.contains("nux")) {
                new ProcessBuilder("xdg-open", url).start();
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(
                "Couldn't open your browser. Visit this URL manually:\n" + url);
        }
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static String clientId() {
        String env = System.getenv("VIBEMATCH_SPOTIFY_CLIENT_ID");
        if (notBlank(env)) {
            return env;
        }
        FileInputStream in = null;
        try {
            in = new FileInputStream(CREDS_FILE);
            Properties p = new Properties();
            p.load(in);
            return p.getProperty("spotify.client.id");
        } catch (Exception e) {
            return null;
        } finally {
            try { if (in != null) in.close(); } catch (Exception ignore) {}
        }
    }

    private static boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
