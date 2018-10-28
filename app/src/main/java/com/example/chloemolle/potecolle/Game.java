package com.example.chloemolle.potecolle;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.firebase.ui.auth.AuthUI.TAG;

/**
 * Created by chloemolle on 19/10/2018.
 */

public class Game {
    private String player1;
    private String player2;
    private String matiere;
    private String sujet;
    private String classe;
    private String repondu;
    private ArrayList<Map<String, Object>> questions;
    private ArrayList<String> questionsId;
    private ArrayList<String> player1Answers = new ArrayList<>();
    private ArrayList<String> player2Answers = new ArrayList<>();
    private String score;
    private String scoreOpponent;

    public Game () {
    }

    public Game (String _player1, String _classe) {
        this.player1 = _player1;
        this.classe = _classe;
    }

    public Game (String _player1, String _player2, String _classe, String _matiere, String _sujet, String _question1Id, String _question2Id, String _question3Id, String _question4Id, String _question5Id, String _question1, String _question2, String _question3, String _question4, String _question5, String _reponse1, String _reponse2, String _reponse3, String _reponse4, String _reponse5, String _score, String _scoreOpponent) {
        this.player1 = _player1;
        this.player2 = _player2;
        this.classe = _classe;
        this.matiere = _matiere;
        this.sujet = _sujet;
        this.questions = new ArrayList<>();
        this.questionsId = new ArrayList<>();
        this.questionsId.add(_question1Id);
        this.questionsId.add(_question2Id);
        this.questionsId.add(_question3Id);
        this.questionsId.add(_question4Id);
        this.questionsId.add(_question5Id);

        Map<String, Object> question1 = new HashMap<>();
        question1.put("question", _question1);
        question1.put("reponse", _reponse1);
        this.questions.add(question1);
        Map<String, Object> question2 = new HashMap<>();
        question2.put("question", _question2);
        question2.put("reponse", _reponse2);
        this.questions.add(question2);
        Map<String, Object> question3 = new HashMap<>();
        question3.put("question", _question3);
        question3.put("reponse", _reponse3);
        this.questions.add(question3);
        Map<String, Object> question4 = new HashMap<>();
        question4.put("question", _question4);
        question4.put("reponse", _reponse4);
        this.questions.add(question4);
        Map<String, Object> question5 = new HashMap<>();
        question5.put("question", _question5);
        question5.put("reponse", _reponse5);
        this.questions.add(question5);

        this.score = _score;
        this.scoreOpponent = _scoreOpponent;

    }

    public Boolean isEqual(Game game) {
        Boolean bool = true;
        for (Integer i = 0; i < questionsId.size(); i ++) {
            bool &= (questionsId.get(i).equals(game.getQuestionsId().get(i)));
        }

        return matiere.equals(game.getMatiere()) && sujet.equals(game.getSujet()) && bool && ((player1.equals(game.getPlayer1()) && player2.equals(game.getPlayer2())) || (player2.equals(game.getPlayer1()) && player1.equals(game.getPlayer2())));
    }

    public Boolean isEqual(String _matiere, String _sujet, String _player1, String _player2, String question1, String question2, String question3, String question4, String question5) {
        Boolean bool = true;
        bool &= (this.questionsId.get(0).equals(question1));
        bool &= (this.questionsId.get(1).equals(question2));
        bool &= (this.questionsId.get(2).equals(question3));
        bool &= (this.questionsId.get(3).equals(question4));
        bool &= (this.questionsId.get(4).equals(question5));

        return this.matiere.equals(_matiere) && this.sujet.equals(_sujet) && bool && ((this.player1.equals(_player1) && this.player2.equals(_player2)) || (this.player2.equals(_player1) && this.player1.equals(_player2)));
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

    public void addQuestions(Map<String, Object> questions) {
        this.questions.add(questions);
    }


    public ArrayList<String> getPlayer1Answers() {
        return player1Answers;
    }

    public void setPlayer1Answers(ArrayList<String> player1Answers) {
        this.player1Answers = player1Answers;
    }

    public void addAnswerForPlayer1(String answer) {
        this.player1Answers.add(answer);
    }

    public ArrayList<String> getPlayer2Answers() {
        return player2Answers;
    }

    public void setPlayer2Answers(ArrayList<String> player2Answers) {
        this.player2Answers = player2Answers;
    }

    public ArrayList<String> getQuestionsId() {
        return questionsId;
    }

    public void setQuestionsId(ArrayList<String> questionsId) {
        this.questionsId = questionsId;
    }

    public String getRepondu() {
        return repondu;
    }

    public void setRepondu(String repondu) {
        this.repondu = repondu;
    }

    public String getScore() {
        return score;
    }

    public String getScoreOpponent() {
        return scoreOpponent;
    }
}
