package model;

import java.util.Map;
import java.util.HashMap;

/*
 * MODEL -- VerificationService
 * owner: Ahmed Khalil Salim   (Table 5)
 *
 * "Sends and checks the Bilkent email verification link that activates a User." (4.1)
 * UML: dashed arrow "verifies" -> User (dependency, doesnt own the User)
 *
 * status: SCAFFOLD ONLY, see User.java for the full note.
 */
public class VerificationService {

    // token -> the user that token belongs to, until its confirmed
    Map<String, User> pendingTokens = new HashMap<>();

    public void sendVerificationEmail(User user){
        // TODO: generate a token, stick it in pendingTokens, actually email it
        // (needs the Bilkent email service from the architecture section, dont have that yet)
    }

    public boolean verifyToken(String token){
        // TODO: check pendingTokens for this token, if found mark the User verified and remove it
        return false;
    }

}
