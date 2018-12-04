package com.example.chloemolle.potecolle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

/**
 * Created by chloemolle on 28/10/2018.
 */

public class ResultPage extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result_page_layout);
        final Globals globalVariables = (Globals) getApplicationContext();

        final Game game = globalVariables.getCurrentGame();

        TextView text = (TextView) findViewById(R.id.result_quiz_text);
        String votreScoreString = game.getScore();
        String sonScoreString = game.getScoreOpponent();

        Integer votreScore = Integer.parseInt(votreScoreString);
        Integer sonScore = Integer.parseInt(sonScoreString);

        String quiAGagne =  votreScore > sonScore ? " Vous avez gagné ! " :
                votreScore == sonScore ? " Vous êtes à égalité ! " : " Vous avez perdu mais pas de soucis, vous pouvez le redéfier ! ";
        text.setText("Voici les résultats !" + quiAGagne);
        text.setTextColor(getResources().getColor(R.color.colorTheme));

        TextView votreScoreText = (TextView) findViewById(R.id.result_quiz_text_votre_score);
        votreScoreText.setText("Votre score " + game.getScore());
        votreScoreText.setTextColor(getResources().getColor(R.color.colorTheme));

        TextView sonScoreText = (TextView) findViewById(R.id.result_quiz_text_son_score);
        sonScoreText.setText("Son score " + game.getScoreOpponent());
        sonScoreText.setTextColor(getResources().getColor(R.color.colorTheme));


        if(!game.getScoreVu()) {
            User user = globalVariables.getUser();
            Integer pointsToAdd = votreScore > sonScore ? 75 + (2 * votreScore - sonScore) * 10 : 50 + votreScore * 10;

            if (globalVariables.getCurrentGame().getTimed().equals("true")) {
               for (Integer integer: globalVariables.getCurrentGame().getReponsesTemps()){
                   pointsToAdd += integer;
               }
            }

            user.addPoints(pointsToAdd);

            HashMap<String, Object> updateUser = new HashMap<>();
            updateUser.put("level", user.getLevel());
            updateUser.put("pointsActuels", user.getPointsActuels());

            final FirebaseFirestore db = FirebaseFirestore.getInstance();
            final FirebaseUser userAuth = FirebaseAuth.getInstance().getCurrentUser();
            final DocumentReference userDB = db.collection("Users").document(userAuth.getEmail());

            userDB.update(updateUser)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d("Success", "C'est tout bon");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("Fail", "C'est pas bon");
                        }
                    });
            userDB.collection("Games").document(game.getId())
                    .update("scoreVu", true);

            game.setScoreVu(true);

        }
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this, NotificationPage.class);
        startActivity(intent);
    }

}

