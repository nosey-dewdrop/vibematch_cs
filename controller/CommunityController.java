package controller;

import model.User;
import model.Community;
import model.RecommendationEngine;
import java.util.List;

/*
 * CONTROLLER -- CommunityController
 * handles: community recommendation + browsing. per Table 5 this is
 * Mete's classes (RecommendationEngine, Community).
 *
 * status: SCAFFOLD ONLY, NOT wired into the view/ package. HomeFeedPanel.java
 * and DiscoverPanel.java in view/ currently sort a hardcoded list with a fake
 * Math.random() match percent -- this controller + RecommendationEngine is
 * meant to eventually replace that with the real thing.
 */
public class CommunityController {

    RecommendationEngine recommendationEngine = new RecommendationEngine();

    public List<Community> getRecommendedFeed(User user){
        // TODO: return recommendationEngine.recommendCommunities(user);
        return null;
    }

    public void joinCommunity(User user, Community community){
        // TODO: community.addMember(user);
    }

    public void leaveCommunity(User user, Community community){
        // TODO: community.removeMember(user);
    }

}
