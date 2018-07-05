package com.example.fitchallenger.fitchallenger;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.content.Intent;
import android.widget.CalendarView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.widget.Toast;

import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateChallengeActivity extends AppCompatActivity {

    private TextView mTextMessage;
    static final int PERMISSION_ACCESS_FINE_LOCATION = 1;
    private LatLng placeLoc;
    String challengeID;

    final Challenge challenge = new Challenge();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.running:
                    mTextMessage.setText("Running");
                    return true;
                case R.id.gym:
                    mTextMessage.setText("Gym");
                    return true;
                case R.id.custom:
                    mTextMessage.setText("Custom");
                    return true;
            }
            return false;
        }
    };
    private FirebaseDatabase db;
    private DatabaseReference dr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_challenge);
///preuzimanje id-a za Challenge
        db = FirebaseDatabase.getInstance();
        dr = db.getReference("Challenge");

        challengeID= dr.push().getKey();

        setTitle("Create running challenge");
        mTextMessage = (TextView) findViewById(R.id.message);
        final BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation1);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        findViewById(R.id.running).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                setTitle("Create running challenge");
                View l=(View)findViewById(R.id.running_container);
                l.setVisibility(View.VISIBLE);
                View g=(View)findViewById(R.id.gym_container);
                g.setVisibility(View.INVISIBLE);
                View l1=(View)findViewById(R.id.calendar_container);
                l1.setVisibility(View.INVISIBLE);
                navigation.getMenu().findItem(R.id.running).setChecked(true);



            }
        });
        findViewById(R.id.gym).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setTitle("Create gym challenge");
                View l=(View)findViewById(R.id.running_container);
                l.setVisibility(View.INVISIBLE);
                View g=(View)findViewById(R.id.gym_container);
                g.setVisibility(View.VISIBLE);
                View l1=(View)findViewById(R.id.calendar_container);
                l1.setVisibility(View.INVISIBLE);
                navigation.getMenu().findItem(R.id.gym).setChecked(true);



            }
        });
        findViewById(R.id.custom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                setTitle("Create custom challenge");
                View l=(View)findViewById(R.id.running_container);
                l.setVisibility(View.INVISIBLE);
                View g=(View)findViewById(R.id.gym_container);
                g.setVisibility(View.INVISIBLE);
                View l1=(View)findViewById(R.id.calendar_container);
                l1.setVisibility(View.INVISIBLE);
                navigation.getMenu().findItem(R.id.custom).setChecked(true);

            }
        });

        findViewById(R.id.calendarButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                View l=(View)findViewById(R.id.calendar_container);
                l.setVisibility(View.VISIBLE);
                View g=(View)findViewById(R.id.gym_container);
                g.setVisibility(View.INVISIBLE);
                View B=(View)findViewById(R.id.running_container);
                B.setVisibility(View.INVISIBLE);
                //View BB=(View)findViewById(R.id.custom_container);
                //BB.setVisibility(View.INVISIBLE);
                navigation.getMenu().findItem(R.id.gym).setChecked(true);


            }
        });

        EditText e1,e2;
        e1 = (EditText)findViewById(R.id.editText3);
        e2 = (EditText)findViewById(R.id.editText4);

        //SharedPreferences sharedPref = getSharedPreferences("CurrentUser", Context.MODE_PRIVATE);
        //double lat = Double.valueOf(sharedPref.getString("latitude","")) ;
        //double lon = Double.valueOf(sharedPref.getString("longitude","")) ;






        LocationManager mLocationManager;
        Location myLocation;


        mLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
                //return;
            }


            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }


        myLocation = bestLocation;

        if(myLocation != null)
        {
            placeLoc = new LatLng(myLocation.getLatitude(),myLocation.getLongitude());

            e1.setText(String.valueOf(placeLoc.latitude));
            e2.setText(String.valueOf(placeLoc.longitude));
            challenge.latitude = String.valueOf(placeLoc.latitude);
            challenge.longitude = String.valueOf(placeLoc.longitude);


        }






        CalendarView view = (CalendarView) findViewById(R.id.calendarView);


        view.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {


                month = month+1;
                Date date = Calendar.getInstance().getTime();
                SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                String t = dayOfMonth+ "/"+ month +"/"+year;
                String test = df.format(date);
                Date d1 = null;
                Date d2 = null;
                try {
                    d1 = df.parse(t);
                    d2 = df.parse(test);
                } catch (ParseException e) {
                    e.printStackTrace();
                }


                if(d2.after(d1))
                {
                    Toast.makeText(CreateChallengeActivity.this,"Pick valid end date",Toast.LENGTH_SHORT).show();

                }
                else
                {
                    challenge.startDate = test;
                    challenge.endDate = t;

                    View l=(View)findViewById(R.id.calendar_container);
                    l.setVisibility(View.INVISIBLE);
                    View g=(View)findViewById(R.id.gym_container);
                    g.setVisibility(View.VISIBLE);
                }

                //Toast.makeText(getApplicationContext(),challenge.startDate,Toast.LENGTH_LONG).show();



            }
        });


        FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        SharedPreferences sharedPref = getSharedPreferences("CurrentUser", Context.MODE_PRIVATE);
        String username=sharedPref.getString("username","");
        challenge.username = username;
        challenge.userID = user.getUid();


       Button create = (Button)findViewById(R.id.create_button);
       create.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {




               EditText t = (EditText)findViewById(R.id.tasks);
               challenge.tasks = t.getText().toString();
               challenge.dynamic = false;
               challenge.type = "gym";
               challenge.points = 10;



               dr.child(challengeID).setValue(challenge).addOnCompleteListener(new OnCompleteListener<Void>() {
                   @Override
                   public void onComplete(@NonNull Task<Void> task) {
                       Toast.makeText(CreateChallengeActivity.this, "Challenge created with id:"+ challengeID, Toast.LENGTH_LONG).show();



                       Intent i = new Intent(CreateChallengeActivity.this,InviteFriendsActivity.class);
                       i.putExtra("challengeID",challengeID);
                       startActivity(i);

                        //challenge id
                       //intent i new activity
                   }
               });

               db.getReference("FinishedBy").child(challengeID).addListenerForSingleValueEvent(new ValueEventListener() {
                   @Override
                   public void onDataChange(DataSnapshot dataSnapshot) {
                       Map<String, String> stringStringHashMap =(Map<String, String>) dataSnapshot.getValue();
                       if(stringStringHashMap==null)
                       {
                           stringStringHashMap=new HashMap<>();
                       }
                       stringStringHashMap.put(challenge.userID,challenge.username);

                       FirebaseDatabase.getInstance().getReference().child("FinishedBy").child(challengeID)
                               .setValue(stringStringHashMap);
                   }

                   @Override
                   public void onCancelled(DatabaseError databaseError) {

                   }
               });
               DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Geofire");
               GeoFire geoFire = new GeoFire(ref);

               geoFire.setLocation(challengeID, new GeoLocation(Double.parseDouble(challenge.latitude),Double.parseDouble(challenge.longitude)), new GeoFire.CompletionListener() {
                   @Override
                   public void onComplete(String key, DatabaseError error) {
                       if (error != null) {
                           System.err.println("There was an error saving the location to GeoFire: " + error);
                       } else {
                           System.out.println("Location saved on server successfully!");
                       }
                   }
               });


           }


       });



    }




}
