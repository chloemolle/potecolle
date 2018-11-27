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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
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
        final Context context = this;
        final Globals globalVariables = (Globals) getApplicationContext();
        User user = globalVariables.getUser();
        ArrayList<String> friends = user.getFriends();
        final LinearLayout layout = (LinearLayout) findViewById(R.id.linear_layout_ami);

        if (friends.size() > 0) {
            final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar_ami);
            progressBar.setVisibility(View.VISIBLE);

            File cacheFile = new File(context.getCacheDir(), "friends_" + globalVariables.getUser().getUsername());

            try {
                FileInputStream fis = new FileInputStream(cacheFile);
                fis.getChannel().position(0);
                BufferedReader bfr = new BufferedReader(new InputStreamReader(fis));
                String currentLine = bfr.readLine();
                if (currentLine == null) {
                    goWithTheDatabase();
                } else {
                    while (currentLine != null) {
                        String[] friendInfo = currentLine.split("SPLIT_USERNAME_EMAIL");
                        createButtonWithPlayerName(friendInfo[0], friendInfo[1]);
                        currentLine = bfr.readLine();
                    }
                }
                fis.close();
                bfr.close();
                progressBar.setVisibility(View.GONE);
            } catch (Exception e) {
                Log.d("problemeCache", e.getMessage());
                goWithTheDatabase();
            }
            progressBar.setVisibility(View.GONE);

        } else {
            final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar_ami);
            progressBar.setVisibility(View.GONE);
            TextView textView = new TextView(this);
            textView.setText(R.string.add_friends);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(130,20,100,0);
            textView.setLayoutParams(params);

            layout.addView(textView);
        }

        Button button = new Button(this);
        button.setText(R.string.entrainer_seul);

        button.setBackgroundColor(getResources().getColor(R.color.white));
        button.setTextColor(getResources().getColor(R.color.colorTheme));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(100,20,100,0);
        button.setLayoutParams(params);


        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Button b = (Button) v;
                Intent intent = new Intent(v.getContext(), ChoixTimer.class);
                globalVariables.getCurrentGame().setPlayer2("");
                globalVariables.getCurrentGame().setAdversaire("");
                globalVariables.getCurrentGame().setSeul(true);
                Date date = new Date();
                Long tmp = date.getTime();
                final String id = tmp.toString();
                globalVariables.getCurrentGame().setId(id);

                startActivity(intent);
            }
        });

        layout.addView(button);
    }


    public void goWithTheDatabase(){
        final Globals globalVariables = (Globals) getApplicationContext();
        User user = globalVariables.getUser();
        ArrayList<String> friends = user.getFriends();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final Context context = this;

        final File cacheFile = new File(context.getCacheDir(), "friends_" + globalVariables.getUser().getUsername());
        try {
            for (String friend: friends) {
                final String email = friend;
                db.collection("Users").document(friend)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                final User user = documentSnapshot.toObject(User.class);
                                createButtonWithPlayerName(user.getUsername(), email);
                                try {
                                    final FileWriter fw = new FileWriter(cacheFile.getAbsoluteFile());
                                    final BufferedWriter bfw = new BufferedWriter(fw);
                                    bfw.write(user.getUsername() + "SPLIT_USERNAME_EMAIL" + email);
                                    bfw.newLine();
                                    bfw.close();
                                } catch (Exception e) {
                                    Log.d("problemeCache", e.getMessage());
                                }
                            }
                        });
            }
        } catch (Exception e) {
            Log.d("problemeCache", e.getMessage());
        }




      }

    public void createButtonWithPlayerName(String _name, final String email){
        final String name = _name;
        final Globals globalVariables = (Globals) getApplicationContext();
        final Context context = this;
        final LinearLayout layout = (LinearLayout) findViewById(R.id.linear_layout_ami);

        Button newButton = new Button(context);
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
                globalVariables.getCurrentGame().setAdversaire(email);
                Date date = new Date();
                Long tmp = date.getTime();
                final String id = tmp.toString();
                globalVariables.getCurrentGame().setId(id);

                startActivity(intent);
            }
        });

        layout.addView(newButton);
    }



}
