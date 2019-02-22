package potecolle.education.app;

import android.app.Activity;
import android.os.Bundle;

import com.example.chloemolle.potecolle.R;

public class NoAccountFailed extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.no_account_failed_layout);
    }

    @Override
    public void onBackPressed(){
        moveTaskToBack(true);
    }

}
