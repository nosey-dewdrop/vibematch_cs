package controller;

import model.User;
import model.Community;
import model.RecommendationEngine;
import model.Tag;
import java.util.List;
import java.util.ArrayList;

/*
 * CONTROLLER -- CommunityController
 * handles: community recommendation + browsing. per Table 5 this is
 * Mete's classes (RecommendationEngine, Community).
 *
 * status: real now, but NOT wired into the view/ package yet -- HomeFeedPanel
 * and DiscoverPanel still run on the view.Community placeholder with the fake
 * Math.random() percent. swapping them over to this is the "bigger change"
 * Khalil's note in model/Community.java already flagged, so its left for the
 * integration step (7.1 puts matching+browsing before chat, after profile data).
 *
 * allCommunities below is in-memory only, standing in for the database exactly
 * like usersByEmail does in AuthController. same warning applies: restart and gone.
 */
public class CommunityController {

    RecommendationEngine recommendationEngine = new RecommendationEngine();
    List<Community> allCommunities = new ArrayList<>();

    public List<Community> getRecommendedFeed(User user){
        return recommendationEngine.recommendCommunities(user);
    }

    public boolean joinCommunity(User user, Community community){
        if (community == null){
            return false;
        }
        return community.addMember(user);
    }

    public boolean leaveCommunity(User user, Community community){
        if (community == null){
            return false;
        }
        return community.removeMember(user);
    }

    // creating communities is technically Administrator's job (4.5), but the
    // admin side needs somewhere to PUT them, and the community list lives in
    // this controller. so AdminController calls this after createCommunity() --
    // and it doubles as the seed method for demo data until the DB exists
    public void addCommunity(Community community){
        if (community == null || allCommunities.contains(community)){
            return;
        }
        allCommunities.add(community);
        // engine keeps the same list reference, so future adds show up there too
        recommendationEngine.setCommunities(allCommunities);
    }

    public boolean removeCommunity(Community community){
        return allCommunities.remove(community);
    }

    public List<Community> getAllCommunities(){
        return allCommunities;
    }

    // browse-by-interest, this is what DiscoverPanel's category shelves become
    // once the view switches off its placeholder class -- the real model has no
    // "category" field, a communitys tags ARE its categories
    public List<Community> getCommunitiesByTag(String tagName){
        List<Community> result = new ArrayList<>();
        if (tagName == null){
            return result;
        }
        for (Community community : allCommunities){
            for (Tag tag : community.getTags()){
                if (tag.getName() != null && tag.getName().equalsIgnoreCase(tagName)){
                    result.add(community);
                    break; // this ones in, no need to check its other tags
                }
            }
        }
        return result;
    }

    // trending = most members, top N -- DiscoverPanel's right-hand list.
    // sorts a copy so were not reordering allCommunities behind everyones back,
    // same bubble sort the panel itself uses for its trending list
    public List<Community> getTrending(int limit){
        List<Community> sorted = new ArrayList<>(allCommunities);
        for (int i = 0; i < sorted.size(); i++){
            for (int j = 0; j < sorted.size() - 1; j++){
                if (sorted.get(j).getMemberCount() < sorted.get(j + 1).getMemberCount()){
                    Community temp = sorted.get(j);
                    sorted.set(j, sorted.get(j + 1));
                    sorted.set(j + 1, temp);
                }
            }
        }
        while (sorted.size() > limit){
            sorted.remove(sorted.size() - 1);
        }
        return sorted;
    }

    // the panels need the engine too (match percents on cards, and profile
    // wiring calls setUserProfile through it) -- handing it out beats making
    // this controller forward every single engine method one by one
    public RecommendationEngine getRecommendationEngine(){
        return recommendationEngine;
    }

}
