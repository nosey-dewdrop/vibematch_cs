package model;

/*
 * One notification for a user: someone sent them a friend request, accepted
 * theirs, messaged them, or replied to their post. type is a short tag we can
 * use to pick an icon, text is the line we show.
 */
public class Notification {

    private int id;
    private String username;   // who this notification is for
    private String type;       // "friend_request", "friend_accepted", "message", "reply"
    private String text;
    private String createdAt;
    private boolean read;

    public Notification() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
}
