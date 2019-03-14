package potecolle.application.about.education;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by chloemolle on 25/10/2018.
 */

public class FirstPage extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Cas où on est déjà connecté
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Intent intent = new Intent(this, MainPage.class);
            startActivity(intent);
            return;
        } else {
            Intent intent = new Intent(this, ConnexionPage.class);
            startActivity(intent);
        }
    }


}
