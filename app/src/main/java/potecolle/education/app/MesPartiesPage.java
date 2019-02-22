package potecolle.education.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.chloemolle.potecolle.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;

public class MesPartiesPage extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mes_parties_page_layout);
        Globals globalVariables = (Globals) getApplicationContext();
        User user = globalVariables.getUser();

        setButtonRetourMainPage();

        ArrayList<Game> mesParties = user.getPartiesEnCours();
        if (mesParties.size() > 0) {
            setLayoutForGames(mesParties);
        } else {
            setLayoutForEnCoursGamesEmpty();
            setLayoutForFiniesGamesEmpty();
        }

    }

    public void setLayoutForEnCoursGamesEmpty() {
        TextView emptyGame = new TextView(this);
        emptyGame.setText("Tu n'as pas de partie en cours... Lance une partie contre un pote ;)");
        emptyGame.setTextColor(getResources().getColor(R.color.colorTheme));
        emptyGame.setTextSize(20);
        emptyGame.setPadding(50, 20, 50, 20);
        emptyGame.setGravity(Gravity.CENTER);
        final LinearLayout layout = (LinearLayout) findViewById(R.id.layout_partie_en_cours);
        layout.addView(emptyGame);
    }

    public void setLayoutForFiniesGamesEmpty() {
        final LinearLayout layout = (LinearLayout) findViewById(R.id.layout_partie_fini);
        layout.removeView((TextView) findViewById(R.id.parties_finies_text));
    }


    public void setLayoutForGames(ArrayList<Game> mesParties) {
        final Context context = this;
        final Globals globalVariables = (Globals) getApplicationContext();
        final DocumentReference userDB = globalVariables.getUserDB();
        final LinearLayout layoutEnCours = (LinearLayout) findViewById(R.id.layout_partie_en_cours);
        final LinearLayout layoutFinies = (LinearLayout) findViewById(R.id.layout_partie_fini);

        Boolean isEnCoursEmpty = true;
        Boolean isFiniesEmpty = true;

        for (final Game game: mesParties) {
            // on ajoute un bouton pour accéder à la partie (/!!!!\ à changer rapidement
            final Button newButton = new Button(this);

            final Boolean repondu = game.getRepondu();
            final Boolean fini = game.getFini();

            final String finiOuPas = (repondu && !fini)?
                    "Attends que " + game.getPlayer2() + " joue!" :
                    (repondu && fini) ?
                            "Regarde les résultats !\n"+ game.getPlayer2() : "Réponds aux questions contre " + game.getPlayer2() + " :) ";


            newButton.setText(finiOuPas + "\n" + game.getClasse() + "\n" + game.getMatiere() + " " + game.getSujet());
            newButton.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(25,25,25,0);
            newButton.setPadding(100, 0, 100, 0);
            newButton.setLayoutParams(params);
            newButton.setTextColor(getResources().getColor(R.color.colorTheme));
            newButton.setBackground(getResources().getDrawable(R.drawable.box_pour_entoure));
            if (!repondu) {
                newButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        globalVariables.setCurrentGame(game);
                        Intent intent = new Intent(v.getContext(), QuizPage.class);
                        startActivity(intent);
                    }
                });
            } else if (fini) {
                newButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        globalVariables.setCurrentGame(game);
                        Intent intent = new Intent(v.getContext(), ResultPage.class);
                        startActivity(intent);
                    }
                });

                newButton.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setMessage("Veux-tu supprimer cette partie ?")
                                .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        userDB.collection("Games")
                                                .document(game.getId())
                                                .delete()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){
                                                            layoutEnCours.removeView(newButton);
                                                            layoutFinies.removeView(newButton);
                                                            Log.d("Success", "document successfully deleted");
                                                        } else {
                                                            Log.d("Error", "document NOT successfully deleted");
                                                        }
                                                    }
                                                });
                                    }
                                })
                                .setNegativeButton("Non", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Log.d("Info","partie conservée");
                                    }
                                }).show();
                        return true;
                    }
                });


            } else {
                newButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        globalVariables.setCurrentGame(game);
                        Intent intent = new Intent(v.getContext(), FinQuizPage.class);
                        startActivity(intent);
                    }
                });

            }

            if (!fini || !repondu) {
                layoutEnCours.addView(newButton);
                isEnCoursEmpty = false;
            } else {
                layoutFinies.addView(newButton);
                isFiniesEmpty = false;
            }

        }

        if(isEnCoursEmpty) {
            setLayoutForEnCoursGamesEmpty();
        }
        if (isFiniesEmpty) {
            setLayoutForFiniesGamesEmpty();
        }

    }

    public void setButtonRetourMainPage() {
        Button retourMainPage = (Button) findViewById(R.id.retour_main_page_from_mes_parties);
        retourMainPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MainPage.class);
                startActivity(intent);
            }
        });
    }
}
