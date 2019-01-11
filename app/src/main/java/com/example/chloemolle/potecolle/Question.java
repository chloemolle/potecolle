package com.example.chloemolle.potecolle;

import android.graphics.Bitmap;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by chloemolle on 28/10/2018.
 */

public class Question {
    private String question;
    private String reponse;
    private String type;
    private String image;
    private Bitmap bmp;
    private Integer temps;
    private ArrayList<String> propositions = new ArrayList<>();
    private String keyboardType;

    public Question(){};

    public Question(String _question, String _reponse, String _type, ArrayList<String> _propositions){
        this.question = _question;
        this.reponse = _reponse;
        this.type = _type;
        this.propositions = _propositions;
    };

    public Question(String _question, String _reponse){
        this.question = _question;
        this.reponse = _reponse;
    };


    public String getReponse() {
        return reponse;
    }

    public String getQuestion() {
        return question;
    }

    public String getType() {
        return type;
    }

    public ArrayList<String> getProposition() {
        return propositions;
    }

    public void setPropositions(ArrayList<String> _propositions) {
        this.propositions = _propositions;
    }

    public String getImage() {
        return image;
    }

    public Bitmap getBmp() {
        return bmp;
    }

    public void setBmp(Bitmap bmp) {
        this.bmp = bmp;
    }

    public Integer getTemps() {
        return temps;
    }

    public String getKeyboardType() {
        return keyboardType;
    }
}
