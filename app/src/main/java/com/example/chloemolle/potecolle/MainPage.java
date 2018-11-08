package com.example.chloemolle.potecolle;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.firebase.ui.auth.AuthUI.TAG;

/**
 * Created by chloemolle on 11/10/2018.
 */

public class MainPage extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page_layout);

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final Globals globalVariables = (Globals) getApplicationContext();
        final Context context = this;

        final LinearLayout layout = (LinearLayout) findViewById(R.id.layout_partie_en_cours);

        final DocumentReference userDB = db.collection("Users").document(user.getEmail());

        userDB.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    //On récupère le nom du joueur
                    Map<String, Object> test = documentSnapshot.getData();
                    final User user = documentSnapshot.toObject(User.class);
                    globalVariables.setUser(user);
                    Button startGame = (Button) findViewById(R.id.lancer_partie);
                    //on crée le bouton pour démarrer une partie
                    startGame.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            globalVariables.setCurrentGame(new Game(globalVariables.getUser().getUsername(), globalVariables.getUser().getClasse()));
                            Intent intent = new Intent(v.getContext(), ChoixMatierePage.class);
                            startActivity(intent);
                        }
                    });

                    TextView studentName = (TextView) findViewById(R.id.student_name);
                    String userName = user.getUsername();
                    studentName.setText("Salut " + userName + " !");

                    final ArrayList<HashMap<String, String>> updatedGame = new ArrayList<>();
                    // On affiche un bouton par partie en cours
                    ArrayList<String> parties = user.getPartiesEnCours();
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

                                                                                newButton.setText(finiOuPas + " " + game.getAdversaire() + " " + game.getClasse() + " " + game.getMatiere() + " " + game.getSujet());
                                                                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                                                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                                                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                                                                );
                                                                                params.setMargins(100, 0, 100, 0);
                                                                                newButton.setLayoutParams(params);
                                                                                newButton.setTextColor(getResources().getColor(R.color.colorTheme));
                                                                                newButton.setBackground(getResources().getDrawable(R.drawable.parties_en_cours));
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
            }

        });
    }

    @Override
    public void onBackPressed(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        final Context context = this;
        alertDialog.setPositiveButton(R.string.se_deconnecter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                FirebaseAuth test = FirebaseAuth.getInstance();
                test.signOut();
                Intent intent = new Intent(context, ConnexionPage.class);
                startActivity(intent);
            }
        });
        alertDialog.setNegativeButton(R.string.rester_connecter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        alertDialog.setTitle("Deconnexion");
        alertDialog.setMessage("Es-tu sur de vouloir te déconnecter?");
        alertDialog.setCancelable(true);
        alertDialog.create().show();
    }

}
