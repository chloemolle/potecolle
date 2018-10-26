package com.example.chloemolle.potecolle;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final Globals globalVariables = (Globals) getApplicationContext();
        final Context context = this;

        final LinearLayout layout = (LinearLayout) findViewById(R.id.layout_partie_en_cours);

        DocumentReference userDB = db.collection("Users").document(user.getEmail());

        userDB.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    User user = documentSnapshot.toObject(User.class);
                    globalVariables.setUser(user);
                    Button startGame = (Button) findViewById(R.id.lancer_partie);
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

                    for (HashMap<String,String> partie: user.getPartiesEnCours()) {
                        Button newButton = new Button(context);

                        String repondu = partie.get("repondu");
                        String fini = partie.get("fini");

                        String finiOuPas = (partie.get("repondu").equals("true") && partie.get("fini").equals("false"))?
                                                            "Attends qu'il réponde aux questions !" :
                                            (partie.get("repondu").equals("true") && partie.get("fini").equals("true")) ?
                                                            "regarde les résultats !" : "Réponds aux questions :) ";

                        newButton.setText(finiOuPas + " " + partie.get("adversaire") + " " + partie.get("classe") + " " + partie.get("matiere") + " " + partie.get("sujet"));
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        params.setMargins(100, 0, 100, 0);
                        newButton.setLayoutParams(params);
                        newButton.setTextColor(getResources().getColor(R.color.colorTheme));
                        newButton.setBackground(getDrawable(R.drawable.parties_en_cours));
                        layout.addView(newButton);

                    }
                } else {
                    Log.d(TAG, "No such document");
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
