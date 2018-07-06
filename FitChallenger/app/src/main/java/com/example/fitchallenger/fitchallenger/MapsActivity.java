package com.example.fitchallenger.fitchallenger;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.location.Location;
import android.widget.LinearLayout;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import android.content.res.*;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    static final int PERMISSION_ACCESS_FINE_LOCATION = 1;
    private LatLng placeLoc;
    String username;
    String myID;
    String challengeID;
    private boolean myChallenges = false;
    public Challenge createdChallenge;
    public Boolean showFriends, showChallenges;
    private FirebaseAuth mAuth;
    private HashMap<Marker, String> markerChallenges;
    private HashMap<Marker, String> markerMyChallenges;
    private HashMap<Marker, String> markerMyFriends;
    public List<Challenge> invitedChallengesOnMap;
    public List<Challenge> myChallengesOnMap;
    private ChildEventListener MyChallengesListener;
    private ChildEventListener InvitedChallengesListener;
    private Query QueryInvitedChallenges;
    private Query QueryMyChallenges;
    String type = "all";
    String expires = "all";
    private Marker MyMarker;

    private BroadcastReceiver broadcastReceiver;
    private DatabaseReference friendRefrence;
    private ValueEventListener friendsListener;
    private Map<String, String> friends;
    private Set<String> friendsIDs;
    private ArrayList<User> friendsOnMap;


    @Override
    protected void onStop() {
        super.onStop();
       // Toast.makeText(this,"STOP",Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        SharedPreferences sharedPref = getSharedPreferences("CurrentUser", Context.MODE_PRIVATE);
        username = sharedPref.getString("username", "");

        showFriends = sharedPref.getBoolean("showFriends", false);
        showChallenges = sharedPref.getBoolean("showChallenges", true);




        mAuth = FirebaseAuth.getInstance();
        myID = mAuth.getCurrentUser().getUid();
        markerChallenges = new HashMap<Marker, String>();
        markerMyChallenges=new HashMap<Marker, String>();
        markerMyFriends=new HashMap<Marker, String>();

        final Intent challengeI = getIntent();

        Bundle challengeIExtras = challengeI.getExtras();

        if (challengeIExtras != null) {
            challengeID = challengeIExtras.getString("challengeID");
            //showChallenge(challengeID); ///kreirani challenge
        }


        invitedChallengesOnMap = new ArrayList<Challenge>();
        myChallengesOnMap=new ArrayList<Challenge>();
        friendsOnMap=new ArrayList<User>();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        if(showChallenges == true)
        {
            if(myChallenges)
            {
                findViewById(R.id.fab).setVisibility(View.INVISIBLE);
                ShowMyChallenges();

            }

            else
            {
                findViewById(R.id.fab).setVisibility(View.VISIBLE);
                ShowInvitedChallenges();
            }



        }

        else
            Toast.makeText(MapsActivity.this,"Show challenges disabled", Toast.LENGTH_SHORT).show();

        if(showFriends == true) {
            Toast.makeText(MapsActivity.this, "Show friends enabled", Toast.LENGTH_SHORT).show();
            ShowFriends();
        }
        else
            Toast.makeText(MapsActivity.this,"Show friends disabled", Toast.LENGTH_SHORT).show();


        final Spinner spinnerType = (Spinner) findViewById(R.id.spinnerType);
        String[] types = new String[]
                {
                        "all",
                        "gym",
                        "fitness",
                        "running",
                        "cycling",
                        "basketball",
                        "soccer",
                        "skiing",
                        "swimming",
                        "fishing"
                };

        ArrayAdapter<String> adapterType = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,types);
        spinnerType.setAdapter(adapterType);

        final Spinner spinnerExpires = (Spinner) findViewById(R.id.spinnerExpires);
        String[] dates = new String[]
                {
                        "all",
                        "today",
                        "this week",
                        "this month"

                };

        ArrayAdapter<String> adapterExpires = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,dates);
        spinnerExpires.setAdapter(adapterExpires);
        //setuju se filteri samo invited challenge-ima!!!
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Insert filters", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();


                findViewById(R.id.filters_container).setVisibility(View.VISIBLE);
                fab.setVisibility(View.INVISIBLE);


            }
        });


        findViewById(R.id.commitFIlter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               type = spinnerType.getSelectedItem().toString();
               expires = spinnerExpires.getSelectedItem().toString();
               findViewById(R.id.fab).setVisibility(View.VISIBLE);
               findViewById(R.id.filters_container).setVisibility(View.INVISIBLE);
                if(QueryInvitedChallenges!=null)
                    QueryInvitedChallenges.removeEventListener(InvitedChallengesListener);
                invitedChallengesOnMap.clear();
                RemoveAllMarkers(markerChallenges);
                ShowInvitedChallenges();
            }
        });




    }

    private void ShowFriends() {

        friendRefrence= FirebaseDatabase.getInstance().getReference().child("Friends").child(myID);
        friendsListener = friendRefrence.addValueEventListener(new ValueEventListener() {


            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                friends = (Map<String, String>) dataSnapshot.getValue();
                if (friends != null) {
                    friendsIDs = friends.keySet();
                }
                List<String> list= new ArrayList<>(friendsIDs);
               for (int i=0;i<list.size();i++)
               {
                   getFriendFromFirebase(list.get(i));
               }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void getFriendFromFirebase(final String s) {

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference dr = db.getReference("User");


        final Query query = dr.orderByKey().equalTo(s);
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String ss) {
                final User user  = dataSnapshot.getValue(User.class);
                if (user.visible) {
                    if (!friendsOnMap.contains(user))
                        friendsOnMap.add(user);

                    final LatLng ll = new LatLng(Double.parseDouble(user.latitude), Double.parseDouble(user.longitude));


                    Thread thread = new Thread(new Runnable(){
                        @Override
                        public void run(){
                            URL url ;
                            final MarkerOptions markerOptions = new MarkerOptions();
                            try {
                                url = new URL(user.picture);
                                final Bitmap bmp  = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                                final Bitmap smallMarker = Bitmap.createScaledBitmap(bmp,110,115,false);
                                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {


                                    markerOptions.position(ll);
                                    markerOptions.snippet("Name: " + user.name + "\n" + "Last name: " + user.lastname);

                                    markerOptions.title(user.username);

                                    Marker marker = mMap.addMarker(markerOptions);
                                    marker.setTag(user);
                                    if (!markerMyFriends.containsValue(s))
                                        markerMyFriends.put(marker, s);

                                }
                            });
                        }
                    });
                    thread.start();





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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(broadcastReceiver != null)
            unregisterReceiver(broadcastReceiver);

    }

    @Override
    protected void onResume()
    {
       super.onResume();
       if(broadcastReceiver == null)
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    //Toast.makeText(getApplicationContext(),intent.getExtras().get("coordinates").toString(),Toast.LENGTH_SHORT).show();
                    Double lat=intent.getDoubleExtra("latitude",0);
                    Double lon=intent.getDoubleExtra("longitude",0);
                    if(lon!=0 && lat!=0)
                    {
                        placeLoc=new LatLng(lat,lon);
                        MyMarker.remove();
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(placeLoc);
                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.mymarker));
                        markerOptions.title(username);

                        MyMarker = mMap.addMarker(markerOptions);

                        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placeLoc, 13));
                    }
                }
            };
        registerReceiver(broadcastReceiver,new IntentFilter("location_update"));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.invited_challenges) {
            findViewById(R.id.fab).setVisibility(View.VISIBLE);
            Toast.makeText(MapsActivity.this,"Show invited Challenges", Toast.LENGTH_SHORT).show();
            myChallenges=false;
            RemoveAllMarkers(markerMyChallenges);
            myChallengesOnMap.clear();
            if(QueryMyChallenges!=null)
            QueryMyChallenges.removeEventListener(MyChallengesListener);
            ShowInvitedChallenges();
        }

        if (id == R.id.my_challenges) {
            findViewById(R.id.fab).setVisibility(View.INVISIBLE);
            Toast.makeText(MapsActivity.this,"Show my Challenges", Toast.LENGTH_SHORT).show();
            myChallenges=true;
            if(QueryInvitedChallenges!=null)
            QueryInvitedChallenges.removeEventListener(InvitedChallengesListener);
            RemoveAllMarkers(markerChallenges);
            invitedChallengesOnMap.clear();
            ShowMyChallenges();
        }
        if (id == R.id.nearest) {

            if(myChallenges)
                ShowNearest(markerMyChallenges);

            else
                ShowNearest(markerChallenges);
            if(showFriends)
            {
                ShowNearest(markerMyFriends);
            }
            
        }
        if (id == R.id.all) {

            if(myChallenges)
                ShowAll(markerMyChallenges);
            else
                ShowAll(markerChallenges);
            if(showFriends)
                ShowAll(markerMyFriends);

        }
        return super.onOptionsItemSelected(item);
    }

    private void ShowAll(HashMap<Marker, String> map)
    {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placeLoc, 13));
        for (Map.Entry<Marker, String> entry : map.entrySet()) {
            Marker m = entry.getKey();
            if(m.isVisible() == false)
                m.setVisible(true);

        }
    }

    private void ShowNearest(HashMap<Marker, String> map) 
    {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placeLoc, 15));

        for (Map.Entry<Marker, String> entry : map.entrySet()) {
            Marker m = entry.getKey();
            

            float[] results = new float[1];
            Location.distanceBetween(placeLoc.latitude,placeLoc.longitude,m.getPosition().latitude,m.getPosition().longitude,results);
            //Toast.makeText(MapsActivity.this,"Distance for: " + m.getTitle() + " " + String.valueOf(results[0]),Toast.LENGTH_LONG).show();
            if(results[0] > 1000)
                m.setVisible(false);
            
        }
        
    }

    private void RemoveAllMarkers(HashMap<Marker, String> map) {
        for (Map.Entry<Marker, String> entry : map.entrySet()) {
            Marker m= entry.getKey();
            m.remove();

        }
        map.clear();
    }

    private void ShowMyChallenges() {

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference dr = db.getReference("Challenge");
        QueryMyChallenges = dr.orderByChild("userID").equalTo(myID);
        MyChallengesListener = QueryMyChallenges.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                Challenge challenge  = dataSnapshot.getValue(Challenge.class);
                String key=dataSnapshot.getKey();
                if(ValidateDate(challenge.endDate))
                   RemoveExpiredChallenge(key);

                else {
                    if (!myChallengesOnMap.contains(challenge))
                        myChallengesOnMap.add(challenge);

                    LatLng ll = new LatLng(Double.parseDouble(challenge.latitude), Double.parseDouble(challenge.longitude));


                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(ll);
                    markerOptions.snippet("Created by: " + challenge.username + "\n" + "You can gain: " + challenge.points + "pts\n" + "Challenge expires on: " + challenge.endDate);

                    String t = challenge.type;

                    int resId = MapsActivity.this.getResources().getIdentifier(
                            t,
                            "drawable",
                            MapsActivity.this.getPackageName()
                    );

                    markerOptions.icon(BitmapDescriptorFactory.fromResource(resId));
                    markerOptions.title(challenge.type);
                    Marker marker = mMap.addMarker(markerOptions);
                    marker.setTag(challenge);
                    if (!markerMyChallenges.containsValue(key))
                        markerMyChallenges.put(marker, key);
                }




            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                //kad se promeni invite - to nikad nece da se desava

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            //desava se


            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

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

            mMap.setMyLocationEnabled(true);


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
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placeLoc, 13));
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(placeLoc);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.mymarker));
            markerOptions.title(username);

            MyMarker = mMap.addMarker(markerOptions);


        }


        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                LinearLayout info = new LinearLayout(MapsActivity.this);
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(MapsActivity.this);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(MapsActivity.this);
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
            }
        });


        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                String i = markerChallenges.get(marker);
                //Toast.makeText(MapsActivity.this,"CH ID "+i,Toast.LENGTH_SHORT).show();
                marker.showInfoWindow();
                return true;
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
            if(marker.equals(MyMarker))
                return;
            if(!myChallenges) {
                LatLng latLon = marker.getPosition();

                if(markerMyFriends.containsKey(marker))
                {

                    String userID = markerMyFriends.get(marker);
                    Intent i = new Intent(MapsActivity.this,FriendProfileActivity.class);
                    i.putExtra("friendID",userID);
                    startActivity(i);
                }

                else
                {
                    String id = markerChallenges.get(marker);

                    Intent i = new Intent(MapsActivity.this, ChallengeProfileActivity.class);
                    i.putExtra("challengeId", id);
                    startActivity(i);
                }






            }




            }
        });







    }
    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case PERMISSION_ACCESS_FINE_LOCATION:
            {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // mMap.setMyLocationEnabled(true);

                        mMap.setMyLocationEnabled(true);

                }
                return;
            }
        }
        // super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void ShowInvitedChallenges()
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
                Toast.makeText(MapsActivity.this,"Obrisan iz baze "+ m,Toast.LENGTH_SHORT).show();


                Marker marker=getKeyByValue(markerChallenges,m);
                Challenge c= (Challenge) marker.getTag();

                invitedChallengesOnMap.remove(c);
                marker.remove();
                markerChallenges.values().removeAll(Collections.singleton(m));

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
                        RemoveExpiredChallenge(m);
                    else
                        if ((type.compareTo("all")==0 || type.compareTo(challenge.type)==0) &&  CheckDates(challenge.endDate))
                        {
                        if (!invitedChallengesOnMap.contains(challenge))
                            invitedChallengesOnMap.add(challenge);

                        LatLng ll = new LatLng(Double.parseDouble(challenge.latitude), Double.parseDouble(challenge.longitude));

                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(ll);
                        markerOptions.snippet("Created by: " + challenge.username + "\n" + "You can gain: " + challenge.points + "pts\n" + "Challenge expires on: " + challenge.endDate);

                        String t = challenge.type;

                        int resId = MapsActivity.this.getResources().getIdentifier(
                                t,
                                "drawable",
                                MapsActivity.this.getPackageName()
                        );

                        markerOptions.icon(BitmapDescriptorFactory.fromResource(resId));
                        markerOptions.title(challenge.type);
                        Marker marker = mMap.addMarker(markerOptions);
                        marker.setTag(challenge);
                        if (!markerChallenges.containsValue(m))
                            markerChallenges.put(marker, m);
                    }

                   query.removeEventListener(this);

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    //valjda se nikad ne poziva!
                    Challenge challenge  = dataSnapshot.getValue(Challenge.class);
                    if (!invitedChallengesOnMap.contains(challenge))
                        invitedChallengesOnMap.add(challenge);

                    LatLng ll = new LatLng(Double.parseDouble(challenge.latitude),Double.parseDouble(challenge.longitude));


                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(ll);
                    String t = challenge.type;

                    int resId = MapsActivity.this.getResources().getIdentifier(
                            t,
                            "drawable",
                            MapsActivity.this.getPackageName()
                    );

                    markerOptions.icon(BitmapDescriptorFactory.fromResource(resId));
                    markerOptions.title(challenge.type);

                    Marker marker = mMap.addMarker(markerOptions);
                    if(!markerChallenges.containsValue(m))
                        markerChallenges.put(marker, m);

                //    Toast.makeText(MapsActivity.this,challenge.endDate,Toast.LENGTH_LONG).show();
                    query.removeEventListener(this);

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
//valjda se nikad ne poziva!
                    Challenge c  = dataSnapshot.getValue(Challenge.class);
                    if (invitedChallengesOnMap.contains(c))
                        invitedChallengesOnMap.remove(c);
                   // Toast.makeText(MapsActivity.this,c.endDate,Toast.LENGTH_LONG).show();
                    query.removeEventListener(this);

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    //valjda se nikad ne poziva!
                    query.removeEventListener(this);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    //valjda se nikad ne poziva!
                    query.removeEventListener(this);

                }
            });





    }

    private boolean CheckDates(String date)
    {
        Date today = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        String todayString = df.format(today);
        Date d1 = null;
        Date d2 = null;

        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();

        try {
            d1 = df.parse(date);
            d2 = df.parse(todayString);

            cal1.setTime(d1);
            cal2.setTime(d2);

        } catch (ParseException e) {
            e.printStackTrace();
        }


        switch (expires)
        {
            case "today":
                if (d1.compareTo(d2) == 0)
                    return true;
                else
                    return false;
            case "this week":
                if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR))
                    return true;
                else
                    return false;
            case "this month":
                if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH))
                    return true;
                else
                    return false;
            case "all":
                return  true;
        }

        return true;
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
            Toast.makeText(MapsActivity.this,"challenge expired"+ endDate,Toast.LENGTH_SHORT).show();

        }
        return d1.after(d2);
    }

    private void RemoveExpiredChallenge(String ID)
    {
        Toast.makeText(MapsActivity.this,"Delete"+ ID,Toast.LENGTH_SHORT).show();
        DatabaseReference database=FirebaseDatabase.getInstance().getReference();
        database.child("Challenge").child(ID).setValue(null);
        database.child("Invites").child(ID).setValue(null);
        database.child("FinishedBy").child(ID).setValue(null);
        database.child("Geofire").child(ID).setValue(null);
}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_maps,menu);
        return true;
    }

    public Marker  getKeyByValue( Map<Marker, String> map, String value) {
        for (Map.Entry<Marker, String> entry : map.entrySet()) {
            if ( value.compareTo(entry.getValue())==0) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void showChallenge(String challengeID) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        final DatabaseReference dr = db.getReference().child("Challenge");//.child(userID);
        final Query query = dr.orderByKey().equalTo(challengeID).limitToFirst(1);

        final ChildEventListener cel = query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                createdChallenge = dataSnapshot.getValue(Challenge.class);

                LatLng ll = new LatLng(Double.parseDouble(createdChallenge.latitude), Double.parseDouble(createdChallenge.longitude));
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(ll);
                String t = createdChallenge.type;

                int resId = MapsActivity.this.getResources().getIdentifier(
                        t,
                        "drawable",
                        MapsActivity.this.getPackageName()
                );

                markerOptions.icon(BitmapDescriptorFactory.fromResource(resId));
                markerOptions.title(createdChallenge.type);

                Marker marker = mMap.addMarker(markerOptions);

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


}
