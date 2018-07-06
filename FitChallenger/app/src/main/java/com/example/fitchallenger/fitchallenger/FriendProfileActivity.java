package com.example.fitchallenger.fitchallenger;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import java.net.URL;

public class FriendProfileActivity extends AppCompatActivity {

    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile);

        Intent friendIntent = getIntent();
        Bundle friendBundle = friendIntent.getExtras();


        userID = friendBundle.getString("friendID","");

        GetFriendInfo();


    }

    private void GetFriendInfo()
    {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference dr = db.getReference("User");


        final Query query = dr.orderByKey().equalTo(userID);
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String ss) {
                final User user  = dataSnapshot.getValue(User.class);

                setTitle(user.username);
                TextView name,lastname,phone,email,age,points;

                name = findViewById(R.id.textView1);
                lastname = findViewById(R.id.textView2);
                phone = findViewById(R.id.textView3);
                email = findViewById(R.id.textView4);
                age = findViewById(R.id.textView5);
                points = findViewById(R.id.textView7);

                name.setText(user.name);
                lastname.setText(user.lastname);
                phone.setText(user.phone);
                email.setText(user.email);
                age.setText(String.valueOf(user.age));
                points.setText(String.valueOf(user.points));

                ImageView i = findViewById(R.id.imageView);
                Picasso.with(FriendProfileActivity.this)
                        .load(user.picture)
                        .into(i);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }
}
