package com.example.chloemolle.potecolle;

import android.app.Application;

/**
 * Created by chloemolle on 19/10/2018.
 */

public class Globals extends Application {

    private Game currentGame;
    private String userName;
    private Integer currentQuestionNumero = 0;

    public Game getCurrentGame() {
        return currentGame;
    }

    public void setCurrentGame(Game currentGame) {
        this.currentGame = currentGame;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getCurrentQuestionNumero() {
        return currentQuestionNumero;
    }

    public void setCurrentQuestionNumero(Integer currentQuestionNumero) {
        this.currentQuestionNumero = currentQuestionNumero;
    }
}
