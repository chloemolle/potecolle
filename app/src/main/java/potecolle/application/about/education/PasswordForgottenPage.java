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
import android.widget.EditText;
import android.widget.ProgressBar;

import com.example.chloemolle.potecolle.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import static com.firebase.ui.auth.ui.email.CheckEmailFragment.TAG;

public class PasswordForgottenPage extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.password_forgotten_page_layout);

        final ProgressBar pgBar = findViewById(R.id.progress_bar_loading_mdp_oublie);
        pgBar.setVisibility(View.GONE);
        Button reinitialisation = findViewById(R.id.reinitialisation_button);
        reinitialisation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText emailEdit = findViewById(R.id.email_edit);
                String email = emailEdit.getText().toString();

                FirebaseAuth auth = FirebaseAuth.getInstance();
                pgBar.setVisibility(View.VISIBLE);

                final Context context = v.getContext();
                auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    pgBar.setVisibility(View.GONE);
                                    LayoutInflater inflater = getLayoutInflater();
                                    View layout = inflater.inflate(R.layout.toast,
                                            (ViewGroup) findViewById(R.id.custom_toast_container));

                                    Globals.makeToast("On vous a envoyé un mail ;)", layout, context);

                                    Intent intent = new Intent(context, ConnexionPage.class);
                                    startActivity(intent);
                                    Log.d(TAG, "Email sent.");
                                }
                            }
                        });
            }
        });
    }
}
