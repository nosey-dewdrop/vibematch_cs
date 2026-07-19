package controller;

import model.User;
import model.VerificationService;

/*
 * CONTROLLER -- AuthController
 * "Sits between View and Model: takes a request from a View, calls the right
 * Model methods, and hands back the result." (3.3)
 *
 * handles: account creation, Bilkent email verification, login. per Table 5
 * this lines up with Ahmed Khalil Salim's classes (User, VerificationService).
 *
 * status: SCAFFOLD ONLY, and NOT wired into the view/ package yet. the current
 * click-through prototype's LoginSignupPanel.java / EmailVerificationPanel.java
 * do their own placeholder logic (just an @ug.bilkent.edu.tr string check) --
 * once this controller is for real, those panels should call into this instead.
 * left untouched for now since thats a bigger change than "add scaffolding".
 */
public class AuthController {

    VerificationService verificationService = new VerificationService();

    public boolean handleSignup(String email, String password){
        // TODO: make a new User, call user.register(), then verificationService.sendVerificationEmail(user)
        return false;
    }

    public boolean handleLogin(String email, String password){
        // TODO: look up the User and call user.login(email, password)
        return false;
    }

    public boolean handleVerify(String token){
        // TODO: verificationService.verifyToken(token)
        return false;
    }

}
