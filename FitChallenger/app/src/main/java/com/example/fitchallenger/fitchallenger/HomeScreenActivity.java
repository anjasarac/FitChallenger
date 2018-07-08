package com.example.fitchallenger.fitchallenger;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.icu.text.AlphabeticIndex;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.app.ActionBar;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class HomeScreenActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Query QueryInvitedChallenges;
    private ChildEventListener InvitedChallengesListener;
    private String myID;
    private String username;
    ArrayList<String> challengeTypes;
    ArrayList<Integer> challengeImages;
    ArrayList<String> challengePoints;
    ArrayList<String> challengeIDs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref = getSharedPreferences("CurrentUser", Context.MODE_PRIVATE);
        String currentID =sharedPref.getString("myID","");
        username = sharedPref.getString("username","");

        mAuth= FirebaseAuth.getInstance();

        myID = mAuth.getUid();

        if(currentID.compareTo(mAuth.getUid())!=0)
            getUserData();
        setContentView(R.layout.activity_home_screen);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        challengeIDs = new ArrayList<>();
        challengeTypes = new ArrayList<>();
        challengeImages = new ArrayList<>();
        challengePoints = new ArrayList<>();

        LoadChallenges();

        ListView list = findViewById(R.id.listViewChallenges);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent i = new Intent(HomeScreenActivity.this,ChallengeProfileActivity.class);
                i.putExtra("challengeId", challengeIDs.get(position));
                startActivity(i);


            }
        });



    }

    @Override
    public void onBackPressed() {
       return;
    }

    private void LoadChallenges()
    {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference dr = db.getReference("Invites");
        QueryInvitedChallenges= dr.orderByChild(myID).equalTo(username);
        InvitedChallengesListener= QueryInvitedChallenges.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                String m=dataSnapshot.getKey();
                //    Toast.makeText(MapsActivity.this,m,Toast.LENGTH_SHORT).show();

                GetChallengesFromFirebase(m);


            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                //kad se promeni invite - to nikad nece da se desava
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

                String m=dataSnapshot.getKey();
                //Toast.makeText(HomeScreenActivity.this,"Obrisan iz baze "+ m,Toast.LENGTH_SHORT).show();

                  //m je ID

                int index = challengeIDs.lastIndexOf(m);
                challengeImages.remove(index);
                challengeTypes.remove(index);
                challengePoints.remove(index);

                challengeIDs.remove(index);
                SetAdapter();




            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void GetChallengesFromFirebase(final String m)
    {

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference dr = db.getReference("Challenge");


        final Query query = dr.orderByKey().equalTo(m);
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Challenge challenge  = dataSnapshot.getValue(Challenge.class);
                if(ValidateDate(challenge.endDate))
                    RemoveExpiredChallenge(m,challenge.dynamic);
               else
                {
                    challengeIDs.add(m);

                    challengeTypes.add(challenge.type);


                    int resId = HomeScreenActivity.this.getResources().getIdentifier(
                            challenge.type,
                            "drawable",
                            HomeScreenActivity.this.getPackageName()
                    );


                    challengePoints.add("You can earn " + String.valueOf(challenge.points) +  " points.");
                    challengeImages.add(resId);

                    SetAdapter();

                }

                query.removeEventListener(this);

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

    private void SetAdapter()
    {
        CustomListAdapter adapter=new CustomListAdapter(this, challengeTypes, challengeImages,null,challengePoints);
        ListView list=(ListView)findViewById(R.id.listViewChallenges);
        list.setAdapter(adapter);
    }


    private void getUserData() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference dr = db.getReference("User").child(mAuth.getUid());
        dr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user=dataSnapshot.getValue(User.class);
                SharedPreferences sharedPref = getSharedPreferences("CurrentUser",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("username",user.username);
                editor.putString("name",user.name);
                editor.putString("lastname",user.lastname);
                editor.putString("phone",user.phone);
                editor.putLong("age",user.age);
                editor.putLong("points",user.points);
                editor.putString("picture",user.picture);
                editor.putString("myID",mAuth.getUid());

                username = user.username;
                myID = mAuth.getUid();

                editor.commit();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(this,SettingsActivity.class);
            startActivity(i);
        }

        if (id == R.id.action_myprofile) {
            Intent i = new Intent(this,MyProfileActivity.class);
            startActivity(i);
        }
        if (id == R.id.action_map) {
            Intent i = new Intent(this,MapsActivity.class);
            startActivity(i);
        }
        if (id == R.id.action_createChallenge) {
            Intent i = new Intent(this,CreateChallengeActivity.class);
            startActivity(i);
        }
        if(id == R.id.action_userBoard){
            Intent i = new Intent(this,UserBoardActivity.class);
            startActivity(i);
        }
        if(id==R.id.action_signout)
        {
            Intent i = new Intent(this,LoginActivity.class);
            mAuth.signOut();
            startActivity(i);
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private Boolean ValidateDate(String endDate)
    {

        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        String test = df.format(date);
        Date d1 = null;
        Date d2 = null;
        try {
            d1 = df.parse(test);
            d2 = df.parse(endDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(d1.after(d2))
        {
            Toast.makeText(HomeScreenActivity.this,"Challenge expired."+ endDate,Toast.LENGTH_SHORT).show();

        }
        return d1.after(d2);
    }

    private void RemoveExpiredChallenge(String ID,Boolean dynamic)
    {
        //Toast.makeText(HomeScreenActivity.this,"Delete"+ ID,Toast.LENGTH_SHORT).show();
        DatabaseReference database=FirebaseDatabase.getInstance().getReference();
        database.child("Challenge").child(ID).setValue(null);
        database.child("Invites").child(ID).setValue(null);
        database.child("FinishedBy").child(ID).setValue(null);
        database.child("Geofire").child(ID).setValue(null);
        if(dynamic)
            database.child("Dynamic").child(ID).setValue(null);

    }
}
