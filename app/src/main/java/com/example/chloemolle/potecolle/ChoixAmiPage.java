package com.example.chloemolle.potecolle;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by chloemolle on 16/10/2018.
 */

public class ChoixAmiPage extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choix_ami_page_layout);
        final LinearLayout layout = (LinearLayout) findViewById(R.id.linear_layout_ami);
        final Context context = this;
        final Globals globalVariables = (Globals) getApplicationContext();

        FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar_ami);
        progressBar.setVisibility(View.VISIBLE);

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        Task task = mFunctions
                .getHttpsCallable("getFriends")
                .call(user)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        ArrayList<Map<String, String>> arr = (ArrayList<Map<String, String>>) task.getResult().getData();
                        for (Map<String, String> s : arr) {
                            final String name = s.get("name");
                            Button newButton = new Button(context);
                            newButton.setText(name);
                            newButton.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    Intent intent = new Intent(v.getContext(), ChoixAmiPage.class);
                                    globalVariables.getCurrentGame().setPlayer2(name);
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
