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

                    final ArrayList<HashMap<String,String>> updatedGame = new ArrayList<>();
                    // On affiche un bouton par partie en cours
                    for (final HashMap<String,String> partie: user.getPartiesEnCours()) {
                        //TODO: Plus tard il faudrait créer une collection quiz par personne
                        final HashMap<String,String> partieTest = partie;

                        db.collection("Users")
                                .whereEqualTo("username", partie.get("adversaire"))
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                String id = document.getId();
                                                final DocumentReference OpponentDB = db.collection("Users").document(id);
                                                OpponentDB.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                        if (documentSnapshot.exists()) {
                                                            //TODO: un jour il faudra s'occuper de quand plusieurs parties ont les mêmes propriétés
                                                            User userOpponent = documentSnapshot.toObject(User.class);

                                                            ArrayList<HashMap<String,String>> arr = userOpponent.getPartiesEnCours();
                                                            for (Integer i = 0; i < arr.size(); i ++) {
                                                                final HashMap<String,String> partieOpponent = arr.get(i);
                                                                if (user.getUsername().equals(partieOpponent.get("adversaire")) &&
                                                                        (partieOpponent.get("question1Id").equals(partieTest.get("question1Id"))) &&
                                                                        (partieOpponent.get("question2Id").equals(partieTest.get("question2Id"))) &&
                                                                        (partieOpponent.get("question3Id").equals(partieTest.get("question3Id"))) &&
                                                                        (partieOpponent.get("question4Id").equals(partieTest.get("question4Id"))) &&
                                                                        (partieOpponent.get("question5Id").equals(partieTest.get("question5Id")))) {
                                                                    if(partieOpponent.get("repondu").equals("true")) {
                                                                        partieTest.put("fini", "true");
                                                                        partieTest.put("scoreOpponent", partieOpponent.get("score"));
                                                                    }
                                                                    updatedGame.add(partieTest);

                                                                    Button newButton = new Button(context);

                                                                    String repondu = partieTest.get("repondu");
                                                                    String fini = partieTest.get("fini");

                                                                    String finiOuPas = (repondu.equals("true") && fini.equals("false"))?
                                                                            "Attends qu'il réponde aux questions !" :
                                                                            (repondu.equals("true") && fini.equals("true")) ?
                                                                                    "regarde les résultats !" : "Réponds aux questions :) ";

                                                                    newButton.setText(finiOuPas + " " + partieTest.get("adversaire") + " " + partieTest.get("classe") + " " + partieTest.get("matiere") + " " + partieTest.get("sujet"));
                                                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                                                            LinearLayout.LayoutParams.MATCH_PARENT,
                                                                            LinearLayout.LayoutParams.WRAP_CONTENT
                                                                    );
                                                                    params.setMargins(100, 0, 100, 0);
                                                                    newButton.setLayoutParams(params);
                                                                    newButton.setTextColor(getResources().getColor(R.color.colorTheme));
                                                                    if (android.os.Build.VERSION.SDK_INT >= 21){
                                                                        newButton.setBackground(getDrawable(R.drawable.parties_en_cours));
                                                                    } else{
                                                                        newButton.setBackground(getResources().getDrawable(R.drawable.parties_en_cours));
                                                                    }
                                                                    if (repondu.equals("false")) {
                                                                        newButton.setOnClickListener(new View.OnClickListener() {
                                                                            public void onClick(View v) {
                                                                                globalVariables.setCurrentGame(new Game(globalVariables.getUser().getUsername(), partieTest.get("adversaire"), globalVariables.getUser().getClasse(), partieTest.get("matiere"), partieTest.get("sujet"), partieTest.get("question1Id"), partieTest.get("question2Id"), partieTest.get("question3Id"), partieTest.get("question4Id"), partieTest.get("question5Id"), partieTest.get("question1"), partieTest.get("question2"), partieTest.get("question3"), partieTest.get("question4"), partieTest.get("question5"), partieTest.get("reponse1"), partieTest.get("reponse2"), partieTest.get("reponse3"), partieTest.get("reponse4"), partieTest.get("reponse5"), "", ""));
                                                                                Intent intent = new Intent(v.getContext(), QuizPage.class);
                                                                                startActivity(intent);
                                                                            }
                                                                        });
                                                                    } else if (fini.equals("true")) {
                                                                        newButton.setOnClickListener(new View.OnClickListener() {
                                                                            public void onClick(View v) {
                                                                                globalVariables.setCurrentGame(new Game(globalVariables.getUser().getUsername(), partieTest.get("adversaire"), globalVariables.getUser().getClasse(), partieTest.get("matiere"), partieTest.get("sujet"), partieTest.get("question1Id"), partieTest.get("question2Id"), partieTest.get("question3Id"), partieTest.get("question4Id"), partieTest.get("question5Id"), partieTest.get("question1"), partieTest.get("question2"), partieTest.get("question3"), partieTest.get("question4"), partieTest.get("question5"), partieTest.get("reponse1"), partieTest.get("reponse2"), partieTest.get("reponse3"), partieTest.get("reponse4"), partieTest.get("reponse5"), partieTest.get("score"), partieOpponent.get("score")));
                                                                                Intent intent = new Intent(v.getContext(), ResultPage.class);
                                                                                startActivity(intent);
                                                                            }
                                                                        });
                                                                    }

                                                                    userDB.update("partiesEnCours", updatedGame)
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

                                                                    layout.addView(newButton);

                                                                    break;
                                                                } else {
                                                                    Log.d("Error", "Et non toujours pas");
                                                                }
                                                            }

                                                        } else {
                                                            Log.d(TAG, "No such document 1");
                                                        }
                                                    }
                                                });
                                            }
                                        } else {
                                            Log.d(TAG, "No such document 2");
                                        }
                                    }
                                });


                    }

                } else {
                    Log.d(TAG, "No such document 3");
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
