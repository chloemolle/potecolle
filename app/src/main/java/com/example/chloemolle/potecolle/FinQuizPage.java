package com.example.chloemolle.potecolle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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
        ArrayList<Map<String,Object>> realAnswers = globalVariables.getCurrentGame().getQuestions();

        LinearLayout ll = (LinearLayout) findViewById(R.id.linear_layout_reponse);

        Integer score = 0;
        for (Integer i = 0; i < player1Answers.size(); i ++) {
            String playerAnswer = player1Answers.get(i);
            Object answer = realAnswers.get(i).get("reponse");
            Object question = realAnswers.get(i).get("question");
            String realAnswer = "";
            try {
                realAnswer = answer.toString();
            } catch (Exception e) {
                Log.e("ERROR", "probleme" + realAnswers.get(i));
            }
            if (playerAnswer.equals(realAnswer)) {
                score ++;
            } else {
                TextView textQuestion = new TextView(this);
                textQuestion.setText("question: " + question);

                TextView textReponse = new TextView(this);
                textReponse.setText("ta réponse: " + playerAnswer);
                textReponse.setTextColor(getResources().getColor(R.color.red));

                TextView textSolution = new TextView(this);
                textSolution.setText("la solution: " + realAnswer);

                ll.addView(textQuestion);
                ll.addView(textReponse);
                ll.addView(textSolution);
            }
        }

        text.setText("Bravo ! Ton score: " + score + ". Reviens plus tard pour voir si tu as battu ton pote ;)");

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseUser userAuth = FirebaseAuth.getInstance().getCurrentUser();
        final DocumentReference userDB = db.collection("Users").document(userAuth.getEmail());
        final Integer scoreFinal = score;

        userDB.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    User user = documentSnapshot.toObject(User.class);
                    Game currentGame = globalVariables.getCurrentGame();
                    ArrayList<HashMap<String,String>> arr = user.getPartiesEnCours();
                    for (Integer i = 0; i < arr.size(); i ++) {
                        HashMap<String,String> gameTmp = arr.get(i);
                        if(currentGame.isEqual(gameTmp.get("matiere"), gameTmp.get("sujet"), user.getUsername(),gameTmp.get("adversaire"), gameTmp.get("question1Id"), gameTmp.get("question2Id"), gameTmp.get("question3Id"), gameTmp.get("question4Id"), gameTmp.get("question5Id"))) {
                            arr.get(i).put("repondu", "true");
                            arr.get(i).put("score", scoreFinal.toString());
                        }
                    }
                    userDB.update("partiesEnCours", arr)
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
                            });;
                } else {
                    Log.d(TAG, "No such document");
                }
            }
        });

    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this, MainPage.class);
        startActivity(intent);
    }

}
