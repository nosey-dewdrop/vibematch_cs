package ui;

import model.User;

/*
 * Holds who is currently logged in. The screens read Session.getUser() instead
 * of passing the user object around everywhere. Cleared on logout.
 */
public class Session {

    private static User currentUser;

    public static User getUser() {
        return currentUser;
    }

    public static void setUser(User user) {
        currentUser = user;
    }

    public static void clear() {
        currentUser = null;
    }

    private Session() {
    }
}
