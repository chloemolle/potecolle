package com.example.chloemolle.potecolle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by chloemolle on 30/10/2018.
 */

public class EcriturePage extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ecriture_page_layout);

        final Globals globalVariables = (Globals) getApplicationContext();

        final EditText brouillon = (EditText) findViewById(R.id.ecriture);
        brouillon.setText(globalVariables.getBrouillonText());


        Button retourQuiz = (Button) findViewById(R.id.retour_quiz);
        retourQuiz.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                globalVariables.setBrouillonText(brouillon.getText().toString());
                Intent intent = new Intent(v.getContext(), QuizPage.class);
                startActivity(intent);
            }
        });

    }


}
