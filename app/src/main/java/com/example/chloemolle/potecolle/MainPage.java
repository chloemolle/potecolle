package com.example.chloemolle.potecolle;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
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
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chloemolle on 11/10/2018.
 */

public class MainPage extends Activity {

    public Handler handler;
    public Runnable runnable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page_layout);
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final Globals globalVariables = (Globals) getApplicationContext();

        final FirebaseUser userFirebase = FirebaseAuth.getInstance().getCurrentUser();
        final String userEmail = userFirebase.getEmail();
        globalVariables.setUserDB(db.collection("Users").document(userEmail));

        final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.layout_parametre);

        linearLayout.setVisibility(View.GONE);


        setMenuAppearance(linearLayout);

        setButtonOfMenu(linearLayout);

        setButtonVoirMesParties();

        setButtonSolo();

        getUserInfo(globalVariables.getUser() == null);

        setHandler();

    }

    private void setButtonNotification() {
        final ImageButton notification = (ImageButton) findViewById(R.id.button_notification);
        final ImageButton notificationOn = (ImageButton) findViewById(R.id.button_notification_on);

        checkForNotification(notification, notificationOn);
        setOnClickListenerOfNotification(notification, notificationOn);

    }


    private void setButtonVoirMesParties() {
        Button voirPartie = (Button) findViewById(R.id.voir_mes_parties);
        voirPartie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MesPartiesPage.class);
                startActivity(intent);
            }
        });
    }

    private Boolean getUserInfo(final Boolean createUser) {
        final FirebaseUser userFirebase = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final Globals globalVariables = (Globals) getApplicationContext();

        final DocumentReference userDB;
        try {
            if (globalVariables.getUserDB() == null) {
                final String userEmail = userFirebase.getEmail();
                userDB = db.collection("Users").document(userEmail);
                globalVariables.setUserDB(userDB);
            } else {
                userDB = globalVariables.getUserDB();
            }
        } catch (Exception e) {
            Log.d("exception", e.getMessage());
            return false;
        }


        userDB.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    //On récupère le nom du joueur
                    final User user;
                    if (createUser) {
                        Log.d("INFO", "on a bien créé un user");
                        user = documentSnapshot.toObject(User.class);
                        globalVariables.setUser(user);
                    } else {
                        user = globalVariables.getUser();
                        Log.d("INFO", "on a récupéré un user");
                    }

                    //delete friends
                    deleteFriend(userDB, user);

                    final Button startGame = (Button) findViewById(R.id.lancer_partie);
                    //on crée le bouton pour démarrer une partie
                    startGame.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            globalVariables.setCurrentGame(new Game(globalVariables.getUser().getUsername(), globalVariables.getUser().getClasse(), "Maths", false, true));
                            Intent intent = new Intent(v.getContext(), ChoixSujetPage.class);
                            startActivity(intent);
                        }
                    });


                    //Init view
                    TextView studentName = (TextView) findViewById(R.id.student_name);

                    TextView level = (TextView) findViewById(R.id.niveau);
                    level.setText("Niveau " + user.getLevel().toString());

                    updateProgressBar();


                    getFriendRequestsAndGames(userDB);

                }
            }

        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Failure", e.getMessage());
                    }
                });
        return true;

    }

    private void updateProgressBar() {
        Globals globalVariables = (Globals) getApplicationContext();
        User user = globalVariables.getUser();
        ProgressBar avancement = (ProgressBar) findViewById(R.id.progressBarAvancement);
        double avancementInteger = user.getFormule();
        double pointsActuelsDouble = user.getPointsActuels();

        TextView avancementText = (TextView) findViewById(R.id.avancement);
        avancementText.setText((int) pointsActuelsDouble + "/" + (int) avancementInteger);
        avancement.setMax((int) avancementInteger);
        avancement.setProgress((int) pointsActuelsDouble);
        avancement.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorTheme)));
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
    public void finish(){
        super.finish();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onBackPressed(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        final Context context = this;
        alertDialog.setPositiveButton(R.string.se_deconnecter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                FirebaseAuth test = FirebaseAuth.getInstance();
                test.signOut();
                flushGlobalVariables();
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


    public void flushGlobalVariables() {
        Globals globalVariables = (Globals) getApplicationContext();
        globalVariables.setCurrentGame(null);
        globalVariables.setUserDB(null);
        globalVariables.setUser(null);
        globalVariables.setCurrentQuestionNumero(0);
        globalVariables.setBrouillonText("");
        globalVariables.setReponseText("");
        globalVariables.setDebug(true);
        globalVariables.setTmpTime(0);
        globalVariables.setTest(new Long(0));
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.toast,
                    (ViewGroup) findViewById(R.id.custom_toast_container));

            TextView text = (TextView) layout.findViewById(R.id.text);
            text.setText("Tout va bien");

            Toast toast = new Toast(getApplicationContext());
            toast.setGravity(Gravity.BOTTOM, 0, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();
        } else {
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.toast,
                    (ViewGroup) findViewById(R.id.custom_toast_container));

            TextView text = (TextView) layout.findViewById(R.id.text);
            text.setText("probleme de handler");

            Toast toast = new Toast(getApplicationContext());
            toast.setGravity(Gravity.BOTTOM, 0, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();

        }
    }

    public void setButtonOfMenu(final LinearLayout linearLayout) {
        final Context context = this;

        TextView voirAmi = (TextView) findViewById(R.id.voir_ami);
        voirAmi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linearLayout.setVisibility(View.GONE);
                Intent intent = new Intent(v.getContext(), VoirAmiPage.class);
                startActivity(intent);
            }
        });


        TextView seDeconnecter = (TextView) findViewById(R.id.se_deconnecter);
        seDeconnecter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                alertDialog.setPositiveButton(R.string.se_deconnecter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        FirebaseAuth.getInstance().signOut();
                        flushGlobalVariables();
                        linearLayout.setVisibility(View.GONE);
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
        });

        TextView configurerCompte = (TextView) findViewById(R.id.configurer_compte);
        configurerCompte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linearLayout.setVisibility(View.GONE);
                Intent intent = new Intent(v.getContext(), ConfigureComptePage.class);
                startActivity(intent);
            }
        });

    }

    public void setMenuAppearance(final LinearLayout linearLayout) {

        ImageButton parametre = (ImageButton) findViewById(R.id.parametre_button);
        parametre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (linearLayout.getVisibility() == View.GONE) {
                    linearLayout.setVisibility(View.VISIBLE);
                } else {
                    linearLayout.setVisibility(View.GONE);
                }
            }
        });


        ConstraintLayout constraintLayout = (ConstraintLayout) findViewById(R.id.constraint_layout_id);
        constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (linearLayout.getVisibility() == View.VISIBLE) {
                    linearLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    public void setHandler() {
        this.handler = new Handler();
        final int delay = 100000; //milliseconds
        final Context self = this;
        this.runnable = new Runnable(){
            public void run() {
                Boolean test = getUserInfo(false);
                if (test) {
                    handler.postDelayed(this, delay);
                }
            }
        };

        handler.postDelayed(runnable, delay);
    }

    public void setOnClickListenerOfNotification(ImageButton notification, ImageButton notificationOn) {
        Globals globalVariables = (Globals) getApplicationContext();
        if (globalVariables.getUser().getFriendRequests().size() > 0 || globalVariables.getUser().getPartiesEnCours().size() > 0) {
            notification.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), NotificationPage.class);
                    startActivity(intent);
                }
            });
        } else {
            notification.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.toast,
                            (ViewGroup) findViewById(R.id.custom_toast_container));

                    Globals.makeToast(getResources().getString(R.string.no_notification), layout, v.getContext());
                }
            });
        }

        notificationOn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), NotificationPage.class);
                startActivity(intent);
            }
        });
    }

    public void checkForNotification(ImageButton notification, ImageButton notificationOn) {
        Globals globalVariables = (Globals) getApplicationContext();
        Boolean thereIsNotification = false;
        for (FriendRequest friendRequest: globalVariables.getUser().getFriendRequests()) {
            if (!friendRequest.getVu()) {
                thereIsNotification = true;
                break;
            }
        }

        if (!thereIsNotification) {
            for (Game game: globalVariables.getUser().getPartiesEnCours()) {
                if (!game.getVu()) {
                    thereIsNotification = true;
                }
            }
        }

        if (thereIsNotification) {
            notification.setVisibility(View.GONE);
            notificationOn.setVisibility(View.VISIBLE);
        } else {
            notificationOn.setVisibility(View.GONE);
            notification.setVisibility(View.VISIBLE);
        }

    }

    public void deleteFriend(final DocumentReference userDB, final User user) {
        userDB.collection("FriendDeletion")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult().getDocuments().size() > 0) {
                            List<DocumentSnapshot> docs = task.getResult().getDocuments();
                            ArrayList<String> friends = user.getFriends();
                            for (DocumentSnapshot doc: docs) {
                                friends.remove(doc.get("email"));
                                userDB.collection("FriendDeletion")
                                        .document(doc.getId())
                                        .delete()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Log.d("Sucess", "sucess");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d("Fail", e.getMessage());
                                            }
                                        });
                            }
                            userDB.update("friends", friends)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Log.d("Success", "friends deleted");

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
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("fail", e.getMessage());
            }
        });

    }

    public void getFriendRequestsAndGames(final DocumentReference userDB) {
        final FirebaseUser userFirebase = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final Globals globalVariables = (Globals) getApplicationContext();
        final User user = globalVariables.getUser();

        userDB.collection("FriendRequests")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            List<DocumentSnapshot> lists = task.getResult().getDocuments();
                            ArrayList<FriendRequest> arrayListFriendsRequests = new ArrayList<>();
                            for(DocumentSnapshot doc: lists) {
                                FriendRequest tmp = doc.toObject(FriendRequest.class);
                                tmp.setId(doc.getId());
                                arrayListFriendsRequests.add(tmp);
                            }
                            user.setFriendRequests(arrayListFriendsRequests);
                            for (FriendRequest friendRequest: arrayListFriendsRequests) {
                                if (friendRequest.getDemande() &&
                                        !friendRequest.getPending() &&
                                        friendRequest.getAccepte()) {

                                    if (!friendRequest.getVu()) {
                                        Integer previousLevel = user.getLevel();
                                        user.addPoints(100);

                                        if(previousLevel != user.getLevel()) {
                                            openPopup();
                                        }
                                        updateProgressBar();
                                        LayoutInflater inflater = getLayoutInflater();
                                        View layout = inflater.inflate(R.layout.toast,
                                                (ViewGroup) findViewById(R.id.custom_toast_container));

                                        Globals.makeToast("Bravo ! Vous avez gagné 100 points en ajoutant un ami!", layout, getApplicationContext());

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


                                        HashMap<String, Object> updateFriendRequest = new HashMap<>();
                                        updateFriendRequest.put("vu", true);
                                        db.collection("Users").document(userFirebase.getEmail())
                                                .collection("FriendRequests")
                                                .document(friendRequest.getId())
                                                .update(updateFriendRequest)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Log.d("Success", "update");
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
                                                ArrayList<Game> arrayListGames = new ArrayList<>();
                                                for(DocumentSnapshot doc: lists) {
                                                    arrayListGames.add(doc.toObject(Game.class));
                                                }
                                                user.setPartiesEnCours(arrayListGames);
                                                globalVariables.setUser(user);
                                                setButtonNotification();
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

    }


    public void setButtonSolo(){
        final Globals globalVariable = (Globals) getApplicationContext();
        Button solo = (Button) findViewById(R.id.s_entrainer);
        solo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ChoixSujetPage.class);
                User user = globalVariable.getUser();
                Game game = new Game(user.getUsername(), user.getClasse(), "Maths", true, false);
                globalVariable.setCurrentGame(game);
                startActivity(intent);
            }
        });
    }

}