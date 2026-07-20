package util;

import app.AppConstants;

/*
 * Small helpers to validate what the user types on the sign up / login forms.
 * Each method returns null if the value is fine, or an error message we can
 * show the user if its not. Returning the message keeps the screen code simple.
 */
public class Validation {

    public static String checkUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return "Please enter a username.";
        }
        username = username.trim();
        if (username.length() < AppConstants.USERNAME_MIN) {
            return "Username is too short (min " + AppConstants.USERNAME_MIN + " characters).";
        }
        if (username.length() > AppConstants.USERNAME_MAX) {
            return "Username is too long.";
        }
        // only letters, numbers and underscore
        for (int i = 0; i < username.length(); i++) {
            char c = username.charAt(i);
            boolean ok = Character.isLetterOrDigit(c) || c == '_';
            if (!ok) {
                return "Username can only have letters, numbers and _";
            }
        }
        return null;
    }

    // must be a bilkent address, we check it ends with one of the allowed domains
    public static String checkEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "Please enter your Bilkent email.";
        }
        email = email.trim().toLowerCase();
        if (email.indexOf('@') < 0) {
            return "That doesn't look like an email.";
        }
        boolean allowed = false;
        for (int i = 0; i < AppConstants.ALLOWED_EMAIL_DOMAINS.length; i++) {
            if (email.endsWith(AppConstants.ALLOWED_EMAIL_DOMAINS[i])) {
                allowed = true;
                break;
            }
        }
        if (!allowed) {
            return "Only Bilkent emails can sign up (@bilkent.edu.tr).";
        }
        return null;
    }

    public static String checkPassword(String password) {
        if (password == null || password.isEmpty()) {
            return "Please enter a password.";
        }
        if (password.length() < AppConstants.PASSWORD_MIN) {
            return "Password must be at least " + AppConstants.PASSWORD_MIN + " characters.";
        }
        return null;
    }

    private Validation() {
    }
}
