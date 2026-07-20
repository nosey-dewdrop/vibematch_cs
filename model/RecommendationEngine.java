package model;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/*
 * MODEL -- RecommendationEngine
 * owner: Mete Kemal Coskuner   (Table 5)
 *
 * "Scores communities against a student's personality, Spotify data, and
 * tags. Turns a student's PersonalityResult, SpotifyProfile, and Tags into
 * the ranked list of communities shown on their home feed." (4.4)
 * UML: dashed arrows "reads" -> User, "scores" -> Community (dependency, doesnt own either).
 *
 * status: real now. the scoring formula wasnt specified anywhere in the report
 * (the old scaffold TODO literally said "need to figure this out"), so heres
 * what i went with -- every component is a 0..1 overlap score:
 *
 *   score = ( wTags * tagOverlap + wPersonality * archetypeMatch + wSpotify * genreOverlap )
 *           / (sum of the weights that actually had data behind them)
 *
 * dividing by the AVAILABLE weight instead of the total is on purpose: spotify
 * is optional per 4.2, so a student who never connected it can still hit 100%
 * instead of being permanently capped at 80%. per the cold-start risk in the
 * design risks table ("weight tags and personality result more heavily at
 * first"), the default weights below lean on tags + personality, not spotify.
 */
public class RecommendationEngine {

    double weightSpotify = 0.2;
    double weightPersonality = 0.4;
    double weightTags = 0.4;

    // the report signature recommendCommunities(user) only takes the user, so
    // the engine has to already know which communities even exist. controller
    // hands them in here -- once the DB layer is real this becomes a query
    List<Community> allCommunities = new ArrayList<>();

    // heads up, this map shouldnt exist long-term. per the UML a User composes
    // its PersonalityResult and aggregates its SpotifyProfile + Tags, but those
    // fields just arent on Khalil's User class yet -- thats his/Yara's side of
    // the fence and im not going to go edit their classes from my slice. so the
    // engine keeps its own userId -> profile-bits map as a stand-in (same move
    // as usersByEmail in AuthController standing in for the DB). whoever wires
    // onboarding calls setUserProfile() after the quiz/tag screens, and once
    // User grows the real fields this map gets deleted and calculateMatchScore()
    // just reads straight off the user.
    Map<String, UserProfileData> profilesByUserId = new HashMap<>();

    // tiny package-private holder for the map above, not a UML class on purpose
    static class UserProfileData {
        PersonalityResult personalityResult;
        SpotifyProfile spotifyProfile;
        List<Tag> tags = new ArrayList<>();
    }

    public void setCommunities(List<Community> communities){
        this.allCommunities = communities;
    }

    public void setUserProfile(User user, PersonalityResult result, SpotifyProfile spotify, List<Tag> tags){
        if (user == null || user.userId == null){
            return;
        }
        UserProfileData data = new UserProfileData();
        data.personalityResult = result;
        data.spotifyProfile = spotify;
        if (tags != null){
            data.tags = tags;
        }
        profilesByUserId.put(user.userId, data);
    }

    public List<Community> recommendCommunities(User user){
        // score every community, then sort highest first. this replaces the
        // Math.random() fake in view/HomeFeedPanel (sort_by_match_oder) -- the
        // panel keeps its own bubble sort for now, this is the real ranking
        List<Community> ranked = new ArrayList<>(allCommunities);

        // precompute so the sort comparator isnt re-scoring the same community
        // over and over (n log n comparisons, only n scores needed)
        Map<String, Double> scores = new HashMap<>();
        for (Community community : ranked){
            scores.put(community.communityId, calculateMatchScore(user, community));
        }

        ranked.sort((a, b) -> Double.compare(scores.get(b.communityId), scores.get(a.communityId)));
        return ranked;
    }

    public double calculateMatchScore(User user, Community community){
        if (user == null || user.userId == null || community == null){
            return 0.0;
        }
        UserProfileData data = profilesByUserId.get(user.userId);
        if (data == null){
            return 0.0; // onboarding never ran for this user, nothing to score with
        }

        double weightedSum = 0.0;
        double availableWeight = 0.0;

        // --- tags: how many of MY tags does this community share ---
        // normalized by the users tag count, not the communitys -- being
        // interested in 2 of a big communitys 10 topics shouldnt read as 20%
        // if those 2 are literally everything i tagged myself with
        if (data.tags != null && !data.tags.isEmpty()){
            int shared = 0;
            for (Tag userTag : data.tags){
                if (communityHasTag(community, userTag.name)){
                    shared++;
                }
            }
            weightedSum += weightTags * ((double) shared / data.tags.size());
            availableWeight += weightTags;
        }

        // --- personality: does this community suit my archetype ---
        // the report never defines how an archetype maps to a community, so the
        // convention is: a community that suits an archetype just carries that
        // archetype name as one of its tags (admin adds it at creation). cheap,
        // needs no extra class, and its all-or-nothing which is honest -- we
        // have no data to say a community "half suits" an Explorer
        if (data.personalityResult != null && data.personalityResult.resultType != null){
            if (communityHasTag(community, data.personalityResult.resultType)){
                weightedSum += weightPersonality;
            }
            availableWeight += weightPersonality;
        }

        // --- spotify: genre overlap, but ONLY if actually connected (4.2 --
        // "optional add-on, not a requirement"). not connected means this whole
        // component stays out of availableWeight, see the class comment
        if (data.spotifyProfile != null && data.spotifyProfile.isConnected){
            List<String> genres = data.spotifyProfile.fetchTopGenres();
            if (genres != null && !genres.isEmpty()){
                int shared = 0;
                for (String genre : genres){
                    if (communityHasTag(community, genre)){
                        shared++;
                    }
                }
                weightedSum += weightSpotify * ((double) shared / genres.size());
                availableWeight += weightSpotify;
            }
        }

        if (availableWeight == 0.0){
            return 0.0; // profile exists but its empty -- true cold start, let them browse manually
        }
        return weightedSum / availableWeight;
    }

    // not in the report, but the view cards show "87% match" as an int -- this
    // just saves every panel from doing its own *100-and-round dance
    public int getMatchPercent(User user, Community community){
        return (int) Math.round(calculateMatchScore(user, community) * 100);
    }

    private boolean communityHasTag(Community community, String name){
        if (name == null){
            return false;
        }
        String wanted = normalize(name);
        for (Tag tag : community.tags){
            if (normalize(tag.name).equals(wanted)){
                return true;
            }
        }
        return false;
    }

    // "#Board Games", "board games" and "boardgames" should all be the same tag.
    // same lowercase-and-strip idea the risks table suggests for the moderation
    // filter, just applied to matching instead of slur-catching
    private static String normalize(String s){
        if (s == null){
            return "";
        }
        return s.toLowerCase().replace("#", "").replace(" ", "").trim();
    }

}
