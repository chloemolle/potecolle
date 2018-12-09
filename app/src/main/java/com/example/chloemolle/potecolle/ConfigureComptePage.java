package com.example.chloemolle.potecolle;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chloemolle on 07/12/2018.
 */

public class ConfigureComptePage extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.configure_compte_page_layout);

        final FirebaseUser userFirebase = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final String userEmail = userFirebase.getEmail();
        final DocumentReference userDB = db.collection("Users").document(userEmail);

        final Globals globalVariables = (Globals) getApplicationContext();

        EditText text = (EditText) findViewById(R.id.username_text);
        text.setText(globalVariables.getUser().getUsername());


        Button backButton = (Button) findViewById(R.id.retour_main_page_from_configuration);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MainPage.class);
                startActivity(intent);
            }
        });

        final Spinner choixClasse = (Spinner) findViewById(R.id.spinner_classe_update);
        ArrayAdapter<CharSequence> adapterFamily = ArrayAdapter.createFromResource(this,
                R.array.classes, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterFamily.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        choixClasse.setAdapter(adapterFamily);
        int spinnerPosition = adapterFamily.getPosition(globalVariables.getUser().getClasse());
        //set the default according to value
        choixClasse.setSelection(spinnerPosition);

        Button supprimerCompte = (Button) findViewById(R.id.supprimer_compte);
        supprimerCompte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(v.getContext());
                alertDialog.setPositiveButton(R.string.supprimer, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        userDB.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.d("Reussi", "User deleted");


                                userFirebase.delete()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Log.d("Reussi", "User deleted");

                                                Intent intent = new Intent(v.getContext(), ConnexionPage.class);
                                                startActivity(intent);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d("Fail", "User NOT deleted");
                                            }
                                        });

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                    Log.d("Fail", "User NOT deleted");
                                }
                            });
                        }
                });

                alertDialog.setNegativeButton(R.string.annuler, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

                alertDialog.setTitle("Suppression compte");
                alertDialog.setMessage(R.string.supprimer_compte_question);
                alertDialog.setCancelable(true);
                alertDialog.create().show();
            }
        });

        Button update = (Button) findViewById(R.id.update);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Spinner choixClasse = (Spinner) findViewById(R.id.spinner_classe_update);

                EditText text = (EditText) findViewById(R.id.username_text);
                String userClass = choixClasse.getSelectedItem().toString();
                final FirebaseFirestore db = FirebaseFirestore.getInstance();
                final FirebaseUser userFirebase = FirebaseAuth.getInstance().getCurrentUser();
                final DocumentReference userDB = db.collection("Users").document(userFirebase.getEmail());

                Map<String, Object> updateUser = new HashMap<>();
                updateUser.put("username", text.getText().toString());
                updateUser.put("classe", userClass);
                updateUser.put("level", 1);
                updateUser.put("pointsActuels", 0);

                User user = globalVariables.getUser();
                user.setUsername(text.getText().toString());
                user.setClasse(userClass);

                userDB.update(updateUser)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("Success", "update ok");
                                LayoutInflater inflater = getLayoutInflater();
                                View layout = inflater.inflate(R.layout.toast,
                                        (ViewGroup) findViewById(R.id.custom_toast_container));

                                TextView textToast = (TextView) layout.findViewById(R.id.text);
                                textToast.setText("Nous avons bien pris en compte vos changements ! :)");
                                Toast toast = new Toast(getApplicationContext());
                                toast.setGravity(Gravity.BOTTOM, 0, 0);
                                toast.setDuration(Toast.LENGTH_LONG);
                                toast.setView(layout);
                                toast.show();

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("Failure", "update pas ok");
                            }
                        });


            }
        });

    }


}