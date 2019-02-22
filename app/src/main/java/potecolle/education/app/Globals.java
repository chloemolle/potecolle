package potecolle.education.app;

import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chloemolle.potecolle.R;
import com.google.firebase.firestore.DocumentReference;

/**
 * Created by chloemolle on 19/10/2018.
 */

public class Globals extends Application {

    private Game currentGame;
    private DocumentReference userDB;
    private User user;
    private Integer currentQuestionNumero = 0;
    private String brouillonText = "";
    private String reponseText = "";
    private Boolean debug = true;
    private int tmpTime = 0;
    private Long test = new Long(0);

    public Game getCurrentGame() {
        return currentGame;
    }

    public void setCurrentGame(Game currentGame) {
        this.currentGame = currentGame;
    }

    public Integer getCurrentQuestionNumero() {
        return currentQuestionNumero;
    }

    public void setCurrentQuestionNumero(Integer currentQuestionNumero) {
        this.currentQuestionNumero = currentQuestionNumero;
    }

    public void addQuestionToGame(Question question) {
        this.currentGame.addQuestions(question);
    }


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getBrouillonText() {
        return brouillonText;
    }

    public void setBrouillonText(String brouillonText) {
        this.brouillonText = brouillonText;
    }

    public String getReponseText() {
        return reponseText;
    }

    public void setReponseText(String reponseText) {
        this.reponseText = reponseText;
    }

    public Boolean getDebug() {
        return debug;
    }

    public void setDebug(Boolean debug) {
        this.debug = debug;
    }

    public int getTmpTime() {
        return tmpTime;
    }

    public void setTmpTime(int tmpTime) {
        this.tmpTime = tmpTime;
    }

    public Long getTest() {
        return test;
    }

    public void setTest(Long test) {
        this.test = test;
    }

    public DocumentReference getUserDB() {
        return userDB;
    }

    public void setUserDB(DocumentReference userDB) {
        this.userDB = userDB;
    }


    public static void makeToast(String textAAfficher, View layout, Context context) {

        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(textAAfficher);

        Toast toast = new Toast(context);
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    public static void openPopup(Context context, Integer level) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_level_up);
        Button retour = (Button) dialog.findViewById(R.id.retour_popup);
        TextView textBravo = (TextView) dialog.findViewById(R.id.bravo_text);
        textBravo.setText("Bravo !");
        TextView text = (TextView) dialog.findViewById(R.id.level_up_text);
        text.setText("Tu passes au niveau " + level + " ;)");
        retour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public static void openPopupThankYou(Context context) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_thank_you);
        Button retour = (Button) dialog.findViewById(R.id.retour_popup);
        retour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }



}
