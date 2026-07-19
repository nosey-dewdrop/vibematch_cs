package model;

import java.util.List;
import java.util.ArrayList;

/*
 * MODEL -- PersonalityTest
 * owner: Yara Bilal Fuad Dalia   (Table 5)
 *
 * "Runs the onboarding questionnaire and turns answers into a result." (4.2)
 * UML: produces exactly 1 PersonalityResult (dashed arrow, "produces")
 *
 * status: SCAFFOLD ONLY.
 * note: the report's UML box literally says "questions : List<Question>" but Question
 * isnt one of the core Model classes in Table 3, so its never actually defined anywhere.
 * left it as List<String> for now (just the question text) -- if this needs multiple
 * choice options per question we probably need a real Question class, TBD.
 */
public class PersonalityTest {

    List<String> questions = new ArrayList<>();

    public void submitAnswers(List<String> answers){
        // TODO: store the answers somewhere so calculateResult() can use them
    }

    public PersonalityResult calculateResult(){
        // TODO: actual scoring logic goes here (this is basically the whole
        // personality quiz -> archetype pipeline). returning null for now so it compiles
        return null;
    }

}
