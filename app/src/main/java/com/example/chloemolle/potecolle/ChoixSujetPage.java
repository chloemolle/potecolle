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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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
        final LinearLayout layout = (LinearLayout) findViewById(R.id.linear_layout_sujet);
        final Context context = this;
        final Globals globalVariables = (Globals) getApplicationContext();

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final String classe = globalVariables.getCurrentGame().getClasse();
        final String matiere = globalVariables.getCurrentGame().getMatiere();

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
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        params.setMargins(20, 0, 20, 0);
                        newButton.setLayoutParams(params);
                        newButton.setBackground(getDrawable(R.drawable.button_with_radius));
                        newButton.setTextColor(getResources().getColor(R.color.white));
                        newButton.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                Intent intent = new Intent(v.getContext(), ChoixAmiPage.class);
                                globalVariables.getCurrentGame().setSujet(sujet);

                                //Creation des questions pour le quiz
                                db.collection(classe).document(matiere).collection(sujet).get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    ArrayList<Map<String, Object>> questionsQuiz = new ArrayList<>();
                                                    Integer nbQuestionDisponible = task.getResult().size();
                                                    ArrayList<Integer> questionToFetch = new ArrayList<>();
                                                    if (nbQuestionDisponible > 5) {
                                                        Random r = new Random();
                                                        while (questionToFetch.size() != 5) {
                                                            Integer tmp = r.nextInt(nbQuestionDisponible - 1);
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
                                                            questionsQuiz.add(document.getData());
                                                            index ++;
                                                        }
                                                    }
                                                    globalVariables.getCurrentGame().setQuestions(questionsQuiz);
                                                } else {
                                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                                }
                                            }
                                        });




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
