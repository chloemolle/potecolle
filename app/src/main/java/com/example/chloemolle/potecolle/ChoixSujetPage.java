package com.example.chloemolle.potecolle;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;


import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static com.firebase.ui.auth.AuthUI.TAG;

/**
 * Created by chloemolle on 16/10/2018.
 */

public class ChoixSujetPage extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choix_sujet_page_layout);
        final Context context = this;
        final Globals globalVariables = (Globals) getApplicationContext();

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar_sujets);
        progressBar.setVisibility(View.VISIBLE);

        File cacheFile = new File(context.getCacheDir(), globalVariables.getUser().getClasse() + "_sujets_" + globalVariables.getCurrentGame().getMatiere());

        try {
            FileInputStream fis = new FileInputStream(cacheFile);
            fis.getChannel().position(0);
            BufferedReader bfr = new BufferedReader(new InputStreamReader(fis));
            String currentLine = bfr.readLine();
            if (currentLine == null) {
                goWithTheDatabase();
            } else {
                while (currentLine != null) {
                    createButtonWithNameForSubject(currentLine);
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

    }

    private void goWithTheDatabase(){
        final Globals globalVariables = (Globals) getApplicationContext();
        FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar_sujets);
        final Context context = this;


        ArrayList<String> data = new ArrayList<>();
        data.add(globalVariables.getUser().getClasse());
        data.add(globalVariables.getCurrentGame().getMatiere());

        mFunctions
                .getHttpsCallable("getCollections")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        ArrayList<String> arr = (ArrayList<String>) task.getResult().getData();
                        FileWriter fw;
                        BufferedWriter bfw;
                        File cacheFile = new File(context.getCacheDir(), globalVariables.getUser().getClasse() + "_sujets_" + globalVariables.getCurrentGame().getMatiere());
                        try {
                            fw = new FileWriter(cacheFile.getAbsoluteFile());
                            bfw = new BufferedWriter(fw);
                            for (String sujet : arr) {
                                createButtonWithNameForSubject(sujet);
                                bfw.write(sujet);
                                bfw.newLine();
                            }
                            bfw.close();
                        } catch (Exception e) {
                            Log.d("problemeCache", e.getMessage());
                        }

                        progressBar.setVisibility(View.GONE);
                        return "";
                    }
                });
        return;
    }

    private void createButtonWithNameForSubject(final String name) {
        final LinearLayout layout = (LinearLayout) findViewById(R.id.linear_layout_sujet);
        final Globals globalVariables = (Globals) getApplicationContext();

        Button newButton = createButton(name);
        newButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent;
                if (globalVariables.getCurrentGame().getRevanche() || globalVariables.getCurrentGame().getSeul()) {
                    intent = new Intent(v.getContext(), QuizPage.class);
                } else {
                    intent = new Intent(v.getContext(), ChoixAmiPage.class);
                }
                globalVariables.setTmpTime(30);
                globalVariables.getCurrentGame().setSujet(name);


                //Creation des questions pour le quiz
                createQuestion(name, intent);
            }
        });
        layout.addView(newButton);
        return;
    }

    public Button createButton(String name_sujet) {
        Button newButton = new Button(this);
        newButton.setText(name_sujet);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(20, 20, 20, 20);
        newButton.setLayoutParams(params);
        newButton.setBackground(getResources().getDrawable(R.drawable.button_with_radius));

        newButton.setTextColor(getResources().getColor(R.color.white));
        return newButton;
    }

    public void createQuestion(String name_sujet, final Intent intent) {
        final Globals globalVariables = (Globals) getApplicationContext();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final String classe = globalVariables.getCurrentGame().getClasse();
        final String matiere = globalVariables.getCurrentGame().getMatiere();

        db.collection(classe).document(matiere).collection(name_sujet).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Game game = globalVariables.getCurrentGame();
                            game.createQuestions(task);
                            startActivity(intent);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }



}