package server;

import data.NotificationDao;
import model.Notification;
import protocol.Json;
import protocol.Request;
import protocol.Response;

import java.util.ArrayList;

/*
 * The client facing side of notifications: give me my notifications, how many
 * are unread, and mark them read.
 */
public class NotificationHandler {

    private NotificationDao dao = new NotificationDao();

    public Response list(Request req, ClientHandler client) {
        ArrayList<Notification> list = dao.forUser(client.getUsername());
        Notification[] array = new Notification[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return Response.reply(req.id, Json.toJson(array));
    }

    public Response unread(Request req, ClientHandler client) {
        int count = dao.unreadCount(client.getUsername());
        return Response.reply(req.id, "{\"count\":" + count + "}");
    }

    public Response markRead(Request req, ClientHandler client) {
        dao.markAllRead(client.getUsername());
        return Response.reply(req.id, "{\"ok\":true}");
    }
}
