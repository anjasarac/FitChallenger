package com.example.fitchallenger.fitchallenger;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.Query;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.json.JSONObject;

public class FriendRequestActivity extends AppCompatActivity {


    String myUsername;
    public User user = new User();
    String userID;
    BluetoothAdapter mBluetoothAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request);


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Intent friendRequestIntent = getIntent();
        Bundle friendRequestBundle = friendRequestIntent.getExtras();

        userID = friendRequestBundle.getString("userID");


        SharedPreferences sharedPref = getSharedPreferences("CurrentUser", Context.MODE_PRIVATE);
        myUsername=sharedPref.getString("username","");

        //TextView tw = (TextView) findViewById(R.id.friendRequestContent);
        //tw.setText(userID);
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        final DatabaseReference dr = db.getReference().child("User");//.child(userID);
        final Query query = dr.orderByKey().equalTo(userID).limitToFirst(1);

        final ChildEventListener cel = query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                user = dataSnapshot.getValue(User.class);

                ImageView iw = (ImageView) findViewById(R.id.friendRequestImage);
                if (user.picture.compareTo("") != 0) {
                    Picasso.with(FriendRequestActivity.this)
                            .load(user.picture)
                            .into(iw);
                }

                String tekst = "User " + user.username + " wants to become your friend. :)";
                TextView tw = (TextView) findViewById(R.id.friendRequestContent);
                tw.setText(tekst);


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

        findViewById(R.id.acceptRequest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                HashMap<String,Boolean> friend=new HashMap<>() ;
                friend.put(userID, true);
                JSONObject json=new JSONObject(friend);

                FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
                final String myID=me.getUid();

                FirebaseDatabase.getInstance().getReference().child("Friends").child(myID)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Map<String, String> stringStringHashMap =(Map<String, String>) dataSnapshot.getValue();
                                if(stringStringHashMap==null)
                                {
                                    stringStringHashMap=new HashMap<>();
                                }
                                stringStringHashMap.put(userID,user.username);


                                FirebaseDatabase.getInstance().getReference().child("Friends").child(myID)
                                        .setValue(stringStringHashMap);

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                FirebaseDatabase.getInstance().getReference().child("Friends").child(userID)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Map<String, String> stringStringHashMap =(Map<String, String>) dataSnapshot.getValue();
                                if(stringStringHashMap==null)
                                {
                                    stringStringHashMap=new HashMap<>();
                                }
                                stringStringHashMap.put(myID,myUsername);

                                FirebaseDatabase.getInstance().getReference().child("Friends").child(userID)
                                        .setValue(stringStringHashMap);

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                final long timeInterval = 3000;
                Runnable runnable = new Runnable() {
                    public void run() {
                        while (true) {
                            // ------- code for task to run

                            // ------- ends here
                            try {
                                Thread.sleep(timeInterval);
                                unpair(user.username);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
                Thread thread = new Thread(runnable);
                thread.start();
           

                setResult(Activity.RESULT_OK);
                finish();
            }
        });


        findViewById(R.id.denyRequest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final long timeInterval = 3000;
                Runnable runnable = new Runnable() {
                    public void run() {
                        while (true) {
                            // ------- code for task to run

                            // ------- ends here
                            try {
                                Thread.sleep(timeInterval);
                                unpair(user.username);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
                Thread thread = new Thread(runnable);
                thread.start();
                setResult(Activity.RESULT_FIRST_USER);
                finish();
            }
        });

    }



    private void unpair(String username)
    {
         Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    try {

                        if (device.getName().compareTo(username) == 0 )
                        {
                            Method m = device.getClass()
                                    .getMethod("removeBond", (Class[]) null);
                            m.invoke(device, (Object[]) null);
                        }

                    } catch (Exception e) {
                        //Log.e("fail", e.getMessage());
                    }
                }
            }
    }


}
