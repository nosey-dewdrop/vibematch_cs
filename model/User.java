package model;

/*
 * MODEL -- User
 * owner: Ahmed Khalil Salim   (see Table 5, Detailed Design Report v2)
 *
 * "The verified student account and profile that everything else in the
 * app is built around." (report section 4.1)
 *
 * UML notes (fig 2):
 *   - composes exactly 1 PersonalityResult (filled diamond)
 *   - optionally aggregates 1 SpotifyProfile (open diamond, can exist without a User)
 *   - tagged with 0..* Tag (association)
 *   - many-to-many association with Community through membership
 *   - VerificationService "verifies" User, RecommendationEngine "reads" User (dashed = dependency)
 *
 * status: SCAFFOLD ONLY. fields + method signatures match the report, bodies are TODO.
 * this class does not need to compile against a real database yet, just needs
 * to exist with the right shape so ChatController/AuthController etc can be written against it.
 */
public class User {

    String userId;
    String email;
    String passwordHash;
    boolean isVerified;
    String displayName;
    String bio;

    // stays inactive until VerificationService.sendVerificationEmail() has been
    // confirmed through verifyToken() -- see VerificationService.java
    public void register(){
        // TODO: create the account row, hash the password, kick off VerificationService.sendVerificationEmail()
    }

    public boolean verifyEmail(String token){
        // TODO: call VerificationService.verifyToken(token) and flip isVerified to true if it checks out
        return false;
    }

    public boolean login(String email, String password){
        // TODO: look up user by email, compare password against passwordHash (hashed, never plain text!!)
        return false;
    }

    public void updateProfile(String bio){
        // TODO: validate + save. probably just this.bio = bio; plus a DB write once we have one
    }

}
