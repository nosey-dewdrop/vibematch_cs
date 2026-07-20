package model;

/*
 * Links a user to a community they joined. Simple join table row really, but
 * having a class for it keeps the dao tidy.
 */
public class Membership {

    private String username;
    private int communityId;
    private String joinedAt;

    public Membership(String username, int communityId, String joinedAt) {
        this.username = username;
        this.communityId = communityId;
        this.joinedAt = joinedAt;
    }

    public String getUsername() { return username; }
    public int getCommunityId() { return communityId; }
    public String getJoinedAt() { return joinedAt; }
}
