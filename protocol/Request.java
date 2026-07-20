package protocol;

import com.google.gson.JsonObject;

/*
 * A request the client sends to the server, one per line of the socket.
 * It has an id (so the reply can be matched back to it), an action name like
 * "login" or "sendMessage", and a data object with whatever that action needs.
 *
 * Example on the wire:
 *   {"id":4,"action":"login","data":{"username":"ada","password":"..."}}
 */
public class Request {

    public int id;
    public String action;
    public JsonObject data;

    public Request() {
    }

    public Request(int id, String action, JsonObject data) {
        this.id = id;
        this.action = action;
        this.data = data;
    }

    // small helpers so handlers can pull fields without null checks everywhere
    public String getString(String key) {
        if (data == null || !data.has(key) || data.get(key).isJsonNull()) {
            return null;
        }
        return data.get(key).getAsString();
    }

    public int getInt(String key) {
        if (data == null || !data.has(key) || data.get(key).isJsonNull()) {
            return 0;
        }
        return data.get(key).getAsInt();
    }
}
