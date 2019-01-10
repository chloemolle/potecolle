package com.example.chloemolle.potecolle;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
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
        final Globals globalVariables = (Globals) getApplicationContext();

        User user = globalVariables.getUser();
        ArrayList<String> friends = user.getFriends();
        final LinearLayout layout = (LinearLayout) findViewById(R.id.linear_layout_ami);

        if (friends.size() > 0) {
            final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar_ami);
            progressBar.setVisibility(View.VISIBLE);

            goWithTheDatabase();

            progressBar.setVisibility(View.GONE);

        } else {
            final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar_ami);
            progressBar.setVisibility(View.GONE);
            TextView textView = new TextView(this);
            textView.setText(R.string.add_friends);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.gravity = Gravity.CENTER_HORIZONTAL;
            params.setMargins(0,20,0,0);
            textView.setLayoutParams(params);

            layout.addView(textView);
        }

        Button ajoutAmi = findViewById(R.id.ajouter_ami_button);
        ajoutAmi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), AddFriendsPage.class);
                startActivity(intent);
            }
        });

    }


    public void goWithTheDatabase(){
        final Globals globalVariables = (Globals) getApplicationContext();
        User user = globalVariables.getUser();
        ArrayList<String> friends = user.getFriends();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        for (String friend: friends) {
            final String email = friend;
            db.collection("Users").document(friend)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            final User user = documentSnapshot.toObject(User.class);
                            createButtonWithPlayerName(user.getUsername(), email);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("Fail", e.getMessage() + " il est fort probable qu'il n'est pas encore accepté la demande");
                        }
                    });
        }

    }

    public void createButtonWithPlayerName(String _name, final String email){
        final String name = _name;
        final Globals globalVariables = (Globals) getApplicationContext();
        final Context context = this;
        final LinearLayout layout = (LinearLayout) findViewById(R.id.linear_layout_ami);

        Button newButton = new Button(context);
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
                Intent intent = new Intent(v.getContext(), QuizPage.class);
                globalVariables.getCurrentGame().setPlayer2(name);
                globalVariables.getCurrentGame().setAdversaire(email);
                setGame();
                startActivity(intent);
            }
        });

        layout.addView(newButton, 0);
    }


    public void setGame(){
        final Globals globalVariables = (Globals) getApplicationContext();
        final FirebaseUser userAuth = FirebaseAuth.getInstance().getCurrentUser();
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