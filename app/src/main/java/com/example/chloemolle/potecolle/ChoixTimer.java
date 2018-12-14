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
        userDB.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    //Creating the document
                    HashMap<String,Object> newGame = new HashMap<>();
                    newGame.put("timed", currentGame.getTimed());
                    newGame.put("adversaire", currentGame.getAdversaire());
                    newGame.put("classe", currentGame.getClasse());
                    newGame.put("matiere", currentGame.getMatiere());
                    newGame.put("sujet", currentGame.getSujet());
                    newGame.put("fini", false);
                    newGame.put("repondu", false);
                    newGame.put("id", currentGame.getId());
                    userDB.collection("Games").document(currentGame.getId())
                            .set(newGame)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "DocumentSnapshot successfully written!");
                                    ArrayList<String> questionsId = currentGame.getQuestionsId();
                                    userDB.collection("Games").document(currentGame.getId())
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

                } else {
                    Log.d(TAG, "No such document");
                }
            }
        });

        //set game for opponent
        db.collection("Users")
                .document(opponentUsername)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            final DocumentReference opponentDB = db.collection("Users").document(document.getId());
                            //Creating the document
                            HashMap<String,Object> newGame = new HashMap<>();
                            newGame.put("timed", currentGame.getTimed());
                            newGame.put("adversaire", userAuth.getEmail());
                            newGame.put("classe", currentGame.getClasse());
                            newGame.put("matiere", currentGame.getMatiere());
                            newGame.put("sujet", currentGame.getSujet());
                            newGame.put("fini", false);
                            newGame.put("repondu", false);
                            newGame.put("id", currentGame.getId());
                            newGame.put("vu", false);
                            opponentDB.collection("Games").document(currentGame.getId())
                                    .set(newGame)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "DocumentSnapshot successfully written!");
                                            Log.d(TAG, "DocumentSnapshot successfully written!");
                                            ArrayList<String> questionsId = currentGame.getQuestionsId();
                                            opponentDB.collection("Games").document(currentGame.getId())
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
                        } else {
                            Log.d(TAG, task.getException().getMessage());
                        }

                    }
                });
    }

}
