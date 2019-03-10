package potecolle.education.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chloemolle.potecolle.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;

/**
 * Created by chloemolle on 07/12/2018.
 */

public class ConfigureComptePage extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.configure_compte_page_layout);

        Button seDeconnecter = findViewById(R.id.supprimer_compte);
        seDeconnecter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(ConfigureComptePage.this);
                alertDialog.setPositiveButton(R.string.se_deconnecter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        FirebaseAuth.getInstance().signOut();
                        flushGlobalVariables();
                        Intent intent = new Intent(ConfigureComptePage.this, ConnexionPage.class);
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
    }

}