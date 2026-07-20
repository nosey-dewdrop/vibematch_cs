package server;

import com.google.gson.JsonObject;

import data.NotificationDao;
import protocol.Json;
import protocol.Response;

/*
 * One little helper the other handlers call when something happened that a user
 * should be told about. It does two things: saves the notification (so it's
 * there on the bell even if they were offline) and, if they are online right
 * now, pushes it so the bell updates live.
 */
public class Notifier {

    private static NotificationDao dao = new NotificationDao();

    public static void notify(ChatServer server, String username, String type, String text) {
        dao.insert(username, type, text);

        JsonObject data = new JsonObject();
        data.addProperty("type", type);
        data.addProperty("text", text);
        server.pushTo(username, Response.push("notification", Json.toJson(data)));
    }

    private Notifier() {
    }
}
