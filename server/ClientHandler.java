package server;

import protocol.Json;
import protocol.Request;
import protocol.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/*
 * Handles one connected client for its whole life. Runs on its own thread.
 *
 * The conversation is line based: the client writes one json request per line,
 * we read it, ask the router what to do, and write one json response back on
 * one line. Because the server can also push things at any time, writing is
 * synchronized so a reply and a push never get tangled up on the wire.
 */
public class ClientHandler implements Runnable {

    private Socket socket;
    private RequestRouter router;
    private ChatServer server;

    private BufferedReader in;
    private PrintWriter out;

    // set once this connection logs in, so we know who it is
    private String username = null;

    public ClientHandler(Socket socket, RequestRouter router, ChatServer server) {
        this.socket = socket;
        this.router = router;
        this.server = server;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new PrintWriter(socket.getOutputStream(), true);

            String line;
            while ((line = in.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                handleLine(line);
            }
        } catch (IOException e) {
            // client dropped, thats normal, just fall through to cleanup
        } finally {
            cleanup();
        }
    }

    private void handleLine(String line) {
        Request request;
        try {
            request = Json.fromJson(line, Request.class);
        } catch (Exception e) {
            // couldnt even parse it, cant match an id so just tell them
            send(Response.fail(0, "bad request"));
            return;
        }

        Response response;
        try {
            response = router.handle(request, this);
        } catch (IllegalArgumentException e) {
            // a normal "user did something invalid" error, safe to show
            response = Response.fail(request.id, e.getMessage());
        } catch (Exception e) {
            // something actually broke on our side
            System.out.println("error handling " + request.action + ": " + e.getMessage());
            response = Response.fail(request.id, "something went wrong on the server");
        }
        if (response != null) {
            send(response);
        }
    }

    // write one response as a single json line. synchronized so pushes and
    // replies dont interleave.
    public synchronized void send(Response response) {
        if (out != null) {
            out.println(Json.toJson(response));
        }
    }

    private void cleanup() {
        server.unregister(username);
        try {
            socket.close();
        } catch (IOException e) {
            // already closing, ignore
        }
    }
}
