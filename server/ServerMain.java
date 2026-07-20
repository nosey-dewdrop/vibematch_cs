package server;

import data.Db;
import data.SampleData;

/*
 * Starts the vibematch server. Open the database (the ONE shared database that
 * every client talks to), drop in the sample data on first run, then start
 * listening for clients. This is what we run on the always on machine.
 *
 * Run it with:  java -cp "build:desktop/lib/*" server.ServerMain
 */
public class ServerMain {

    public static void main(String[] args) {
        System.out.println("starting vibematch server...");
        Db.connect();
        SampleData.seedIfNeeded();

        ChatServer server = new ChatServer();
        server.start(); // this blocks forever, accepting clients
    }
}
