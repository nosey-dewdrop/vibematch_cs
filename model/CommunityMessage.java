package model;

// One line in a community's guestbook: who wrote it, in which community, and
// what they said.
public class CommunityMessage {

    private int id;
    private int communityId;
    private String sender;
    private String body;
    private String createdAt;

    public CommunityMessage() {}

    public CommunityMessage(int communityId, String sender, String body) {
        this.communityId = communityId;
        this.sender = sender;
        this.body = body;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCommunityId() { return communityId; }
    public void setCommunityId(int communityId) { this.communityId = communityId; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
