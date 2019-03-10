package potecolle.education.app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static com.firebase.ui.auth.AuthUI.TAG;

/**
 * Created by chloemolle on 19/10/2018.
 */

public class Game {
    private String player1;
    private String player2;
    private String adversaire;
    private String matiere;
    private String sujet;
    private String classe;
    private Boolean repondu;
    private Boolean fini;
    private Boolean revanche = false;
    private ArrayList<Question> questions= new ArrayList<>();
    private ArrayList<String> questionsId;
    private ArrayList<Integer> reponsesTemps = new ArrayList<>();
    private ArrayList<Integer> reponsesTempsOpponent = new ArrayList<>();
    private ArrayList<String> player1Answers = new ArrayList<>();
    private ArrayList<String> player2Answers = new ArrayList<>();
    private String score;
    private String id;
    private String scoreOpponent;
    private Boolean timed;
    private Boolean seul = false;
    private Boolean scoreVu = false;
    private Boolean vu = false;
    private String pathQuestion;

    public Game () {
    }


    public Game (String _player1, String _classe, String _matiere, Boolean _seul, Boolean _timed, String _id) {
        this.player1 = _player1;
        this.classe = _classe;
        this.matiere = _matiere;
        this.seul =_seul;
        this.timed = _timed;
        this.id = _id;
        this.setPathQuestion(this.classe + "/" + this.matiere);
    }


    public Game (String _player1, String _classe) {
        this.player1 = _player1;
        this.classe = _classe;
    }

    public Game (String _player1, String _player2, String _player2Email, String _classe, Boolean _revanche) {
        this.player1 = _player1;
        this.classe = _classe;
        this.player2 = _player2;
        this.adversaire = _player2Email;
        this.revanche = _revanche;
        this.setPathQuestion(this.classe);
    }

    public Game (String _player1, String _player2, String _classe, String _matiere, String _sujet) {
        this.player1 = _player1;
        this.player2 = _player2;
        this.sujet = _sujet;
        this.matiere = _matiere;
        this.classe = _classe;
        this.setPathQuestion(this.classe + "/" + this.matiere + "/" + this.sujet);
    }


    public Game (String _player1, String _player2, String _classe, String _matiere, String _sujet, String _question1Id, String _question2Id, String _question3Id, String _question4Id, String _question5Id, Question q1, Question q2, Question q3, Question q4, Question q5, String _score, String _scoreOpponent) {
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

        this.questions.add(q1);
        this.questions.add(q2);
        this.questions.add(q3);
        this.questions.add(q4);
        this.questions.add(q5);

        this.score = _score;
        this.scoreOpponent = _scoreOpponent;
        this.setPathQuestion(this.classe + "/" + this.matiere + "/" + this.sujet);

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

        bool &= (this.questionsId.indexOf(question1) != -1);
        bool &= (this.questionsId.indexOf(question2) != -1);
        bool &= (this.questionsId.indexOf(question3) != -1);
        bool &= (this.questionsId.indexOf(question4) != -1);
        bool &= (this.questionsId.indexOf(question5) != -1);

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
        this.setPathQuestion(this.classe + "/" + this.matiere + "/" + this.sujet);
    }

    public String getSujet() {
        return sujet;
    }

    public void setSujet(String sujet) {
        this.sujet = sujet;
        this.setPathQuestion(this.classe + "/" + this.matiere + "/" + this.sujet);
    }

    public ArrayList<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(ArrayList<Question> questions) {
        this.questions = questions;
    }

    public void addQuestions(Question question) {
        this.questions.add(question);
    }


    public ArrayList<String> getPlayer1Answers() {
        return player1Answers;
    }

    public void setPlayer1Answers(ArrayList<String> player1Answers) {
        this.player1Answers = player1Answers;
    }

    public void addAnswerForPlayer1(Integer index, String answer) {
        if (index < this.player1Answers.size()) {
            this.player1Answers.remove(this.player1Answers.get(index));
            this.player1Answers.add(index, answer);
        } else {
            this.player1Answers.add(answer);
        }
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

    public Boolean getRepondu() {
        return repondu;
    }

    public void setRepondu(Boolean repondu) {
        this.repondu = repondu;
    }

    public Boolean getFini() {
        return fini;
    }

    public void setFini(Boolean fini) {
        this.fini = fini;
    }

    public String getScore() {
        return score;
    }

    public String getScoreOpponent() {
        return scoreOpponent;
    }

    public String getAdversaire() {
        return adversaire;
    }

    public void setAdversaire(String adversaire) {
        this.adversaire = adversaire;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getTimed() {
        return timed;
    }

    public void setTimed(Boolean timed) {
        this.timed = timed;
    }

    public Boolean getSeul() {
        return seul;
    }

    public void setSeul(Boolean seul) {
        this.seul = seul;
    }

    public void setScoreVu(Boolean scoreVu) {
        this.scoreVu = scoreVu;
    }

    public Boolean getScoreVu() {
        return scoreVu;
    }

    public void setReponsesTemps(ArrayList<Integer> reponsesTemps) {
        this.reponsesTemps = reponsesTemps;
    }

    public ArrayList<Integer> getReponsesTemps() {
        return reponsesTemps;
    }

    public void addReponsesTemps(Integer reponseTemps) {
        if (this.timed) {
            this.reponsesTemps.add(reponseTemps);
        }
    }

    public void setReponsesTempsIndexScore(Integer index) {
        if (this.timed) {
            this.reponsesTemps.set(index, this.reponsesTemps.get(index) * 2);
        }
    }

    public void setReponsesTempsIndexScore0(Integer index) {
        if (this.timed) {
            this.reponsesTemps.set(index, 0);
        }
    }

    public ArrayList<Integer> getReponsesTempsOpponent() {
        return reponsesTempsOpponent;
    }

    public void setVu(Boolean vu) {
        this.vu = vu;
    }

    public Boolean getVu() {
        return vu;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public void setScoreOpponent(String scoreOpponent) {
        this.scoreOpponent = scoreOpponent;
    }

    public Boolean getRevanche() {
        return revanche;
    }

    public void setRevanche(Boolean revanche) {
        this.revanche = revanche;
    }

    public void createQuestions(Task<QuerySnapshot> task) {
        ArrayList<Question> questionsQuiz = new ArrayList<>();
        ArrayList<String> questionsQuizId = new ArrayList<>();
        Integer nbQuestionDisponible = task.getResult().size();
        ArrayList<Integer> questionToFetch = new ArrayList<>();

        Random r = new Random();
        //On récupère 5 index de questions à récupérer
        while (questionToFetch.size() != 10) {
            Integer tmp = r.nextInt(nbQuestionDisponible);
            if (questionToFetch.indexOf(tmp) == -1) {
                questionToFetch.add(tmp);
            }
        }
        List<DocumentSnapshot> documents = task.getResult().getDocuments();
        for (int i = 0; i < questionToFetch.size(); i ++) {
            DocumentSnapshot document = documents.get(questionToFetch.get(i));
            final Question question = document.toObject(Question.class);
            if (question.getType().contains("image")){
                getImage(question);
            }
            ArrayList<String> propositions = (ArrayList<String>) document.getData().get("propositions");

            if (propositions != null && propositions.size() > 0) {
                setPropositions(question, propositions);
            }

            questionsQuiz.add(question);
            questionsQuizId.add(document.getId());
        }
        this.setQuestions(questionsQuiz);
        this.setQuestionsId(questionsQuizId);
    }



    public void getImage(final Question question){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        // Create a storage reference from our app
        StorageReference storageRef = storage.getReference();
        StorageReference mountainImagesRef = storageRef.child(question.getImage());
        mountainImagesRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Use the bytes to display the image
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                question.setBmp(bmp);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }

    public void setPropositions(Question question, ArrayList<String> propositions) {
        ArrayList<String> propositionsShuffled = new ArrayList<>();

        while (propositions.size() != 0) {
            int indexProp = (int) Math.floor(Math.random() * propositions.size());
            propositionsShuffled.add(propositions.get(indexProp));
            propositions.remove(indexProp);
        }
        question.setPropositions(propositionsShuffled);
    }

    public void setGame(Globals globalVariables){
        final FirebaseUser userAuth = FirebaseAuth.getInstance().getCurrentUser();

        //set game for user
        setGameForOneUser(globalVariables.getUser().getId(), true, this.adversaire, this.player2);

        //set game for opponent
        setGameForOneUser(this.adversaire, false, globalVariables.getUser().getId(), globalVariables.getUser().getUsername());
    }

    public void setGameForOneUser(final String monMail, final Boolean vu, String adversaire, String player2) {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        HashMap<String,Object> newGame = new HashMap<>();
        newGame.put("timed", this.timed);
        newGame.put("adversaire", adversaire);
        newGame.put("player2", player2);
        newGame.put("classe", this.classe);
        newGame.put("matiere", this.matiere);
        newGame.put("sujet", this.sujet);
        newGame.put("fini", false);
        newGame.put("repondu", false);
        newGame.put("vu", vu);
        newGame.put("id", this.id);

        final Game currentGame = this;

        ArrayList<HashMap<String, Object>> questions = new ArrayList<>();

        for (Question question: currentGame.getQuestions()) {
            HashMap<String, Object> questionTmp = new HashMap<>();
            questionTmp.put("question", question.getQuestion());
            questionTmp.put("reponse", question.getReponse());
            questionTmp.put("type", question.getType());
            questionTmp.put("image", question.getImage());
            questionTmp.put("bmp", question.getBmp());
            questionTmp.put("temps", question.getTemps());
            questionTmp.put("propositions", question.getProposition());
            questionTmp.put("keyboardType", question.getKeyboardType());
            questions.add(questionTmp);
        }

        newGame.put("questionsId", currentGame.getQuestionsId());
        newGame.put("questions", questions);
        newGame.put("pathQuestion", currentGame.getPathQuestion());

        db.collection("Users")
                .document(monMail)
                .collection("Games")
                .document(this.id)
                .set(newGame)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        ArrayList<String> questionsId = currentGame.getQuestionsId();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });

    }
    //Permet de recréer une partie avec les mêmes paramètres de début
    public void flushGame() {
        this.repondu = false;
        this.fini = false;
        this.player1Answers = new ArrayList<>();
        this.player2Answers = new ArrayList<>();
        this.score = "0";
        this.id = "0";
        this.scoreOpponent = "0";
        this.scoreVu = false;
        this.vu = false;
    }


    public String getPathQuestion() {
        return pathQuestion;
    }

    public void setPathQuestion(String pathQuestion) {
        this.pathQuestion = pathQuestion;
    }

}