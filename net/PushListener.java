package net;

/*
 * A screen implements this if it wants to hear about pushes from the server
 * (a new message, a new post, ...). The event tells you what happened and the
 * dataJson is the payload to parse.
 */
public interface PushListener {
    void onPush(String event, String dataJson);
}
