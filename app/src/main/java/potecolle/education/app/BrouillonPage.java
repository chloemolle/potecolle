package potecolle.education.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.chloemolle.potecolle.R;

/**
 * Created by chloemolle on 30/10/2018.
 */

public class BrouillonPage extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.brouillon_page_layout);

        final Globals globalVariables = (Globals) getApplicationContext();

        final EditText brouillon = (EditText) findViewById(R.id.ecriture);
        brouillon.setText(globalVariables.getBrouillonText());
        final Integer currentQuestionNumber = globalVariables.getCurrentQuestionNumero();
        final Question currentQuestion = globalVariables.getCurrentGame().getQuestions().get(currentQuestionNumber);

        setQuestion(currentQuestion);
        Button retourQuiz = (Button) findViewById(R.id.retour_quiz);
        setProgressBar();

        if(globalVariables.getCurrentGame().getTimed()) {
            final Handler handler = setHandler(currentQuestion, currentQuestionNumber);
            retourQuiz.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    globalVariables.setBrouillonText(brouillon.getText().toString());
                    handler.removeCallbacksAndMessages(null);
                    Intent intent = new Intent(v.getContext(), QuizPage.class);
                    startActivity(intent);
                }
            });
        } else {
            retourQuiz.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    globalVariables.setBrouillonText(brouillon.getText().toString());
                    Intent intent = new Intent(v.getContext(), QuizPage.class);
                    startActivity(intent);
                }
            });
        }


    }

    private void setProgressBar() {
        Globals globalVariables = (Globals) getApplicationContext();
        ProgressBar pgBar = (ProgressBar) findViewById(R.id.progressBarTimer);
        if (!globalVariables.getCurrentGame().getTimed()) {
            pgBar.setVisibility(View.GONE);
        } else {
            pgBar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.green)));
            pgBar.setMax(30);
            pgBar.setProgress(globalVariables.getTmpTime());
        }

    }

    public void nextPage(Context c) {
        Globals globalVariables = (Globals) getApplicationContext();
        Integer currentQuestionNumber = globalVariables.getCurrentQuestionNumero();
        if (currentQuestionNumber == 4) {
            Intent intent = new Intent(c, FinQuizPage.class);
            startActivity(intent);
        } else {
            globalVariables.setCurrentQuestionNumero(currentQuestionNumber + 1);
            Intent intent = new Intent(c, QuizPage.class);
            startActivity(intent);
        }
    }

    public void setQuestion(Question currentQuestion){
        ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.layout_brouillon);
        if (currentQuestion.getType().toString().contains("image")) {
            final ImageView imageView = (ImageView) findViewById(R.id.brouillon_question_quiz);
            imageView.setImageBitmap(Bitmap.createScaledBitmap(currentQuestion.getBmp(), 200,
                    200, false));
            TextView brouillonQuestion = (TextView) findViewById(R.id.brouillon_question);
            brouillonQuestion.setVisibility(View.GONE);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(layout);
            constraintSet.connect(R.id.ecriture,ConstraintSet.TOP,R.id.brouillon_question_quiz,ConstraintSet.BOTTOM,36);
            constraintSet.applyTo(layout);
        } else {
            TextView brouillonQuestion = (TextView) findViewById(R.id.brouillon_question);
            brouillonQuestion.setText(currentQuestion.getQuestion().toString());
            final ImageView imageView = (ImageView) findViewById(R.id.brouillon_question_quiz);
            imageView.setVisibility(View.GONE);
        }

    }

    public Handler setHandler(final Question currentQuestion, final Integer currentQuestionNumber){
        final Globals globalVariables = (Globals) getApplicationContext();
        final int delay = 1000; //milliseconds
        final ProgressBar pgBar = (ProgressBar) findViewById(R.id.progressBarTimer);

        final Handler handler = new Handler();

        final Runnable runnable = new Runnable(){
            public void run(){
                //do something
                globalVariables.setTmpTime(globalVariables.getTmpTime() - 1);
                if (globalVariables.getTmpTime() == 0) {
                    if (currentQuestion.getType().equals("qcm") || currentQuestion.getType().equals("questionInversé")) {
                        globalVariables.setBrouillonText("");
                        globalVariables.setReponseText("");
                        globalVariables.getCurrentGame().addAnswerForPlayer1(currentQuestionNumber, "");
                    } else {
                        String userAnswerText = globalVariables.getReponseText();
                        globalVariables.setReponseText("");
                        globalVariables.setBrouillonText("");
                        globalVariables.getCurrentGame().addAnswerForPlayer1(currentQuestionNumber, userAnswerText);
                    }
                    pgBar.setProgress(30);
                    nextPage(getBaseContext());
                } else {
                    Log.d("ProgressBar", "current is " + globalVariables.getTmpTime());
                    pgBar.setProgress(globalVariables.getTmpTime());
                    if (globalVariables.getTmpTime() < 10) {
                        pgBar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));
                    }
                    handler.postDelayed(this, delay);
                }
            }
        };

        handler.postDelayed(runnable, delay);

        return handler;
    }

}
