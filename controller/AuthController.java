package controller;

import model.User;
import model.VerificationService;
import java.util.Map;
import java.util.HashMap;

/*
 * CONTROLLER -- AuthController
 * "Sits between View and Model: takes a request from a View, calls the right
 * Model methods, and hands back the result." (3.3)
 *
 * handles: account creation, Bilkent email verification, login. per Table 5
 * this lines up with Ahmed Khalil Salim's classes (User, VerificationService).
 *
 * status: real now, wired into LoginSignupPanel.java / EmailVerificationPanel.java
 * (see login_functionality.md for the full writeup). still in-memory only tho --
 * usersByEmail below is just standing in for the database until that gets built.
 */
public class AuthController {

    VerificationService verificationService = new VerificationService();
    Map<String, User> usersByEmail = new HashMap<>();

    public boolean handleSignup(String email, String password){
        if (usersByEmail.containsKey(email)){
            return false; // someone already signed up with that email, no dupes
        }
        User user = new User(email, password);
        user.register();
        usersByEmail.put(email, user);
        verificationService.sendVerificationEmail(user);
        return true;
    }

    public boolean handleLogin(String email, String password){
        User user = usersByEmail.get(email);
        if (user == null){
            return false; // no account at all with that email
        }
        return user.login(email, password);
    }

    public boolean handleVerify(String token){
        return verificationService.verifyToken(token);
    }

    // not one of the report's 3 handler methods, but the view needs this -- e.g.
    // to check isVerified() before letting someone log in, or to resend a code.
    // keeps usersByEmail itself private so the view cant poke around in it directly
    public User getUser(String email){
        return usersByEmail.get(email);
    }

    public boolean resendVerification(String email){
        User user = usersByEmail.get(email);
        if (user == null){
            return false;
        }
        verificationService.sendVerificationEmail(user);
        return true;
    }

}
