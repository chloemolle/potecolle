package potecolle.application.about.education;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.chloemolle.potecolle.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.github.kexanie.library.MathView;

import static com.firebase.ui.auth.AuthUI.TAG;

/**
 * Created by chloemolle on 24/10/2018.
 */

public class FinQuizPage extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.final_quiz_page_layout);


        final Globals globalVariables = (Globals) getApplicationContext();

        globalVariables.setCurrentQuestionNumero(0);
        ArrayList<String> player1Answers = globalVariables.getCurrentGame().getPlayer1Answers();

        Integer score = 0;

        score = processAnswer(0, score, R.id.linear_layout_reponse1, R.id.question1_text_change, R.id.reponse1_text_change, R.id.reponse1_text, R.id.solution1_text_change, R.id.layout_solution1);
        score = processAnswer(1, score, R.id.linear_layout_reponse2, R.id.question2_text_change, R.id.reponse2_text_change, R.id.reponse2_text, R.id.solution2_text_change, R.id.layout_solution2);
        score = processAnswer(2, score, R.id.linear_layout_reponse3, R.id.question3_text_change, R.id.reponse3_text_change, R.id.reponse3_text, R.id.solution3_text_change, R.id.layout_solution3);
        score = processAnswer(3, score, R.id.linear_layout_reponse4, R.id.question4_text_change, R.id.reponse4_text_change, R.id.reponse4_text, R.id.solution4_text_change, R.id.layout_solution4);
        score = processAnswer(4, score, R.id.linear_layout_reponse5, R.id.question5_text_change, R.id.reponse5_text_change, R.id.reponse5_text, R.id.solution5_text_change, R.id.layout_solution5);
        score = processAnswer(5, score, R.id.linear_layout_reponse6, R.id.question6_text_change, R.id.reponse6_text_change, R.id.reponse6_text, R.id.solution6_text_change, R.id.layout_solution6);
        score = processAnswer(6, score, R.id.linear_layout_reponse7, R.id.question7_text_change, R.id.reponse7_text_change, R.id.reponse7_text, R.id.solution7_text_change, R.id.layout_solution7);
        score = processAnswer(7, score, R.id.linear_layout_reponse8, R.id.question8_text_change, R.id.reponse8_text_change, R.id.reponse8_text, R.id.solution8_text_change, R.id.layout_solution8);
        score = processAnswer(8, score, R.id.linear_layout_reponse9, R.id.question9_text_change, R.id.reponse9_text_change, R.id.reponse9_text, R.id.solution9_text_change, R.id.layout_solution9);
        score = processAnswer(9, score, R.id.linear_layout_reponse10, R.id.question10_text_change, R.id.reponse10_text_change, R.id.reponse10_text, R.id.solution10_text_change, R.id.layout_solution10);

        TextView scoreText = (TextView) findViewById(R.id.fin_quiz_text2);
        scoreText.setText(score + "/" + globalVariables.getCurrentGame().getQuestionsId().size());
        scoreText.setGravity(Gravity.CENTER);
        scoreText.setTextSize(30);
        scoreText.setTextColor(getResources().getColor(R.color.colorTheme));

        setTextSiNonSeul();

        setTextFin(score);


        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseUser userAuth = FirebaseAuth.getInstance().getCurrentUser();
        final DocumentReference userDB = db.collection("Users").document(userAuth.getEmail());
        final Integer scoreFinal = score;

        if (!globalVariables.getCurrentGame().getSeul()) {
            updateGames(scoreFinal);

        } else {
            //Ajoute les points
            processPoints(score, userDB);
        }

        Button retourMainPage = (Button) findViewById(R.id.retour_main_page);
        retourMainPage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MainPage.class);
                startActivity(intent);
            }
        });

        Button revanche = (Button) findViewById(R.id.rejouer_revanche);
        if (globalVariables.getCurrentGame().getSeul()) {
            revanche.setText(R.string.rejouer);
        }

        revanche.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Game game = globalVariables.getCurrentGame();
                Intent intent = new Intent(v.getContext(), QuizPage.class);
                createQuestion(game.getSujet(), intent);
            }
        });



    }


    public Integer processAnswer(Integer i, Integer score, Integer id_layout, Integer id_question, Integer id_reponse, Integer id_reponse_inchange, Integer id_solution, Integer id_layout_solution) {
        final Globals globalVariables = (Globals) getApplicationContext();

        Game game = globalVariables.getCurrentGame();
        ArrayList<String> player1Answers = game.getPlayer1Answers();

        ArrayList<Question> realAnswers = game.getQuestions();

        String playerAnswer = player1Answers.get(i);
        Object answer = realAnswers.get(i).getReponse();
        String question = realAnswers.get(i).getQuestion();

        MathView textQuestion = findViewById(id_question);
        textQuestion.setText(question);

        String realAnswer = "";
        try {
            realAnswer = answer.toString();
        } catch (Exception e) {
            Log.e("ERROR", "probleme" + realAnswers.get(i));
        }

        LinearLayout llText = (LinearLayout) findViewById(id_layout);
        globalVariables.getCurrentGame().getQuestions().get(i).setNombrePose(globalVariables.getCurrentGame().getQuestions().get(i).getNombreReussi() + 1);

        if (playerAnswer.replaceAll("\\s", "").equalsIgnoreCase(realAnswer.replaceAll("\\s", ""))) {
            score ++;
            globalVariables.getCurrentGame().setReponsesTempsIndexScore(i);
            globalVariables.getCurrentGame().getQuestions().get(i).setNombreReussi(globalVariables.getCurrentGame().getQuestions().get(i).getNombreReussi() + 1);

            TextView textReponseInchange = (TextView) findViewById(id_reponse_inchange);
            textReponseInchange.setTextColor(getResources().getColor(R.color.green));

            MathView textReponse = findViewById(id_reponse);
            textReponse.setText(playerAnswer);
            MathView textSolution = findViewById(id_solution);
            llText.removeView(findViewById(id_layout_solution));
            llText.setBackground(getResources().getDrawable(R.drawable.reponse_true));
        } else {
            globalVariables.getCurrentGame().setReponsesTempsIndexScore0(i);

            TextView textReponseInchange = (TextView) findViewById(id_reponse_inchange);
            textReponseInchange.setTextColor(getResources().getColor(R.color.red));

            MathView textReponse = findViewById(id_reponse);
            textReponse.setText(playerAnswer);
            MathView textSolution = findViewById(id_solution);
            textSolution.setText(realAnswer);
            llText.setBackground(getResources().getDrawable(R.drawable.reponse_false));
        }


        String pathQuestion = globalVariables.getCurrentGame().getPathQuestion();

        ArrayList<String> data = new ArrayList<>();
        data.add(pathQuestion);
        data.add(globalVariables.getCurrentGame().getQuestionsId().get(i));
        data.add(globalVariables.getCurrentGame().getQuestions().get(i).getNombreReussi().toString());
        data.add(globalVariables.getCurrentGame().getQuestions().get(i).getNombrePose().toString());


        FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
        mFunctions.getHttpsCallable("updateQuestion")
                .call(data)
                .addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
                    @Override
                    public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                        if(task.isSuccessful()) {
                            Log.d("reussi", "question update");
                        } else {
                            Log.d("fail", task.getException().getMessage());
                        }

                    }
                });

        return score;
    }

    public void setTextSiNonSeul() {
        Globals globalVariables = (Globals) getApplicationContext();
        TextView siNonSeul = (TextView) findViewById(R.id.text_reviens_plus_tard);
        String textSiNonSeul = "";
        if (!globalVariables.getCurrentGame().getSeul()) {
            textSiNonSeul += " Reviens plus tard pour voir les résultats de ton pote";
            siNonSeul.setVisibility(View.VISIBLE);
            siNonSeul.setGravity(Gravity.CENTER);
            siNonSeul.setText(textSiNonSeul);
            siNonSeul.setTextSize(20);
        } else {
            siNonSeul.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this, MainPage.class);
        startActivity(intent);
    }

    public void setTextFin(Integer score){
        String textFin = "";
        if (score > 2) {
            textFin += "Bravo !";
        } else {
            textFin += "Ne baisse pas les bras !";
        }
        TextView text = (TextView) findViewById(R.id.fin_quiz_text);
        text.setGravity(Gravity.CENTER);
        text.setText(textFin);
        text.setTextSize(25);
    }

    public void processPoints(Integer score, DocumentReference userDB) {
        Globals globalVariables = (Globals) getApplicationContext();
        Integer newLevelPoints = 25 + 10 * score;

        if (globalVariables.getCurrentGame().getTimed()) {
            for (Integer integer: globalVariables.getCurrentGame().getReponsesTemps()){
                newLevelPoints += integer;
            }
        }

        User userForLevel = globalVariables.getUser();

        Integer previousLevel = userForLevel.getLevel();

        userForLevel.addPoints(newLevelPoints);

        if (previousLevel != userForLevel.getLevel()) {
            Globals.openPopup(this, globalVariables.getUser().getLevel());
        }

        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast,
                (ViewGroup) findViewById(R.id.custom_toast_container));

        Globals.makeToast("Bravo ! Vous avez gagné: " + newLevelPoints + "points !", layout, this);

        HashMap<String, Object> updateUser = new HashMap<>();
        updateUser.put("level", userForLevel.getLevel());
        updateUser.put("pointsActuels", userForLevel.getPointsActuels());

        userDB.update(updateUser)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("Success", "C'est tout bon");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Fail", "C'est pas bon");
                    }
                });
    }

    public void updateGames(Integer scoreFinal) {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseUser userAuth = FirebaseAuth.getInstance().getCurrentUser();
        final DocumentReference userDB = db.collection("Users").document(userAuth.getEmail());

        final Globals globalVariables = (Globals) getApplicationContext();
        Map<String, Object> updateFields = new HashMap<>();
        updateFields.put("score", scoreFinal.toString());
        updateFields.put("repondu", true);
        updateFields.put("player1Answers", globalVariables.getCurrentGame().getPlayer1Answers());
        updateFields.put("vu", true);
        updateFields.put("reponsesTemps", globalVariables.getCurrentGame().getReponsesTemps());


        userDB.collection("Games").document(globalVariables.getCurrentGame().getId())
                .update(updateFields)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });


        final Map<String, Object> updateOtherFields = new HashMap<>();
        updateOtherFields.put("reponsesTempsOpponent", globalVariables.getCurrentGame().getReponsesTemps());
        updateOtherFields.put("scoreOpponent", scoreFinal.toString());
        updateOtherFields.put("player2Answers", globalVariables.getCurrentGame().getPlayer1Answers());
        updateOtherFields.put("fini", true);
        updateOtherFields.put("vu", false);


        db.collection("Users")
                .document(globalVariables.getCurrentGame().getAdversaire())
                .collection("Games")
                .document(globalVariables.getCurrentGame().getId())
                .update(updateOtherFields)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });

    }

    public void createQuestion(String name_sujet, final Intent intent) {
        final Globals globalVariables = (Globals) getApplicationContext();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final String classe = globalVariables.getCurrentGame().getClasse();
        final String matiere = globalVariables.getCurrentGame().getMatiere();

        db.collection(classe).document(matiere).collection(name_sujet).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Game game = globalVariables.getCurrentGame();
                            game.flushGame();
                            game.createQuestions(task);
                            if (!game.getSeul()) {
                                Date date = new Date();
                                Long tmpDate = date.getTime();
                                final String id = tmpDate.toString();
                                game.setId(id);
                                game.setGame(globalVariables);
                            }
                            startActivity(intent);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }


}
