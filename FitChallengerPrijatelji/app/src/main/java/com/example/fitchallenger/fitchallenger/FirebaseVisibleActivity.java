package com.example.fitchallenger.fitchallenger;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseVisibleActivity extends AppCompatActivity {

    String myID;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_visible);

        mAuth = FirebaseAuth.getInstance();
        myID= mAuth.getCurrentUser().getUid();


        findViewById(R.id.yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseDatabase db = FirebaseDatabase.getInstance();
                DatabaseReference dr = db.getReference("User").child(myID);
                dr.child("visible").setValue(true);

                finish();
            }
        });



        findViewById(R.id.no).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseDatabase db = FirebaseDatabase.getInstance();
                DatabaseReference dr = db.getReference("User").child(myID);
                dr.child("visible").setValue(false);

                finish();
            }
        });


    }





}
