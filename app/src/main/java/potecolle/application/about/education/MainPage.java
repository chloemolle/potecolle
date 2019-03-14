package potecolle.application.about.education;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.chloemolle.potecolle.R;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by chloemolle on 11/10/2018.
 */

public class MainPage extends Activity {

    public Handler handler;
    public Runnable runnable;
    private Context context;

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

        getUserInfo((globalVariables.getUser() == null || globalVariables.getUser().getLevel() == null), true);

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

    private Boolean getUserInfo(final Boolean createUser, final Boolean firstOccurence) {
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
                            Date date = new Date();
                            Long tmpDate = date.getTime();
                            final String id = tmpDate.toString();
                            globalVariables.setCurrentGame(new Game(globalVariables.getUser().getUsername(), globalVariables.getUser().getClasse(), "Maths", false, true, id));
                            Intent intent = new Intent(v.getContext(), ChoixSujetPage.class);
                            startActivity(intent);
                        }
                    });


                    //Init view
                    TextView studentName = (TextView) findViewById(R.id.student_name);

                    TextView level = (TextView) findViewById(R.id.niveau);
                    level.setText("Niveau " + user.getLevel().toString());

                    updateProgressBar();

                    if (firstOccurence) {
                        setLeaderboard(globalVariables);
                        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.layout_parametre);
                        setButtonOfMenu(linearLayout);
                        setButtonVoirMesParties();
                        setButtonSolo();
                    }

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
        Double avancementInteger = user.getFormule();
        Double pointsActuelsDouble = user.getPointsActuels();

        TextView avancementText = (TextView) findViewById(R.id.avancement);
        avancementText.setText(pointsActuelsDouble.intValue() + "/" + avancementInteger.intValue());
        avancement.setMax(avancementInteger.intValue());
        avancement.setProgress(pointsActuelsDouble.intValue());
        avancement.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorTheme)));
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
                Boolean test = getUserInfo(false, false);
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
                                            Globals.openPopup(context, globalVariables.getUser().getLevel());
                                        }
                                        updateProgressBar();
                                        LayoutInflater inflater = getLayoutInflater();
                                        View layout = inflater.inflate(R.layout.toast,
                                                (ViewGroup) findViewById(R.id.custom_toast_container));

                                        Globals.makeToast("Bravo ! Vous avez gagné 100 points en ajoutant un pote !", layout, getApplicationContext());

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
                                                    Game game = doc.toObject(Game.class);
                                                    for(int i = 0; i < game.getQuestions().size(); i ++) {
                                                        ArrayList<HashMap<String, Object>> questions = (ArrayList<HashMap<String, Object>>) doc.get("questions");
                                                        game.setPropositions(game.getQuestions().get(i), (ArrayList<String>) questions.get(i).get("propositions"));
                                                    }
                                                    arrayListGames.add(game);
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
                Date date = new Date();
                Long tmpDate = date.getTime();
                final String id = tmpDate.toString();
                Game game = new Game(user.getUsername(), user.getClasse(), "Maths", true, false, id);
                globalVariable.setCurrentGame(game);
                startActivity(intent);
            }
        });
    }

    public void setLeaderboard(final Globals globalVariables) {
        final Context context = this;
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users")
                .whereArrayContains("friends", globalVariables.getUserDB().getId())
                .orderBy("level")
                .orderBy("pointsActuels")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            User user = globalVariables.getUser();
                            ArrayList<User> leaderboardData = new ArrayList<>();
                            Log.d("Success", "ok ! ");
                            List<DocumentSnapshot> query = task.getResult().getDocuments();
                            Boolean alreadyAdded = false;
                            for (DocumentSnapshot friend: query) {
                                User friendUser = friend.toObject(User.class);

                                if (user.getFriends().contains(friend.getId())) {
                                    if (!alreadyAdded && (Integer.valueOf(friendUser.getLevel()) == user.getLevel() && Double.valueOf(friendUser.getPointsActuels()) <= user.getPointsActuels()) || Integer.valueOf(friendUser.getLevel()) < user.getLevel())  {
                                        alreadyAdded = true;
                                        leaderboardData.add(user);
                                    }
                                    leaderboardData.add(friendUser);
                                }

                            }

                            if (!alreadyAdded) {
                                leaderboardData.add(user);
                            }

                            LinearLayout nestedScrollView = (LinearLayout) findViewById(R.id.nested_scrollview_leaderboard_linear_layout);

                            for (User leader: leaderboardData) {
                                LinearLayout linearLayout = new LinearLayout(context);
                                linearLayout.setBackground(getResources().getDrawable(R.drawable.box_sans_radius));
                                linearLayout.setPadding(2, 0, 2, 2);

                                TextView textViewUsername = new TextView(context);
                                textViewUsername.setGravity(Gravity.CENTER);
                                textViewUsername.setTextColor(getResources().getColor(R.color.colorTheme));
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                        0,
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        1
                                );
                                textViewUsername.setLayoutParams(params);

                                TextView textViewLevel = new TextView(context);
                                textViewLevel.setGravity(Gravity.CENTER);
                                textViewLevel.setTextColor(getResources().getColor(R.color.colorTheme));
                                textViewLevel.setLayoutParams(params);

                                TextView textViewPoints = new TextView(context);
                                textViewPoints.setGravity(Gravity.CENTER);
                                textViewPoints.setTextColor(getResources().getColor(R.color.colorTheme));
                                textViewPoints.setLayoutParams(params);

                                linearLayout.addView(textViewUsername);
                                linearLayout.addView(textViewLevel);
                                linearLayout.addView(textViewPoints);

                                if (leader.getUsername().equals(user.getUsername())) {
                                    textViewUsername.setText("Moi");
                                    textViewLevel.setText(user.getLevel().toString());
                                    textViewPoints.setText("" + user.getPointsActuels().intValue());
                                } else {
                                    textViewUsername.setText(leader.getUsername());
                                    textViewLevel.setText(leader.getLevel().toString());
                                    textViewPoints.setText("" + leader.getPointsActuels().intValue());
                                }
                                nestedScrollView.addView(linearLayout);

                            }

                        } else {
                            Log.d("Fail", task.getException().getMessage());
                        }
                    }
                });
    }

}