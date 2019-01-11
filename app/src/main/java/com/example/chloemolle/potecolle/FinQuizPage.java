package com.example.chloemolle.potecolle;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
        for (Integer i = 0; i < player1Answers.size(); i ++) {
            score = processAnswer(i, score);
        }

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

    private void openPopup() {
        Globals globalVariables = (Globals) getApplicationContext();
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_level_up);
        Button retour = (Button) dialog.findViewById(R.id.retour_popup);
        TextView textBravo = (TextView) dialog.findViewById(R.id.bravo_text);
        textBravo.setText("Bravo !");
        TextView text = (TextView) dialog.findViewById(R.id.level_up_text);
        text.setText("Tu passes au niveau " + globalVariables.getUser().getLevel() + " ;)");
        retour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public Integer processAnswer(Integer i, Integer score) {
        final Globals globalVariables = (Globals) getApplicationContext();

        ArrayList<String> player1Answers = globalVariables.getCurrentGame().getPlayer1Answers();
        ArrayList<Question> realAnswers = globalVariables.getCurrentGame().getQuestions();

        LinearLayout ll = (LinearLayout) findViewById(R.id.linear_layout_reponse);

        String playerAnswer = player1Answers.get(i);
        Object answer = realAnswers.get(i).getReponse();
        Object question = realAnswers.get(i).getQuestion();
        String realAnswer = "";
        try {
            realAnswer = answer.toString();
        } catch (Exception e) {
            Log.e("ERROR", "probleme" + realAnswers.get(i));
        }
        LinearLayout llText = new LinearLayout(this);
        llText.setOrientation(LinearLayout.VERTICAL);
        llText.setPadding(10, 10, 10, 10);
        LinearLayout.LayoutParams layoutParamsQuestion = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParamsQuestion.setMargins(20, 10, 20, 10);
        llText.setLayoutParams(layoutParamsQuestion);
        TextView textQuestion = new TextView(this);
        textQuestion.setText("question: " + question);

        if (playerAnswer.replaceAll("\\s", "").equalsIgnoreCase(realAnswer.replaceAll("\\s", ""))) {
            score ++;
            globalVariables.getCurrentGame().setReponsesTempsIndexScore(i);
            TextView textReponse = new TextView(this);
            textReponse.setText("ta réponse: " + playerAnswer);
            textReponse.setTextColor(getResources().getColor(R.color.green));
            llText.addView(textQuestion);
            llText.addView(textReponse);
            llText.setBackground(getResources().getDrawable(R.drawable.reponse_true));
        } else {
            globalVariables.getCurrentGame().setReponsesTempsIndexScore0(i);
            TextView textReponse = new TextView(this);
            textReponse.setText("ta réponse: " + playerAnswer);
            textReponse.setTextColor(getResources().getColor(R.color.red));
            TextView textSolution = new TextView(this);
            textSolution.setText("la solution: " + realAnswer);
            llText.addView(textQuestion);
            llText.addView(textReponse);
            llText.addView(textSolution);
            llText.setBackground(getResources().getDrawable(R.drawable.reponse_false));
        }
        ll.addView(llText);
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
            openPopup();
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
