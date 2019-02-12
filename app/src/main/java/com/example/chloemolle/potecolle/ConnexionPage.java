package com.example.chloemolle.potecolle;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.*;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.firebase.ui.auth.AuthUI.TAG;

/**
 * Created by chloemolle on 11/10/2018.
 */


public class ConnexionPage extends Activity {

    private static int RC_SIGN_IN = 100;
    private static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Cas où on est déjà connecté
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Intent intent = new Intent(this, MainPage.class);
            startActivity(intent);
            return;
        }
        super.onCreate(savedInstanceState);
        ConnexionPage.context = this;
        setContentView(R.layout.connexion_page_layout);

        Button seConnecter = (Button) findViewById(R.id.s_inscrire_bouton);
        seConnecter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Choose authentication providers
                List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.EmailBuilder().build());

                // Create and launch sign-in intent
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setAvailableProviders(providers)
                                .build(),
                        RC_SIGN_IN);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            final IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                // Successfully signed in

                createUser();

            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }


    public void createUser() {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String email = user.getEmail();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final Context context = this;

        db.collection("Users")
                .document(email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            User user = task.getResult().toObject(User.class);
                            if (user != null) {
                                ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.layout_connexion);
                                Button bouton = (Button) findViewById(R.id.s_inscrire_bouton);
                                ImageView logo = (ImageView) findViewById(R.id.logo);
                                layout.removeView(bouton);
                                layout.removeView(logo);
                                ProgressBar progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleLarge);
                                progressBar.setVisibility(View.VISIBLE);
                                layout.addView(progressBar);

                                Globals globalVariables = (Globals) getApplicationContext();
                                globalVariables.setUser(user);
                                if (user.getLevel() != null) {
                                    Map<String, Object> doc = task.getResult().getData();
                                    Intent intent = new Intent(ConnexionPage.context, MainPage.class);
                                    startActivity(intent);
                                } else {
                                    Log.d(TAG, "Creating new user: ", task.getException());
                                    Map<String, Object> new_user = new HashMap<>();
                                    new_user.put("friends", new ArrayList<String>());
                                    new_user.put("level", 1);
                                    new_user.put("pointsActuels", 0);

                                    db.collection("Users").document(email)
                                            .update(new_user)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d(TAG, "DocumentSnapshot successfully written!");
                                                    Intent intent = new Intent(ConnexionPage.context, AskForUsername.class);
                                                    startActivity(intent);
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.w(TAG, "Error writing document", e);
                                                }
                                            });

                                }
                            } else {
                                LayoutInflater inflater = getLayoutInflater();
                                View layout = inflater.inflate(R.layout.toast,
                                        (ViewGroup) findViewById(R.id.custom_toast_container));

                                Globals.makeToast("Ton école n'a pas l'air de t'avoir créé un compte. Demande lui de t'en créer un pour que tu puisses jouer ;)",layout,context);

                                AuthUI.getInstance()
                                        .delete(context)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    // Deletion succeeded
                                                } else {
                                                    // Deletion failed
                                                }
                                            }
                                        });

                            }
                        }
                    }
                });
    }

    @Override
    public void onBackPressed(){
        moveTaskToBack(true);
    }


}
