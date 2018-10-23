package com.example.chloemolle.potecolle;

/**
 * Created by chloemolle on 19/10/2018.
 */

public class Game {
    private String player1;
    private String player2;
    private String matiere;
    private String sujet;
    private String classe;

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
}
