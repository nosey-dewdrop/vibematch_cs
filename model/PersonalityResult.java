package model;

/*
 * MODEL -- PersonalityResult
 * owner: Yara Bilal Fuad Dalia   (Table 5)
 *
 * "Stores the outcome of the personality test for use in matching." (4.2)
 * UML: composed by exactly 1 User (filled diamond -- a PersonalityResult cant
 * exist without its User), produced by PersonalityTest.
 *
 * status: SCAFFOLD ONLY. this one's simple enough that its basically just a data holder.
 */
public class PersonalityResult {

    String resultType;
    String description;

    public String getResultType(){
        // TODO: probably just "return resultType;" once the field actually gets set somewhere
        return resultType;
    }

}
