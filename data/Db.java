package data;

import app.AppConstants;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.locks.ReentrantLock;

/*
 * Db is our little database layer. It opens one sqlite connection and makes
 * sure all the tables exist. The DAO classes (UserDao, CommunityDao, ...) use
 * getConnection() to run their queries.
 *
 * There is a single shared connection, and the server is multi threaded (one
 * thread per client). A JDBC Connection is not thread safe, so every unit of
 * db work has to run under LOCK. The server takes the lock around each request
 * (see RequestRouter) so two clients never touch the connection at once.
 */
public class Db {

    private static Connection connection;

    // serializes all access to the single shared connection. fair = true so
    // no client's request gets starved when the server is busy.
    public static final ReentrantLock LOCK = new ReentrantLock(true);

    // open the connection and build the schema. call this once at startup.
    public static void connect() {
        try {
            // sqlite stores everything in one file in the project root
            String url = "jdbc:sqlite:" + AppConstants.DB_FILE;
            connection = DriverManager.getConnection(url);

            // sqlite has foreign keys off by default, turn them on
            Statement pragma = connection.createStatement();
            pragma.execute("PRAGMA foreign_keys = ON");
            // when two clients hit the db at the same moment, wait a bit instead
            // of failing straight away with "database is locked"
            pragma.execute("PRAGMA busy_timeout = 5000");
            pragma.close();

            createTables();
        } catch (SQLException e) {
            // if we cant open the db there is no point continuing
            throw new RuntimeException("could not open database", e);
        }
    }

    public static synchronized Connection getConnection() {
        if (connection == null) {
            connect();
        }
        return connection;
    }

    public static void close() {
        try {
            if (connection != null) {
                connection.close();
                connection = null;
            }
        } catch (SQLException e) {
            // closing on exit, nothing useful to do if it fails
            System.out.println("warning: could not close db cleanly");
        }
    }

    // create every table if it isnt there yet
    private static void createTables() throws SQLException {
        Statement st = connection.createStatement();

        st.execute(
            "CREATE TABLE IF NOT EXISTS users (" +
            "  username TEXT PRIMARY KEY," +
            "  display_name TEXT NOT NULL," +
            "  email TEXT NOT NULL UNIQUE," +
            "  pass_hash TEXT NOT NULL," +
            "  salt TEXT NOT NULL," +
            "  bio TEXT," +
            "  verified INTEGER NOT NULL DEFAULT 0," +
            "  mbti_type TEXT," +
            "  created_at TEXT NOT NULL" +
            ")"
        );

        st.execute(
            "CREATE TABLE IF NOT EXISTS user_interests (" +
            "  username TEXT NOT NULL," +
            "  interest TEXT NOT NULL," +
            "  PRIMARY KEY (username, interest)," +
            "  FOREIGN KEY (username) REFERENCES users(username)" +
            ")"
        );

        st.execute(
            "CREATE TABLE IF NOT EXISTS communities (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  name TEXT NOT NULL," +
            "  description TEXT," +
            "  category TEXT," +
            "  emoji TEXT," +
            "  cover_color TEXT," +
            "  created_by TEXT," +
            "  created_at TEXT NOT NULL" +
            ")"
        );

        // tags of a community, used for matching against user interests
        st.execute(
            "CREATE TABLE IF NOT EXISTS community_tags (" +
            "  community_id INTEGER NOT NULL," +
            "  tag TEXT NOT NULL," +
            "  PRIMARY KEY (community_id, tag)," +
            "  FOREIGN KEY (community_id) REFERENCES communities(id)" +
            ")"
        );

        st.execute(
            "CREATE TABLE IF NOT EXISTS memberships (" +
            "  username TEXT NOT NULL," +
            "  community_id INTEGER NOT NULL," +
            "  joined_at TEXT NOT NULL," +
            "  PRIMARY KEY (username, community_id)," +
            "  FOREIGN KEY (username) REFERENCES users(username)," +
            "  FOREIGN KEY (community_id) REFERENCES communities(id)" +
            ")"
        );

        // forum posts inside a community
        st.execute(
            "CREATE TABLE IF NOT EXISTS posts (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  community_id INTEGER NOT NULL," +
            "  author TEXT NOT NULL," +
            "  title TEXT NOT NULL," +
            "  body TEXT," +
            "  created_at TEXT NOT NULL," +
            "  FOREIGN KEY (community_id) REFERENCES communities(id)," +
            "  FOREIGN KEY (author) REFERENCES users(username)" +
            ")"
        );

        // comments on posts. parent_id = 0 means top level, otherwise its a reply
        st.execute(
            "CREATE TABLE IF NOT EXISTS comments (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  post_id INTEGER NOT NULL," +
            "  author TEXT NOT NULL," +
            "  body TEXT NOT NULL," +
            "  parent_id INTEGER NOT NULL DEFAULT 0," +
            "  created_at TEXT NOT NULL," +
            "  FOREIGN KEY (post_id) REFERENCES posts(id)" +
            ")"
        );

        // friendships. one row per request: requester = user_a, target = user_b.
        // status is 'pending' until the target accepts, then 'accepted'.
        st.execute(
            "CREATE TABLE IF NOT EXISTS friendships (" +
            "  user_a TEXT NOT NULL," +
            "  user_b TEXT NOT NULL," +
            "  status TEXT NOT NULL," +
            "  created_at TEXT NOT NULL," +
            "  PRIMARY KEY (user_a, user_b)," +
            "  FOREIGN KEY (user_a) REFERENCES users(username)," +
            "  FOREIGN KEY (user_b) REFERENCES users(username)" +
            ")"
        );

        // notifications shown on the bell
        st.execute(
            "CREATE TABLE IF NOT EXISTS notifications (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  username TEXT NOT NULL," +
            "  type TEXT NOT NULL," +
            "  text TEXT NOT NULL," +
            "  created_at TEXT NOT NULL," +
            "  is_read INTEGER NOT NULL DEFAULT 0," +
            "  FOREIGN KEY (username) REFERENCES users(username)" +
            ")"
        );

        // 1 on 1 direct messages
        st.execute(
            "CREATE TABLE IF NOT EXISTS messages (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  sender TEXT NOT NULL," +
            "  receiver TEXT NOT NULL," +
            "  body TEXT NOT NULL," +
            "  created_at TEXT NOT NULL," +
            "  is_read INTEGER NOT NULL DEFAULT 0" +
            ")"
        );

        st.close();
    }

    private Db() {
    }
}
