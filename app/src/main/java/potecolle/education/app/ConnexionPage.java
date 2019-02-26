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

import com.example.chloemolle.potecolle.R;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
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

        TextView seConnecter = findViewById(R.id.se_connecter_button);
        seConnecter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ConnexionSecondPage.class);
                startActivity(intent);
            }
        });

        Button sInscrire = findViewById(R.id.s_inscrire_bouton);
        sInscrire.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), SubscriptionPage.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed(){
        moveTaskToBack(true);
    }


}
