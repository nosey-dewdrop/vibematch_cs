package controller;

import model.User;
import model.PersonalityTest;
import model.PersonalityResult;
import model.SpotifyProfile;
import model.Tag;
import java.util.List;

/*
 * CONTROLLER -- ProfileController
 * handles: personality test, profile builder, spotify connect, interest tags.
 * per Table 5 this covers Yara's classes (PersonalityTest, PersonalityResult, Tag)
 * plus Ahmed's SpotifyProfile.
 *
 * status: SCAFFOLD ONLY, NOT wired into the view/ package. InterestSelectionPanel.java,
 * PersonalityQuizPanel.java and VibeProfilePanel.java in view/ currently just fake
 * this whole flow (random archetype picker etc) -- this is what would eventually replace that.
 */
public class ProfileController {

    public PersonalityResult submitQuiz(PersonalityTest test, List<String> answers){
        // TODO: test.submitAnswers(answers); return test.calculateResult();
        return null;
    }

    public void connectSpotify(SpotifyProfile profile, String authCode){
        // TODO: profile.connect(authCode);
    }

    public void addTag(User user, String tagName){
        // TODO: make a new Tag(tagName), attach it to the user somehow
    }

}
