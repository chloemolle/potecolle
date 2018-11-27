package com.example.chloemolle.potecolle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chloemolle on 23/10/2018.
 */

public class User {

    private String classe;
    private String username;
    private ArrayList<String> friends;
    private ArrayList<String> partiesEnCours;
    private ArrayList<HashMap<String, String>> friendRequests = new ArrayList<>();

    public User() {};

    public String getClasse() {
        return classe;
    }

    public ArrayList<String> getFriends() {
        return friends;
    }

    public String getUsername() {
        return username;
    }

    public ArrayList<String> getPartiesEnCours() {
        return partiesEnCours;
    }

    public ArrayList<HashMap<String, String>> getFriendRequests() {
        return friendRequests;
    }

    public void setFriendRequests(ArrayList<HashMap<String, String>> friendRequests) {
        this.friendRequests = friendRequests;
    }
}
