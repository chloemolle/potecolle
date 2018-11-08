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
    private ArrayList<HashMap<String,String>> friends;
    private ArrayList<String> partiesEnCours;


    public User() {};

    public String getClasse() {
        return classe;
    }

    public ArrayList<HashMap<String, String>> getFriends() {
        return friends;
    }

    public String getUsername() {
        return username;
    }

    public ArrayList<String> getPartiesEnCours() {
        return partiesEnCours;
    }
}
