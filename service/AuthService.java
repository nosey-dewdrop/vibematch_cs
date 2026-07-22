package service;

import app.AppConstants;
import data.UserDao;
import model.User;
import util.PasswordUtil;
import util.Validation;

import java.security.SecureRandom;

/*
 * AuthService is the layer between the login / register screens and the
 * database. It does the validation, uniqueness checks, hashing and so on, so
 * the screens dont have to know any of that.
 *
 * When something is wrong we throw IllegalArgumentException with a message that
 * is safe to show the user.
 */
public class AuthService {

    private UserDao userDao = new UserDao();
    private static final SecureRandom RANDOM = new SecureRandom();

    /*
     * Create a new (unverified) account. Returns the created user. The caller
     * still has to send + check the email code before the account is usable.
     */
    public User register(String displayName, String username, String email, String password) {
        if (displayName == null || displayName.trim().isEmpty()) {
            throw new IllegalArgumentException("Please enter your name.");
        }

        String usernameError = Validation.checkUsername(username);
        if (usernameError != null) {
            throw new IllegalArgumentException(usernameError);
        }
        String emailError = Validation.checkEmail(email);
        if (emailError != null) {
            throw new IllegalArgumentException(emailError);
        }
        String passwordError = Validation.checkPassword(password);
        if (passwordError != null) {
            throw new IllegalArgumentException(passwordError);
        }

        username = username.trim();
        email = email.trim().toLowerCase();

        if (userDao.usernameExists(username)) {
            throw new IllegalArgumentException("That username is already taken.");
        }
        if (userDao.emailExists(email)) {
            throw new IllegalArgumentException("There is already an account with this email.");
        }

        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hash(password, salt);

        User u = new User(username, displayName.trim(), email);
        u.setSalt(salt);
        u.setPassHash(hash);
        u.setVerified(false);

        userDao.insert(u);
        return u;
    }

    /*
     * Log in with either the username or the email plus the password. Returns
     * the user on success, throws otherwise.
     */
    public User login(String usernameOrEmail, String password) {
        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Please enter your username or email.");
        }
        usernameOrEmail = usernameOrEmail.trim();

        User u;
        if (usernameOrEmail.indexOf('@') >= 0) {
            u = userDao.findByEmail(usernameOrEmail.toLowerCase());
        } else {
            u = userDao.findByUsername(usernameOrEmail);
        }

        if (u == null) {
            throw new IllegalArgumentException("No account found with that username or email.");
        }
        if (!PasswordUtil.verify(password, u.getSalt(), u.getPassHash())) {
            throw new IllegalArgumentException("Wrong password, try again.");
        }
        if (!u.isVerified()) {
            throw new IllegalArgumentException("Please verify your email first.");
        }
        // if this account is still on the old hashing scheme, quietly upgrade it
        // now that we have the plaintext password in hand
        if (PasswordUtil.needsUpgrade(u.getPassHash())) {
            String newSalt = PasswordUtil.generateSalt();
            String newHash = PasswordUtil.hash(password, newSalt);
            userDao.updatePassword(u.getUsername(), newHash, newSalt);
            u.setSalt(newSalt);
            u.setPassHash(newHash);
        }
        return u;
    }

    public void markVerified(String username) {
        userDao.setVerified(username, true);
    }

    // a fresh 6 digit code for email verification
    public String generateVerificationCode() {
        int max = 1;
        for (int i = 0; i < AppConstants.VERIFICATION_CODE_LENGTH; i++) {
            max = max * 10;
        }
        int number = RANDOM.nextInt(max);
        // pad with leading zeros so it is always the right length
        String code = Integer.toString(number);
        while (code.length() < AppConstants.VERIFICATION_CODE_LENGTH) {
            code = "0" + code;
        }
        return code;
    }
}
