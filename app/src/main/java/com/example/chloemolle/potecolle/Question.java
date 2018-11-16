package com.example.chloemolle.potecolle;

import android.graphics.Bitmap;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by chloemolle on 28/10/2018.
 */

public class Question {
    private Object question;
    private Object reponse;
    private Object type;
    private String image;
    private Bitmap bmp;
    private ArrayList<String> propositions = new ArrayList<>();

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


    public Object getReponse() {
        return reponse;
    }

    public Object getQuestion() {
        return question;
    }

    public Object getType() {
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
}
