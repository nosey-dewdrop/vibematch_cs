package server;

import data.CommunityDao;
import data.UserDao;
import model.Community;
import model.User;
import protocol.Json;
import protocol.Request;
import protocol.Response;
import service.CommunityService;
import service.MatchService;

import java.util.ArrayList;

/*
 * All the community actions: listing, searching, the matched home feed, and
 * joining / leaving. These just call the same service and dao classes we
 * already had, the only new thing is packing the result into json.
 */
public class CommunityHandler {

    private CommunityService communities = new CommunityService();
    private CommunityDao communityDao = new CommunityDao();
    private MatchService matcher = new MatchService();
    private UserDao userDao = new UserDao();

    public Response list(Request req, ClientHandler client) {
        return scoredListResponse(client, req, communities.getAll());
    }

    public Response get(Request req) {
        Community c = communities.findById(req.getInt("id"));
        if (c == null) {
            return Response.fail(req.id, "community not found");
        }
        return Response.reply(req.id, Json.toJson(c));
    }

    public Response byCategory(Request req, ClientHandler client) {
        return scoredListResponse(client, req, communities.getByCategory(req.getString("category")));
    }

    public Response search(Request req, ClientHandler client) {
        return scoredListResponse(client, req, communities.search(req.getString("text")));
    }

    public Response joined(Request req, ClientHandler client) {
        return listResponse(req.id, communities.getJoined(client.getUsername()));
    }

    public Response isMember(Request req, ClientHandler client) {
        boolean member = communities.isMember(client.getUsername(), req.getInt("communityId"));
        return Response.reply(req.id, "{\"member\":" + member + "}");
    }

    public Response join(Request req, ClientHandler client) {
        communities.join(client.getUsername(), req.getInt("communityId"));
        return Response.reply(req.id, "{\"ok\":true}");
    }

    public Response leave(Request req, ClientHandler client) {
        communities.leave(client.getUsername(), req.getInt("communityId"));
        return Response.reply(req.id, "{\"ok\":true}");
    }

    // the home feed: score every community this user hasnt joined and return
    // the best ones, with the match percent already filled in
    public Response homeMatches(Request req, ClientHandler client) {
        String username = client.getUsername();
        User user = userDao.findByUsername(username);
        if (user == null) {
            return Response.fail(req.id, "user not found");
        }
        ArrayList<Community> all = communities.getAll();
        ArrayList<Community> notJoined = new ArrayList<Community>();
        for (int i = 0; i < all.size(); i++) {
            if (!communities.isMember(username, all.get(i).getId())) {
                notJoined.add(all.get(i));
            }
        }
        ArrayList<Community> top = matcher.topMatches(user, notJoined, 6);
        return listResponse(req.id, top);
    }

    // score a single community for a user (used on the detail page)
    public Response scoreOne(Request req, ClientHandler client) {
        String username = client.getUsername();
        User user = userDao.findByUsername(username);
        Community c = communities.findById(req.getInt("communityId"));
        if (user == null || c == null) {
            return Response.fail(req.id, "not found");
        }
        matcher.scoreFor(user, c);
        c.setMember(communities.isMember(username, c.getId()));
        return Response.reply(req.id, Json.toJson(c));
    }

    // like listResponse but fills in each community's match percent AND whether
    // the user is already a member, so the client doesnt have to ask community
    // by community (that was slow). we load the user's joined ids once here.
    private Response scoredListResponse(ClientHandler client, Request req, ArrayList<Community> list) {
        String username = client.getUsername();
        if (username != null) {
            User user = userDao.findByUsername(username);
            if (user != null) {
                ArrayList<Community> mine = communities.getJoined(username);
                for (int i = 0; i < list.size(); i++) {
                    Community c = list.get(i);
                    matcher.scoreFor(user, c);
                    c.setMember(containsId(mine, c.getId()));
                }
            }
        }
        return listResponse(req.id, list);
    }

    private boolean containsId(ArrayList<Community> list, int id) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId() == id) {
                return true;
            }
        }
        return false;
    }

    // helper: send an array of communities
    private Response listResponse(int id, ArrayList<Community> list) {
        Community[] array = new Community[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return Response.reply(id, Json.toJson(array));
    }
}
