package com.example.chloemolle.potecolle;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.firebase.ui.auth.AuthUI.TAG;

/**
 * Created by chloemolle on 22/11/2018.
 */

public class AddFriendsPage extends Activity {

    private FirebaseFirestore db;
    private FirebaseUser userAuth;
    private ArrayAdapter<String> users;
    private Globals globalVariables;

    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setContentView(R.layout.add_friends_page_layout);
        this.db = FirebaseFirestore.getInstance();
        this.userAuth = FirebaseAuth.getInstance().getCurrentUser();
        this.users = new ArrayAdapter<>(this, R.layout.layout_text_view);
        this.globalVariables = (Globals) getApplicationContext();

        setSearchFriends();
        getUsers();
    }

    public void setSearchFriends() {
        SearchView searchView = (SearchView) findViewById(R.id.add_friends_search);
        searchView.setQueryHint("Chercher un pote");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                users.getFilter().filter(newText);
                return false;
            }
        });
    }

    public void getUsers(){
        final ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBarAddFriends);
        progressBar.setVisibility(View.VISIBLE);

        FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
        mFunctions
                .getHttpsCallable("getUsers")
                .call("")
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        dealWithUsers(task, progressBar);
                        return "";
                    }
                });

    }

    public void dealWithUsers(Task<HttpsCallableResult> task, final ProgressBar progressBar){

        final ArrayList<HashMap<String, String>> arr = (ArrayList<HashMap<String, String>>) task.getResult().getData();

        for (final HashMap<String, String> ami : arr) {
            if (dealWithOneUser(ami)) {
                arr.remove(ami);
            }
        }

        final ListView listView = (ListView) findViewById(R.id.add_friends_list);
        listView.setAdapter(users);
        progressBar.setVisibility(View.GONE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                final View view = v;
                // Fill in the friend database
                HashMap<String, String> ami = arr.get(position);
                String email = ami.get("email");
                User user = globalVariables.getUser();

                ArrayList<String> friends = user.getFriends();

                //On vérifie encore qu'il n'est pas dans nos potes
                if (friends.indexOf(email) == -1) {
                    HashMap<String, Object> updateUser = new HashMap<>();
                    friends.add(email);
                    updateUser.put("friends", friends);
                    //On l'ajoute à nos amis
                    db.collection("Users").document(userAuth.getEmail())
                            .update(updateUser)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d("Success", "friend added");
                                    } else {
                                        Log.d("Error", task.getException().getMessage());
                                    }
                                }
                            });
                }


                //On met à jour la friendRequest de notre ami et la notre
                fillDatabase(email, userAuth.getEmail(), user.getUsername(), false, false, false, false, getApplicationContext(), getResources().getString(R.string.demande_envoye));
                fillDatabase(userAuth.getEmail(), email, ami.get("username"), true, true, false, true, getApplicationContext(), "");


            }
        });
    }

    public Boolean dealWithOneUser(final HashMap<String, String> ami){
        final String email = ami.get("email");
        //Si l'ami n'est pas nous même
        if (!userAuth.getEmail().equals(email)) {
            final ArrayList<String> friends = globalVariables.getUser().getFriends();
            ArrayList<FriendRequest> listFriendRequests = globalVariables.getUser().getFriendRequests();
            ArrayList<String> emailFriendRequest = new ArrayList<>();
            for (FriendRequest friendRequest : listFriendRequests) {
                emailFriendRequest.add(friendRequest.getEmail());
            }
            if (friends.indexOf(email) >= 0 || emailFriendRequest.indexOf(email) >= 0) {
                return true;
            } else {
                users.add(ami.get("username"));
                return false;
            }

        } else {
            // On l'enlève parce que c'est nous même
            return true;
        }
    }

    public void fillDatabase(String monEmail, String emailAdv, String username, Boolean demande, Boolean pending, Boolean accepte, Boolean vu, final Context context, final String forToast) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("email", emailAdv);
        hashMap.put("username", username);
        hashMap.put("demande", demande);
        hashMap.put("pending", pending);
        hashMap.put("accepte", accepte);
        hashMap.put("vu", vu);

        db.collection("Users").document(monEmail)
                .collection("FriendRequests")
                .document(emailAdv)
                .set(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");

                        if (!forToast.equals("")) {
                            LayoutInflater inflater = getLayoutInflater();
                            View layout = inflater.inflate(R.layout.toast,
                                    (ViewGroup) findViewById(R.id.custom_toast_container));

                            Globals.makeToast(forToast, layout, getApplicationContext());
                        }
                        Intent intent = new Intent(context, MainPage.class);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }


}