package potecolle.application.about.education;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.chloemolle.potecolle.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
