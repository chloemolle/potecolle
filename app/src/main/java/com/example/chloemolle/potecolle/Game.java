package com.example.chloemolle.potecolle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chloemolle on 19/10/2018.
 */

public class Game {
    private String player1;
    private String player2;
    private String matiere;
    private String sujet;
    private String classe;
    private ArrayList<Map<String, Object>> questions;
    private ArrayList<String> player1Answers;
    private ArrayList<String> player2Answers;


    public Game (String _player1, String _classe) {
        this.player1 = _player1;
        this.classe = _classe;
    }

    public String getPlayer1() {
        return player1;
    }

    public void setPlayer1(String player1) {
        this.player1 = player1;
    }

    public String getPlayer2() {
        return player2;
    }

    public void setPlayer2(String player2) {
        this.player2 = player2;
    }

    public String getClasse() {
        return classe;
    }

    public void setClasse(String classe) {
        this.classe = classe;
    }

    public String getMatiere() {
        return matiere;
    }

    public void setMatiere(String matiere) {
        this.matiere = matiere;
    }

    public String getSujet() {
        return sujet;
    }

    public void setSujet(String sujet) {
        this.sujet = sujet;
    }

    public ArrayList<Map<String, Object>> getQuestions() {
        return questions;
    }

    public void setQuestions(ArrayList<Map<String, Object>> questions) {
        this.questions = questions;
    }

    public ArrayList<String> getPlayer1Answers() {
        return player1Answers;
    }

    public void setPlayer1Answers(ArrayList<String> player1Answers) {
        this.player1Answers = player1Answers;
    }

    public void addAnswerForPlayer1(String answer) {
        if (this.player1Answers.isEmpty()) {
            this.player1Answers = new ArrayList<>();
        }
        this.player1Answers.add(answer);
    }

    public ArrayList<String> getPlayer2Answers() {
        return player2Answers;
    }

    public void setPlayer2Answers(ArrayList<String> player2Answers) {
        this.player2Answers = player2Answers;
    }
}
