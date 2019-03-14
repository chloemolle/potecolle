package potecolle.application.about.education;

import android.app.Activity;
import android.os.Bundle;

import com.example.chloemolle.potecolle.R;

public class NoAccountPage extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.no_account_page_layout);
    }

    @Override
    public void onBackPressed(){
        moveTaskToBack(true);
    }

}
