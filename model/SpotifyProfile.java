package model;

import java.util.List;
import java.util.ArrayList;

/*
 * MODEL -- SpotifyProfile
 * owner: Ahmed Khalil Salim   (Table 5)
 *
 * "Holds a student's optional top genres/artists pulled from Spotify." (4.2)
 * filled in only if a student chooses to connect(authCode) -- its an optional
 * add-on, not a requirement, so isConnected should default to false.
 * UML: optionally aggregated by User (open diamond, 0..1), RecommendationEngine "reads" it.
 *
 * status: SCAFFOLD ONLY. the real version of this needs actual Spotify OAuth (see
 * design risks table -- "Spotify OAuth complexity" is literally called out as a risk).
 */
public class SpotifyProfile {

    List<String> topGenres = new ArrayList<>();
    List<String> topArtists = new ArrayList<>();
    boolean isConnected = false;

    public void connect(String authCode){
        // TODO: this is the big one -- actual OAuth handshake with the Spotify Web API.
        // set isConnected = true once it actually works
    }

    public List<String> fetchTopGenres(){
        // TODO: hit the spotify api, for now just returns whatever we already have (probably empty)
        return topGenres;
    }

    public List<String> fetchTopArtists(){
        // TODO: same deal as fetchTopGenres()
        return topArtists;
    }

}
