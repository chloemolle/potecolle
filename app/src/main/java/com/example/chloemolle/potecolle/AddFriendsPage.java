package com.example.chloemolle.potecolle;

import android.app.Activity;
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

    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setContentView(R.layout.add_friends_page_layout);

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseUser userAuth = FirebaseAuth.getInstance().getCurrentUser();
        final ArrayAdapter<String> users = new ArrayAdapter<String>(this, R.layout.layout_text_view);
        final Globals globalVariables = (Globals) getApplicationContext();


        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBarAddFriends);
        progressBar.setVisibility(View.VISIBLE);


        SearchView searchView = (SearchView) findViewById(R.id.add_friends_search);
        searchView.setQueryHint("Chercher un ami");
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

        FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
        mFunctions
                .getHttpsCallable("getUsers")
                .call("")
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        final ArrayList<HashMap<String, String>> arr = (ArrayList<HashMap<String, String>>) task.getResult().getData();
                        final ArrayList<Integer> indexesToRemove = new ArrayList<>();
                        for (final HashMap<String, String> ami : arr) {
                            final String email = ami.get("email");
                            if (!userAuth.getEmail().equals(email)) {
                                final ArrayList<String> friends = globalVariables.getUser().getFriends();
                                db.collection("Users").document(userAuth.getEmail())
                                        .collection("FriendRequests")
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                List<DocumentSnapshot> listFriendRequests = task.getResult().getDocuments();
                                                ArrayList<String> emailFriendRequest = new ArrayList<>();
                                                for (DocumentSnapshot friendRequest : listFriendRequests) {
                                                    emailFriendRequest.add(friendRequest.get("email").toString());
                                                }
                                                if (friends.indexOf(email) >= 0 || emailFriendRequest.indexOf(email) >= 0) {
                                                    indexesToRemove.add(arr.indexOf(ami));
                                                } else {
                                                    users.add(ami.get("username") + "\n" + email);
                                                }
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d("Fail", e.getMessage());
                                                if (!userAuth.getEmail().equals(email)) {
                                                    if (friends.indexOf(email) >= 0) {
                                                        indexesToRemove.add(arr.indexOf(ami));
                                                    } else {
                                                        users.add(ami.get("username") + "\n" + email);
                                                    }
                                                } else {
                                                    indexesToRemove.add(arr.indexOf(ami));
                                                }
                                            }
                                        });
                            } else {
                                indexesToRemove.add(arr.indexOf(ami));

                            }
                        }
                        for (Integer indexToRemove: indexesToRemove) {
                            arr.remove(arr.get(indexToRemove));
                        }
                        final ArrayList<HashMap<String, String>> newArr = arr;
                        final ListView listView = (ListView) findViewById(R.id.add_friends_list);
                        listView.setAdapter(users);
                        progressBar.setVisibility(View.GONE);
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                                final View view = v;
                                // Fill in the friend database
                                HashMap<String, String> ami = newArr.get(position);
                                String email = ami.get("email");
                                User user = globalVariables.getUser();

                                ArrayList<String> friends = user.getFriends();
                                if (friends.indexOf(email) == -1) {
                                    HashMap<String, Object> updateUser = new HashMap<>();
                                    updateUser.put("friends", friends);
                                    friends.add(email);
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



                                HashMap<String, String> hashMap = new HashMap<String, String>();
                                hashMap.put("email", userAuth.getEmail());
                                hashMap.put("username", user.getUsername());
                                hashMap.put("demande", "false");
                                hashMap.put("pending", "false");
                                hashMap.put("accepte", "false");
                                hashMap.put("vu", "false");



                                db.collection("Users").document(email)
                                        .collection("FriendRequests")
                                        .document(userAuth.getEmail())
                                        .set(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "DocumentSnapshot successfully written!");
                                                LayoutInflater inflater = getLayoutInflater();
                                                View layout = inflater.inflate(R.layout.toast,
                                                        (ViewGroup) findViewById(R.id.custom_toast_container));

                                                TextView text = (TextView) layout.findViewById(R.id.text);
                                                text.setText(R.string.demande_envoye);

                                                Toast toast = new Toast(getApplicationContext());
                                                toast.setGravity(Gravity.BOTTOM, 0, 0);
                                                toast.setDuration(Toast.LENGTH_LONG);
                                                toast.setView(layout);
                                                toast.show();
                                                Intent intent = new Intent(view.getContext(), MainPage.class);
                                                startActivity(intent);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w(TAG, "Error writing document", e);
                                            }
                                        });


                                //Fill in your database
                                HashMap<String, String> addFriendRequest = new HashMap<String, String>();
                                hashMap.put("email", email);
                                hashMap.put("username", ami.get("username"));
                                hashMap.put("demande", "true");
                                hashMap.put("pending", "true");
                                hashMap.put("accepte", "false");
                                hashMap.put("vu", "false");


                                db.collection("Users").document(userAuth.getEmail())
                                        .collection("FriendRequests")
                                        .document(email)
                                        .set(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.w(TAG, "Friend request added to your database");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w(TAG, "Error writing document", e);
                                            }
                                        });

                            }
                        });

                        return "";
                    }
                });


    }
}