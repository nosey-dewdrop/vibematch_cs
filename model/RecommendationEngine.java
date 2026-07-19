package model;

import java.util.List;
import java.util.ArrayList;

/*
 * MODEL -- RecommendationEngine
 * owner: Mete Kemal Coskuner   (Table 5)
 *
 * "Scores communities against a student's personality, Spotify data, and
 * tags. Turns a student's PersonalityResult, SpotifyProfile, and Tags into
 * the ranked list of communities shown on their home feed." (4.4)
 * UML: dashed arrows "reads" -> User, "scores" -> Community (dependency, doesnt own either).
 *
 * status: SCAFFOLD ONLY.
 * see design risks -- "Cold-start recommendations": a brand new user only has
 * a personality result and no history, so weight tags + personality more
 * heavily at first per the report's mitigation plan.
 */
public class RecommendationEngine {

    double weightSpotify;
    double weightPersonality;
    double weightTags;

    public List<Community> recommendCommunities(User user){
        // TODO: loop over all communities, call calculateMatchScore() on each,
        // sort by score descending. this is basically what HomeFeedPanel fakes
        // with Math.random() right now in the view/ prototype -- this is meant
        // to replace that eventually
        return new ArrayList<>();
    }

    public double calculateMatchScore(User user, Community community){
        // TODO: weightSpotify * (spotify genre overlap) + weightPersonality * (...) + weightTags * (...)
        // the actual scoring formula isnt specified anywhere in the report, need to figure this out
        return 0.0;
    }

}
