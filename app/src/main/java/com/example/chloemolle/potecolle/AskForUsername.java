package com.example.chloemolle.potecolle;

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

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chloemolle on 21/11/2018.
 */

public class AskForUsername extends Activity {

    @Override
    public void onBackPressed(){
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.ask_for_username_page);

        final ProgressBar progressBarCheckUsername = (ProgressBar) findViewById(R.id.progress_bar_check_username);
        progressBarCheckUsername.setVisibility(View.GONE);

        final Spinner choixClasse = setSpinner();

        Button nextPage = (Button) findViewById(R.id.ok);
        nextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText text = (EditText) findViewById(R.id.username_text);
                final Context context = v.getContext();
                progressBarCheckUsername.setVisibility(View.VISIBLE);


                FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
                mFunctions
                        .getHttpsCallable("getUsers")
                        .call("")
                        .continueWith(new Continuation<HttpsCallableResult, String>() {
                            @Override
                            public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                                if (!checkIfAlreadyUsed(task, text)) {
                                    updateUser(choixClasse);
                                    Intent intent = new Intent(context, MainPage.class);
                                    startActivity(intent);
                                } else {
                                    progressBarCheckUsername.setVisibility(View.GONE);
                                    LayoutInflater inflater = getLayoutInflater();
                                    View layout = inflater.inflate(R.layout.toast,
                                            (ViewGroup) findViewById(R.id.custom_toast_container));

                                    Globals.makeToast(getResources().getString(R.string.username_already_used), layout, getApplicationContext());
                                }
                                return "";
                            }
                        });
            }
        });
    }

    public Boolean checkIfAlreadyUsed(Task<HttpsCallableResult> task, EditText text) {
        final ArrayList<HashMap<String, String>> arr = (ArrayList<HashMap<String, String>>) task.getResult().getData();
        String username = text.getText().toString();
        for (final HashMap<String, String> user : arr) {
            final String usernameTemp = user.get("username");
            if (usernameTemp.toLowerCase().trim().equals(username.toLowerCase().trim())) {
                return true;
            }
        }
        return false;
    }

    public Spinner setSpinner(){
        Spinner choixClasse = (Spinner) findViewById(R.id.spinner_classe);
        ArrayAdapter<CharSequence> adapterFamily = ArrayAdapter.createFromResource(this,
                R.array.classes, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterFamily.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        choixClasse.setAdapter(adapterFamily);
        return choixClasse;
    }

    public void updateUser(Spinner choixClasse) {
        final EditText text = (EditText) findViewById(R.id.username_text);
        final String userClass = choixClasse.getSelectedItem().toString();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseUser userFirebase = FirebaseAuth.getInstance().getCurrentUser();
        final DocumentReference userDB = db.collection("Users").document(userFirebase.getEmail());

        Map<String, Object> updateUser = new HashMap<>();
        updateUser.put("username", text.getText().toString());
        updateUser.put("classe", userClass);
        updateUser.put("level", 1);
        updateUser.put("pointsActuels", 0);

        userDB.update(updateUser)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Success", "update ok");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Failure", "update pas ok");
                    }
                });
    }

}