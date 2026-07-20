package server;

import model.User;

/*
 * Helpers for turning our model objects into something safe to send to a
 * client. The main job here is to never let a password hash or salt leave the
 * server, even though they live on the User object.
 */
public class Dto {

    // a copy of the user with the secret fields stripped out
    public static User safeUser(User u) {
        if (u == null) {
            return null;
        }
        User safe = new User(u.getUsername(), u.getDisplayName(), u.getEmail());
        safe.setBio(u.getBio());
        safe.setVerified(u.isVerified());
        safe.setMbtiType(u.getMbtiType());
        safe.setCreatedAt(u.getCreatedAt());
        safe.setInterests(u.getInterests());
        // pass hash and salt are deliberately left null
        return safe;
    }

    private Dto() {
    }
}
