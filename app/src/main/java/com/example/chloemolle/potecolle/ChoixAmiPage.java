package com.example.chloemolle.potecolle;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

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
import java.util.Map;

import static com.firebase.ui.auth.AuthUI.TAG;

/**
 * Created by chloemolle on 16/10/2018.
 */

public class ChoixAmiPage extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choix_ami_page_layout);
        final LinearLayout layout = (LinearLayout) findViewById(R.id.linear_layout_ami);
        final Context context = this;
        final Globals globalVariables = (Globals) getApplicationContext();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseUser userAuth = FirebaseAuth.getInstance().getCurrentUser();

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar_ami);
        progressBar.setVisibility(View.VISIBLE);

        User user = globalVariables.getUser();
        ArrayList<HashMap<String,String>> friends = user.getFriends();
        for (HashMap<String,String> friend: friends) {
            Button newButton = new Button(context);
            final String name = friend.get("name");
            newButton.setText(name);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(20, 20, 20, 20);
            newButton.setLayoutParams(params);
            newButton.setTextColor(getResources().getColor(R.color.white));

            newButton.setBackground(getResources().getDrawable(R.drawable.button_with_radius));
            newButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Button b = (Button) v;
                    String opponentUsername = b.getText().toString();
                    Intent intent = new Intent(v.getContext(), QuizPage.class);
                    globalVariables.getCurrentGame().setPlayer2(name);
                    globalVariables.getCurrentGame().setAdversaire(name);
                    Date date = new Date();
                    Long tmp = date.getTime();
                    final String id = tmp.toString();
                    globalVariables.getCurrentGame().setId(id);

                    final DocumentReference userDB = db.collection("Users").document(userAuth.getEmail());
                    //set game for user
                    userDB.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                User user = documentSnapshot.toObject(User.class);
                                final Game currentGame = globalVariables.getCurrentGame();
                                //Updating the array of the player
                                ArrayList<String> arr = user.getPartiesEnCours();
                                arr.add(id);
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
                                        });

                                //Creating the document
                                HashMap<String,String> newGame = new HashMap<>();
                                newGame.put("adversaire", currentGame.getPlayer2());
                                newGame.put("classe", currentGame.getClasse());
                                newGame.put("matiere", currentGame.getMatiere());
                                newGame.put("sujet", currentGame.getSujet());
                                newGame.put("fini", "false");
                                newGame.put("repondu", "false");
                                newGame.put("id", id);
                                userDB.collection("Games").document(id)
                                        .set(newGame)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "DocumentSnapshot successfully written!");
                                                ArrayList<String> questionsId = currentGame.getQuestionsId();
                                                userDB.collection("Games").document(id)
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
                            .whereEqualTo("username", opponentUsername)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    final DocumentReference opponentDB = db.collection("Users").document(document.getId());
                                    opponentDB.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            if (documentSnapshot.exists()) {
                                                User user = documentSnapshot.toObject(User.class);
                                                final Game currentGame = globalVariables.getCurrentGame();

                                                //Updating the array of the player
                                                ArrayList<String> arr = user.getPartiesEnCours();
                                                arr.add(id);
                                                opponentDB.update("partiesEnCours", arr)
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

                                                //Creating the document
                                                HashMap<String,String> newGame = new HashMap<>();
                                                newGame.put("adversaire", currentGame.getPlayer1());
                                                newGame.put("classe", currentGame.getClasse());
                                                newGame.put("matiere", currentGame.getMatiere());
                                                newGame.put("sujet", currentGame.getSujet());
                                                newGame.put("fini", "false");
                                                newGame.put("repondu", "false");
                                                newGame.put("id", id);
                                                opponentDB.collection("Games").document(id)
                                                        .set(newGame)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Log.d(TAG, "DocumentSnapshot successfully written!");
                                                                Log.d(TAG, "DocumentSnapshot successfully written!");
                                                                ArrayList<String> questionsId = currentGame.getQuestionsId();
                                                                opponentDB.collection("Games").document(id)
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
                                }
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        }
                    });

                    startActivity(intent);
                }
            });

            layout.addView(newButton);
        }

        progressBar.setVisibility(View.GONE);
    }


}
