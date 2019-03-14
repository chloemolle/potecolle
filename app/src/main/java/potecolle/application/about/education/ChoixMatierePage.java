package potecolle.application.about.education;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.chloemolle.potecolle.R;

/**
 * Created by chloemolle on 16/10/2018.
 */

public class ChoixMatierePage extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choix_matiere_page_layout);

        final Globals globalVariables = (Globals) getApplicationContext();
        final Button bouton = (Button) findViewById(R.id.maths_button);

        bouton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ChoixSujetPage.class);
                globalVariables.getCurrentGame().setMatiere("Maths");
                startActivity(intent);
            }
        });

    }
}
