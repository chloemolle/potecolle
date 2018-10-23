package com.example.chloemolle.potecolle;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by chloemolle on 23/10/2018.
 */

public class QuizPage extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quiz_page_layout);
        final LinearLayout layout = (LinearLayout) findViewById(R.id.linear_layout_quiz);

        final Globals globalVariables = (Globals) getApplicationContext();


        TextView text1 = new TextView(this) ;
        text1.setText(globalVariables.getCurrentGame().getClasse());
        layout.addView(text1);

        TextView text2 = new TextView(this) ;
        text2.setText(globalVariables.getCurrentGame().getMatiere());
        layout.addView(text2);

        TextView text4 = new TextView(this) ;
        text4.setText(globalVariables.getCurrentGame().getSujet());
        layout.addView(text4);

        TextView text3 = new TextView(this) ;
        text3.setText(globalVariables.getCurrentGame().getPlayer1());
        layout.addView(text3);

        TextView text5 = new TextView(this) ;
        text5.setText(globalVariables.getCurrentGame().getPlayer2());
        layout.addView(text5);
    }
}
