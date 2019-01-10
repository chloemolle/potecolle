package com.example.chloemolle.potecolle;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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

        final LinearLayout layout = (LinearLayout) findViewById(R.id.layout_partie_en_cours);

        final User user = globalVariables.getUser();
        ArrayList<Game> parties = user.getPartiesEnCours();

        final DocumentReference userDB = db.collection("Users").document(userFirebase.getEmail());

        Button backButton = (Button) findViewById(R.id.retour_main_page_from_notification);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MainPage.class);
                startActivity(intent);
            }
        });


        for (final Game partie: parties) {
            createButton(partie, layout, userDB);
        }

        ArrayList<FriendRequest> friendRequests = user.getFriendRequests();

        for (final FriendRequest friendRequest: friendRequests) {
            if (friendRequest.getDemande()) {
                if (friendRequest.getPending()) {
                    addButtonFriendRequestSent(friendRequest);
                } else {
                    addButtonFriendRequestAccepted(friendRequest);
                }
            } else {
                addButtonFriendRequestReceived(friendRequest);
            }
        }
    }

    private void addButtonFriendRequestSent(final FriendRequest friendRequest){
        final Context context = this;
        final Button newButton = new Button(context);
        final FirebaseUser userFirebase = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final LinearLayout layout = (LinearLayout) findViewById(R.id.layout_partie_en_cours);

        newButton.setText(friendRequest.getUsername() + ": demande envoyé !");
        newButton.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(25,25,25,0);
        newButton.setPadding(100, 0, 100, 0);
        newButton.setLayoutParams(params);
        newButton.setTextColor(getResources().getColor(R.color.colorTheme));
        newButton.setBackground(getResources().getDrawable(R.drawable.box_pour_entoure));
        newButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Veux-tu annuler ton invitation à " + friendRequest.getUsername())
                        .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //On supprime la requête de notre database
                                suppressRequestFromDatabase(friendRequest, false, newButton, true, true);

                                HashMap<String, Object> demandeAccepte = new HashMap<>();
                                demandeAccepte.put("pending", false);
                                demandeAccepte.put("accepte", true);

                                //On supprime  celle du demandeur
                                db.collection("Users").document(friendRequest.getEmail())
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
                                Log.d("Info","invitation conservé");
                            }
                        }).show();
            }
        });
        layout.addView(newButton);
    }

    private void addButtonFriendRequestAccepted(final FriendRequest friendRequest){
        final Context context = this;
        final Button newButton = new Button(context);
        final LinearLayout layout = (LinearLayout) findViewById(R.id.layout_partie_en_cours);


        newButton.setText(friendRequest.getUsername() + " a accepté votre demande !");
        newButton.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(25,25,25,0);
        newButton.setPadding(100, 0, 100, 0);
        newButton.setLayoutParams(params);
        newButton.setTextColor(getResources().getColor(R.color.colorTheme));
        newButton.setBackground(getResources().getDrawable(R.drawable.box_pour_entoure));
        suppressRequestFromDatabase(friendRequest, false, newButton, false, false);
        layout.addView(newButton);
    }

    private void addButtonFriendRequestReceived(final FriendRequest friendRequest){
        final Context context = this;
        final Button newButton = new Button(context);
        final FirebaseUser userFirebase = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final LinearLayout layout = (LinearLayout) findViewById(R.id.layout_partie_en_cours);
        final Globals globalVariables = (Globals) getApplicationContext();
        final User user = globalVariables.getUser();

        if (!friendRequest.getVu()) {
            globalVariables.getUserDB().collection("FriendRequests")
                    .document(friendRequest.getId())
                    .update("vu", true)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d("Reussi", "update réussi");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("Fail", e.getMessage());
                        }
                    });
            newButton.setBackground(getResources().getDrawable(R.drawable.box_pour_entoure));
        } else {
            newButton.setBackground(getResources().getDrawable(R.drawable.box_pour_entoure_deja_vu));
        }

        newButton.setText(friendRequest.getUsername() + " souhaiterait devenir votre pote !");
        newButton.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(25,25,25,0);
        newButton.setPadding(100, 0, 100, 0);
        newButton.setLayoutParams(params);
        newButton.setTextColor(getResources().getColor(R.color.colorTheme));
        newButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Veux-tu ajouter " + friendRequest.getUsername() + " à tes potes?")
                        .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //On supprime la requête de notre database
                                suppressRequestFromDatabase(friendRequest, true, newButton, true, true);
                                Integer previousLevel = user.getLevel();
                                user.addPoints(100);

                                if (previousLevel != user.getLevel()) {
                                    openPopup();
                                }

                                HashMap<String, Object> updateUser = new HashMap<>();
                                updateUser.put("level", user.getLevel());
                                updateUser.put("pointsActuels", user.getPointsActuels());
                                db.collection("Users").document(userFirebase.getEmail())
                                        .update(updateUser)
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
                                LayoutInflater inflater = getLayoutInflater();
                                View layout = inflater.inflate(R.layout.toast,
                                        (ViewGroup) findViewById(R.id.custom_toast_container));

                                TextView textToast = (TextView) layout.findViewById(R.id.text);
                                textToast.setText("Bravo ! Vous avez gagné 100 points en ajoutant un pote!");
                                Toast toast = new Toast(getApplicationContext());
                                toast.setGravity(Gravity.BOTTOM, 0, 0);
                                toast.setDuration(Toast.LENGTH_LONG);
                                toast.setView(layout);
                                toast.show();



                                HashMap<String, Object> demandeAccepte = new HashMap<>();
                                demandeAccepte.put("pending", false);
                                demandeAccepte.put("accepte", true);

                                //On met à jour celle de notre pote
                                db.collection("Users").document(friendRequest.getEmail())
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
                                suppressRequestFromDatabase(friendRequest, false, newButton, true, true);

                                //On supprime  celle du demandeur
                                db.collection("Users").document(friendRequest.getEmail())
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
    public void suppressRequestFromDatabase(final FriendRequest friendRequest, final Boolean addFriend, final Button newButton, final Boolean showMessage, final Boolean suppressLayout){
        final Context context = this;
        final FirebaseUser userFirebase = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final LinearLayout layout = (LinearLayout) findViewById(R.id.layout_partie_en_cours);
        final Globals globalVariables = (Globals) getApplicationContext();
        final User user = globalVariables.getUser();

        db.collection("Users").document(userFirebase.getEmail())
                .collection("FriendRequests")
                .document(friendRequest.getEmail())
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            if(addFriend) {
                                ArrayList<String> friends = user.getFriends();
                                if (friends.indexOf(friendRequest.getEmail()) == -1) {
                                    friends.add(friendRequest.getEmail());
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
                            Log.d("Success", "document successfully deleted");
                            if(suppressLayout) {
                                layout.removeView(newButton);
                            }
                            if (showMessage) {
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
                            }

                        } else {
                            Log.d("Error", "document NOT successfully deleted");
                        }
                    }
                });

    }

    private void createButton(final Game game, final LinearLayout layout, final DocumentReference userDB){
        final Context context = this;
        final Globals globalVariables = (Globals) getApplicationContext();
        // on ajoute un bouton pour accéder à la partie (/!!!!\ à changer rapidement
        final Button newButton = new Button(this);

        final Boolean repondu = game.getRepondu();
        final Boolean fini = game.getFini();

        final String finiOuPas = (repondu && !fini)?
                "Attends que ton pote " + game.getPlayer2() + " joue!" :
                (repondu && fini) ?
                        "Regarde les résultats !\n"+ game.getPlayer2() : "Réponds aux questions de " + game.getPlayer2() + " :) ";


        newButton.setText(finiOuPas + "\n" + game.getClasse() + "\n" + game.getMatiere() + " " + game.getSujet());
        newButton.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(25,25,25,0);
        newButton.setPadding(100, 0, 100, 0);
        newButton.setLayoutParams(params);
        newButton.setTextColor(getResources().getColor(R.color.colorTheme));

        if (!game.getVu()) {
            globalVariables.getUserDB().collection("Games")
                    .document(game.getId())
                    .update("vu", true)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d("Reussi", "update réussi");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("Fail", e.getMessage());
                        }
                    });
            newButton.setBackground(getResources().getDrawable(R.drawable.box_pour_entoure));
        } else {
            newButton.setBackground(getResources().getDrawable(R.drawable.box_pour_entoure_deja_vu));
        }

        if (!repondu) {
            newButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    globalVariables.setCurrentGame(game);
                    Intent intent = new Intent(v.getContext(), LoadingQuizPage.class);
                    startActivity(intent);
                }
            });
        } else if (fini) {
            newButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    globalVariables.setCurrentGame(game);
                    Intent intent = new Intent(v.getContext(), ResultPage.class);
                    startActivity(intent);
                }
            });

            newButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("Veux-tu supprimer cette partie ?")
                            .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    userDB.collection("Games")
                                            .document(game.getId())
                                            .delete()
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
                                }
                            })
                            .setNegativeButton("Non", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.d("Info","partie conservée");
                                }
                            }).show();
                    return true;
                }
            });


        }

        layout.addView(newButton);

    }


    private void openPopup() {
        Globals globalVariables = (Globals) getApplicationContext();
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_level_up);
        Button retour = (Button) dialog.findViewById(R.id.retour_popup);
        TextView textBravo = (TextView) dialog.findViewById(R.id.bravo_text);
        textBravo.setText("Bravo !");
        TextView text = (TextView) dialog.findViewById(R.id.level_up_text);
        text.setText("Tu passes au niveau " + globalVariables.getUser().getLevel() + " ;)");
        retour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this, MainPage.class);
        startActivity(intent);
    }


}
