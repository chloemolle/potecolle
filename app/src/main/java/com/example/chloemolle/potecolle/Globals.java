package com.example.chloemolle.potecolle;

import android.app.Application;

import com.google.firebase.firestore.DocumentReference;

import java.util.Map;

/**
 * Created by chloemolle on 19/10/2018.
 */

public class Globals extends Application {

    private Game currentGame;
    private DocumentReference userDB;
    private User user;
    private Integer currentQuestionNumero = 0;
    private String brouillonText = "";
    private String reponseText = "";
    private Boolean debug = true;
    private int tmpTime = 0;
    private Long test = new Long(0);

    public Game getCurrentGame() {
        return currentGame;
    }

    public void setCurrentGame(Game currentGame) {
        this.currentGame = currentGame;
    }

    public Integer getCurrentQuestionNumero() {
        return currentQuestionNumero;
    }

    public void setCurrentQuestionNumero(Integer currentQuestionNumero) {
        this.currentQuestionNumero = currentQuestionNumero;
    }

    public void addQuestionToGame(Question question) {
        this.currentGame.addQuestions(question);
    }


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getBrouillonText() {
        return brouillonText;
    }

    public void setBrouillonText(String brouillonText) {
        this.brouillonText = brouillonText;
    }

    public String getReponseText() {
        return reponseText;
    }

    public void setReponseText(String reponseText) {
        this.reponseText = reponseText;
    }

    public Boolean getDebug() {
        return debug;
    }

    public void setDebug(Boolean debug) {
        this.debug = debug;
    }

    public int getTmpTime() {
        return tmpTime;
    }

    public void setTmpTime(int tmpTime) {
        this.tmpTime = tmpTime;
    }

    public Long getTest() {
        return test;
    }

    public void setTest(Long test) {
        this.test = test;
    }

    public DocumentReference getUserDB() {
        return userDB;
    }

    public void setUserDB(DocumentReference userDB) {
        this.userDB = userDB;
    }

}
