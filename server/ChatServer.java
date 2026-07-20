package server;

import protocol.Response;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/*
 * The heart of the server. It opens a plain TCP ServerSocket and waits for
 * clients to connect. Every time one does, we hand that connection to its own
 * ClientHandler running on its own thread, so lots of people can be connected
 * at the same time.
 *
 * We also keep a map of who is online (username -> their handler). That map is
 * what makes real time work: when something happens that another user should
 * see right away (a new message, a new post), we look them up here and push it
 * straight down their socket instead of waiting for them to ask.
 */
public class ChatServer {

    public static final int PORT = 5050;

    // everyone currently connected AND logged in
    private HashMap<String, ClientHandler> online = new HashMap<String, ClientHandler>();
    private RequestRouter router;

    public ChatServer() {
        this.router = new RequestRouter(this);
    }

    public void start() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("vibematch server listening on port " + PORT);
        } catch (IOException e) {
            System.out.println("could not start server on port " + PORT + ": " + e.getMessage());
            return;
        }

        // accept loop, runs forever
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("a client connected from " + socket.getInetAddress());
                ClientHandler handler = new ClientHandler(socket, router, this);
                Thread t = new Thread(handler);
                t.start();
            } catch (IOException e) {
                System.out.println("problem accepting a client: " + e.getMessage());
            }
        }
    }

    // called by a handler once its user logs in / registers
    public synchronized void register(String username, ClientHandler handler) {
        online.put(username, handler);
        System.out.println(username + " is now online (" + online.size() + " online)");
    }

    // called when a client disconnects
    public synchronized void unregister(String username) {
        if (username != null) {
            online.remove(username);
            System.out.println(username + " went offline (" + online.size() + " online)");
        }
    }

    public synchronized boolean isOnline(String username) {
        return online.containsKey(username);
    }

    // push an event to one user, if they happen to be online right now
    public synchronized void pushTo(String username, Response push) {
        ClientHandler handler = online.get(username);
        if (handler != null) {
            handler.send(push);
        }
    }

    // push the same event to a bunch of users (e.g. everyone in a community)
    public synchronized void pushToMany(ArrayList<String> usernames, Response push) {
        for (int i = 0; i < usernames.size(); i++) {
            pushTo(usernames.get(i), push);
        }
    }
}
