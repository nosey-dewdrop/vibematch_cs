package server;

import com.google.gson.JsonArray;

import data.UserDao;
import model.MbtiResult;
import model.User;
import protocol.Json;
import protocol.Request;
import protocol.Response;
import service.MbtiService;

import java.util.ArrayList;

/*
 * Onboarding + profile bits that write to the user: saving interests and
 * scoring / storing the vibe test. The mbti scoring itself is done here on the
 * server so the result and the stored type can never disagree.
 */
public class ProfileHandler {

    private UserDao userDao = new UserDao();
    private MbtiService mbti = new MbtiService();

    // save the picked interests (replaces whatever they had)
    public Response setInterests(Request req, ClientHandler client) {
        String username = client.getUsername();
        ArrayList<String> picked = new ArrayList<String>();
        if (req.data != null && req.data.has("interests")) {
            JsonArray arr = req.data.getAsJsonArray("interests");
            for (int i = 0; i < arr.size(); i++) {
                picked.add(arr.get(i).getAsString());
            }
        }
        userDao.setInterests(username, picked);
        return Response.reply(req.id, "{\"ok\":true}");
    }

    // score the 16 answers, store the type, return the full result for the bars
    public Response submitMbti(Request req, ClientHandler client) {
        String username = client.getUsername();
        if (req.data == null || !req.data.has("answers")) {
            return Response.fail(req.id, "No answers given.");
        }
        JsonArray arr = req.data.getAsJsonArray("answers");
        // the test is exactly 16 questions, each answered 0 or 1. a short or
        // malformed submission would silently produce a wrong personality type,
        // so reject it instead of scoring garbage.
        if (arr.size() != 16) {
            return Response.fail(req.id, "Please answer all 16 questions.");
        }
        int[] answers = new int[16];
        for (int i = 0; i < 16; i++) {
            int v;
            try {
                v = arr.get(i).getAsInt();
            } catch (Exception e) {
                return Response.fail(req.id, "Invalid answer format.");
            }
            if (v != 0 && v != 1) {
                return Response.fail(req.id, "Invalid answer value.");
            }
            answers[i] = v;
        }
        MbtiResult result = mbti.score(answers);
        userDao.updateMbti(username, result.getType());
        return Response.reply(req.id, Json.toJson(result));
    }

    // reload a user (with their interests) after changes
    public Response getUser(Request req) {
        User u = userDao.findByUsername(req.getString("username"));
        if (u == null) {
            return Response.fail(req.id, "user not found");
        }
        return Response.reply(req.id, Json.toJson(Dto.safeUser(u)));
    }
}
