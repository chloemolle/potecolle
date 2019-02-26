package potecolle.education.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chloemolle.potecolle.R;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static com.firebase.ui.auth.AuthUI.TAG;

public class SubscriptionPage extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subscription_page_layout);

        final ProgressBar pgBar = findViewById(R.id.progress_bar_loading_connexion);
        pgBar.setVisibility(View.GONE);

        Button creerCompte = findViewById(R.id.creer_compte_button);
        creerCompte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pgBar.setVisibility(View.VISIBLE);
                TextView emailEdit = findViewById(R.id.email_edit);
                Pattern mailPattern = Pattern.compile("[^@]+@[^.]+.(fr|com)");
                Boolean isOk = true;
                if (!mailPattern.matcher(emailEdit.getText().toString()).matches()){
                    LayoutInflater inflater = getLayoutInflater();
                    View layoutToast = inflater.inflate(R.layout.toast,
                            (ViewGroup) findViewById(R.id.custom_toast_container));

                    Globals.makeToast("Ton email n'est pas valide", layoutToast, getApplicationContext());
                    emailEdit.setBackgroundColor(getResources().getColor(R.color.red));
                    isOk = false;
                }
                final TextView mdpEdit = findViewById(R.id.mdp_edit);
                if (mdpEdit.getText().toString().length() < 5){
                    LayoutInflater inflater = getLayoutInflater();
                    View layoutToast = inflater.inflate(R.layout.toast,
                            (ViewGroup) findViewById(R.id.custom_toast_container));

                    Globals.makeToast("Ton mot de passe n'est pas suffisament long", layoutToast, getApplicationContext());
                    mdpEdit.setBackgroundColor(getResources().getColor(R.color.red));
                    isOk = false;
                }

                if (!isOk) {
                    pgBar.setVisibility(View.GONE);
                    return;
                }
                final String email = emailEdit.getText().toString();
                final Context context = v.getContext();


                FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
                mFunctions
                        .getHttpsCallable("doesUserExist")
                        .call(email)
                        .continueWith(new Continuation<HttpsCallableResult, String>() {
                            @Override
                            public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                                int result = (int) task.getResult().getData();
                                if (result == 2) {
                                    LayoutInflater inflater = getLayoutInflater();
                                    View layoutToast = inflater.inflate(R.layout.toast,
                                            (ViewGroup) findViewById(R.id.custom_toast_container));

                                    Globals.makeToast("Tu as déjà un compte !", layoutToast, getApplicationContext());
                                    Intent intent = new Intent(context, ConnexionPage.class);
                                    startActivity(intent);
                                } else if (result == 0) {
                                    LayoutInflater inflater = getLayoutInflater();
                                    View layout = inflater.inflate(R.layout.toast,
                                            (ViewGroup) findViewById(R.id.custom_toast_container));
                                    Intent intent = new Intent(context, ConnexionPage.class);
                                    startActivity(intent);

                                    Globals.makeToast("Ton école n'a pas l'air de t'avoir créé un compte. Demande lui de t'en créer un pour que tu puisses jouer ;)", layout, context);


                                } else if (result == 1) {
                                    final FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    final FirebaseAuth mAuth = FirebaseAuth.getInstance();

                                    mAuth.createUserWithEmailAndPassword(email, mdpEdit.getText().toString())
                                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                    if (task.isSuccessful()) {
                                                        // Sign in success, update UI with the signed-in user's information
                                                        Log.d(TAG, "createUserWithEmail:success");
                                                        FirebaseUser userDB = mAuth.getCurrentUser();

                                                        db.collection("Users").document(userDB.getEmail())
                                                                .get()
                                                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                        if(task.isSuccessful()) {

                                                                            final User user = task.getResult().toObject(User.class);
                                                                            Map<String, Object> new_user = new HashMap<>();
                                                                            new_user.put("friends", new ArrayList<String>());
                                                                            new_user.put("level", 1);
                                                                            new_user.put("pointsActuels", 0);
                                                                            user.setFriends(new ArrayList<String>());
                                                                            user.setLevel(1);
                                                                            user.setPointsActuels(0.0);

                                                                            FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
                                                                            mFirebaseAnalytics.setUserProperty("ecole", user.getTypeAbonnement());

                                                                            db.collection("Users").document(email)
                                                                                    .update(new_user)
                                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                        @Override
                                                                                        public void onSuccess(Void aVoid) {
                                                                                            Log.d(TAG, "DocumentSnapshot successfully written!");
                                                                                            Globals globalVariables = (Globals) getApplicationContext();
                                                                                            globalVariables.setUser(user);
                                                                                            Intent intent = new Intent(context, AskForUsername.class);
                                                                                            startActivity(intent);
                                                                                        }
                                                                                    })
                                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                                        @Override
                                                                                        public void onFailure(@NonNull Exception e) {
                                                                                            Log.w(TAG, "Error writing document", e);
                                                                                        }
                                                                                    });



                                                                        } else {
                                                                            LayoutInflater inflater = getLayoutInflater();
                                                                            View layout = inflater.inflate(R.layout.toast,
                                                                                    (ViewGroup) findViewById(R.id.custom_toast_container));

                                                                            Globals.makeToast("Problème de création du compte", layout, context);
                                                                        }
                                                                    }
                                                                });

                                                    } else {
                                                        // If sign in fails, display a message to the user.
                                                        LayoutInflater inflater = getLayoutInflater();
                                                        View layout = inflater.inflate(R.layout.toast,
                                                                (ViewGroup) findViewById(R.id.custom_toast_container));

                                                        Globals.makeToast("Problème de création du compte", layout, context);
                                                    }

                                                    // ...
                                                }
                                            });

                                }
                                return "";
                            }

                        });

            }
        });

    }
}
