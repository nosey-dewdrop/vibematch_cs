package model;

/*
 * A forum post inside a community. Comments live in their own table and are
 * loaded separately (see PostDao / CommentDao stuff).
 */
public class Post {

    private int id;
    private int communityId;
    private String author;
    private String title;
    private String body;
    private String createdAt;

    private int commentCount;   // computed when listing posts

    public Post() {
    }

    public Post(int communityId, String author, String title, String body) {
        this.communityId = communityId;
        this.author = author;
        this.title = title;
        this.body = body;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCommunityId() { return communityId; }
    public void setCommunityId(int communityId) { this.communityId = communityId; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }
}
