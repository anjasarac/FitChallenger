package com.example.fitchallenger.fitchallenger;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class InviteFriendsActivity extends AppCompatActivity {


    String myID;
    Map<String,String> friends;
    Collection<String> friendsUsername;
    Collection<String> friendsIDs;
    FirebaseAuth mAuth;
    private DatabaseReference friendRefrence;
    private ValueEventListener friendsListener;
    private List friendList;
    private List friendListID;
    Map<String,String> Invitedfriends;
    String ChallengeID;
    //FirebaseUser me;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_friends);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);


        setTitle("Invite friends");

        final Intent challengeI = getIntent();
        Bundle challengeIExtras = challengeI.getExtras();

        ChallengeID = challengeIExtras.getString("challengeID");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.inviteFriendsButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Friends have been invited", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                FirebaseDatabase.getInstance().getReference().child("Invites").child(ChallengeID).setValue(Invitedfriends).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                      Toast.makeText(InviteFriendsActivity.this,"Friends invited.",Toast.LENGTH_SHORT).show();

                      Intent i = new Intent(InviteFriendsActivity.this,MapsActivity.class);
                      i.putExtra("challengeID",ChallengeID);
                      startActivity(i);


                    }
                });



            }
        });

        mAuth = FirebaseAuth.getInstance();
        myID= mAuth.getCurrentUser().getUid();

        friends=new HashMap<>();
        friendsUsername=new ArrayList<>();
        Invitedfriends=new HashMap<>();
        friendsIDs=new ArrayList<>();

        ListView friendsList = (ListView) findViewById(R.id.listViewFriends);
        friendsList.setOnItemClickListener(itemClickListener);

        getAllFriends();



    }

    OnItemClickListener itemClickListener = new OnItemClickListener() {
        @TargetApi(Build.VERSION_CODES.N)
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
            // AdapterView is the parent class of ListView
            ListView lv = (ListView) arg0;
            if(lv.isItemChecked(position)){
                Invitedfriends.put((String) friendListID.get(position), (String) friendList.get(position));
            }else{
                //Toast.makeText(getBaseContext(), "You unchecked " + friendList.get(position), Toast.LENGTH_SHORT).show();
                Invitedfriends.remove((String) friendListID.get(position));
            }
        }
    };

    // Setting the ItemClickEvent listener for the listview


    @Override
    public void onBackPressed() {
        return;
    }



    private void getAllFriends()
    {
        friendRefrence= FirebaseDatabase.getInstance().getReference().child("Friends").child(myID);
        friendsListener=friendRefrence.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                friends =(Map<String, String>) dataSnapshot.getValue();
                if(friends!=null) {
                    friendsUsername = friends.values();
                    friendsIDs=friends.keySet();
                }
                else
                {
                    friendsUsername=new ArrayList<>();
                }
                friendList = new ArrayList(friendsUsername);
                friendListID=new ArrayList(friendsIDs);
                ListView friendsList = (ListView) findViewById(R.id.listViewFriends);
                friendsList.setAdapter(new ArrayAdapter<String>(InviteFriendsActivity.this, android.R.layout.simple_list_item_multiple_choice,friendList ));
                friendsList.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

}
