package server;

import com.google.gson.JsonObject;

import data.CommunityDao;
import data.CommunityMessageDao;
import model.CommunityMessage;
import protocol.Json;
import protocol.Request;
import protocol.Response;

import java.util.ArrayList;

/*
 * The community guestbook. Every member of a community can post a line, and it's
 * pushed live to every other member who's online -- like a shared wall for the
 * community. Only members can read or post (privacy).
 */
public class CommunityChatHandler {

    private CommunityMessageDao dao = new CommunityMessageDao();
    private CommunityDao communityDao = new CommunityDao();
    private ChatServer server;

    public CommunityChatHandler(ChatServer server) {
        this.server = server;
    }

    public Response post(Request req, ClientHandler client) {
        String sender = client.getUsername();
        int communityId = req.getInt("communityId");
        String body = req.getString("body");

        if (body == null || body.trim().isEmpty()) {
            return Response.fail(req.id, "Message can't be empty.");
        }
        // only members can post
        if (!communityDao.isMember(sender, communityId)) {
            return Response.fail(req.id, "Join the community to post here.");
        }

        CommunityMessage m = new CommunityMessage(communityId, sender, body.trim());
        dao.post(m);

        // push a live update to every member of the community
        JsonObject data = new JsonObject();
        data.addProperty("communityId", communityId);
        Response push = Response.push("communityMessage", Json.toJson(data));
        ArrayList<String> members = communityDao.getMemberUsernames(communityId);
        server.pushToMany(members, push);

        return Response.reply(req.id, "{\"ok\":true}");
    }

    public Response list(Request req, ClientHandler client) {
        int communityId = req.getInt("communityId");
        // only members can read the guestbook
        if (!communityDao.isMember(client.getUsername(), communityId)) {
            return Response.fail(req.id, "Join the community to see its chat.");
        }
        ArrayList<CommunityMessage> msgs = dao.list(communityId);
        CommunityMessage[] arr = new CommunityMessage[msgs.size()];
        for (int i = 0; i < msgs.size(); i++) {
            arr[i] = msgs.get(i);
        }
        return Response.reply(req.id, Json.toJson(arr));
    }
}
