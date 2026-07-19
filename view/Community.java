package view;

import java.util.ArrayList;

// heads up -- this is NOT the real Model Community class from the UML diagram.
// this is just the dummy data holder the click-through prototype uses to fake a
// community card (match_percent etc are demo-only fields, not in the actual design).
// the real one lives at model/Community.java, different package so the names dont clash.
public class Community {

    int communityId;
    String name;
    String description;
    String category;
    int member_count;
    int match_percent = 0;
    ArrayList<Message> messages = new ArrayList<>();
    ArrayList<String> events = new ArrayList<>();

    Community(int communityId,String name,String description,String category,int member_count){
        this.communityId = communityId;
        this.name = name;
        this.description = description;
        this.category = category;
        this.member_count = member_count;
        //this.match_percent = match_percent;
    }

    public String get_name(){
        return this.name;
    }
    public String get_description(){
        return this.description;
    }
    public String get_category(){
        return this.category;
    }
    public int get_member_count(){
        return this.member_count;
    }
    public int get_match_percent(){
        return this.match_percent;
    }
    public void set_match_percent(int p){
        this.match_percent = p;
    }
    public ArrayList<Message> get_messages(){
        return this.messages;
    }
    public ArrayList<String> get_events(){
        return this.events;
    }



}
