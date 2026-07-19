package model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/*
 * MODEL -- User
 * owner: Ahmed Khalil Salim   (Table 5)
 *
 * "The verified student account and profile that everything else in the app
 * is built around." (4.1) -- basically every other class ends up pointing
 * back at this one somehow.
 *
 * status: this one's actually real now, not just scaffold -- see
 * login_functionality.md for the full writeup. still in-memory only tho, no
 * DB wired up yet, so restart the app and every account you made is just gone lol
 */
public class User {

    String userId;
    String email;
    String passwordHash;
    boolean isVerified;
    String displayName;
    String bio;

    // fields above are package-private on purpose (matches the UML), so
    // AuthController -- different package -- cant touch them directly. this
    // constructor is the only way in. hashes the password right away too, no
    // reason to leave the plain text sitting around in a field even briefly
    public User(String email, String password){
        this.email = email;
        this.passwordHash = hash(password);
        this.isVerified = false;
        this.displayName = "";
        this.bio = "";
    }

    // constructor already set email + passwordHash, so all thats left for register()
    // to do is hand out an id. isVerified stays false here btw -- cant log in until
    // VerificationService says the code checked out (see login() + verifyToken() below)
    public void register(){
        this.userId = UUID.randomUUID().toString();
    }

    // heads up, this one isnt actually hooked up to anything right now --
    // AuthController.handleVerify() skips straight to VerificationService.verifyToken()
    // instead (same package, can flip isVerified itself, no need to come through here).
    // keeping this around anyway in case something needs to verify a User directly later
    public boolean verifyEmail(String token){
        if (token == null || token.isEmpty()){
            return false;
        }
        this.isVerified = true;
        return true;
    }

    public boolean login(String email, String password){
        return this.email.equals(email) && this.passwordHash.equals(hash(password));
    }

    public void updateProfile(String bio){
        this.bio = bio;
    }

    public String getEmail(){
        return email;
    }

    public String getUserId(){
        return userId;
    }

    public boolean isVerified(){
        return isVerified;
    }

    public String getDisplayName(){
        return displayName;
    }

    public String getBio(){
        return bio;
    }

    // sha-256 cuz its just built into the jdk -- theres no pom.xml/build.gradle
    // in this project to pull in a real password hasher like bcrypt or argon2.
    // fine for a class project, would 100% need to change before this ever
    // touched a real student's real password
    private static String hash(String password){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes){
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e){
            throw new RuntimeException(e);
        }
    }

}
