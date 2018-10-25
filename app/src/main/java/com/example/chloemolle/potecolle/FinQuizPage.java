package com.example.chloemolle.potecolle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by chloemolle on 24/10/2018.
 */

public class FinQuizPage extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.final_quiz_page_layout);

        TextView text = (TextView) findViewById(R.id.fin_quiz_text);

        final Globals globalVariables = (Globals) getApplicationContext();

        ArrayList<String> player1Answers = globalVariables.getCurrentGame().getPlayer1Answers();
        ArrayList<Map<String,Object>> realAnswers = globalVariables.getCurrentGame().getQuestions();

        Integer score = 0;
        for (Integer i = 0; i < player1Answers.size(); i ++) {
            String playerAnswer = player1Answers.get(i);
            Object answer = realAnswers.get(i).get("reponse");
            String realAnswer = "";
            try {
                realAnswer = answer.toString();
            } catch (Exception e) {
                Log.e("ERROR", "probleme" + realAnswers.get(i));
            }
            if (playerAnswer.equals(realAnswer)) {
                score ++;
            }
        }

        text.setText("Bravo ! Ton score: " + score + ". Reviens plus tard pour voir si tu as battu ton pote ;)");

    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this, MainPage.class);
        startActivity(intent);
    }

}
