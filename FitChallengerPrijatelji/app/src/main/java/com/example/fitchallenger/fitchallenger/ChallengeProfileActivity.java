package com.example.fitchallenger.fitchallenger;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import static com.example.fitchallenger.fitchallenger.R.color.common_google_signin_btn_text_dark_disabled;


public class ChallengeProfileActivity extends AppCompatActivity {

    String challengeId;

    long finishedBy;
    private FirebaseAuth mAuth;
    private String myID;
    private String username;
    private long points;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        finishedBy=0;
        mAuth = FirebaseAuth.getInstance();
        myID = mAuth.getCurrentUser().getUid();
        SharedPreferences sharedPref = getSharedPreferences("CurrentUser", Context.MODE_PRIVATE);
        username = sharedPref.getString("username", "");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        Intent challengeIntent = getIntent();
        Bundle challengeBundle = challengeIntent.getExtras();

        challengeId = challengeBundle.getString("challengeId");

        Toast.makeText(ChallengeProfileActivity.this,"Challenge id: " + challengeId,Toast.LENGTH_SHORT).show();

        LoadChallengeInfo();
        LoadFinishedBy();


        findViewById(R.id.startChallenge).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.staticChallengeInfo).setVisibility(View.INVISIBLE);
                findViewById(R.id.staticChallengeTasks).setVisibility(View.VISIBLE);

            }
        });


        findViewById(R.id.finishChallenge).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final FirebaseDatabase db = FirebaseDatabase.getInstance();
                db.getReference("FinishedBy").child(challengeId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Map<String, String> stringStringHashMap =(Map<String, String>) dataSnapshot.getValue();
                        if(stringStringHashMap==null)
                        {
                            stringStringHashMap=new HashMap<>();
                        }
                        stringStringHashMap.put(myID,username);

                        FirebaseDatabase.getInstance().getReference().child("FinishedBy").child(challengeId)
                                .setValue(stringStringHashMap);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        db.getReference("User").child(myID).child("points").addListenerForSingleValueEvent(new ValueEventListener() {
    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        long p=((Long) dataSnapshot.getValue()) ;
        p+=points;
        SharedPreferences sharedPref = getSharedPreferences("CurrentUser", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPref.edit();
        editor.putLong("points",p);
        editor.commit();
        db.getReference("User").child(myID).child("points").setValue(p);
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
});


                Intent i = new Intent(ChallengeProfileActivity.this,MapsActivity.class);
                startActivity(i);
            }
        });


    }

    private void LoadFinishedBy() {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference("FinishedBy");
        rootRef.child(challengeId).addValueEventListener(new ValueEventListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                finishedBy=dataSnapshot.getChildrenCount();
                TextView fb=findViewById(R.id.finishedBy);
                fb.setText(String.valueOf(finishedBy));
                Map<String,String> finishedByMap=(Map<String, String>)dataSnapshot.getValue();
                if(finishedByMap!=null)
                    if(finishedByMap.containsKey(myID))
                    {
                        Button b=findViewById(R.id.startChallenge);
                        b.setClickable(false);
                        b.setBackgroundColor(R.color.common_google_signin_btn_text_dark_disabled);
                    }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void LoadChallengeInfo() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference dr = db.getReference("Challenge");


        final Query query = dr.orderByKey().equalTo(challengeId);
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Challenge challenge = dataSnapshot.getValue(Challenge.class);
                TextView pts=findViewById(R.id.points);
                points=challenge.points;
                pts.setText(String.valueOf(challenge.points));
                TextView type=findViewById(R.id.type);
                type.setText(challenge.type);
                TextView creator=findViewById(R.id.creator);
                creator.setText(challenge.username);
                TextView sDate=findViewById(R.id.startDate);
                sDate.setText(challenge.startDate);
                TextView eDate=findViewById(R.id.endDate);
                eDate.setText(challenge.endDate);


                ShowTasks(challenge.tasks);
                setTitle(challenge.type);
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

    private void ShowTasks(String tasks)
    {
        ListView lw = findViewById(R.id.listViewTasks);
        String[] tasks1 = tasks.split("\n");
        lw.setAdapter(new ArrayAdapter<String>(ChallengeProfileActivity.this, android.R.layout.simple_list_item_multiple_choice,tasks1 ));

        lw.setVisibility(View.VISIBLE);

    }

}
