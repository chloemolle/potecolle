package com.example.chloemolle.potecolle;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
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
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
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
        final ProgressBar progressBarCheckUsername = (ProgressBar) findViewById(R.id.progress_bar_check_username_configuration);

        progressBarCheckUsername.setVisibility(View.GONE);
        final Globals globalVariables = (Globals) getApplicationContext();
        final String previousUserName = globalVariables.getUser().getUsername();
        setUserInformation(previousUserName);


        Button backButton = (Button) findViewById(R.id.retour_main_page_from_configuration);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MainPage.class);
                startActivity(intent);
            }
        });

        //Bouton pour supprimer le compte
        supprimerCompte(previousUserName);

        //Bouton pour modifier le compte
        updateCompte(progressBarCheckUsername, previousUserName);

    }

    public void updateCompte(final ProgressBar progressBarCheckUsername, final String previousUserName) {
        Button update = (Button) findViewById(R.id.update);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Spinner choixClasse = (Spinner) findViewById(R.id.spinner_classe_update);
                final Globals globalVariables = (Globals) getApplicationContext();
                final EditText text = (EditText) findViewById(R.id.username_text);
                final String userClass = choixClasse.getSelectedItem().toString();
                final FirebaseFirestore db = FirebaseFirestore.getInstance();
                final FirebaseUser userFirebase = FirebaseAuth.getInstance().getCurrentUser();
                final DocumentReference userDB = db.collection("Users").document(userFirebase.getEmail());
                final Context context = v.getContext();

                progressBarCheckUsername.setVisibility(View.VISIBLE);

                FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
                mFunctions
                        .getHttpsCallable("getUsers")
                        .call("")
                        .continueWith(new Continuation<HttpsCallableResult, String>() {
                            @Override
                            public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                                final ArrayList<HashMap<String, String>> arr = (ArrayList<HashMap<String, String>>) task.getResult().getData();
                                Boolean alreadyUsed = false;
                                String username = text.getText().toString();
                                for (final HashMap<String, String> ami : arr) {
                                    final String usernameTemp = ami.get("username");
                                    if (!usernameTemp.toLowerCase().trim().equals(previousUserName.toLowerCase().trim()) && usernameTemp.toLowerCase().trim().equals(username.toLowerCase().trim())) {
                                        alreadyUsed = true;
                                        break;
                                    }
                                }
                                if (!alreadyUsed) {
                                    Map<String, Object> updateUser = new HashMap<>();
                                    updateUser.put("username", text.getText().toString());
                                    updateUser.put("classe", userClass);

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

                                                    Globals.makeToast("Nous avons bien pris en compte vos changements ! :)", layout,  context);

                                                    progressBarCheckUsername.setVisibility(View.GONE);

                                                    Intent intent = new Intent(context, MainPage.class);
                                                    startActivity(intent);

                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d("Failure", "update pas ok");
                                                }
                                            });
                                } else {
                                    progressBarCheckUsername.setVisibility(View.GONE);
                                    LayoutInflater inflater = getLayoutInflater();
                                    View layout = inflater.inflate(R.layout.toast,
                                            (ViewGroup) findViewById(R.id.custom_toast_container));

                                    TextView text = (TextView) layout.findViewById(R.id.text);
                                    text.setText(R.string.username_already_used);
                                    Toast toast = new Toast(getApplicationContext());
                                    toast.setGravity(Gravity.BOTTOM, 0, 0);
                                    toast.setDuration(Toast.LENGTH_LONG);
                                    toast.setView(layout);
                                    toast.show();
                                }
                                return "";
                            }
                        });


            }
        });

    }

    public void setUserInformation(String previousUserName) {
        final Globals globalVariables = (Globals) getApplicationContext();
        EditText text = (EditText) findViewById(R.id.username_text);
        text.setText(previousUserName);

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
    }

    public void supprimerCompte(String previousUserName) {
        final FirebaseUser userFirebase = FirebaseAuth.getInstance().getCurrentUser();
        final String userEmail = userFirebase.getEmail();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference userDB = db.collection("Users").document(userEmail);

        Button supprimerCompte = (Button) findViewById(R.id.supprimer_compte);
        supprimerCompte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(v.getContext());
                alertDialog.setPositiveButton(R.string.supprimer, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        suppressionParties(userDB);

                        suppressionFriendRequest();

                        suppressionUser(userDB, userFirebase, v.getContext());

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

    }

    public void suppressionParties(DocumentReference userDB) {
        final Globals globalVariables = (Globals) getApplicationContext();

        for (final Game partie: globalVariables.getUser().getPartiesEnCours()) {
            userDB.collection("Games")
                    .document(partie.getId())
                    .delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d("réussi", "C'est bon");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("fail", e.getMessage());
                        }
                    });
        }

    }

    public void suppressionFriendRequest() {
        final FirebaseUser userFirebase = FirebaseAuth.getInstance().getCurrentUser();
        final String userEmail = userFirebase.getEmail();

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference userDB = db.collection("Users").document(userEmail);
        final Globals globalVariables = (Globals) getApplicationContext();

        for (final FriendRequest friendRequest: globalVariables.getUser().getFriendRequests()) {
            userDB.collection("FriendRequests")
                    .document(friendRequest.getId())
                    .delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            db.collection("Users").document(friendRequest.getId())
                                    .collection("friendRequests")
                                    .document(userFirebase.getEmail())
                                    .delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Log.d("reussi", "yes");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d("fail", e.getMessage());
                                        }
                                    });
                            Log.d("réussi", "yeayyy");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("fail", e.getMessage());
                        }
                    });
        }

    }

    public void suppressionUser(DocumentReference userDB, final FirebaseUser userFirebase, final Context context) {
        userDB.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d("Reussi", "User deleted");


                userFirebase.delete()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.d("Reussi", "User deleted");

                                Intent intent = new Intent(context, ConnexionPage.class);
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

}