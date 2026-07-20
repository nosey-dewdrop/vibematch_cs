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
    private int energy;
    private int social;
    private int creativity;
    private int chill;

    public PersonalityResult(String resultType, String description, int energy, int social, int creativity,int chill){

        this.resultType = resultType;
        this.description = description;
        this.energy = energy;
        this.social = social;
        this.creativity = creativity;
        this.chill = chill;


    }

    public String getResultType(){

        return resultType;

    }

    public String getDescription(){

        return description;

    }
    public int getEnergy(){
        return energy;
    }

    public int getSocial(){
        return social;
    }

    public int getCreativity(){
        return creativity;
    }

    public int getChill(){
        return chill;
    }

    public void setResultType(String resultType){

        this.resultType = resultType;

    }

    public void setDescription(String description){

        this.description = description;

    }

}