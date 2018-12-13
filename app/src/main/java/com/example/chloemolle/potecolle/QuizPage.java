package com.example.chloemolle.potecolle;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * Created by chloemolle on 23/10/2018.
 */

public class QuizPage extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Globals globalVariables = (Globals) getApplicationContext();
        final Integer currentQuestionNumber = globalVariables.getCurrentQuestionNumero();
        final Question currentQuestion = globalVariables.getCurrentGame().getQuestions().get(currentQuestionNumber);

        if (currentQuestion.getType().equals("image")) {
            setContentView(R.layout.image_page_layout);
        } else if (currentQuestion.getType().equals("imageqcm")) {
            setContentView(R.layout.image_qcm_page_layout);
        } else if (currentQuestion.getType().equals("qcm") || currentQuestion.getType().equals("questionInversé")) {
            setContentView(R.layout.qcm_page_layout);
        } else {
            setContentView(R.layout.quiz_page_layout);
        }

        if (globalVariables.getCurrentGame().getTimed().equals("true")) {
            ImageButton boutonPrecedent = (ImageButton) findViewById(R.id.retour_precedent_quiz);
            boutonPrecedent.setVisibility(View.GONE);
        }

        final ProgressBar pgBar = (ProgressBar) findViewById(R.id.progressBarTimer);
        final Handler handler = new Handler();
        final int delay = 1000; //milliseconds

        final EditText userAnswer = (EditText) findViewById(R.id.user_answer);

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
                        String userAnswerText = userAnswer.getText().toString();
                        globalVariables.setReponseText("");
                        globalVariables.setBrouillonText("");
                        globalVariables.getCurrentGame().addAnswerForPlayer1(currentQuestionNumber, userAnswerText);
                    }
                    globalVariables.getCurrentGame().addReponsesTemps(0);
                    globalVariables.setTmpTime(30);
                    nextPage(getBaseContext());
                } else {
                    pgBar.setProgress(globalVariables.getTmpTime());
                    Log.d("ProgressBar", "current is " + globalVariables.getTmpTime());
                    if (globalVariables.getTmpTime() < 10) {
                        pgBar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));
                    }
                    handler.postDelayed(this, delay);
                }
            }
        };

        if (globalVariables.getCurrentGame().getTimed().equals("true")) {
            handler.postDelayed(runnable, delay);
        }


        if (currentQuestion.getType().toString().contains("image")){
            setRetourButton();
            setProgressBar();

            ImageButton boutonBrouillon = (ImageButton) findViewById(R.id.brouillon_button);
            boutonBrouillon.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), EcriturePage.class);
                    if (globalVariables.getCurrentGame().getTimed().equals("true")) {
                        handler.removeCallbacks(runnable);
                    }
                    startActivity(intent);
                }
            });

            if (currentQuestion.getType().equals("image")) {
                ImageButton bouton = (ImageButton) findViewById(R.id.next_quiz);
                bouton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        String userAnswerText = userAnswer.getText().toString();
                        if (userAnswerText.isEmpty()) {
                            return;
                        }
                        globalVariables.setReponseText("");
                        globalVariables.setBrouillonText("");
                        globalVariables.getCurrentGame().addReponsesTemps(globalVariables.getTmpTime());
                        globalVariables.getCurrentGame().addAnswerForPlayer1(currentQuestionNumber, userAnswerText);
                        globalVariables.setTmpTime(30);
                        if (globalVariables.getCurrentGame().getTimed().equals("true")) {
                            handler.removeCallbacks(runnable);
                        }
                        nextPage(v.getContext());
                    }
                });
            }

            final ImageView imageView = (ImageView) findViewById(R.id.question_quiz);
            imageView.setImageBitmap(Bitmap.createScaledBitmap(currentQuestion.getBmp(), 200,
                    200, false));


            if (currentQuestion.getType().toString().contains("qcm")) {


                ArrayList<String> propositionsShuffled = currentQuestion.getProposition();

                String answer = "";
                if (globalVariables.getCurrentGame().getPlayer1Answers().size() > currentQuestionNumber) {
                    answer = globalVariables.getCurrentGame().getPlayer1Answers().get(currentQuestionNumber);
                }


                final TextView prop1 = (TextView) findViewById(R.id.reponse_quiz1);
                prop1.setPadding(20, 20, 20, 20);
                prop1.setText(propositionsShuffled.get(0));
                if (answer.equals(prop1.getText().toString())) {
                    prop1.setBackground(getResources().getDrawable(R.drawable.background_quiz_selected));
                    prop1.setTextColor(getResources().getColor(R.color.white));
                } else {
                    prop1.setBackground(getResources().getDrawable(R.drawable.background_quiz));
                    prop1.setTextColor(getResources().getColor(R.color.black));
                }

                prop1.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        String userAnswerText = prop1.getText().toString();
                        if (userAnswerText.isEmpty()) {
                            return;
                        }
                        qcmNext(userAnswerText, handler,runnable, v.getContext(), prop1);
                    }
                });

                final TextView prop2 = (TextView) findViewById(R.id.reponse_quiz2);
                prop2.setPadding(20, 20, 20, 20);
                prop2.setText(propositionsShuffled.get(1));
                if (answer.equals(prop2.getText().toString())) {
                    prop2.setBackground(getResources().getDrawable(R.drawable.background_quiz_selected));
                    prop2.setTextColor(getResources().getColor(R.color.white));
                } else {
                    prop2.setBackground(getResources().getDrawable(R.drawable.background_quiz));
                    prop2.setTextColor(getResources().getColor(R.color.black));
                }

                prop2.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        String userAnswerText = prop2.getText().toString();
                        if (userAnswerText.isEmpty()) {
                            return;
                        }
                        qcmNext(userAnswerText, handler,runnable, v.getContext(), prop2);
                    }
                });

                final TextView prop3 = (TextView) findViewById(R.id.reponse_quiz3);
                prop3.setPadding(20, 20, 20, 20);
                prop3.setText(propositionsShuffled.get(2));
                if (answer.equals(prop3.getText().toString())) {
                    prop3.setBackground(getResources().getDrawable(R.drawable.background_quiz_selected));
                    prop3.setTextColor(getResources().getColor(R.color.white));
                } else {
                    prop3.setBackground(getResources().getDrawable(R.drawable.background_quiz));
                    prop3.setTextColor(getResources().getColor(R.color.black));
                }

                prop3.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        String userAnswerText = prop3.getText().toString();
                        if (userAnswerText.isEmpty()) {
                            return;
                        }
                        qcmNext(userAnswerText, handler,runnable, v.getContext(), prop3);
                    }
                });

                final TextView prop4 = (TextView) findViewById(R.id.reponse_quiz4);
                prop4.setPadding(20, 20, 20, 20);
                if (answer.equals(prop4.getText().toString())) {
                    prop4.setBackground(getResources().getDrawable(R.drawable.background_quiz_selected));
                    prop4.setTextColor(getResources().getColor(R.color.white));
                } else {
                    prop4.setBackground(getResources().getDrawable(R.drawable.background_quiz));
                    prop4.setTextColor(getResources().getColor(R.color.black));
                }
                prop4.setText(propositionsShuffled.get(3));


                prop4.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        String userAnswerText = prop4.getText().toString();
                        if (userAnswerText.isEmpty()) {
                            return;
                        }
                        qcmNext(userAnswerText, handler,runnable, v.getContext(), prop4);
                    }
                });
            }

        } else if (currentQuestion.getType().equals("qcm") || currentQuestion.getType().equals("questionInversé")) {
            setRetourButton();
            setProgressBar();

            ImageButton boutonBrouillon = (ImageButton) findViewById(R.id.brouillon_button);
            boutonBrouillon.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), EcriturePage.class);
                    if (globalVariables.getCurrentGame().getTimed().equals("true")) {
                        handler.removeCallbacks(runnable);
                    }
                    startActivity(intent);
                }
            });


            ArrayList<String> propositionsShuffled = currentQuestion.getProposition();

            final TextView question = (TextView) findViewById(R.id.question_quiz);
            question.setText(currentQuestion.getQuestion().toString());
            String answer = "";
            if (globalVariables.getCurrentGame().getPlayer1Answers().size() > currentQuestionNumber) {
                answer = globalVariables.getCurrentGame().getPlayer1Answers().get(currentQuestionNumber);
            }

            final TextView prop1 = (TextView) findViewById(R.id.reponse_quiz1);
            prop1.setPadding(20, 20, 20, 20);
            prop1.setText(propositionsShuffled.get(0));
            String test = propositionsShuffled.get(0);
            if (answer.equals(test)) {
                prop1.setBackground(getResources().getDrawable(R.drawable.background_quiz_selected));
                prop1.setTextColor(getResources().getColor(R.color.white));
            } else {
                prop1.setBackground(getResources().getDrawable(R.drawable.background_quiz));
                prop1.setTextColor(getResources().getColor(R.color.black));
            }

            prop1.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    String userAnswerText = prop1.getText().toString();
                    if (userAnswerText.isEmpty()) {
                        return;
                    }
                    qcmNext(userAnswerText, handler,runnable, v.getContext(), prop1);

                }
            });

            final TextView prop2 = (TextView) findViewById(R.id.reponse_quiz2);
            prop2.setPadding(20, 20, 20, 20);
            prop2.setText(propositionsShuffled.get(1));
            if (answer.equals(prop2.getText().toString())) {
                prop2.setBackground(getResources().getDrawable(R.drawable.background_quiz_selected));
                prop2.setTextColor(getResources().getColor(R.color.white));
            } else {
                prop2.setBackground(getResources().getDrawable(R.drawable.background_quiz));
                prop2.setTextColor(getResources().getColor(R.color.black));
            }

            prop2.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    String userAnswerText = prop2.getText().toString();
                    if (userAnswerText.isEmpty()) {
                        return;
                    }
                    qcmNext(userAnswerText, handler,runnable, v.getContext(), prop2);

                }
            });

            final TextView prop3 = (TextView) findViewById(R.id.reponse_quiz3);
            prop3.setPadding(20, 20, 20, 20);
            prop3.setText(propositionsShuffled.get(2));
            if (answer.equals(prop3.getText().toString())) {
                prop3.setBackground(getResources().getDrawable(R.drawable.background_quiz_selected));
                prop3.setTextColor(getResources().getColor(R.color.white));
            } else {
                prop3.setBackground(getResources().getDrawable(R.drawable.background_quiz));
                prop3.setTextColor(getResources().getColor(R.color.black));
            }

            prop3.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    String userAnswerText = prop3.getText().toString();
                    if (userAnswerText.isEmpty()) {
                        return;
                    }
                    qcmNext(userAnswerText, handler,runnable, v.getContext(), prop3);

                }
            });

            final TextView prop4 = (TextView) findViewById(R.id.reponse_quiz4);
            prop4.setPadding(20, 20, 20, 20);
            prop4.setText(propositionsShuffled.get(3));
            if (answer.equals(prop4.getText().toString())) {
                prop4.setBackground(getResources().getDrawable(R.drawable.background_quiz_selected));
                prop4.setTextColor(getResources().getColor(R.color.white));
            } else {
                prop4.setBackground(getResources().getDrawable(R.drawable.background_quiz));
                prop3.setTextColor(getResources().getColor(R.color.black));
            }


            prop4.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    String userAnswerText = prop4.getText().toString();
                    if (userAnswerText.isEmpty()) {
                        return;
                    }
                    qcmNext(userAnswerText, handler,runnable, v.getContext(), prop4);
                }
            });


        } else {
            setRetourButton();
            setProgressBar();
            final TextView question = (TextView) findViewById(R.id.question_quiz);
            question.setText(currentQuestion.getQuestion().toString());
            if (currentQuestionNumber < globalVariables.getCurrentGame().getPlayer1Answers().size()) {
                globalVariables.setReponseText(globalVariables.getCurrentGame().getPlayer1Answers().get(currentQuestionNumber));
            }

            final FirebaseFirestore db = FirebaseFirestore.getInstance();
            ImageButton bouton = (ImageButton) findViewById(R.id.next_quiz);
            userAnswer.setText(globalVariables.getReponseText());
            ImageButton boutonBrouillon = (ImageButton) findViewById(R.id.brouillon_button);
            boutonBrouillon.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    globalVariables.setReponseText(userAnswer.getText().toString());
                    if (globalVariables.getCurrentGame().getTimed().equals("true")) {
                        handler.removeCallbacks(runnable);
                    }
                    Intent intent = new Intent(v.getContext(), EcriturePage.class);
                    startActivity(intent);
                }
            });


            userAnswer.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    String userAnswerText = userAnswer.getText().toString();
                    if (userAnswerText.isEmpty()) {
                        return false;
                    }
                    globalVariables.setReponseText("");
                    globalVariables.setBrouillonText("");
                    globalVariables.getCurrentGame().addReponsesTemps(globalVariables.getTmpTime());
                    globalVariables.getCurrentGame().addAnswerForPlayer1(currentQuestionNumber, userAnswerText);
                    globalVariables.setTmpTime(30);
                    if (globalVariables.getCurrentGame().getTimed().equals("true")) {
                        handler.removeCallbacks(runnable);
                    }
                    nextPage(v.getContext());
                    return true;
                }
            });

            bouton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    String userAnswerText = userAnswer.getText().toString();
                    if (userAnswerText.isEmpty()) {
                        return;
                    }
                    globalVariables.setReponseText("");
                    globalVariables.setBrouillonText("");
                    globalVariables.getCurrentGame().addReponsesTemps(globalVariables.getTmpTime());
                    globalVariables.getCurrentGame().addAnswerForPlayer1(currentQuestionNumber, userAnswerText);
                    globalVariables.setTmpTime(30);
                    if (globalVariables.getCurrentGame().getTimed().equals("true")) {
                        handler.removeCallbacks(runnable);
                    }
                    nextPage(v.getContext());
                }
            });

        }

    }


    private void setRetourButton() {
        final Globals globalVariables = (Globals) getApplicationContext();
        ImageButton bouton = (ImageButton) findViewById(R.id.retour_precedent_quiz);
        final Integer currentQuestionNumber = globalVariables.getCurrentQuestionNumero();
        if(currentQuestionNumber == 0) {
            bouton.setAlpha((float) 0.5);
        }
        if (currentQuestionNumber > 0) {
            bouton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    globalVariables.setReponseText("");
                    globalVariables.setBrouillonText("");
                    globalVariables.setCurrentQuestionNumero(currentQuestionNumber - 1);
                    globalVariables.setTmpTime(30);
                    Intent intent = new Intent(v.getContext(), QuizPage.class);
                    startActivity(intent);
                }
            });
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

    private void setProgressBar() {
        Globals globalVariables = (Globals) getApplicationContext();
        ProgressBar pgBar = (ProgressBar) findViewById(R.id.progressBarTimer);
        if (globalVariables.getCurrentGame().getTimed().equals("false")) {
            pgBar.setVisibility(View.GONE);
        } else {
            pgBar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.green)));
            pgBar.setMax(30);
            Log.d("ProgressBar", "SetMaxto 30");
            Log.d("ProgressBar", "current is " + globalVariables.getTmpTime());
            pgBar.setProgress(globalVariables.getTmpTime());
        }
    }

    private void qcmNext(String userAnswerText, Handler handler, Runnable runnable, Context context, TextView prop) {
        Globals globalVariables = (Globals) getApplicationContext();
        Integer currentQuestionNumber = globalVariables.getCurrentQuestionNumero();
        globalVariables.setBrouillonText("");
        globalVariables.setReponseText("");
        globalVariables.getCurrentGame().addReponsesTemps(globalVariables.getTmpTime());
        globalVariables.getCurrentGame().addAnswerForPlayer1(currentQuestionNumber, userAnswerText);
        globalVariables.setTmpTime(30);
        final TextView prop1 = (TextView) findViewById(R.id.reponse_quiz1);
        prop1.setBackground(getResources().getDrawable(R.drawable.background_quiz));
        prop1.setTextColor(getResources().getColor(R.color.black));

        final TextView prop2 = (TextView) findViewById(R.id.reponse_quiz2);
        prop2.setBackground(getResources().getDrawable(R.drawable.background_quiz));
        prop2.setTextColor(getResources().getColor(R.color.black));

        final TextView prop3 = (TextView) findViewById(R.id.reponse_quiz3);
        prop3.setBackground(getResources().getDrawable(R.drawable.background_quiz));
        prop3.setTextColor(getResources().getColor(R.color.black));

        final TextView prop4 = (TextView) findViewById(R.id.reponse_quiz4);
        prop4.setBackground(getResources().getDrawable(R.drawable.background_quiz));
        prop4.setTextColor(getResources().getColor(R.color.black));


        prop.setBackground(getResources().getDrawable(R.drawable.background_quiz_selected));
        prop.setTextColor(getResources().getColor(R.color.white));
        if (globalVariables.getCurrentGame().getTimed().equals("true")) {
            handler.removeCallbacks(runnable);
        }
        nextPage(context);

    }


    @Override
    public void onBackPressed(){
        final Globals globalVariables = (Globals) getApplicationContext();
        if (!globalVariables.getCurrentGame().getTimed().equals("true")) {
            globalVariables.setReponseText("");
            globalVariables.setBrouillonText("");
            globalVariables.setCurrentQuestionNumero(globalVariables.getCurrentQuestionNumero() - 1);
            Intent intent = new Intent(this, QuizPage.class);
            startActivity(intent);
        }
    }


}
