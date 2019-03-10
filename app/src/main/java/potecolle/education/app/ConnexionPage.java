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
import com.firebase.ui.auth.IdpResponse;
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
        setContentView(R.layout.connexion_page_layout);

        Button sInscrire = findViewById(R.id.s_inscrire_bouton);
        sInscrire.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final FirebaseAuth mAuth = FirebaseAuth.getInstance();
                final Context context = v.getContext();
                mAuth.signInAnonymously()
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "signInAnonymously:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    final FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    HashMap<String, Object> newUser = new HashMap<>();
                                    newUser.put("typeAbonnement", "startupForKids");
                                    newUser.put("classe", "StartupForKids");
                                    newUser.put("friends", new ArrayList<String>());
                                    newUser.put("level", 1);
                                    newUser.put("id", user.getUid());
                                    newUser.put("pointsActuels", 0);
                                    Long date = new Date().getTime();
                                    Long numero = date % 10000;

                                    newUser.put("username", "joueur" + numero);

                                    db.collection("Users")
                                            .document(user.getUid())
                                            .set(newUser)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Intent intent = new Intent(context, MainPage.class);
                                                        startActivity(intent);
                                                    } else {
                                                        Log.d("Fail", task.getException().getMessage());
                                                    }
                                                }
                                            });
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInAnonymously:failure", task.getException());
                                }

                                // ...
                            }
                        });

            }
        });
    }

    @Override
    public void onBackPressed(){
        moveTaskToBack(true);
    }


}
