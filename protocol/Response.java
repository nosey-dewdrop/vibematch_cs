package protocol;

/*
 * What the server sends back. Two kinds:
 *
 *  - a reply to a request  -> type = "reply", carries the same id, plus ok /
 *    error / data
 *  - a push (server started it, e.g. a new message arrived) -> type = "push",
 *    carries an event name instead of an id
 *
 * The client reader looks at type to decide: match a reply to whoever is
 * waiting, or hand a push to the listeners.
 */
public class Response {

    public String type;    // "reply" or "push"
    public int id;         // set for replies
    public boolean ok;     // set for replies
    public String error;   // set for replies that failed
    public String event;   // set for pushes, e.g. "newMessage"
    public String data;    // json payload as a string (parsed by the receiver)

    public Response() {
    }

    // build a successful reply
    public static Response reply(int id, String dataJson) {
        Response r = new Response();
        r.type = "reply";
        r.id = id;
        r.ok = true;
        r.data = dataJson;
        return r;
    }

    // build a failed reply
    public static Response fail(int id, String error) {
        Response r = new Response();
        r.type = "reply";
        r.id = id;
        r.ok = false;
        r.error = error;
        return r;
    }

    // build a push the server sends on its own
    public static Response push(String event, String dataJson) {
        Response r = new Response();
        r.type = "push";
        r.event = event;
        r.data = dataJson;
        return r;
    }
}
