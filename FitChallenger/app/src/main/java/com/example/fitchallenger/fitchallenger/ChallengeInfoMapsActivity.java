package com.example.fitchallenger.fitchallenger;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
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

public class ChallengeInfoMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String challengeId;
    private LatLng placeLoc;
    private Marker MyMarker;
    private FirebaseAuth mAuth;
    private String myID;
    private String username;
    static final int PERMISSION_ACCESS_FINE_LOCATION = 1;
    BroadcastReceiver broadcastReceiver;
    List<LatLng> path;
    Long points;
    boolean service;

    Intent i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_info_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        path = new ArrayList<>();

        SharedPreferences sharedPref = getSharedPreferences("CurrentUser", Context.MODE_PRIVATE);
        username = sharedPref.getString("username", "");
        mAuth = FirebaseAuth.getInstance();
        myID = mAuth.getCurrentUser().getUid();

        Intent challengeIntent = getIntent();
        Bundle challengeBundle = challengeIntent.getExtras();

        challengeId = challengeBundle.getString("challengeId");
        points=challengeBundle.getLong("challengePoints");

        i = new Intent(getApplicationContext(), MyService.class);
        if (!isMyServiceRunning(MyService.class)) {
            startService(i);
            service=false;
        }else
        {
            service=true;
        }


        findViewById(R.id.finish_dynamic_challenge).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                float[] results = new float[1];

                LatLng challengeFinish = path.get(path.size()-1);

                Location.distanceBetween(placeLoc.latitude,placeLoc.longitude,challengeFinish.latitude,challengeFinish.longitude,results);
                //Toast.makeText(MapsActivity.this,"Distance for: " + m.getTitle() + " " + String.valueOf(results[0]),Toast.LENGTH_LONG).show();
                if(results[0] > 100)
                {
                    Toast.makeText(ChallengeInfoMapsActivity.this,"In order to finish the challenge you need to cross the finish line.",Toast.LENGTH_SHORT).show();
                    return;
                }


                if (!service)
                    stopService(i);

                //Toast.makeText(ChallengeInfoMapsActivity.this,"KLIK",Toast.LENGTH_SHORT).show();
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
                //Toast.makeText(ChallengeInfoMapsActivity.this,"poeni",Toast.LENGTH_SHORT).show();
                db.getReference("User").child(myID).child("points").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        long p=((Long) dataSnapshot.getValue()) ;
                        SharedPreferences sharedPref = getSharedPreferences("CurrentUser", Context.MODE_PRIVATE);
                        p+=points;
                        sharedPref = getSharedPreferences("CurrentUser", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor=sharedPref.edit();
                        editor.putLong("points",p);
                        editor.commit();
                        db.getReference("User").child(myID).child("points").setValue(p);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                //Toast.makeText(ChallengeInfoMapsActivity.this,"mapa",Toast.LENGTH_SHORT).show();
                Intent i = new Intent(ChallengeInfoMapsActivity.this,MapsActivity.class);
                startActivity(i);
            }
        });




    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        LocationManager mLocationManager;
        Location myLocation;


        mLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

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
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placeLoc, 17));
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(placeLoc);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.mymarker));
            markerOptions.title(username);
            //path.add(placeLoc);
            MyMarker = mMap.addMarker(markerOptions);


        }


        ShowChallengeRoute();


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

    private void ShowChallengeRoute()
    {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference dr = db.getReference("Dynamic").child(challengeId);
        dr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               List<Object> hm = (List<Object>) dataSnapshot.getValue();
                //path=new ArrayList<>(hm.values());
                LatLng l;
                HashMap<Integer,Object> coordinates;
                Collection<Object> c;
                for (int i=0;i<hm.size();i++)
                {
                    coordinates= (HashMap<Integer, Object>) hm.get(i);
                    c=coordinates.values();
                    ArrayList<Object> d=new ArrayList<>(c);
                    l=new LatLng((Double) d.get(0),(Double) d.get(1));
                   path.add(l);

                }
                PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.MAGENTA).width(10);
                mMap.addPolyline(opts);

                MarkerOptions start = new MarkerOptions();
                start.position(path.get(0));
                start.icon(BitmapDescriptorFactory.fromResource(R.mipmap.start));
                MarkerOptions finish = new MarkerOptions();
                finish.position(path.get(path.size()-1));
                finish.icon(BitmapDescriptorFactory.fromResource(R.mipmap.finish));

                mMap.addMarker(start);
                mMap.addMarker(finish);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
