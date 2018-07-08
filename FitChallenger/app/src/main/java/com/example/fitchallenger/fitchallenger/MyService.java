package com.example.fitchallenger.fitchallenger;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyService extends Service {

    String myID;
    FirebaseAuth mAuth;
    private LocationListener listener;
    private LocationManager locationManager;
    GeoFire geoFire;
    static final int PERMISSION_ACCESS_FINE_LOCATION = 1;
    private GeoQuery geoQuery;
    private Map<String,Boolean> notificationList;

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        mAuth = FirebaseAuth.getInstance();
        myID = mAuth.getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Geofire");
        geoFire = new GeoFire(ref);
        notificationList=new HashMap<>();

        InitListener();

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Intent i = new Intent("location_update");
                i.putExtra("latitude", location.getLatitude());
                i.putExtra("longitude", location.getLongitude());
                sendBroadcast(i);
                Log.i("listen", String.valueOf(location.getLatitude()));

                FirebaseDatabase db = FirebaseDatabase.getInstance();
                DatabaseReference dr = db.getReference("User").child(myID);
                dr.child("latitude").setValue(String.valueOf(location.getLatitude()));
                dr.child("longitude").setValue(String.valueOf(location.getLongitude()));

                geoQuery.removeAllListeners();
                checkMap(notificationList);



                geoQuery = geoFire.queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), 0.5);
                geoQuery.addGeoQueryDataEventListener(new GeoQueryDataEventListener() {
                    @Override
                    public void onDataEntered(DataSnapshot dataSnapshot, GeoLocation location) {
                        Object o = dataSnapshot.getValue();


                        final String challengeID = dataSnapshot.getKey();

                        FirebaseDatabase db = FirebaseDatabase.getInstance();
                        DatabaseReference dr = db.getReference("Invites").child(challengeID);
                        dr.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                HashMap<String, String> users = (HashMap<String, String>) dataSnapshot.getValue();

                                if (users != null && users.containsKey(myID)) {
                                    if(notificationList.containsKey(challengeID)) {
                                        notificationList.remove(challengeID);
                                        notificationList.put(challengeID,true);
                                    }
                                    else
                                    {
                                        notificationList.put(challengeID,true);
                                        SendNotification(challengeID);
                                    }
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                    }

                    @Override
                    public void onDataExited(DataSnapshot dataSnapshot) {


                    }

                    @Override
                    public void onDataMoved(DataSnapshot dataSnapshot, GeoLocation location) {

                    }

                    @Override
                    public void onDataChanged(DataSnapshot dataSnapshot, GeoLocation location) {

                    }

                    @Override
                    public void onGeoQueryReady() {

                    }

                    @Override
                    public void onGeoQueryError(DatabaseError error) {

                    }
                });

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.i("status", "dddddd");
            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        };
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, listener);

    }

    private void checkMap(Map<String, Boolean> notificationList) {
        for (Map.Entry<String, Boolean> entry : notificationList.entrySet()) {
            if ( entry.getValue()==true) {
               entry.setValue(false);
            }
            else
                notificationList.remove(entry.getKey());

        }
    }

    private void InitListener() {

        LocationManager mLocationManager;
        Location location;


        mLocationManager =(LocationManager)getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {


            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            @SuppressLint("MissingPermission") Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }


        location = bestLocation;

        if (location != null) {
            FirebaseDatabase db = FirebaseDatabase.getInstance();
            DatabaseReference dr = db.getReference("User").child(myID);
            dr.child("latitude").setValue(String.valueOf(location.getLatitude()));
            dr.child("longitude").setValue(String.valueOf(location.getLongitude()));

            geoQuery = geoFire.queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), 0.5);
            geoQuery.addGeoQueryDataEventListener(new GeoQueryDataEventListener() {
                @Override
                public void onDataEntered(DataSnapshot dataSnapshot, GeoLocation location) {
                    Object o = dataSnapshot.getValue();


                    final String challengeID = dataSnapshot.getKey();

                    FirebaseDatabase db = FirebaseDatabase.getInstance();
                    DatabaseReference dr = db.getReference("Invites").child(challengeID);
                    dr.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            HashMap<String,String> users = (HashMap<String, String>) dataSnapshot.getValue();

                            if (users!=null && users.containsKey(myID)) {

                                SendNotification(challengeID);

                                notificationList.put(challengeID,true);

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });



                }

                @Override
                public void onDataExited(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onDataMoved(DataSnapshot dataSnapshot, GeoLocation location) {

                }

                @Override
                public void onDataChanged(DataSnapshot dataSnapshot, GeoLocation location) {

                }

                @Override
                public void onGeoQueryReady() {

                }

                @Override
                public void onGeoQueryError(DatabaseError error) {

                }
            });


        }


    }

    private void SendNotification(String challengeID)
    {
        //Get an instance of NotificationManager//

        Intent intent = new Intent(MyService.this, MapsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);


        Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder =//SettingsActivity.this
                new NotificationCompat.Builder(getApplicationContext(), "CHANNEL_ID")
                        .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                        .setContentTitle("Challenge nearby")
                        .setSound(uri)
                        .setSubText(challengeID)
                        .setContentText("There's a challenge nearby. Click here to open map ")
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);


        // Gets an instance of the NotificationManager service//

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // When you issue multiple notifications about the same type of event,
        // it’s best practice for your app to try to update an existing notification
        // with this new information, rather than immediately creating a new notification.
        // If you want to update this notification at a later date, you need to assign it an ID.
        // You can then use this ID whenever you issue a subsequent notification.
        // If the previous notification is still visible, the system will update this existing notification,
        // rather than create a new one. In this example, the notification’s ID is 001//

        int m = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
        mNotificationManager.notify(m, mBuilder.build());


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null)
        {
            locationManager.removeUpdates(listener);
        }
        Log.i("destroy","dddddd");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}