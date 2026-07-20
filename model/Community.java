package model;

import java.util.ArrayList;

/*
 * A community / club people can join. The tags are interest tags we use to
 * match communities to a users interests. memberCount and matchPercent are
 * filled in when we show it on a screen, they are not columns in the table.
 */
public class Community {

    private int id;
    private String name;
    private String description;
    private String category;
    private String emoji;
    private String coverColor;   // hex string, picked when seeding
    private String createdBy;
    private String createdAt;

    private ArrayList<String> tags = new ArrayList<String>();

    private int memberCount;     // computed
    private int matchPercent;    // computed by MatchService
    private boolean member;      // is the current user a member (computed per request)

    public Community() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }

    public String getCoverColor() { return coverColor; }
    public void setCoverColor(String coverColor) { this.coverColor = coverColor; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public ArrayList<String> getTags() { return tags; }
    public void setTags(ArrayList<String> tags) { this.tags = tags; }

    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }

    public int getMatchPercent() { return matchPercent; }
    public void setMatchPercent(int matchPercent) { this.matchPercent = matchPercent; }

    public boolean isMember() { return member; }
    public void setMember(boolean member) { this.member = member; }
}
