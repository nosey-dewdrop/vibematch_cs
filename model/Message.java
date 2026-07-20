package model;

/*
 * One direct message between two users. We just store sender, receiver and the
 * text. is_read is there for later if we want unread badges.
 */
public class Message {

    private int id;
    private String sender;
    private String receiver;
    private String body;
    private String createdAt;
    private boolean read;

    public Message() {
    }

    public Message(String sender, String receiver, String body) {
        this.sender = sender;
        this.receiver = receiver;
        this.body = body;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getReceiver() { return receiver; }
    public void setReceiver(String receiver) { this.receiver = receiver; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
}
