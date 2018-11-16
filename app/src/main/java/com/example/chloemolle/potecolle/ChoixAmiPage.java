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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.firebase.ui.auth.AuthUI.TAG;

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

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar_ami);
        progressBar.setVisibility(View.VISIBLE);

        User user = globalVariables.getUser();
        ArrayList<String> friends = user.getFriends();
        for (String friend: friends) {
            Button newButton = new Button(context);
            final String name = friend;
            newButton.setText(name);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(20, 20, 20, 20);
            newButton.setLayoutParams(params);
            newButton.setTextColor(getResources().getColor(R.color.white));

            newButton.setBackground(getResources().getDrawable(R.drawable.button_with_radius));
            newButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Button b = (Button) v;
                    Intent intent = new Intent(v.getContext(), ChoixTimer.class);
                    globalVariables.getCurrentGame().setPlayer2(name);
                    globalVariables.getCurrentGame().setAdversaire(name);
                    Date date = new Date();
                    Long tmp = date.getTime();
                    final String id = tmp.toString();
                    globalVariables.getCurrentGame().setId(id);

                    startActivity(intent);
                }
            });

            layout.addView(newButton);
        }

        progressBar.setVisibility(View.GONE);
    }


}
