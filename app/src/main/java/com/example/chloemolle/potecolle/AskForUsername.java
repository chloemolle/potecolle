package com.example.chloemolle.potecolle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chloemolle on 21/11/2018.
 */

public class AskForUsername extends Activity {

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.ask_for_username_page);

        final Spinner choixClasse = (Spinner) findViewById(R.id.spinner_classe);
        ArrayAdapter<CharSequence> adapterFamily = ArrayAdapter.createFromResource(this,
                R.array.classes, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterFamily.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        choixClasse.setAdapter(adapterFamily);


        Button nextPage = (Button) findViewById(R.id.ok);
        nextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText text = (EditText) findViewById(R.id.username_text);
                String userClass = choixClasse.getSelectedItem().toString();
                final FirebaseFirestore db = FirebaseFirestore.getInstance();
                final FirebaseUser userFirebase = FirebaseAuth.getInstance().getCurrentUser();
                final DocumentReference userDB = db.collection("Users").document(userFirebase.getEmail());

                Map<String, Object> updateUser = new HashMap<>();
                updateUser.put("username", text.getText().toString());
                updateUser.put("classe", userClass);

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
                Intent intent = new Intent(v.getContext(), MainPage.class);
                startActivity(intent);
            }
        });
    }
}
