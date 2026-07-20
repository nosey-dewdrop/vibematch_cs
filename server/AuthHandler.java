package server;

import com.google.gson.JsonObject;

import data.UserDao;
import model.User;
import protocol.Json;
import protocol.Request;
import protocol.Response;
import service.AuthService;
import util.EmailSender;

import java.util.HashMap;

/*
 * Handles the account actions coming over the socket: register, verify, login,
 * resend code. It leans on AuthService for the real work and just deals with
 * the request / response side of things here.
 *
 * The pending verification codes are kept in memory (username -> code) because
 * the server is one long running process. If email is set up we mail the code;
 * if not, we send it back in the reply so the client can show it (same fallback
 * idea as before, just decided on the server now).
 */
public class AuthHandler {

    private AuthService auth = new AuthService();
    private UserDao userDao = new UserDao();

    // a code only stays valid for a short while, and only a handful of wrong
    // tries, so nobody can sit there guessing the 6 digits
    private static final long CODE_TTL_MS = 10 * 60 * 1000; // 10 minutes
    private static final int MAX_ATTEMPTS = 5;

    // codes we are waiting to be confirmed, keyed by username
    private HashMap<String, PendingCode> pendingCodes = new HashMap<String, PendingCode>();

    // a code together with when it dies and how many wrong tries it has left
    private static class PendingCode {
        String code;
        long expiresAt;
        int attemptsLeft;

        PendingCode(String code, long expiresAt, int attemptsLeft) {
            this.code = code;
            this.expiresAt = expiresAt;
            this.attemptsLeft = attemptsLeft;
        }
    }

    public Response register(Request req) {
        String name = req.getString("displayName");
        String username = req.getString("username");
        String email = req.getString("email");
        String password = req.getString("password");

        // AuthService throws IllegalArgumentException with a nice message if
        // something is wrong, and the client handler turns that into a fail
        User user = auth.register(name, username, email, password);

        String code = auth.generateVerificationCode();
        storeCode(user.getUsername(), code);

        boolean emailed = EmailSender.sendVerificationCode(user.getEmail(), code);

        JsonObject data = new JsonObject();
        data.addProperty("username", user.getUsername());
        data.addProperty("email", user.getEmail());
        data.addProperty("emailed", emailed);
        if (!emailed) {
            // no smtp set up, let the client show it so testing still works
            data.addProperty("code", code);
        }
        return Response.reply(req.id, Json.toJson(data));
    }

    public Response verify(Request req, ClientHandler client, ChatServer server) {
        String username = req.getString("username");
        String typed = req.getString("code");

        PendingCode pending = pendingCodes.get(username);
        if (pending == null) {
            return Response.fail(req.id, "No code to check. Ask for a new one.");
        }
        if (System.currentTimeMillis() > pending.expiresAt) {
            pendingCodes.remove(username);
            return Response.fail(req.id, "That code expired. Ask for a new one.");
        }
        if (typed == null || !typed.trim().equals(pending.code)) {
            pending.attemptsLeft--;
            if (pending.attemptsLeft <= 0) {
                pendingCodes.remove(username);
                return Response.fail(req.id, "Too many wrong tries. Ask for a new code.");
            }
            return Response.fail(req.id, "That code isn't right, check again.");
        }
        auth.markVerified(username);
        pendingCodes.remove(username);

        // this connection now belongs to them, so mark it online for pushes
        client.setUsername(username);
        server.register(username, client);

        User user = userDao.findByUsername(username);
        return Response.reply(req.id, Json.toJson(Dto.safeUser(user)));
    }

    public Response resend(Request req) {
        String username = req.getString("username");
        String email = req.getString("email");
        String code = auth.generateVerificationCode();
        storeCode(username, code);
        boolean emailed = EmailSender.sendVerificationCode(email, code);

        JsonObject data = new JsonObject();
        data.addProperty("emailed", emailed);
        if (!emailed) {
            data.addProperty("code", code);
        }
        return Response.reply(req.id, Json.toJson(data));
    }

    // login also marks this socket as belonging to that user, so the server can
    // push things to them later
    public Response login(Request req, ClientHandler client, ChatServer server) {
        String usernameOrEmail = req.getString("usernameOrEmail");
        String password = req.getString("password");

        User user = auth.login(usernameOrEmail, password);

        client.setUsername(user.getUsername());
        server.register(user.getUsername(), client);

        return Response.reply(req.id, Json.toJson(Dto.safeUser(user)));
    }

    // save a fresh code with its expiry and a full set of attempts
    private void storeCode(String username, String code) {
        long expiresAt = System.currentTimeMillis() + CODE_TTL_MS;
        pendingCodes.put(username, new PendingCode(code, expiresAt, MAX_ATTEMPTS));
    }
}
