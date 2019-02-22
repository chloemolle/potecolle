package potecolle.education.app;

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
import android.widget.TextView;

import com.example.chloemolle.potecolle.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * Created by chloemolle on 16/10/2018.
 */

public class ChoixAmiPage extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choix_ami_page_layout);
        final Globals globalVariables = (Globals) getApplicationContext();

        User user = globalVariables.getUser();
        ArrayList<String> friends = user.getFriends();
        final LinearLayout layout = (LinearLayout) findViewById(R.id.linear_layout_ami);

        if (friends.size() > 0) {
            final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar_ami);
            progressBar.setVisibility(View.VISIBLE);

            goWithTheDatabase();

            progressBar.setVisibility(View.GONE);

        } else {
            final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar_ami);
            progressBar.setVisibility(View.GONE);
            TextView textView = new TextView(this);
            textView.setText(R.string.add_friends);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.gravity = Gravity.CENTER_HORIZONTAL;
            params.setMargins(0,20,0,0);
            textView.setLayoutParams(params);

            layout.addView(textView);
        }

        Button ajoutAmi = findViewById(R.id.ajouter_ami_button);
        ajoutAmi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), AddFriendsPage.class);
                startActivity(intent);
            }
        });

    }


    public void goWithTheDatabase(){
        final Globals globalVariables = (Globals) getApplicationContext();
        User user = globalVariables.getUser();
        ArrayList<String> friends = user.getFriends();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        for (String friend: friends) {
            final String email = friend;
            db.collection("Users").document(friend)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            final User user = documentSnapshot.toObject(User.class);
                            createButtonWithPlayerName(user.getUsername(), email);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("Fail", e.getMessage() + " il est fort probable qu'il n'est pas encore accepté la demande");
                        }
                    });
        }

    }

    public void createButtonWithPlayerName(String _name, final String email){
        final String name = _name;
        final Globals globalVariables = (Globals) getApplicationContext();
        final Context context = this;
        final LinearLayout layout = (LinearLayout) findViewById(R.id.linear_layout_ami);

        Button newButton = new Button(context);
        newButton.setText(name);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(20, 20, 20, 20);
        newButton.setLayoutParams(params);
        newButton.setTextColor(getResources().getColor(R.color.white));

        newButton.setBackground(getResources().getDrawable(R.drawable.button_with_radius));
        newButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Button b = (Button) v;
                Intent intent = new Intent(v.getContext(), QuizPage.class);
                Game game = globalVariables.getCurrentGame();
                game.setPlayer2(name);
                game.setAdversaire(email);
                game.setGame(globalVariables);
                startActivity(intent);
            }
        });

        layout.addView(newButton, 0);
    }

}