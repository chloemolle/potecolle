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
    private ArrayList<Game> partiesEnCours = new ArrayList<>();
    private ArrayList<FriendRequest> friendRequests = new ArrayList<>();
    private Integer level;
    private double pointsActuels;

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

    public ArrayList<Game> getPartiesEnCours() {
        return partiesEnCours;
    }

    public void setPartiesEnCours(ArrayList<Game> partiesEnCours) {
        this.partiesEnCours = partiesEnCours;
    }

    public ArrayList<FriendRequest> getFriendRequests() {
        return friendRequests;
    }

    public void setFriendRequests(ArrayList<FriendRequest> friendRequests) {
        this.friendRequests = friendRequests;
    }

    public Integer getLevel() {
        return level;
    }

    public double getPointsActuels() {
        return pointsActuels;
    }

    public void addPoints(Integer points) {
        double nextFloor = this.getFormule();
        double newPoints = getPointsActuels() + points;
        while (newPoints >= nextFloor) {
            newPoints = newPoints - nextFloor;
            level ++;
            nextFloor = this.getFormule();
        }
        pointsActuels = newPoints;
        return;
    }

    public double getFormule() {
        return 250 * Math.pow(2, this.getLevel() - 1);
    }

    public void setClasse(String classe) {
        this.classe = classe;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
