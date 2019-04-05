package potecolle.application.about.education;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chloemolle.potecolle.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.github.kexanie.library.MathView;

import static com.firebase.ui.auth.AuthUI.TAG;

/**
 * Created by chloemolle on 23/10/2018.
 */

public class QuizPage extends Activity {

    private Handler handler;
    private Runnable runnable;
    private Boolean enteredToBackground = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Globals globalVariables = (Globals) getApplicationContext();
        final Integer currentQuestionNumber = globalVariables.getCurrentQuestionNumero();
        final Question currentQuestion = globalVariables.getCurrentGame().getQuestions().get(currentQuestionNumber);

        if (currentQuestion.getType().equals("image")) {
            setContentView(R.layout.image_page_layout);
            EditText editText = (EditText) findViewById(R.id.user_answer);
            if (currentQuestion.getKeyboardType().equals("numberSigned")) {
                editText.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            }
        } else if (currentQuestion.getType().equals("imageqcm")) {
            setContentView(R.layout.image_qcm_page_layout);
        } else if (currentQuestion.getType().equals("qcm") || currentQuestion.getType().equals("questionInversé")) {
            setContentView(R.layout.qcm_page_layout);
            if (currentQuestionNumber < globalVariables.getCurrentGame().getPlayer1Answers().size()) {
                ImageButton bouton = (ImageButton) findViewById(R.id.next_quiz);
                bouton.setAlpha((float) 1.0);
                bouton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                            globalVariables.setBrouillonText("");
                            if (globalVariables.getCurrentGame().getTimed()) {
                                handler.removeCallbacks(runnable);
                            }
                            nextPage(v.getContext());
                    }
                });
            }
        } else {
            setContentView(R.layout.quiz_page_layout);
            EditText editText = (EditText) findViewById(R.id.user_answer);
            if (currentQuestion.getKeyboardType().equals("numberSigned")) {
                editText.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            }
        }

        if (globalVariables.getCurrentGame().getTimed()) {
            ImageButton boutonPrecedent = (ImageButton) findViewById(R.id.retour_precedent_quiz);
            boutonPrecedent.setVisibility(View.GONE);
        }

        TextView progressionQuiz = findViewById(R.id.avancement_quiz);
        Integer numeroQuesion = globalVariables.getCurrentQuestionNumero() + 1;
        progressionQuiz.setText(numeroQuesion + "/10");

        final ProgressBar pgBar = (ProgressBar) findViewById(R.id.progressBarTimer);
        this.handler = new Handler();
        final int delay = 1000; //milliseconds

        final EditText userAnswer = (EditText) findViewById(R.id.user_answer);

        this.runnable = new Runnable() {
            public void run() {
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
                    globalVariables.setTmpTime(60);
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

        if (globalVariables.getCurrentGame().getTimed()) {
            this.handler.postDelayed(this.runnable, delay);
        }


        if (currentQuestion.getType().contains("image")) {
            setRetourButton();
            setProgressBar();

            ImageButton boutonBrouillon = (ImageButton) findViewById(R.id.brouillon_button);
            boutonBrouillon.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), BrouillonPage.class);
                    if (globalVariables.getCurrentGame().getTimed()) {
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
                        globalVariables.setTmpTime(60);
                        if (globalVariables.getCurrentGame().getTimed()) {
                            handler.removeCallbacks(runnable);
                        }
                        nextPage(v.getContext());
                    }
                });
            }

            final ImageView imageView = (ImageView) findViewById(R.id.question_quiz);
            imageView.setImageBitmap(Bitmap.createScaledBitmap(currentQuestion.getBmp(), 200,
                    200, false));


            if (currentQuestion.getType().contains("qcm")) {


                ArrayList<String> propositionsShuffled = currentQuestion.getProposition();

                String answer = "";
                Boolean isAnswered = false;
                if (globalVariables.getCurrentGame().getPlayer1Answers().size() > currentQuestionNumber) {
                    answer = globalVariables.getCurrentGame().getPlayer1Answers().get(currentQuestionNumber);
                    isAnswered = true;
                }


                final MathView prop1 = (MathView) findViewById(R.id.reponse_quiz1);
                prop1.setPadding(20, 20, 20, 20);
                prop1.setText(propositionsShuffled.get(0));
                LinearLayout layout1 = findViewById(R.id.reponse_quiz1_layout);

                if (answer.equals(prop1.getText()) || !isAnswered) {
                  layout1.setAlpha(1);
                } else {
                  layout1.setAlpha((float) 0.5);
                }

                setOnClickListenerForLayout(R.id.reponse_quiz1_layout, prop1, R.id.reponse_quiz1_layout);


                final MathView prop2 = (MathView) findViewById(R.id.reponse_quiz2);
                prop2.setPadding(20, 20, 20, 20);
                prop2.setText(propositionsShuffled.get(1));
                LinearLayout layout2 = findViewById(R.id.reponse_quiz2_layout);

                if (answer.equals(prop2.getText()) || !isAnswered) {
                    layout2.setAlpha(1);
                } else {
                    layout2.setAlpha((float) 0.5);
                }


                setOnClickListenerForLayout(R.id.reponse_quiz2_layout, prop2, R.id.reponse_quiz2_layout);

                final MathView prop3 = (MathView) findViewById(R.id.reponse_quiz3);
                prop3.setPadding(20, 20, 20, 20);
                prop3.setText(propositionsShuffled.get(2));
                LinearLayout layout3 = findViewById(R.id.reponse_quiz3_layout);

                if (answer.equals(prop3.getText()) || !isAnswered) {
                    layout3.setAlpha(1);
                } else {
                    layout3.setAlpha((float) 0.5);
                }

                setOnClickListenerForLayout(R.id.reponse_quiz3_layout, prop3, R.id.reponse_quiz3_layout);

                final MathView prop4 = (MathView) findViewById(R.id.reponse_quiz4);
                prop4.setPadding(20, 20, 20, 20);
                prop4.setText(propositionsShuffled.get(3));
                LinearLayout layout4 = findViewById(R.id.reponse_quiz4_layout);

                if (answer.equals(prop4.getText()) || !isAnswered) {
                    layout4.setAlpha(1);
                } else {
                    layout4.setAlpha((float) 0.5);
                }


                setOnClickListenerForLayout(R.id.reponse_quiz4_layout, prop4, R.id.reponse_quiz4_layout);
            }

        } else if (currentQuestion.getType().equals("qcm") || currentQuestion.getType().equals("questionInversé")) {
            setRetourButton();
            setProgressBar();

            ImageButton boutonBrouillon = (ImageButton) findViewById(R.id.brouillon_button);
            boutonBrouillon.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), BrouillonPage.class);
                    if (globalVariables.getCurrentGame().getTimed()) {
                        handler.removeCallbacks(runnable);
                    }
                    startActivity(intent);
                }
            });


            ArrayList<String> propositionsShuffled = currentQuestion.getProposition();

            final MathView question = (MathView) findViewById(R.id.question_quiz);
            String text = currentQuestion.getQuestion();
            question.setText(currentQuestion.getQuestion());
            String answer = "";
            Boolean isAnswered = false;
            if (globalVariables.getCurrentGame().getPlayer1Answers().size() > currentQuestionNumber) {
                answer = globalVariables.getCurrentGame().getPlayer1Answers().get(currentQuestionNumber);
                isAnswered = true;
            }

            final MathView prop1 = (MathView) findViewById(R.id.reponse_quiz1);
            prop1.setPadding(20, 20, 20, 20);
            prop1.setText(propositionsShuffled.get(0));
            String test = propositionsShuffled.get(0);
            LinearLayout layout1 = findViewById(R.id.reponse_quiz1_layout);

            if (answer.equals(prop1.getText()) || !isAnswered) {
                layout1.setAlpha(1);
            } else {
                layout1.setAlpha((float) 0.5);
            }


            setOnClickListenerForLayout(R.id.reponse_quiz1_layout, prop1, R.id.reponse_quiz1_layout);


            final MathView prop2 = (MathView) findViewById(R.id.reponse_quiz2);
            prop2.setPadding(20, 20, 20, 20);
            prop2.setText(propositionsShuffled.get(1));
            LinearLayout layout2 = findViewById(R.id.reponse_quiz2_layout);

            if (answer.equals(prop2.getText()) || !isAnswered) {
                layout2.setAlpha(1);
            } else {
                layout2.setAlpha((float) 0.5);
            }


            setOnClickListenerForLayout(R.id.reponse_quiz2_layout, prop2, R.id.reponse_quiz2_layout);


            final MathView prop3 = (MathView) findViewById(R.id.reponse_quiz3);
            prop3.setPadding(20, 20, 20, 20);
            prop3.setText(propositionsShuffled.get(2));
            LinearLayout layout3 = findViewById(R.id.reponse_quiz3_layout);

            if (answer.equals(prop3.getText()) || !isAnswered) {
                layout3.setAlpha(1);
            } else {
                layout3.setAlpha((float) 0.5);
            }

            setOnClickListenerForLayout(R.id.reponse_quiz3_layout, prop3, R.id.reponse_quiz3_layout);


            final MathView prop4 = (MathView) findViewById(R.id.reponse_quiz4);
            prop4.setPadding(20, 20, 20, 20);
            prop4.setText(propositionsShuffled.get(3));
            LinearLayout layout4 = findViewById(R.id.reponse_quiz4_layout);

            if (answer.equals(prop4.getText()) || !isAnswered) {
                layout4.setAlpha(1);
            } else {
                layout4.setAlpha((float) 0.5);
            }


            setOnClickListenerForLayout(R.id.reponse_quiz4_layout, prop4, R.id.reponse_quiz4_layout);


        } else {
            setRetourButton();
            setProgressBar();
            final MathView question = (MathView) findViewById(R.id.question_quiz);
            question.setText(currentQuestion.getQuestion());
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
                    if (globalVariables.getCurrentGame().getTimed()) {
                        handler.removeCallbacks(runnable);
                    }
                    Intent intent = new Intent(v.getContext(), BrouillonPage.class);
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
                    globalVariables.setTmpTime(60);
                    if (globalVariables.getCurrentGame().getTimed()) {
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
                    globalVariables.setTmpTime(60);
                    if (globalVariables.getCurrentGame().getTimed()) {
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
        if (currentQuestionNumber == 0) {
            bouton.setAlpha((float) 0.5);
        }
        if (currentQuestionNumber > 0) {
            bouton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    globalVariables.setReponseText("");
                    globalVariables.setBrouillonText("");
                    globalVariables.setCurrentQuestionNumero(currentQuestionNumber - 1);
                    globalVariables.setTmpTime(60);
                    Intent intent = new Intent(v.getContext(), QuizPage.class);
                    startActivity(intent);
                }
            });
        }

    }

    public void nextPage(final Context c) {
        final Globals globalVariables = (Globals) getApplicationContext();
        Integer currentQuestionNumber = globalVariables.getCurrentQuestionNumero();
        if (currentQuestionNumber == 9) {

            final FirebaseFirestore db = FirebaseFirestore.getInstance();
            final FirebaseUser userAuth = FirebaseAuth.getInstance().getCurrentUser();
            final DocumentReference userDB = db.collection("Users").document(userAuth.getEmail());
            HashMap<String, Object> updateUser = new HashMap<>();
            if(globalVariables.getCurrentGame().getSeul()) {
                Intent intent = new Intent(c, FinQuizPage.class);
                startActivity(intent);

                final Integer numberOfQuiz = globalVariables.getUser().getNumberOfQuizSolo() + 1;
                Log.d("Success", "Number quiz ok : " + globalVariables.getUser().getNumberOfQuizSolo());
                globalVariables.getUser().setNumberOfQuizSolo(numberOfQuiz);
                updateUser.put("numberOfQuizSolo", globalVariables.getUser().getNumberOfQuizSolo());
                userDB.update(updateUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Success", "Number quiz ok : " + numberOfQuiz);
                    }
                }). addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Failure", "number quiz not ok : " + numberOfQuiz);
                    }
                });





            } else {


                final Integer numberOfQuiz = globalVariables.getUser().getNumberOfQuizDuel() + 1;
                Log.d("Success", "Number quiz ok : " + globalVariables.getUser().getNumberOfQuizDuel());
                globalVariables.getUser().setNumberOfQuizDuel(numberOfQuiz);
                updateUser.put("numberOfQuizDuel", globalVariables.getUser().getNumberOfQuizDuel());
                userDB.update(updateUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Success", "Number quiz ok : " + numberOfQuiz);
                    }
                }). addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Failure", "number quiz not ok : " + numberOfQuiz);
                    }
                });


                userDB.collection("Games")
                        .document(globalVariables.getCurrentGame().getId())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    Game game = task.getResult().toObject(Game.class);
                                    if (game.getFini()) {
                                        updateUserGame();
                                        globalVariables.setCurrentQuestionNumero(0);
                                        updateGames(Integer.valueOf(globalVariables.getCurrentGame().getScore()));
                                        if (game.getScoreOpponent() != null){
                                            globalVariables.getCurrentGame().setScoreOpponent(game.getScoreOpponent());
                                            globalVariables.getCurrentGame().setPlayer2Answers(game.getPlayer2Answers());
                                            globalVariables.getCurrentGame().setReponsesTempsOpponent(game.getReponsesTempsOpponent());
                                        }

                                        Intent intent = new Intent(c, ResultPage.class);
                                        startActivity(intent);
                                    } else {
                                        Intent intent = new Intent(c, FinQuizPage.class);
                                        startActivity(intent);
                                    }
                                } else {
                                    Log.d("Fail", task.getException().getMessage());
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("fail", e.getMessage());
                            }
                        });
            }
        } else {
            globalVariables.setCurrentQuestionNumero(currentQuestionNumber + 1);
            Intent intent = new Intent(c, QuizPage.class);
            startActivity(intent);
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
            Log.d("ProgressBar", "SetMaxto 30");
            Log.d("ProgressBar", "current is " + globalVariables.getTmpTime());
            pgBar.setProgress(globalVariables.getTmpTime());
        }
    }

    private void qcmNext(String userAnswerText, Handler handler, Runnable runnable, Context context, MathView prop, Integer id_layout) {
        Globals globalVariables = (Globals) getApplicationContext();
        Integer currentQuestionNumber = globalVariables.getCurrentQuestionNumero();
        globalVariables.setBrouillonText("");
        globalVariables.setReponseText("");
        globalVariables.getCurrentGame().addReponsesTemps(globalVariables.getTmpTime());
        globalVariables.getCurrentGame().addAnswerForPlayer1(currentQuestionNumber, userAnswerText);
        globalVariables.setTmpTime(60);

        LinearLayout layoutReponse1 = (LinearLayout) findViewById(R.id.reponse_quiz1_layout);
        layoutReponse1.setAlpha((float) 0.5);
        LinearLayout layoutReponse2 = (LinearLayout) findViewById(R.id.reponse_quiz2_layout);
        layoutReponse2.setAlpha((float) 0.5);
        LinearLayout layoutReponse3 = (LinearLayout) findViewById(R.id.reponse_quiz3_layout);
        layoutReponse3.setAlpha((float) 0.5);
        LinearLayout layoutReponse4 = (LinearLayout) findViewById(R.id.reponse_quiz4_layout);
        layoutReponse4.setAlpha((float) 0.5);

        LinearLayout layoutReponse = (LinearLayout) findViewById(id_layout);
        layoutReponse.setAlpha(1);

        if (globalVariables.getCurrentGame().getTimed()) {
            handler.removeCallbacks(runnable);
        }
        nextPage(context);

    }

    @Override
    public void onBackPressed() {
        final Globals globalVariables = (Globals) getApplicationContext();
        if (!globalVariables.getCurrentGame().getTimed() && globalVariables.getCurrentQuestionNumero() > 0) {
            globalVariables.setReponseText("");
            globalVariables.setBrouillonText("");
            globalVariables.setCurrentQuestionNumero(globalVariables.getCurrentQuestionNumero() - 1);
            Intent intent = new Intent(this, QuizPage.class);
            startActivity(intent);
        }
    }

    public void updateUserGame() {
        final Globals globalVariables = (Globals) getApplicationContext();
        ArrayList<String> player1Answers = globalVariables.getCurrentGame().getPlayer1Answers();
        ArrayList<Question> realAnswers = globalVariables.getCurrentGame().getQuestions();

        Integer score = 0;
        for (Integer i = 0; i < player1Answers.size(); i++) {
            String playerAnswer = player1Answers.get(i);
            Object answer = realAnswers.get(i).getReponse();
            String realAnswer = "";
            try {
                realAnswer = answer.toString();
            } catch (Exception e) {
                Log.e("ERROR", "probleme" + realAnswers.get(i));
            }

            if (playerAnswer.replaceAll("\\s", "").equalsIgnoreCase(realAnswer.replaceAll("\\s", ""))) {
                score++;
                globalVariables.getCurrentGame().setReponsesTempsIndexScore(i);
            }
        }


        globalVariables.getCurrentGame().setScore(score.toString());

        /*

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseUser userAuth = FirebaseAuth.getInstance().getCurrentUser();
        final DocumentReference userDB = db.collection("Users").document(userAuth.getEmail());
        final Integer scoreFinal = score;
        if (!globalVariables.getCurrentGame().getSeul()) {

            Map<String, Object> updateFields = new HashMap<>();
            updateFields.put("score", scoreFinal.toString());
            updateFields.put("repondu", true);
            updateFields.put("vu", true);
            updateFields.put("reponsesTemps", globalVariables.getCurrentGame().getReponsesTemps());


            userDB.collection("Games").document(globalVariables.getCurrentGame().getId())
                    .update(updateFields)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully updated!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error updating document", e);
                        }
                    });


            final Map<String, Object> updateOtherFields = new HashMap<>();
            updateOtherFields.put("reponsesTempsOpponent", globalVariables.getCurrentGame().getReponsesTemps());
            updateOtherFields.put("scoreOpponent", scoreFinal.toString());
            updateOtherFields.put("fini", true);
            updateOtherFields.put("vu", false);


            db.collection("Users")
                    .document(globalVariables.getCurrentGame().getAdversaire())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                final DocumentReference opponentDB = db.collection("Users").document(document.getId());
                                opponentDB.collection("Games")
                                        .document(globalVariables.getCurrentGame().getId())
                                        .update(updateOtherFields)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "DocumentSnapshot successfully updated!");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w(TAG, "Error updating document", e);
                                            }
                                        });
                            }

                        }
                    });
        }
*/
    }

    @Override
    public void onPause() {
        super.onPause();
        this.enteredToBackground = true;
        final Globals globalVariables = (Globals) getApplicationContext();
        if (globalVariables.getCurrentGame().getTimed()) {
            if (this.handler != null) {
                this.handler.removeCallbacks(this.runnable);
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        final Globals globalVariables = (Globals) getApplicationContext();
        if (globalVariables.getCurrentGame().getTimed() && enteredToBackground) {
            Intent intent = new Intent(this, MainPage.class);
            startActivity(intent);
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.toast,
                    (ViewGroup) findViewById(R.id.custom_toast_container));

            TextView text = (TextView) layout.findViewById(R.id.text);
            globalVariables.setCurrentQuestionNumero(0);

            if(globalVariables.getCurrentGame().getSeul()) {
                text.setText(R.string.stop_partie_seul);
            } else {
                text.setText(R.string.stop_partie);
            }

            Toast toast = new Toast(getApplicationContext());
            toast.setGravity(Gravity.BOTTOM, 0, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();

        }
        this.enteredToBackground = false;
    }


    public void updateGames(Integer scoreFinal) {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseUser userAuth = FirebaseAuth.getInstance().getCurrentUser();
        final DocumentReference userDB = db.collection("Users").document(userAuth.getEmail());

        final Globals globalVariables = (Globals) getApplicationContext();
        Map<String, Object> updateFields = new HashMap<>();
        updateFields.put("score", scoreFinal.toString());
        updateFields.put("repondu", true);
        updateFields.put("player1Answers", globalVariables.getCurrentGame().getPlayer1Answers());
        updateFields.put("vu", true);
        updateFields.put("reponsesTemps", globalVariables.getCurrentGame().getReponsesTemps());

        Game game = globalVariables.getCurrentGame();
        game.setScore(scoreFinal.toString());
        game.setRepondu(true);
        game.setVu(true);

        userDB.collection("Games").document(globalVariables.getCurrentGame().getId())
                .update(updateFields)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });


        final Map<String, Object> updateOtherFields = new HashMap<>();
        updateOtherFields.put("reponsesTempsOpponent", globalVariables.getCurrentGame().getReponsesTemps());
        updateOtherFields.put("scoreOpponent", scoreFinal.toString());
        updateOtherFields.put("player2Answers", globalVariables.getCurrentGame().getPlayer1Answers());
        updateOtherFields.put("fini", true);
        updateOtherFields.put("vu", false);


        db.collection("Users")
                .document(globalVariables.getCurrentGame().getAdversaire())
                .collection("Games")
                .document(globalVariables.getCurrentGame().getId())
                .update(updateOtherFields)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });

    }

    private void setOnClickListenerForLayout(Integer id, final MathView prop, final Integer id_layout) {
        LinearLayout prop1Layout = (LinearLayout) findViewById(id);
        prop1Layout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String userAnswerText = prop.getText();
                if (userAnswerText.isEmpty()) {
                    return;
                }
                qcmNext(userAnswerText, handler, runnable, v.getContext(), prop, id_layout);
            }
        });

    }


}
