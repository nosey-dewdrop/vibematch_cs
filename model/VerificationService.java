package model;

import java.util.Map;
import java.util.HashMap;
import java.util.Random;

/*
 * MODEL -- VerificationService
 * owner: Ahmed Khalil Salim   (Table 5)
 *
 * "Sends and checks the Bilkent email verification link that activates a User." (4.1)
 * UML: dashed arrow "verifies" -> User (dependency, doesnt own the User)
 *
 * status: real now (see login_functionality.md), minus the actual emailing part --
 * we dont have the Bilkent email service from the architecture section hooked up
 * yet, so the code just gets printed to the console instead of mailed out lol.
 * good enough to test the whole flow yourself without needing a real mail server.
 */
public class VerificationService {

    // token -> the user that token belongs to, until its confirmed
    Map<String, User> pendingTokens = new HashMap<>();

    public void sendVerificationEmail(User user){
        String code = generateCode();
        pendingTokens.put(code, user);
        // "sending the email" -- really just printing it, see the status note up top
        System.out.println("[VerificationService] verification code for " + user.email + ": " + code);
    }

    public boolean verifyToken(String token){
        User user = pendingTokens.get(token);
        if (user == null){
            return false; // no pending token matches, either its wrong or already used
        }
        user.isVerified = true;
        pendingTokens.remove(token);
        return true;
    }

    // 4 digits since thats what the code entry screen expects (4 boxes, EmailVerificationPanel)
    private String generateCode(){
        Random random = new Random();
        int code = 1000 + random.nextInt(9000);
        return String.valueOf(code);
    }

}
