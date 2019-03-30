package potecolle.application.about.education;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;


import com.example.chloemolle.potecolle.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;

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

        goWithTheDatabase();
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
                        if (task.isSuccessful()) {
                            ArrayList<String> arr = (ArrayList<String>) task.getResult().getData();
                            for (String sujet : arr) {
                                createButtonWithNameForSubject(sujet);
                            }

                            progressBar.setVisibility(View.GONE);
                        } else {
                            Log.d("Fail", task.getException().getMessage());
                        }
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
                globalVariables.setTmpTime(60);
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
                800,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.CENTER_HORIZONTAL;
        params.setMargins(100, 20, 100, 20);
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