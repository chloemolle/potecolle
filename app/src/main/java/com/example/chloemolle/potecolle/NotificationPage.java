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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

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

        final User user = globalVariables.getUser();
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
                                final Game game = documentSnapshot.toObject(Game.class);

                                //On met à jour la partie suivant si l'adversaire a repondu aux questions
                                db.collection("Users")
                                        .document(game.getAdversaire())
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot document = task.getResult();
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
                                                } else {
                                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                                }

                                            }
                                        });

                            }
                        }
                    });

        }

        ArrayList<HashMap<String, String>> friendRequests = user.getFriendRequests();

        for (final HashMap<String, String> friendRequest: friendRequests) {
            if (friendRequest.get("demande").equals("true")) {
                if (friendRequest.get("pending").equals("true")) {
                    addButtonFriendRequestSent(friendRequest);
                } else {
                    addButtonFriendRequestAccepted(friendRequest);
                }
            } else {
                addButtonFriendRequestReceived(friendRequest);
            }


        }
    }

    private void addButtonFriendRequestSent(final HashMap<String, String> friendRequest){
        final Context context = this;
        final Button newButton = new Button(context);
        final FirebaseUser userFirebase = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final LinearLayout layout = (LinearLayout) findViewById(R.id.layout_partie_en_cours);
        final Globals globalVariables = (Globals) getApplicationContext();


        newButton.setText("Vous avez envoyé une demande d'ami à: " + friendRequest.get("username") + " " + friendRequest.get("email"));
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
        newButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Veux-tu annuler ta demande d'ami à " + friendRequest.get("username"))
                        .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //On supprime la requête de notre database
                                suppressRequestFromDatabase(friendRequest, false);

                                HashMap<String, Object> demandeAccepte = new HashMap<>();
                                demandeAccepte.put("pending", "false");
                                demandeAccepte.put("accepte", "true");

                                //On supprime  celle du demandeur
                                db.collection("Users").document(friendRequest.get("email"))
                                        .collection("FriendRequests")
                                        .document(userFirebase.getEmail())
                                        .delete()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    Log.d("Success", "document successfully deleted");
                                                } else {
                                                    Log.d("Error", "document NOT successfully deleted");
                                                }
                                            }
                                        });
                            }
                        })
                        .setNegativeButton("Non", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d("Info","demande d'ami conservé");
                            }
                        }).show();
            }
        });
        layout.addView(newButton);
    }

    private void addButtonFriendRequestAccepted(final HashMap<String, String> friendRequest){
        final Context context = this;
        final Button newButton = new Button(context);
        final FirebaseUser userFirebase = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final LinearLayout layout = (LinearLayout) findViewById(R.id.layout_partie_en_cours);
        final Globals globalVariables = (Globals) getApplicationContext();


        newButton.setText(friendRequest.get("username") + " " + friendRequest.get("email") + " a accepté votre demande !");
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
        suppressRequestFromDatabase(friendRequest, true);
        layout.addView(newButton);
    }

    private void addButtonFriendRequestReceived(final HashMap<String, String> friendRequest){
        final Context context = this;
        final Button newButton = new Button(context);
        final FirebaseUser userFirebase = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final LinearLayout layout = (LinearLayout) findViewById(R.id.layout_partie_en_cours);
        final Globals globalVariables = (Globals) getApplicationContext();
        final User user = globalVariables.getUser();


        newButton.setText(friendRequest.get("username") + " " + friendRequest.get("email") + " souhaiterait devenir votre ami !");
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
        newButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Veux-tu ajouter " + friendRequest.get("username") + " à tes amis?")
                        .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //On supprime la requête de notre database
                                suppressRequestFromDatabase(friendRequest, true);

                                HashMap<String, Object> demandeAccepte = new HashMap<>();
                                demandeAccepte.put("pending", "false");
                                demandeAccepte.put("accepte", "true");

                                //On met à jour celle de notre pote
                                db.collection("Users").document(friendRequest.get("email"))
                                        .collection("FriendRequests")
                                        .document(userFirebase.getEmail())
                                        .update(demandeAccepte)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    Log.d("Success", "document successfully update");
                                                } else {
                                                    Log.d("Error", "document NOT successfully updated");
                                                }
                                            }
                                        });
                            }
                        })
                        .setNegativeButton("Non", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //On supprime la requête de notre database
                                suppressRequestFromDatabase(friendRequest, false);

                                //On supprime  celle du demandeur
                                db.collection("Users").document(friendRequest.get("email"))
                                        .collection("FriendRequests")
                                        .document(userFirebase.getEmail())
                                        .delete()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    Log.d("Success", "document successfully deleted");
                                                } else {
                                                    Log.d("Error", "document NOT successfully deleted");
                                                }
                                            }
                                        });
                            }
                        }).show();
            }
        });
        layout.addView(newButton);
    }

    // Supprime une requete de FriendRequests et ajoute ou non l'ami à notre base de donnée
    public void suppressRequestFromDatabase(final HashMap<String, String> friendRequest, final Boolean addFriend){
        final Context context = this;
        final Button newButton = new Button(context);
        final FirebaseUser userFirebase = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final LinearLayout layout = (LinearLayout) findViewById(R.id.layout_partie_en_cours);
        final Globals globalVariables = (Globals) getApplicationContext();
        final User user = globalVariables.getUser();

        db.collection("Users").document(userFirebase.getEmail())
                .collection("FriendRequests")
                .document(friendRequest.get("email"))
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            if(addFriend) {
                                ArrayList<String> friends = user.getFriends();
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
                            Log.d("Success", "document successfully deleted");
                            layout.removeView(newButton);
                            LayoutInflater inflater = getLayoutInflater();
                            View layout = inflater.inflate(R.layout.toast,
                                    (ViewGroup) findViewById(R.id.custom_toast_container));

                            TextView text = (TextView) layout.findViewById(R.id.text);
                            if (addFriend) {
                                text.setText(R.string.demande_accepte);
                            } else {
                                text.setText(R.string.demande_deleted);
                            }

                            Toast toast = new Toast(getApplicationContext());
                            toast.setGravity(Gravity.BOTTOM, 0, 0);
                            toast.setDuration(Toast.LENGTH_LONG);
                            toast.setView(layout);
                            toast.show();
                        } else {
                            Log.d("Error", "document NOT successfully deleted");
                        }
                    }
                });

    }


    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this, MainPage.class);
        startActivity(intent);
    }


}
