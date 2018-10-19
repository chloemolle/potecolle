package com.example.chloemolle.potecolle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;

/**
 * Created by chloemolle on 16/10/2018.
 */

public class ChoixMatierePage extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choix_matiere_page_layout);

        Button mathsButton = (Button) findViewById(R.id.maths_button);
        mathsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ChoixSujetPage.class);
                intent.putExtra("matière", "Maths");
                startActivity(intent);
            }
        });

    }
}
