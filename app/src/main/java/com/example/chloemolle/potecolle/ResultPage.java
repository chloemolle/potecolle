package com.example.chloemolle.potecolle;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

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

        String quiAGagne =  votreScore > sonScore ? " VICTOIRE ! :D" :
                votreScore == sonScore ? " EGALITE ! :)" : " Perdu :( \n Prends ta revanche ! ";
        text.setText(quiAGagne);
        text.setGravity(Gravity.CENTER);
        text.setTextSize(30);
        text.setTextColor(getResources().getColor(R.color.colorTheme));

        TextView votreScoreText = (TextView) findViewById(R.id.result_quiz_text_votre_score);
        votreScoreText.setTextSize(20);
        votreScoreText.setText("Votre score: " + game.getScore() + "/" + game.getQuestionsId().size());
        votreScoreText.setTextColor(getResources().getColor(R.color.colorTheme));

        TextView sonScoreText = (TextView) findViewById(R.id.result_quiz_text_son_score);
        sonScoreText.setTextSize(20);
        sonScoreText.setText("Son score: " + game.getScoreOpponent() + "/" + game.getQuestionsId().size());
        sonScoreText.setTextColor(getResources().getColor(R.color.colorTheme));


        if(!game.getScoreVu()) {
            User user = globalVariables.getUser();
            Integer pointsToAdd = votreScore > sonScore ? 75 + (2 * votreScore - sonScore) * 10 : 50 + votreScore * 10;

            if (globalVariables.getCurrentGame().getTimed().equals("true")) {
               for (Integer integer: globalVariables.getCurrentGame().getReponsesTemps()){
                   pointsToAdd += integer;
               }
            }

            Integer previousLevel = user.getLevel();
            user.addPoints(pointsToAdd);
            if (previousLevel != user.getLevel()) {
                openPopup();
            }
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.toast,
                    (ViewGroup) findViewById(R.id.custom_toast_container));

            TextView textToast = (TextView) layout.findViewById(R.id.text);
            textToast.setText("Bravo ! Vous avez gagné: " + pointsToAdd + " points !");
            Toast toast = new Toast(getApplicationContext());
            toast.setGravity(Gravity.BOTTOM, 0, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();


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

    private void openPopup() {
        Globals globalVariables = (Globals) getApplicationContext();
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_level_up);
        Button retour = (Button) dialog.findViewById(R.id.retour_popup);
        TextView textBravo = (TextView) dialog.findViewById(R.id.bravo_text);
        textBravo.setText("Bravo !");
        TextView text = (TextView) dialog.findViewById(R.id.level_up_text);
        text.setText("Tu passes au niveau " + globalVariables.getUser().getLevel() + " ;)");
        retour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this, NotificationPage.class);
        startActivity(intent);
    }

}

