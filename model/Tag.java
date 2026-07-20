package model;

/*
 * MODEL -- Tag
 * owner: Yara Bilal Fuad Dalia  
 *
 * "A single free-form interest tag such as #chess or #hiking." (4.2)
 * UML: User is tagged with 0..* Tag.
 *
 */
public class Tag {

    private String name;

    public Tag(String name){

        this.name = name;

    }

    public String getName(){

        return name;

    }

    public void setName(String name){

        this.name = name;

    }

}