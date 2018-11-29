package com.example.chloemolle.potecolle;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.List;
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

        final FirebaseUser userFirebase = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final Globals globalVariables = (Globals) getApplicationContext();
        final ImageButton notification = (ImageButton) findViewById(R.id.button_notification);
        final ImageButton notificationOn = (ImageButton) findViewById(R.id.button_notification_on);

        notification.setVisibility(View.GONE);
        notificationOn.setVisibility(View.GONE);

        final DocumentReference userDB = db.collection("Users").document(userFirebase.getEmail());

//      A decommenter si on veut flusher la base de données des parties en cours
/*
            userDB.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        ArrayList<String> arr = new ArrayList<>();
                        userDB.update("partiesEnCours", arr);
                        userDB.collection("Games").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        if (document.get("real") != null && document.get("real").equals("false")) {
                                            continue;
                                        } else {
                                            userDB.collection("Games").document(document.getId()).delete()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.w(TAG, "Error deleting document", e);
                                                        }
                                                    });
                                        }
                                    }
                                }
                            }
                        });
                    }
                });*/

    //FIN de ce qu'il faut décommenter


        notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate(R.layout.toast,
                        (ViewGroup) findViewById(R.id.custom_toast_container));

                TextView text = (TextView) layout.findViewById(R.id.text);
                text.setText(R.string.no_notification);

                Toast toast = new Toast(getApplicationContext());
                toast.setGravity(Gravity.BOTTOM, 0, 0);
                toast.setDuration(Toast.LENGTH_LONG);
                toast.setView(layout);
                toast.show();

            }
        });

        notificationOn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), NotificationPage.class);
                startActivity(intent);
            }
        });



        userDB.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    //On récupère le nom du joueur
                    Map<String, Object> test = documentSnapshot.getData();
                    final User user = documentSnapshot.toObject(User.class);
                    globalVariables.setUser(user);
                    final Button startGame = (Button) findViewById(R.id.lancer_partie);
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


                    userDB.collection("FriendRequests")
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if(task.isSuccessful()) {
                                        List<DocumentSnapshot> lists = task.getResult().getDocuments();
                                        ArrayList<HashMap<String, String>> arrayListFriendsRequests = new ArrayList<>();
                                        for(DocumentSnapshot doc: lists) {
                                            HashMap<String, String> friendRequest = new HashMap<>();
                                            friendRequest.put("email", doc.getData().get("email").toString());
                                            friendRequest.put("username", doc.getData().get("username").toString());
                                            friendRequest.put("demande", doc.getData().get("demande").toString());
                                            friendRequest.put("pending", doc.getData().get("pending").toString());
                                            friendRequest.put("accepte", doc.getData().get("accepte").toString());

                                            arrayListFriendsRequests.add(friendRequest);
                                        }
                                        user.setFriendRequests(arrayListFriendsRequests);

                                        for (HashMap<String, String> friendRequest: arrayListFriendsRequests) {
                                            if (friendRequest.get("demande").equals("true") &&
                                                    friendRequest.get("pending").equals("false") &&
                                                    friendRequest.get("accepte").equals("true")) {

                                                ArrayList<String> friends = user.getFriends();
                                                if (friends.indexOf(friendRequest.get("email")) == -1) {
                                                    friends.add(friendRequest.get("email"));
                                                    db.collection("Users").document(userFirebase.getEmail())
                                                            .update("friends", friends)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        Log.d("Success", "friend added");
                                                                    } else {
                                                                        Log.d("Error", task.getException().getMessage());
                                                                    }
                                                                }
                                                            });
                                                }
                                            }
                                        }

                                        userDB.collection("Games")
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                        if(task.isSuccessful()) {
                                                            List<DocumentSnapshot> lists = task.getResult().getDocuments();
                                                            ArrayList<String> arrayListGames = new ArrayList<>();
                                                            for(DocumentSnapshot doc: lists) {
                                                                arrayListGames.add(lists.get(0).getId());
                                                            }
                                                            user.setPartiesEnCours(arrayListGames);
                                                            // On affiche un bouton par partie en cours

                                                            if (arrayListGames.size() > 0 || user.getFriendRequests().size() > 0) {
                                                                notification.setVisibility(View.GONE);
                                                                notificationOn.setVisibility(View.VISIBLE);
                                                            } else {
                                                                notification.setVisibility(View.VISIBLE);
                                                                notificationOn.setVisibility(View.GONE);
                                                            }

                                                        } else {
                                                            Log.d("Error getting documents", task.getException().getMessage());
                                                        }
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.d("Error getting documents", e.getMessage());
                                                    }
                                                });
                                    } else {
                                        Log.d("Error getting documents", task.getException().getMessage());
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("Error getting documents", e.getMessage());
                                }
                            });


                    Button addFriends = (Button) findViewById(R.id.add_friends);
                    addFriends.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(v.getContext(), AddFriendsPage.class);
                            startActivity(intent);
                        }
                    });


                }
            }

        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Failure", "problème dans la requête du profil");
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
