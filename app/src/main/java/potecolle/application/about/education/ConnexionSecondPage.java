package potecolle.application.about.education;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.chloemolle.potecolle.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.regex.Pattern;

import static com.firebase.ui.auth.AuthUI.TAG;

public class ConnexionSecondPage extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connexion_second_page_layout);

        final ProgressBar pgBar = findViewById(R.id.progress_bar_loading_connexion);
        pgBar.setVisibility(View.GONE);

        Button connexion = findViewById(R.id.connexion_button);
        connexion.setOnClickListener(new View.OnClickListener() {
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

                final FirebaseAuth mAuth = FirebaseAuth.getInstance();
                final FirebaseFirestore db = FirebaseFirestore.getInstance();

                mAuth.signInWithEmailAndPassword(email, mdpEdit.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "signInWithEmail:success");
                                    FirebaseUser userDB = mAuth.getCurrentUser();
                                    db.collection("Users").document(userDB.getEmail())
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                       @Override
                                                                       public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                           if (task.isSuccessful()) {

                                                                               final User user = task.getResult().toObject(User.class);

                                                                               Globals globalVariables = (Globals) getApplicationContext();
                                                                               globalVariables.setUser(user);
                                                                               Intent intent = new Intent(context, MainPage.class);
                                                                               startActivity(intent);
                                                                           }
                                                                       }
                                                                   });
                                } else {
                                    // If sign in fails, display a message to the user.
                                    LayoutInflater inflater = getLayoutInflater();
                                    View layoutToast = inflater.inflate(R.layout.toast,
                                            (ViewGroup) findViewById(R.id.custom_toast_container));
                                    pgBar.setVisibility(View.GONE);
                                    Log.d("Fail", task.getException().getMessage());
                                    Globals.makeToast("Votre identifiant et/ou mot de passe est incorrect", layoutToast, getApplicationContext());
                                }

                            }
                        });


            }
        });

        TextView mdpOublie = findViewById(R.id.mdp_oublie);
        mdpOublie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), PasswordForgottenPage.class);
                startActivity(intent);
            }
        });

    }
}
