package potecolle.education.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chloemolle.potecolle.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

import io.github.kexanie.library.MathView;

/**
 * Created by chloemolle on 28/10/2018.
 */

public class ResultPage extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result_page_layout);
        final Globals globalVariables = (Globals) getApplicationContext();

        final Game game = globalVariables.getCurrentGame();

        TextView text = (TextView) findViewById(R.id.result_quiz_text);
        String votreScoreString = game.getScore();
        String sonScoreString = game.getScoreOpponent();

        Integer votreScore = Integer.parseInt(votreScoreString);
        Integer sonScore = Integer.parseInt(sonScoreString);

        ArrayList<Integer> reponsesTempsOpponent = game.getReponsesTempsOpponent();
        ArrayList<Integer> reponsesTemps = game.getReponsesTemps();

        Button revanche = (Button) findViewById(R.id.revanche);
        revanche.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                globalVariables.setCurrentGame(new Game(globalVariables.getUser().getUsername(), globalVariables.getCurrentGame().getPlayer2(), globalVariables.getCurrentGame().getAdversaire(), globalVariables.getUser().getClasse(), true));
                Intent intent = new Intent(v.getContext(), ChoixMatierePage.class);
                startActivity(intent);
            }
        });


        String quiAGagne = "";
        String diffSeconds = "";
        if (game.getTimed()) {
            Integer diff = 0;
            if (votreScore > sonScore) {
                quiAGagne =  " VICTOIRE ! :D" ;
            } else if (votreScore < sonScore) {
                quiAGagne =  " Perdu :( \n Prends ta revanche ! " ;
            } else {
                String diffTempsText = "";
                for (int i = 0; i < reponsesTemps.size(); i ++) {
                    diff += reponsesTemps.get(i) - reponsesTempsOpponent.get(i);
                }
                if (diff > 0) {
                    quiAGagne =  " VICTOIRE ! :D" ;
                    diffTempsText = "Vous avez le même score mais vous avez répondu plus rapidement que votre adversaire ;)";
                    TextView textDiffTemps = (TextView) findViewById(R.id.text_diff_temps);
                    textDiffTemps.setText(diffTempsText);
                } else if (diff < 0) {
                    quiAGagne =  " Perdu :( \n Prends ta revanche ! " ;
                    diffTempsText = "Vous avez le même score mais vous avez répondu moins rapidement que votre adversaire";
                    TextView textDiffTemps = (TextView) findViewById(R.id.text_diff_temps);
                    textDiffTemps.setText(diffTempsText);
                } else {
                    quiAGagne =  "   E ! :)" ;
                }
            }

        } else {
            quiAGagne =  votreScore > sonScore ? " VICTOIRE ! :D" :
                    votreScore == sonScore ? " EGALITE ! :)" : " Perdu :( \n Prends ta revanche ! ";
        }
        text.setText(quiAGagne);
        text.setGravity(Gravity.CENTER);
        text.setTextSize(30);
        text.setTextColor(getResources().getColor(R.color.colorTheme));

        TextView votreScoreText = (TextView) findViewById(R.id.result_quiz_text_votre_score);
        votreScoreText.setTextSize(20);
        String textScore = "Votre score: " + game.getScore() + "/" + game.getQuestionsId().size();
        if (globalVariables.getCurrentGame().getTimed().equals("true")) {
            textScore += diffSeconds;
        }
        votreScoreText.setText(textScore);
        votreScoreText.setTextColor(getResources().getColor(R.color.colorTheme));

        TextView sonScoreText = (TextView) findViewById(R.id.result_quiz_text_son_score);
        sonScoreText.setTextSize(20);
        String textSonScore = "Son score: " + game.getScoreOpponent() + "/" + game.getQuestionsId().size();
        sonScoreText.setText(textSonScore);
        sonScoreText.setTextColor(getResources().getColor(R.color.colorTheme));

        setResults();


        if(!game.getScoreVu()) {
            User user = globalVariables.getUser();
            Integer pointsToAdd = votreScore > sonScore ? 75 + (2 * votreScore - sonScore) * 10 : 50 + votreScore * 10;

            if (globalVariables.getCurrentGame().getTimed().equals("true")) {
               for (Integer integer: globalVariables.getCurrentGame().getReponsesTemps()){
                   pointsToAdd += integer;
               }
            }

            Integer previousLevel = user.getLevel();
            user.addPoints(pointsToAdd);
            if (previousLevel != user.getLevel()) {
                Globals.openPopup(this, globalVariables.getUser().getLevel());
            }
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.toast,
                    (ViewGroup) findViewById(R.id.custom_toast_container));

            TextView textToast = (TextView) layout.findViewById(R.id.text);
            textToast.setText("Bravo ! Vous avez gagné: " + pointsToAdd + " points !");
            Toast toast = new Toast(getApplicationContext());
            toast.setGravity(Gravity.BOTTOM, 0, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();


            HashMap<String, Object> updateUser = new HashMap<>();
            updateUser.put("level", user.getLevel());
            updateUser.put("pointsActuels", user.getPointsActuels());

            final FirebaseFirestore db = FirebaseFirestore.getInstance();
            final FirebaseUser userAuth = FirebaseAuth.getInstance().getCurrentUser();
            final DocumentReference userDB = db.collection("Users").document(userAuth.getEmail());

            userDB.update(updateUser)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d("Success", "C'est tout bon");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("Fail", "C'est pas bon");
                        }
                    });
            userDB.collection("Games").document(game.getId())
                    .update("scoreVu", true);

            game.setScoreVu(true);

        }
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this, MainPage.class);
        startActivity(intent);
    }

    public void setResults() {
        Globals globalVariables = (Globals) getApplicationContext();
        ArrayList<String> player1Answers = globalVariables.getCurrentGame().getPlayer1Answers();

        processAnswer(0, R.id.linear_layout_reponse1, R.id.question1_text_change, R.id.reponse1_text_change, R.id.reponse1_text, R.id.sa_reponse1_text_change, R.id.sa_reponse1_text, R.id.solution1_text_change, R.id.layout_solution1);
        processAnswer(1, R.id.linear_layout_reponse2, R.id.question2_text_change, R.id.reponse2_text_change, R.id.reponse2_text, R.id.sa_reponse2_text_change, R.id.sa_reponse2_text, R.id.solution2_text_change, R.id.layout_solution2);
        processAnswer(2, R.id.linear_layout_reponse3, R.id.question3_text_change, R.id.reponse3_text_change, R.id.reponse3_text, R.id.sa_reponse3_text_change, R.id.sa_reponse3_text, R.id.solution3_text_change, R.id.layout_solution3);
        processAnswer(3, R.id.linear_layout_reponse4, R.id.question4_text_change, R.id.reponse4_text_change, R.id.reponse4_text, R.id.sa_reponse4_text_change, R.id.sa_reponse4_text, R.id.solution4_text_change, R.id.layout_solution4);
        processAnswer(4, R.id.linear_layout_reponse5, R.id.question5_text_change, R.id.reponse5_text_change, R.id.reponse5_text, R.id.sa_reponse5_text_change, R.id.sa_reponse5_text, R.id.solution5_text_change, R.id.layout_solution5);

    }

    public void processAnswer(Integer i, Integer id_layout, Integer id_question, Integer id_reponse, Integer id_reponse_inchange,  Integer id_sa_reponse, Integer id_sa_reponse_inchange, Integer id_solution, Integer id_layout_solution) {
        final Globals globalVariables = (Globals) getApplicationContext();

        Game game = globalVariables.getCurrentGame();
        ArrayList<String> player1Answers = game.getPlayer1Answers();
        ArrayList<String> player2Answers = game.getPlayer2Answers();

        ArrayList<Question> realAnswers = game.getQuestions();

        String playerAnswer = player1Answers.get(i);
        String adversaireAnswer = player2Answers.get(i);
        String answer = realAnswers.get(i).getReponse();
        String question = realAnswers.get(i).getQuestion();

        MathView textQuestion = findViewById(id_question);
        textQuestion.setText(question);

        String realAnswer = "";
        try {
            realAnswer = answer;
        } catch (Exception e) {
            Log.e("ERROR", "probleme" + realAnswers.get(i));
        }
        globalVariables.getCurrentGame().getQuestions().get(i).setNombrePose(globalVariables.getCurrentGame().getQuestions().get(i).getNombreReussi() + 1);


        LinearLayout llText = findViewById(id_layout);

        if (playerAnswer.replaceAll("\\s", "").equalsIgnoreCase(realAnswer.replaceAll("\\s", ""))) {

            globalVariables.getCurrentGame().setReponsesTempsIndexScore(i);

            TextView textReponseInchange = (TextView) findViewById(id_reponse_inchange);
            textReponseInchange.setTextColor(getResources().getColor(R.color.green));

            MathView textReponse = findViewById(id_reponse);
            textReponse.setText(playerAnswer);
            llText.removeView(findViewById(id_layout_solution));
            llText.setBackground(getResources().getDrawable(R.drawable.reponse_true));
        } else {
            globalVariables.getCurrentGame().setReponsesTempsIndexScore0(i);

            TextView textReponseInchange = (TextView) findViewById(id_reponse_inchange);
            textReponseInchange.setTextColor(getResources().getColor(R.color.red));

            MathView textReponse = findViewById(id_reponse);
            textReponse.setText(playerAnswer);
            MathView textSolution = findViewById(id_solution);
            textSolution.setText(realAnswer);
            llText.setBackground(getResources().getDrawable(R.drawable.reponse_false));
        }

        if (adversaireAnswer.replaceAll("\\s", "").equalsIgnoreCase(realAnswer.replaceAll("\\s", ""))) {
            globalVariables.getCurrentGame().setReponsesTempsIndexScore(i);

            TextView textReponseInchange = (TextView) findViewById(id_sa_reponse_inchange);
            textReponseInchange.setTextColor(getResources().getColor(R.color.green));

            MathView textReponse = findViewById(id_sa_reponse);
            textReponse.setText(adversaireAnswer);
        } else {
            globalVariables.getCurrentGame().setReponsesTempsIndexScore0(i);

            TextView textReponseInchange = (TextView) findViewById(id_sa_reponse_inchange);
            textReponseInchange.setTextColor(getResources().getColor(R.color.red));

            MathView textReponse = findViewById(id_sa_reponse);
            textReponse.setText(adversaireAnswer);
        }

    }

}

