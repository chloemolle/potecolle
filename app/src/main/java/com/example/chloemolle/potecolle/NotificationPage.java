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

import static com.firebase.ui.auth.AuthUI.TAG;

/**
 * Created by chloemolle on 09/11/2018.
 */

public class NotificationPage extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_page_layout);


        final FirebaseUser userFirebase = FirebaseAuth.getInstance().getCurrentUser();

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final Globals globalVariables = (Globals) getApplicationContext();
        final Context context = this;

        final LinearLayout layout = (LinearLayout) findViewById(R.id.layout_partie_en_cours);

        User user = globalVariables.getUser();
        ArrayList<String> parties = user.getPartiesEnCours();

        final DocumentReference userDB = db.collection("Users").document(userFirebase.getEmail());



        for (String partie: parties) {
            final String partieTmp = partie;
            userDB.collection("Games").document(partieTmp)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                String scoreOpponent = (String) documentSnapshot.get("scoreOpponent");
                                final Game game = documentSnapshot.toObject(Game.class);

                                //On met à jour la partie suivant si l'adversaire a repondu aux questions
                                db.collection("Users")
                                        .whereEqualTo("username", game.getAdversaire())
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                                        db.collection("Users").document(document.getId())
                                                                .collection("Games").document(partieTmp)
                                                                .get()
                                                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                    @Override
                                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                        if (documentSnapshot.exists()) {
                                                                            if (documentSnapshot.get("repondu").equals("true") && game.getRepondu().equals("true")) {
                                                                                DocumentReference doc = userDB.collection("Games").document(partieTmp);
                                                                                doc.update("fini", "true")
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

                                                                            // on ajoute un bouton pour accéder à la partie (/!!!!\ à changer rapidement
                                                                            Button newButton = new Button(context);

                                                                            String repondu = game.getRepondu();
                                                                            String fini = game.getFini();

                                                                            String finiOuPas = (repondu.equals("true") && fini.equals("false"))?
                                                                                    "Attends qu'il réponde aux questions !" :
                                                                                    (repondu.equals("true") && fini.equals("true")) ?
                                                                                            "regarde les résultats !" : "Réponds aux questions :) ";

                                                                            newButton.setText(finiOuPas + "\n" + game.getAdversaire() + " " + game.getClasse() + "\n" + game.getMatiere() + " " + game.getSujet());
                                                                            newButton.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                                                                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                                                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                                                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                                                            );
                                                                            params.setMargins(25,25,25,0);
                                                                            newButton.setPadding(100, 0, 100, 0);
                                                                            newButton.setLayoutParams(params);
                                                                            newButton.setTextColor(getResources().getColor(R.color.colorTheme));
                                                                            newButton.setBackgroundColor(getResources().getColor(R.color.white));
                                                                            if (repondu.equals("false")) {
                                                                                newButton.setOnClickListener(new View.OnClickListener() {
                                                                                    public void onClick(View v) {
                                                                                        globalVariables.setCurrentGame(game);
                                                                                        Intent intent = new Intent(v.getContext(), LoadingQuizPage.class);
                                                                                        startActivity(intent);
                                                                                    }
                                                                                });
                                                                            } else if (fini.equals("true")) {
                                                                                newButton.setOnClickListener(new View.OnClickListener() {
                                                                                    public void onClick(View v) {
                                                                                        globalVariables.setCurrentGame(game);
                                                                                        Intent intent = new Intent(v.getContext(), ResultPage.class);
                                                                                        startActivity(intent);
                                                                                    }
                                                                                });
                                                                            }

                                                                            layout.addView(newButton);

                                                                        }
                                                                    }
                                                                });
                                                    }
                                                } else {
                                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                                }
                                            }
                                        });


                            }
                        }
                    });

        }
    }


    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this, MainPage.class);
        startActivity(intent);
    }


}
