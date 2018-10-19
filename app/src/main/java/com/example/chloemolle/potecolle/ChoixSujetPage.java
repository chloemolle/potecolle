package com.example.chloemolle.potecolle;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by chloemolle on 16/10/2018.
 */

public class ChoixSujetPage extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choix_sujet_page_layout);
        final LinearLayout layout = (LinearLayout) findViewById(R.id.linear_layout_sujet);
        final Context context = this;
        final Globals globalVariables = (Globals) getApplicationContext();

        FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar_sujets);
        progressBar.setVisibility(View.VISIBLE);

        ArrayList<String> data = new ArrayList<String>();
        data.add("Troisieme");
        data.add(globalVariables.getCurrentGame().getMatiere());

        mFunctions
            .getHttpsCallable("getCollections")
            .call(data)
            .continueWith(new Continuation<HttpsCallableResult, String>() {
                @Override
                public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                    ArrayList<String> arr = (ArrayList<String>) task.getResult().getData();
                    for (String s : arr) {
                        final String sujet = s;
                        Button newButton = new Button(context);
                        newButton.setText(s);
                        newButton.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                Intent intent = new Intent(v.getContext(), ChoixAmiPage.class);
                                globalVariables.getCurrentGame().setSujet(sujet);
                                startActivity(intent);
                            }
                        });
                        layout.addView(newButton);
                    }
                    progressBar.setVisibility(View.GONE);
                    return "";
                }
            });

    }
}
