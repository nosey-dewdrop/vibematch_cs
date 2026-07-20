package protocol;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/*
 * Thin wrapper around Gson so the rest of the code doesnt import it directly.
 * We use this on both sides: the server turns objects into json to send, the
 * client turns json back into objects. Keeping it in one place means the two
 * sides always agree on the format.
 */
public class Json {

    private static final Gson GSON = new Gson();

    public static String toJson(Object o) {
        return GSON.toJson(o);
    }

    public static <T> T fromJson(String text, Class<T> type) {
        return GSON.fromJson(text, type);
    }

    public static JsonObject parse(String text) {
        return JsonParser.parseString(text).getAsJsonObject();
    }

    private Json() {
    }
}
