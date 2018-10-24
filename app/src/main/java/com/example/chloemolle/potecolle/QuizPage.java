package com.example.chloemolle.potecolle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.util.Map;

import static com.firebase.ui.auth.AuthUI.TAG;

/**
 * Created by chloemolle on 23/10/2018.
 */

public class QuizPage extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quiz_page_layout);

        final Globals globalVariables = (Globals) getApplicationContext();

        final TextView question = (TextView) findViewById(R.id.question_quiz);
        final Integer currentQuestionNumber = globalVariables.getCurrentQuestionNumero();
        Map<String, Object> currentQuestion = globalVariables.getCurrentGame().getQuestions().get(currentQuestionNumber);
        question.setText(currentQuestion.get("question").toString());

        Button bouton = (Button) findViewById(R.id.next_quiz);
        if (currentQuestionNumber == 4) {
            bouton.setText("Fin");
        }

        final EditText userAnswer = (EditText) findViewById(R.id.user_answer);

        bouton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                globalVariables.getCurrentGame().addAnswerForPlayer1(userAnswer.getText().toString());
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
