package net;

import com.google.gson.JsonObject;

import protocol.Json;
import protocol.Request;
import protocol.Response;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.SwingUtilities;

/*
 * The desktop app's connection to the server. There is one of these for the
 * whole app (getInstance). It:
 *
 *   - opens a socket to the server
 *   - lets a screen send a request and wait for the reply (send)
 *   - runs a background thread that reads everything coming in, matches replies
 *     back to whoever is waiting, and hands pushes to the listeners
 *
 * Requests and replies are matched by an id, because a push from the server can
 * land in the middle of us waiting for a reply.
 */
public class ServerClient {

    private static ServerClient instance = new ServerClient();

    public static ServerClient getInstance() {
        return instance;
    }

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean connected = false;

    private AtomicInteger nextId = new AtomicInteger(1);

    // requests we are still waiting on, by id
    private HashMap<Integer, Holder> pending = new HashMap<Integer, Holder>();
    // screens that want to hear about pushes
    private ArrayList<PushListener> listeners = new ArrayList<PushListener>();

    private ServerClient() {
    }

    public boolean isConnected() {
        return connected;
    }

    // open the connection and start the reader thread. returns false if the
    // server isnt reachable.
    public boolean connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new PrintWriter(socket.getOutputStream(), true);
            connected = true;
            startReader();
            return true;
        } catch (Exception e) {
            connected = false;
            return false;
        }
    }

    private void startReader() {
        Thread reader = new Thread(new Runnable() {
            public void run() {
                readLoop();
            }
        });
        reader.setDaemon(true);
        reader.start();
    }

    private void readLoop() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                Response r = Json.fromJson(line, Response.class);
                if (r.type != null && r.type.equals("push")) {
                    deliverPush(r);
                } else {
                    deliverReply(r);
                }
            }
        } catch (Exception e) {
            // connection closed, nothing more to read
        }
        connected = false;
    }

    private void deliverReply(Response r) {
        Holder holder;
        synchronized (pending) {
            holder = pending.remove(r.id);
        }
        if (holder != null) {
            synchronized (holder) {
                holder.response = r;
                holder.done = true;
                holder.notifyAll();
            }
        }
    }

    // hand a push to every listener, on the swing thread so they can touch the ui
    private void deliverPush(final Response r) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ArrayList<PushListener> copy;
                synchronized (listeners) {
                    copy = new ArrayList<PushListener>(listeners);
                }
                for (int i = 0; i < copy.size(); i++) {
                    copy.get(i).onPush(r.event, r.data);
                }
            }
        });
    }

    /*
     * Send a request and block until the reply comes back. Returns the data
     * part of the reply as a json string. Throws IllegalArgumentException with
     * the server's message if the server said no.
     */
    public String send(String action, JsonObject data) {
        if (!connected) {
            throw new IllegalArgumentException("Not connected to the server.");
        }

        Holder holder = new Holder();
        int id = nextId.getAndIncrement();
        synchronized (pending) {
            pending.put(id, holder);
        }

        Request req = new Request(id, action, data);
        synchronized (this) {
            out.println(Json.toJson(req));
        }

        // wait for the reader thread to fill in the reply
        synchronized (holder) {
            long waitedUntil = System.currentTimeMillis() + 10000; // 10 sec safety
            while (!holder.done) {
                long remaining = waitedUntil - System.currentTimeMillis();
                if (remaining <= 0) {
                    break;
                }
                try {
                    holder.wait(remaining);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

        if (holder.response == null) {
            throw new IllegalArgumentException("The server didn't respond, try again.");
        }
        if (!holder.response.ok) {
            throw new IllegalArgumentException(holder.response.error);
        }
        return holder.response.data;
    }

    public void addPushListener(PushListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    public void removePushListener(PushListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    // little box a waiting caller sits on until the reply arrives
    private static class Holder {
        Response response;
        boolean done = false;
    }
}
