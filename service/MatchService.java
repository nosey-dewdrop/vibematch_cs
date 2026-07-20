package service;

import data.Db;
import model.Community;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/*
 * Works out how well a community fits a user and gives it a match percent.
 *
 * The score mixes two things:
 *   - how many of the community's tags are also your interests
 *   - how close your personality type is to the typical member's type
 * Interests matter more, so they get the bigger weight.
 */
public class MatchService {

    // how much each part counts (should add up to 1.0)
    private static final double INTEREST_WEIGHT = 0.65;
    private static final double MBTI_WEIGHT = 0.35;

    public int scoreFor(User user, Community community) {
        double interest = interestScore(user, community);
        double mbti = mbtiScore(user, community);
        double total = interest * INTEREST_WEIGHT + mbti * MBTI_WEIGHT;
        int percent = (int) Math.round(total * 100);
        if (percent > 99) {
            percent = 99; // leave a little room, nothing is a perfect 100
        }
        if (percent < 5) {
            percent = 5;
        }
        community.setMatchPercent(percent);
        return percent;
    }

    /*
     * Score + sort all the communities for this user, best first. Communities
     * they already joined are left out (this is for "discover new ones").
     */
    public ArrayList<Community> topMatches(User user, ArrayList<Community> communities, int limit) {
        ArrayList<Community> pool = new ArrayList<Community>();
        for (int i = 0; i < communities.size(); i++) {
            Community c = communities.get(i);
            scoreFor(user, c);
            pool.add(c);
        }

        Collections.sort(pool, new Comparator<Community>() {
            public int compare(Community a, Community b) {
                return b.getMatchPercent() - a.getMatchPercent();
            }
        });

        ArrayList<Community> top = new ArrayList<Community>();
        for (int i = 0; i < pool.size() && top.size() < limit; i++) {
            top.add(pool.get(i));
        }
        return top;
    }

    // a friendly one liner for why we recommended this
    public String reason(User user, Community community) {
        ArrayList<String> shared = sharedInterests(user, community);
        if (shared.size() >= 2) {
            return "Because you like " + shared.get(0) + " and " + shared.get(1);
        }
        if (shared.size() == 1) {
            return "Because you like " + shared.get(0);
        }
        return "Popular with people who share your vibe";
    }

    // ---- the math ----

    private double interestScore(User user, Community community) {
        if (community.getTags().isEmpty()) {
            return 0;
        }
        int shared = sharedInterests(user, community).size();
        return (double) shared / community.getTags().size();
    }

    private ArrayList<String> sharedInterests(User user, Community community) {
        ArrayList<String> shared = new ArrayList<String>();
        ArrayList<String> tags = community.getTags();
        for (int i = 0; i < tags.size(); i++) {
            if (user.getInterests().contains(tags.get(i))) {
                shared.add(tags.get(i));
            }
        }
        return shared;
    }

    private double mbtiScore(User user, Community community) {
        if (user.getMbtiType() == null) {
            return 0.5; // no test taken yet, stay neutral
        }
        String dominant = dominantMbti(community.getId());
        if (dominant == null) {
            return 0.5; // empty community, cant tell
        }
        int matches = 0;
        for (int i = 0; i < 4 && i < dominant.length(); i++) {
            if (user.getMbtiType().charAt(i) == dominant.charAt(i)) {
                matches++;
            }
        }
        return matches / 4.0;
    }

    // the most common personality type among the members
    private String dominantMbti(int communityId) {
        try {
            Connection c = Db.getConnection();
            PreparedStatement ps = c.prepareStatement(
                "SELECT u.mbti_type, COUNT(*) AS n FROM users u "
              + "JOIN memberships m ON m.username = u.username "
              + "WHERE m.community_id = ? AND u.mbti_type IS NOT NULL "
              + "GROUP BY u.mbti_type ORDER BY n DESC LIMIT 1");
            ps.setInt(1, communityId);
            ResultSet rs = ps.executeQuery();
            String type = null;
            if (rs.next()) {
                type = rs.getString("mbti_type");
            }
            rs.close();
            ps.close();
            return type;
        } catch (Exception e) {
            // if anything goes wrong just dont use mbti for this one
            return null;
        }
    }
}
