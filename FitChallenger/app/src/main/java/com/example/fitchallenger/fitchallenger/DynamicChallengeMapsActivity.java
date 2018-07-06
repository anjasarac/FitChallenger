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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class DynamicChallengeMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String challengeId;
    private LatLng placeLoc;
    private Marker MyMarker;
    String username;
    static final int PERMISSION_ACCESS_FINE_LOCATION = 1;
    List<LatLng> path;
    List<Coordinates> coordinates;
    private BroadcastReceiver broadcastReceiver;
    boolean service;

    Intent i;
    private Polyline polyline;

    @Override
    protected void onResume() {
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
                        path.add(placeLoc);
                        coordinates.add(new Coordinates(placeLoc.latitude,placeLoc.longitude));
                        PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.MAGENTA).width(10);
                        if(polyline!=null)
                        polyline.remove();
                        polyline =mMap.addPolyline(opts);
                        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placeLoc, 13));
                    }
                }
            };
        registerReceiver(broadcastReceiver,new IntentFilter("location_update"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_challenge_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        path = new ArrayList();
        coordinates=new ArrayList<>();


        i = new Intent(getApplicationContext(), MyService.class);
        if (!isMyServiceRunning(MyService.class)) {
            startService(i);
            service=false;
        }else
        {
            service=true;
        }
        SharedPreferences sharedPref = getSharedPreferences("CurrentUser", Context.MODE_PRIVATE);
        username = sharedPref.getString("username", "");

        Intent challengeIntent = getIntent();
        Bundle challengeBundle = challengeIntent.getExtras();

        challengeId = challengeBundle.getString("challengeID");

        findViewById(R.id.finishDynamic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase db= FirebaseDatabase.getInstance();
                db.getReference("Dynamic").child(challengeId).setValue(coordinates);
                Intent i = new Intent(DynamicChallengeMapsActivity.this,InviteFriendsActivity.class);
                i.putExtra("challengeID",challengeId);
                startActivity(i);

                 if(!service)
                     stopService(i);
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
            path.add(placeLoc);
            MyMarker = mMap.addMarker(markerOptions);
            MarkerOptions markerstart = new MarkerOptions();
            markerstart.position(placeLoc);
            markerstart.icon(BitmapDescriptorFactory.fromResource(R.mipmap.start));
            mMap.addMarker(markerstart);

        }


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
