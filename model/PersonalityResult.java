package model;

/*
 * MODEL -- PersonalityResult
 * owner: Yara Bilal Fuad Dalia 
 *
 * "Stores the outcome of the personality test for use in matching." (4.2)
 * UML: composed by exactly 1 User (filled diamond -- a PersonalityResult cant
 * exist without its User), produced by PersonalityTest.
 *
 */

public class PersonalityResult {

    private String resultType;
    private String description;

    public PersonalityResult(String resultType, String description){

        this.resultType = resultType;
        this.description = description;

    }

    public String getResultType(){

        return resultType;

    }

    public String getDescription(){

        return description;

    }

    public void setResultType(String resultType){

        this.resultType = resultType;

    }

    public void setDescription(String description){

        this.description = description;

    }

}