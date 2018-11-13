package com.example.chloemolle.potecolle;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

/**
 * Created by chloemolle on 16/10/2018.
 */

public class ChoixMatierePage extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choix_matiere_page_layout);
/*
        FirebaseStorage storage = FirebaseStorage.getInstance();
*/
        // Create a storage reference from our app
/*
        StorageReference storageRef = storage.getReference();
        StorageReference mountainImagesRef = storageRef.child("maths/maths.png");
        mountainImagesRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Use the bytes to display the image
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                imageButton.setImageBitmap(Bitmap.createScaledBitmap(bmp, imageButton.getWidth(),
                        imageButton.getHeight(), false));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
*/

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
