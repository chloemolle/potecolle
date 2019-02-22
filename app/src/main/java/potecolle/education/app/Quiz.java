package potecolle.education.app;

/**
 * Created by chloemolle on 23/10/2018.
 */

public class Quiz {
    private String matiere;
    private String classe;
    private String sujet;
    private String question;
    private String reponse;

    public Quiz() {};

    public String getClasse() {
        return classe;
    }

    public String getSujet() {
        return sujet;
    }

    public String getMatiere() {
        return matiere;
    }

    public String getQuestion() {
        return question;
    }

    public String getReponse() {
        return reponse;
    }
}
