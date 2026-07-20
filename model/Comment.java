package model;

/*
 * A comment on a post. parentId of 0 means it is a top level comment, any
 * other value means it is a reply to the comment with that id. Thats how we
 * get the reddit style threading.
 */
public class Comment {

    private int id;
    private int postId;
    private String author;
    private String body;
    private int parentId;     // 0 = top level
    private String createdAt;

    public Comment() {
    }

    public Comment(int postId, String author, String body, int parentId) {
        this.postId = postId;
        this.author = author;
        this.body = body;
        this.parentId = parentId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPostId() { return postId; }
    public void setPostId(int postId) { this.postId = postId; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public int getParentId() { return parentId; }
    public void setParentId(int parentId) { this.parentId = parentId; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public boolean isTopLevel() {
        return parentId == 0;
    }
}
