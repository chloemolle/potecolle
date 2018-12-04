package com.example.chloemolle.potecolle;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

/**
 * Created by chloemolle on 06/11/2018.
 */

public class LoadingQuizPage extends Activity {

    private Context self = this;
    private ProgressBar progressBar;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_quiz_page_layout);
        this.progressBar = findViewById(R.id.progress_bar_loading_page);
        this.progressBar.setVisibility(View.VISIBLE);
        final Globals globalVariables = (Globals) getApplicationContext();
        new DownloadQuestions().execute();
    }

    private class DownloadQuestions extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            final Globals globalVariables = (Globals) getApplicationContext();
            ArrayList<String> arr = globalVariables.getCurrentGame().getQuestionsId();
            final FirebaseFirestore db = FirebaseFirestore.getInstance();
            Game currentGame = globalVariables.getCurrentGame();
            for (String id: arr) {
                db.collection(currentGame.getClasse()).document(currentGame.getMatiere()).collection(currentGame.getSujet()).document(id)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                ArrayList<String> propo = (ArrayList<String>) documentSnapshot.get("propositions");
                                final Question question = documentSnapshot.toObject(Question.class);
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
                                if (propo != null && propo.size() > 0) {
                                    question.setPropositions(propo);
                                }
                                globalVariables.getCurrentGame().addQuestions(question);
                                Integer size = globalVariables.getCurrentGame().getQuestions().size();
                                if (globalVariables.getCurrentGame().getQuestions().size() == 5) {
                                    Intent intent = new Intent(self, QuizPage.class);
                                    startActivity(intent);
                                }
                            }
                        });

            }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            Log.e("progress", progress.toString());
        }

        protected void onPostExecute(Void result) {
            Log.e("INFO","Questions récupérées");
        }
    }
}


