package com.example.chloemolle.potecolle;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by chloemolle on 07/12/2018.
 */

public class VoirAmiPage extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.voir_ami_page_layout);
        final Globals globalVariables = (Globals) getApplicationContext();
        User user = globalVariables.getUser();
        ArrayList<String> friends = user.getFriends();
        final LinearLayout layout = (LinearLayout) findViewById(R.id.linear_layout_voir_ami);

        if (friends.size() > 0) {
            final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar_voir_ami);
            progressBar.setVisibility(View.VISIBLE);

            goWithTheDatabase();

            progressBar.setVisibility(View.GONE);

        } else {
            final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar_voir_ami);
            progressBar.setVisibility(View.GONE);
            TextView textView = new TextView(this);
            textView.setText(R.string.add_friends);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.gravity = Gravity.CENTER_HORIZONTAL;
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
        final Context context = this;

        for (String friend: friends) {
            final String email = friend;
            db.collection("Users").document(friend)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            final User user = documentSnapshot.toObject(User.class);
                            createButtonWithPlayerName(user.getUsername(), email, user.getLevel());
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

    public void createButtonWithPlayerName(final String name, final String email, Integer level){
        final Globals globalVariables = (Globals) getApplicationContext();
        final Context context = this;
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final LinearLayout layout = (LinearLayout) findViewById(R.id.linear_layout_voir_ami);
        final FirebaseUser userFirebase = FirebaseAuth.getInstance().getCurrentUser();


        final Button newButton = new Button(context);
        newButton.setText(name + "\nNiveau " + level.toString());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(20, 20, 20, 20);
        newButton.setLayoutParams(params);
        newButton.setTextColor(getResources().getColor(R.color.colorTheme));

        newButton.setBackground(getResources().getDrawable(R.drawable.box_pour_entoure));
        newButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Veux-tu l'enlever de tes potes ?")
                        .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                ArrayList<String> amis = globalVariables.getUser().getFriends();
                                amis.remove(email);
                                db.collection("Users")
                                        .document(userFirebase.getEmail())
                                        .update("friends", amis)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    layout.removeView(newButton);
                                                    Log.d("Success", "document successfully deleted");
                                                } else {
                                                    Log.d("Error", "document NOT successfully deleted");
                                                }
                                            }
                                        });

                                db.collection("Users")
                                        .document(userFirebase.getEmail())
                                        .collection("Games")
                                        .whereEqualTo("adversaire", email)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    QuerySnapshot query = task.getResult();
                                                    List<DocumentSnapshot> docs = query.getDocuments();
                                                    for (DocumentSnapshot doc: docs) {
                                                        db.collection("Users")
                                                                .document(userFirebase.getEmail())
                                                                .collection("Games")
                                                                .document(doc.getId())
                                                                .delete()
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        Log.d("Success", "Tout bon");
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
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d("failt", e.getMessage());
                                            }
                                        });


                                HashMap<String, Object> friendDeletion = new HashMap<>();
                                friendDeletion.put("email", userFirebase.getEmail());

                                db.collection("Users")
                                        .document(email)
                                        .collection("FriendDeletion")
                                        .document()
                                        .set(friendDeletion)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Log.d("Success", "ok");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d("Fail", e.getMessage());
                                            }
                                        });

                                db.collection("Users")
                                        .document(email)
                                        .collection("Games")
                                        .whereEqualTo("adversaire", userFirebase.getEmail())
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    QuerySnapshot query = task.getResult();
                                                    List<DocumentSnapshot> docs = query.getDocuments();
                                                    for (DocumentSnapshot doc: docs) {
                                                        db.collection("Users")
                                                                .document(email)
                                                                .collection("Games")
                                                                .document(doc.getId())
                                                                .delete()
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        Log.d("Success", "Tout bon");
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
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d("failt", e.getMessage());
                                            }
                                        });


                            }
                        })
                        .setNegativeButton("Non", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d("Info","partie conservée");
                            }
                        }).show();

                return false;
            }
        });

        layout.addView(newButton, 0);
    }
}
