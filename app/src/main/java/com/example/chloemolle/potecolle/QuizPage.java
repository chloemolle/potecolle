package com.example.chloemolle.potecolle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

import static com.firebase.ui.auth.AuthUI.TAG;

/**
 * Created by chloemolle on 23/10/2018.
 */

public class QuizPage extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Globals globalVariables = (Globals) getApplicationContext();
        final Integer currentQuestionNumber = globalVariables.getCurrentQuestionNumero();
        Question currentQuestion = globalVariables.getCurrentGame().getQuestions().get(currentQuestionNumber);

        if (currentQuestion.getType().equals("qcm") || currentQuestion.getType().equals("questionInversé")) {
            setContentView(R.layout.qcm_page_layout);
            Button bouton = (Button) findViewById(R.id.next_quiz);
            final TextView question = (TextView) findViewById(R.id.question_quiz);
            question.setText(currentQuestion.getQuestion().toString());

            final TextView prop1 = (TextView) findViewById(R.id.reponse_quiz1);
            prop1.setPadding(20, 20, 20, 20);
            prop1.setText(currentQuestion.getProposition().get(0).toString());
            prop1.setBackground(getResources().getDrawable(R.drawable.background_quiz));

            prop1.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    String userAnswerText = prop1.getText().toString();
                    if (userAnswerText.isEmpty()) {
                        return;
                    }
                    globalVariables.getCurrentGame().addAnswerForPlayer1(userAnswerText);
                    if (currentQuestionNumber == 4) {
                        Intent intent = new Intent(v.getContext(), FinQuizPage.class);
                        startActivity(intent);
                    } else {
                        globalVariables.setCurrentQuestionNumero(currentQuestionNumber + 1);
                        Intent intent = new Intent(v.getContext(), QuizPage.class);
                        startActivity(intent);
                    }
                }
            });

            final TextView prop2 = (TextView) findViewById(R.id.reponse_quiz2);
            prop2.setPadding(20, 20, 20, 20);
            prop2.setText(currentQuestion.getProposition().get(1).toString());
            prop2.setBackground(getResources().getDrawable(R.drawable.background_quiz));

            prop2.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    String userAnswerText = prop2.getText().toString();
                    if (userAnswerText.isEmpty()) {
                        return;
                    }
                    globalVariables.getCurrentGame().addAnswerForPlayer1(userAnswerText);
                    if (currentQuestionNumber == 4) {
                        Intent intent = new Intent(v.getContext(), FinQuizPage.class);
                        startActivity(intent);
                    } else {
                        globalVariables.setCurrentQuestionNumero(currentQuestionNumber + 1);
                        Intent intent = new Intent(v.getContext(), QuizPage.class);
                        startActivity(intent);
                    }
                }
            });

            final TextView prop3 = (TextView) findViewById(R.id.reponse_quiz3);
            prop3.setPadding(20, 20, 20, 20);
            prop3.setText(currentQuestion.getProposition().get(2).toString());
            prop3.setBackground(getResources().getDrawable(R.drawable.background_quiz));

            prop3.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    String userAnswerText = prop3.getText().toString();
                    if (userAnswerText.isEmpty()) {
                        return;
                    }
                    globalVariables.getCurrentGame().addAnswerForPlayer1(userAnswerText);
                    if (currentQuestionNumber == 4) {
                        Intent intent = new Intent(v.getContext(), FinQuizPage.class);
                        startActivity(intent);
                    } else {
                        globalVariables.setCurrentQuestionNumero(currentQuestionNumber + 1);
                        Intent intent = new Intent(v.getContext(), QuizPage.class);
                        startActivity(intent);
                    }
                }
            });

            final TextView prop4 = (TextView) findViewById(R.id.reponse_quiz4);
            prop4.setPadding(20, 20, 20, 20);
            prop4.setBackground(getResources().getDrawable(R.drawable.background_quiz));
            prop4.setText(currentQuestion.getProposition().get(3).toString());


            prop4.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    String userAnswerText = prop4.getText().toString();
                    if (userAnswerText.isEmpty()) {
                        return;
                    }
                    globalVariables.getCurrentGame().addAnswerForPlayer1(userAnswerText);
                    if (currentQuestionNumber == 4) {
                        Intent intent = new Intent(v.getContext(), FinQuizPage.class);
                        startActivity(intent);
                    } else {
                        globalVariables.setCurrentQuestionNumero(currentQuestionNumber + 1);
                        Intent intent = new Intent(v.getContext(), QuizPage.class);
                        startActivity(intent);
                    }
                }
            });

        } else {
            setContentView(R.layout.quiz_page_layout);
            final TextView question = (TextView) findViewById(R.id.question_quiz);
            question.setText(currentQuestion.getQuestion().toString());

            final FirebaseFirestore db = FirebaseFirestore.getInstance();
            Button bouton = (Button) findViewById(R.id.next_quiz);
            if (currentQuestionNumber == 4) {
                bouton.setText("Fin");
            }

            final EditText userAnswer = (EditText) findViewById(R.id.user_answer);


            userAnswer.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    String userAnswerText = userAnswer.getText().toString();
                    if (userAnswerText.isEmpty()) {
                        return false;
                    }
                    globalVariables.getCurrentGame().addAnswerForPlayer1(userAnswerText);
                    if (currentQuestionNumber == 4) {
                        Intent intent = new Intent(v.getContext(), FinQuizPage.class);
                        startActivity(intent);
                    } else {
                        globalVariables.setCurrentQuestionNumero(currentQuestionNumber + 1);
                        Intent intent = new Intent(v.getContext(), QuizPage.class);
                        startActivity(intent);
                    }
                    return true;
                }
            });

            bouton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    String userAnswerText = userAnswer.getText().toString();
                    if (userAnswerText.isEmpty()) {
                        return;
                    }
                    globalVariables.getCurrentGame().addAnswerForPlayer1(userAnswerText);
                    if (currentQuestionNumber == 4) {
                        Intent intent = new Intent(v.getContext(), FinQuizPage.class);
                        startActivity(intent);
                    } else {
                        globalVariables.setCurrentQuestionNumero(currentQuestionNumber + 1);
                        Intent intent = new Intent(v.getContext(), QuizPage.class);
                        startActivity(intent);
                    }
                }
            });

        }

    }


    @Override
    public void onBackPressed(){
        return;
    }


}
