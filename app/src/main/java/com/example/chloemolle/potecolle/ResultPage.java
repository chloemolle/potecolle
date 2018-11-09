package com.example.chloemolle.potecolle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Created by chloemolle on 28/10/2018.
 */

public class ResultPage extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result_page_layout);
        final Globals globalVariables = (Globals) getApplicationContext();

        TextView text = (TextView) findViewById(R.id.result_quiz_text);
        String votreScoreString = globalVariables.getCurrentGame().getScore();
        String sonScoreString = globalVariables.getCurrentGame().getScoreOpponent();

        Integer votreScore = Integer.parseInt(votreScoreString);
        Integer sonScore = Integer.parseInt(sonScoreString);

        String quiAGagne =  votreScore > sonScore ? " Vous avez gagné ! " :
                votreScore == sonScore ? " Vous êtes à égalité ! " : " Vous avez perdu mais pas de soucis, vous pouvez le redéfier ! ";
        text.setText("Voici les résultats !" + quiAGagne);
        text.setTextColor(getResources().getColor(R.color.colorTheme));

        TextView votreScoreText = (TextView) findViewById(R.id.result_quiz_text_votre_score);
        votreScoreText.setText("Votre score " + globalVariables.getCurrentGame().getScore());
        votreScoreText.setTextColor(getResources().getColor(R.color.colorTheme));

        TextView sonScoreText = (TextView) findViewById(R.id.result_quiz_text_son_score);
        sonScoreText.setText("Son score " + globalVariables.getCurrentGame().getScoreOpponent());
        sonScoreText.setTextColor(getResources().getColor(R.color.colorTheme));
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this, NotificationPage.class);
        startActivity(intent);
    }

}

