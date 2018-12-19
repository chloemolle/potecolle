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

    private void createButtonWithNameForSubject(String name) {
        final LinearLayout layout = (LinearLayout) findViewById(R.id.linear_layout_sujet);
        final Context context = this;
        final Globals globalVariables = (Globals) getApplicationContext();
        final String name_sujet = name;
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final String classe = globalVariables.getCurrentGame().getClasse();
        final String matiere = globalVariables.getCurrentGame().getMatiere();

        Button newButton = new Button(context);
        newButton.setText(name_sujet);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(20, 20, 20, 20);
        newButton.setLayoutParams(params);
        newButton.setBackground(getResources().getDrawable(R.drawable.button_with_radius));

        newButton.setTextColor(getResources().getColor(R.color.white));
        newButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent;
                if (globalVariables.getCurrentGame().getRevanche()) {
                    intent = new Intent(v.getContext(), ChoixTimer.class);
                } else {
                    intent = new Intent(v.getContext(), ChoixAmiPage.class);
                }
                globalVariables.getCurrentGame().setSujet(name_sujet);
                Date date = new Date();
                Long tmp = date.getTime();
                final String id = tmp.toString();
                globalVariables.getCurrentGame().setId(id);


                //Creation des questions pour le quiz
                db.collection(classe).document(matiere).collection(name_sujet).get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    ArrayList<Question> questionsQuiz = new ArrayList<>();
                                    ArrayList<String> questionsQuizId = new ArrayList<>();
                                    Integer nbQuestionDisponible = task.getResult().size();
                                    ArrayList<Integer> questionToFetch = new ArrayList<>();
                                    if (nbQuestionDisponible > 5) {
                                        Random r = new Random();
                                        while (questionToFetch.size() != 5) {
                                            Integer tmp = r.nextInt(nbQuestionDisponible);
                                            if (questionToFetch.indexOf(tmp) == -1) {
                                                questionToFetch.add(tmp);
                                            }
                                        }
                                    } else {
                                        questionToFetch = new ArrayList<>();
                                        questionToFetch.add(0);
                                        questionToFetch.add(1);
                                        questionToFetch.add(2);
                                        questionToFetch.add(3);
                                        questionToFetch.add(4);
                                    }
                                    Integer index = 0;
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        if (questionToFetch.indexOf(index) != -1) {
                                            if (questionsQuiz.size() > 0) {
                                                Random r = new Random();
                                                Integer randomInt = r.nextInt(questionsQuiz.size());
                                                final Question question = document.toObject(Question.class);
                                                if (question.getType().toString().contains("image")){
                                                    FirebaseStorage storage = FirebaseStorage.getInstance();
                                                    // Create a storage reference from our app
                                                    StorageReference storageRef = storage.getReference();
                                                    StorageReference mountainImagesRef = storageRef.child(question.getImage());
                                                    mountainImagesRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                                        @Override
                                                        public void onSuccess(byte[] bytes) {
                                                            // Use the bytes to display the image
                                                            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                                            question.setBmp(bmp);
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception exception) {
                                                            // Handle any errors
                                                        }
                                                    });
                                                }
                                                ArrayList<String> propositions = (ArrayList<String>) document.getData().get("propositions");

                                                if (propositions != null && propositions.size() > 0) {
                                                    ArrayList<String> propositionsShuffled = new ArrayList<>();

                                                    while (propositions.size() != 0) {
                                                        int indexProp = (int) Math.floor(Math.random() * propositions.size());
                                                        propositionsShuffled.add(propositions.get(indexProp));
                                                        propositions.remove(indexProp);
                                                    }
                                                    question.setPropositions(propositionsShuffled);
                                                }

                                                questionsQuiz.add(randomInt, question);
                                                questionsQuizId.add(randomInt, document.getId());
                                            } else {
                                                Question question = document.toObject(Question.class);
                                                ArrayList<String> propositions = (ArrayList<String>) document.getData().get("propositions");
                                                question.setPropositions(propositions);
                                                questionsQuiz.add(question);
                                                questionsQuizId.add(document.getId());
                                            }
                                        }
                                        index ++;
                                    }
                                    globalVariables.getCurrentGame().setQuestions(questionsQuiz);
                                    globalVariables.getCurrentGame().setQuestionsId(questionsQuizId);
                                    Log.d("Information", "Voici les questions: " + questionsQuiz.toString());
                                } else {
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        });




                startActivity(intent);
            }
        });
        layout.addView(newButton);
        return;
    }


}
