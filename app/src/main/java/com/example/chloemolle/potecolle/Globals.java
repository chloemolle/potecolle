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

    public void addQuestionToGame(Map<String, Object> question) {
        this.currentGame.addQuestions(question);
    }


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
