package com.example.chloemolle.potecolle;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
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
                ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.layout_connexion);
                Button bouton = (Button) findViewById(R.id.s_inscrire_bouton);
                TextView seConnecter = (TextView) findViewById(R.id.se_connecter_texte);
                ImageView logo = (ImageView) findViewById(R.id.logo);
                layout.removeView(seConnecter);
                layout.removeView(bouton);
                layout.removeView(logo);
                ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);
                progressBar.setVisibility(View.VISIBLE);
                layout.addView(progressBar);
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                final String email = user.getEmail();
                final FirebaseFirestore db = FirebaseFirestore.getInstance();

                db.collection("Users")
                        .document(email)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful() && task.getResult().getData() != null) {
                                    Map<String, Object> doc = task.getResult().getData();
                                    Intent intent = new Intent(ConnexionPage.context, MainPage.class);
                                    startActivity(intent);

                                } else {
                                    Log.d(TAG, "Creating new user: ", task.getException());
                                    Map<String, Object> new_user = new HashMap<>();
                                    new_user.put("classe", "Troisieme");
                                    new_user.put("friends", new ArrayList<String>());
                                    new_user.put("username", email);
                                    new_user.put("level", 1);
                                    new_user.put("pointsActuels", 0);

                                    db.collection("Users").document(email)
                                            .set(new_user)
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
                            }
                        });

            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }

    @Override
    public void onBackPressed(){
        moveTaskToBack(true);
    }


}
