package com.example.chloemolle.potecolle;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

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

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.firebase.ui.auth.AuthUI.TAG;

/**
 * Created by chloemolle on 24/10/2018.
 */

public class FinQuizPage extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.final_quiz_page_layout);

        TextView text = (TextView) findViewById(R.id.fin_quiz_text);

        final Globals globalVariables = (Globals) getApplicationContext();

        globalVariables.setCurrentQuestionNumero(0);
        ArrayList<String> player1Answers = globalVariables.getCurrentGame().getPlayer1Answers();
        ArrayList<Question> realAnswers = globalVariables.getCurrentGame().getQuestions();

        LinearLayout ll = (LinearLayout) findViewById(R.id.linear_layout_reponse);

        Integer score = 0;
        for (Integer i = 0; i < player1Answers.size(); i ++) {
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
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            layoutParamsQuestion.setMargins(10, 10, 10, 0);
            llText.setLayoutParams(layoutParamsQuestion);
            TextView textQuestion = new TextView(this);
            textQuestion.setText("question: " + question);

            if (playerAnswer.replaceAll("\\s", "").equalsIgnoreCase(realAnswer.replaceAll("\\s", ""))) {
                score ++;
                TextView textReponse = new TextView(this);
                textReponse.setText("ta réponse: " + playerAnswer);
                textReponse.setTextColor(getResources().getColor(R.color.green));
                llText.addView(textQuestion);
                llText.addView(textReponse);
            } else {
                TextView textReponse = new TextView(this);
                textReponse.setText("ta réponse: " + playerAnswer);
                textReponse.setTextColor(getResources().getColor(R.color.red));
                TextView textSolution = new TextView(this);
                textSolution.setText("la solution: " + realAnswer);
                llText.addView(textQuestion);
                llText.addView(textReponse);
                llText.addView(textSolution);
            }
            llText.setBackground(getResources().getDrawable(R.drawable.parties_en_cours));
            ll.addView(llText);
        }

        text.setText("Bravo ! Ton score: " + score + ". Reviens plus tard pour voir si tu as battu ton pote ;)");

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseUser userAuth = FirebaseAuth.getInstance().getCurrentUser();
        final DocumentReference userDB = db.collection("Users").document(userAuth.getEmail());
        final Integer scoreFinal = score;

        Map<String, Object> updateFields = new HashMap<>();
        updateFields.put("score", scoreFinal.toString());
        updateFields.put("repondu", "true");


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
        updateOtherFields.put("scoreOpponent", scoreFinal.toString());
        updateOtherFields.put("fini", "true");


        db.collection("Users")
                .whereEqualTo("username", globalVariables.getCurrentGame().getAdversaire())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                final DocumentReference opponentDB = db.collection("Users").document(document.getId());
                                opponentDB.collection("Games")
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
                        }
                    }
                });

        Button retourMainPage = (Button) findViewById(R.id.retour_main_page);
        retourMainPage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MainPage.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this, MainPage.class);
        startActivity(intent);
    }

}
