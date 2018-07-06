package com.example.fitchallenger.fitchallenger;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class MyService extends Service
{

    String myID;
    FirebaseAuth mAuth;
    private LocationListener listener;
    private LocationManager locationManager;

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate()
    {
        mAuth = FirebaseAuth.getInstance();
        myID= mAuth.getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Geofire");
        final GeoFire geoFire = new GeoFire(ref);

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Intent i = new Intent("location_update");
                i.putExtra("latitude",location.getLatitude());
                i.putExtra("longitude",location.getLongitude());
                sendBroadcast(i);
                Log.i("listen",String.valueOf(location.getLatitude()));

                FirebaseDatabase db = FirebaseDatabase.getInstance();
                DatabaseReference dr = db.getReference("User").child(myID);
                dr.child("latitude").setValue(String.valueOf(location.getLatitude()));
                dr.child("longitude").setValue(String.valueOf(location.getLongitude()));

                GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), 0.5);
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

                                if (users!=null && users.containsKey(myID))
                                    SendNotification(challengeID);

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
                Log.i("status","dddddd");
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
        locationManager = (LocationManager)getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,0,listener);

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
                        .setContentText("There's a challenge nearby. Click here to open map")
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

        mNotificationManager.notify(001, mBuilder.build());


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