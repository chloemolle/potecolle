package com.example.chloemolle.potecolle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

/**
 * Created by chloemolle on 11/10/2018.
 */

public class MainPage extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page_layout);

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final Globals globalVariables = (Globals) getApplicationContext();


        DocumentReference userDB = db.collection("Users").document(user.getEmail());

        userDB.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task< DocumentSnapshot > task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot doc = task.getResult();
                            TextView studentName = (TextView) findViewById(R.id.student_name);
                            String userName = doc.getString("username");
                            studentName.setText("Salut " + userName + " !");
                            globalVariables.setUserName(userName);
                            globalVariables.setCurrentGame(new Game(globalVariables.getUserName(), doc.getString("classe")));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });

        Button startGame = (Button) findViewById(R.id.lancer_partie);
        startGame.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ChoixMatierePage.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed(){
        moveTaskToBack(true);
    }

}
