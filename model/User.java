package model;

import java.util.ArrayList;

/*
 * A user of the app. Passwords are never stored in plain text, we keep the
 * hash and the salt that was used (see util.PasswordUtil).
 *
 * The interests list is filled in by the dao when we need it, it is not always
 * loaded.
 */
public class User {

    private String username;
    private String displayName;
    private String email;
    private String passHash;
    private String salt;
    private String bio;
    private boolean verified;
    private String mbtiType;     // like "INFP", null until they take the test
    private String createdAt;

    private ArrayList<String> interests = new ArrayList<String>();

    public User() {
    }

    public User(String username, String displayName, String email) {
        this.username = username;
        this.displayName = displayName;
        this.email = email;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassHash() { return passHash; }
    public void setPassHash(String passHash) { this.passHash = passHash; }

    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public String getMbtiType() { return mbtiType; }
    public void setMbtiType(String mbtiType) { this.mbtiType = mbtiType; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public ArrayList<String> getInterests() { return interests; }
    public void setInterests(ArrayList<String> interests) { this.interests = interests; }

    // handy for the test, did they finish onboarding (picked interests + did mbti)
    public boolean hasVibe() {
        return mbtiType != null && !interests.isEmpty();
    }
}
