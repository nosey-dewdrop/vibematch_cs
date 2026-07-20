package model;

import java.util.List;
import java.util.ArrayList;

/*
 * MODEL -- PersonalityTest
 * owner: Yara Bilal Fuad Dalia
 *
 * "Runs the onboarding questionnaire and turns answers into a result." (4.2)
 * UML: produces exactly 1 PersonalityResult (dashed arrow, "produces")
 */

public class PersonalityTest {

    private List<String> questions;
    private List<String> answers;

    public PersonalityTest(){

        questions = new ArrayList<>();
        answers = new ArrayList<>();

        // only 3 questions for now
        questions.add("I feel energized after spending time with a big group of people");
        questions.add("I prefer planning things out instead of going with the flow");
        questions.add("I would rather explore a new place than revisit a favorite one");

    }

    public List<String> getQuestions(){

        return questions;

    }

    public void submitAnswers(List<String> answers){

        this.answers = answers;

    }

    public PersonalityResult calculateResult(){

        int score = 0;

        // agree = +1
        // neutral = 0
        // disagree = -1

        for(int i = 0; i < answers.size(); i++){

            String ans = answers.get(i);

            if(ans.equalsIgnoreCase("agree")){

                score++;

            }

            else if(ans.equalsIgnoreCase("disagree")){

                score--;

            }

        }

        if(score >= 2){

            return new PersonalityResult(
                    "The Explorer",
                    "Curious, outgoing and always excited to try something new."
            );

        }

        else if(score == 1){

            return new PersonalityResult(
                    "The Spark",
                    "Energetic and optimistic. You enjoy bringing people together."
            );

        }

        else if(score == 0){

            return new PersonalityResult(
                    "The Harmonizer",
                    "Balanced, supportive and easy to get along with."
            );

        }

        else{

            return new PersonalityResult(
                    "The Thinker",
                    "Thoughtful, analytical and happiest when solving interesting problems."
            );

        }

    }

}