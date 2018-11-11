package com.example.chloemolle.potecolle;

import android.app.Application;

import java.util.Map;

/**
 * Created by chloemolle on 19/10/2018.
 */

public class Globals extends Application {

    private Game currentGame;
    private User user;
    private Integer currentQuestionNumero = 0;
    private String brouillonText = "";
    private String reponseText = "";
    private Boolean debug = true;

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
}
