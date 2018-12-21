package com.example.chloemolle.potecolle;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static com.firebase.ui.auth.AuthUI.TAG;

/**
 * Created by chloemolle on 11/11/2018.
 */

public class ChoixTimer extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choix_timer_layout);

        Button boutonOui = (Button) findViewById(R.id.button_timer);
        Button boutonNon = (Button) findViewById(R.id.button_no_timer);

        boutonOui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Globals globalVariables = (Globals) getApplicationContext();
                final Game currentGame = globalVariables.getCurrentGame();
                currentGame.setTimed(true);
                globalVariables.setTmpTime(30);
                if (!globalVariables.getCurrentGame().getSeul()){
                    setGame(true);
                }
                Intent intent = new Intent(v.getContext(), QuizPage.class);
                startActivity(intent);
            }
        });

        boutonNon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Globals globalVariables = (Globals) getApplicationContext();
                final Game currentGame = globalVariables.getCurrentGame();
                currentGame.setTimed(false);
                if (!globalVariables.getCurrentGame().getSeul()){
                    setGame(false);
                }
                Intent intent = new Intent(v.getContext(), QuizPage.class);
                startActivity(intent);
            }
        });

    }

    public void setGame(final Boolean isTimer){
        final Globals globalVariables = (Globals) getApplicationContext();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseUser userAuth = FirebaseAuth.getInstance().getCurrentUser();
        final DocumentReference userDB = db.collection("Users").document(userAuth.getEmail());
        final Game currentGame = globalVariables.getCurrentGame();
        final String opponentUsername = currentGame.getAdversaire();

        //set game for user
        setGameForOneUser(userAuth.getEmail(), true, currentGame.getAdversaire(), currentGame.getPlayer2());

        //set game for opponent
        setGameForOneUser(opponentUsername, false, userAuth.getEmail(), globalVariables.getUser().getUsername());
    }

    public void setGameForOneUser(final String monMail, final Boolean vu, String adversaire, String player2) {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        Globals globalVariables = (Globals) getApplicationContext();
        final Game currentGame = globalVariables.getCurrentGame();

        HashMap<String,Object> newGame = new HashMap<>();
        newGame.put("timed", currentGame.getTimed());
        newGame.put("adversaire", adversaire);
        newGame.put("player2", player2);
        newGame.put("classe", currentGame.getClasse());
        newGame.put("matiere", currentGame.getMatiere());
        newGame.put("sujet", currentGame.getSujet());
        newGame.put("fini", false);
        newGame.put("repondu", false);
        newGame.put("vu", vu);
        newGame.put("id", currentGame.getId());

        db.collection("Users")
                .document(monMail)
                .collection("Games")
                .document(currentGame.getId())
                .set(newGame)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        ArrayList<String> questionsId = currentGame.getQuestionsId();
                        db.collection("Users")
                                .document(monMail).collection("Games").document(currentGame.getId())
                                .update("questionsId", questionsId)
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
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });

            }
}
