package protocol;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;

/*
 * A tiny builder so we can put together the data part of a request without
 * writing out a JsonObject by hand every time.
 *
 *   Params.of().put("username", name).put("password", pass).json()
 */
public class Params {

    private JsonObject obj = new JsonObject();

    public static Params of() {
        return new Params();
    }

    public Params put(String key, String value) {
        obj.addProperty(key, value);
        return this;
    }

    public Params put(String key, int value) {
        obj.addProperty(key, value);
        return this;
    }

    // a list of strings, e.g. the picked interests
    public Params putList(String key, ArrayList<String> values) {
        JsonArray arr = new JsonArray();
        for (int i = 0; i < values.size(); i++) {
            arr.add(values.get(i));
        }
        obj.add(key, arr);
        return this;
    }

    // an int array, e.g. the mbti answers
    public Params putInts(String key, int[] values) {
        JsonArray arr = new JsonArray();
        for (int i = 0; i < values.length; i++) {
            arr.add(values[i]);
        }
        obj.add(key, arr);
        return this;
    }

    public JsonObject json() {
        return obj;
    }
}
