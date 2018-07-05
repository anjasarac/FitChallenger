package com.example.fitchallenger.fitchallenger;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothManager;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.bluetooth.BluetoothDevice;
import java.util.Set;
import android.app.NotificationManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import java.util.ArrayList;
import android.net.Uri;
import android.media.RingtoneManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.Query;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.squareup.picasso.Picasso;
import android.support.v4.app.*;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.view.ContextMenu;
import android.widget.AdapterView;
import  android.view.MenuItem;
import java.lang.reflect.Method;

public class SettingsActivity extends AppCompatActivity {

BluetoothAdapter mBluetoothAdapter;
ArrayList<String> usersString;
ArrayList<User> users;

Map<String,String> friends;
Collection<String> friendsUsername;

AcceptThread serverThread;
ConnectThread clientThread;
ArrayList<BluetoothDevice> devices;
BluetoothDevice bluetoothDevice;
String userRequest;
String myID;
FirebaseAuth mAuth;
Intent i;
int REQUEST_ENABLE_BT=1;
int REQUEST_CHANGE_BT=2;


public android.os.Handler mHandler = new android.os.Handler(){





        @Override
        public void handleMessage(android.os.Message msg) {
            //Looper.prepare();
            Log.i("tag", "server hendler");
            switch (msg.what)
            {
                case ConnectThread.MessageConstants.MESSAGE_READ:
                    byte[] b = (byte[]) msg.obj;
                    String s = new String(b,0,msg.arg1);
                    Log.i("Podaci what : ",s);

                    ShowMessage(s);


                    break;
            }
           // super.handleMessage(msg);

        }

    };

    public  android.os.Handler cHandler=new android.os.Handler(){
        @Override
        public void handleMessage(android.os.Message msg) {
            Log.i("tag", "klijent hendler");
            byte[] b;
            String s;
            boolean vOut;
            switch (msg.what)
            {
                case ConnectThread.MessageConstants.MESSAGE_WRITE:

                  b = (byte[]) msg.obj;


                      s = new String(b,0,msg.arg1);
                      if(s.compareTo("0000000000000000000000000000")!=0) {
                          ShowUserNotification(s);
                      }
                      Toast.makeText(SettingsActivity.this, "You are  " + s + ".", Toast.LENGTH_LONG).show();




                  break;

            }
            super.handleMessage(msg);

        }
    };
    private ValueEventListener friendsListener;
    private DatabaseReference friendRefrence;
    private String username;

    private void ShowUserNotification(String userID)
    {

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        final DatabaseReference dr = db.getReference().child("User");//.child(userID);
        final Query query = dr.orderByKey().equalTo(userID).limitToFirst(1);

        final ChildEventListener cel = query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                User user = dataSnapshot.getValue(User.class);

                Toast.makeText(SettingsActivity.this, "You are now friends with " + user.username + ".",Toast.LENGTH_LONG).show();

                //Get an instance of NotificationManager//

                Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                NotificationCompat.Builder mBuilder =//SettingsActivity.this
                        new NotificationCompat.Builder(getApplicationContext(), "CHANNEL_ID")
                                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                                .setContentTitle(user.username)
                                .setSound(uri)
                                .setContentText(user.name + " has accepted your friend request.");


                // Gets an instance of the NotificationManager service//

                NotificationManager mNotificationManager =

                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                // When you issue multiple notifications about the same type of event,
                // it’s best practice for your app to try to update an existing notification
                // with this new information, rather than immediately creating a new notification.
                // If you want to update this notification at a later date, you need to assign it an ID.
                // You can then use this ID whenever you issue a subsequent notification.
                // If the previous notification is still visible, the system will update this existing notification,
                // rather than create a new one. In this example, the notification’s ID is 001//

                mNotificationManager.notify(001, mBuilder.build());






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


    private final BroadcastReceiver mReceiver;

    {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                Switch s;
                if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                            BluetoothAdapter.ERROR);
                    switch (state) {
                        case BluetoothAdapter.STATE_OFF:
                            s = (Switch) findViewById(R.id.switch_bluetooth);
                            s.setChecked(false);
                            if (clientThread != null)
                                clientThread.cancel();
                            if (serverThread != null)
                                serverThread.cancel();
                           // Toast.makeText(SettingsActivity.this, "Bluetooth OFF", Toast.LENGTH_LONG).show();
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            //Toast.makeText(SettingsActivity.this, "Bluetooth off...", Toast.LENGTH_LONG).show();
                            break;
                        case BluetoothAdapter.STATE_ON:
                            mBluetoothAdapter.setName(username);
                            s = (Switch) findViewById(R.id.switch_bluetooth);
                            s.setChecked(true);
                            serverThread = new AcceptThread(mHandler,myID);
                            serverThread.mSettingsActivity = SettingsActivity.this;


                            serverThread.start();
                            //Toast.makeText(SettingsActivity.this, "Server started", Toast.LENGTH_LONG).show();

                            //Thread myThreadBack = new Thread(backgroundTask, "backAlias1" ); myThreadBack.start();
                           // Toast.makeText(SettingsActivity.this, "Bluetooth ON", Toast.LENGTH_LONG).show();
                            break;
                        case BluetoothAdapter.STATE_TURNING_ON:
                            //Toast.makeText(SettingsActivity.this, "Bluetooth on...", Toast.LENGTH_LONG).show();
                            break;

                    }
                }
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address

                    showDevices(deviceName, device);

                    //Toast.makeText(SettingsActivity.this, "Uredjaj1: " + deviceName + "," + deviceHardwareAddress, Toast.LENGTH_LONG).show();
                }


            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        myID= mAuth.getCurrentUser().getUid();
        friends=new HashMap<>();
        friendsUsername=new ArrayList<>();
        getAllFriends();


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.cancelDiscovery();

        final SharedPreferences sharedPref = getSharedPreferences("CurrentUser", Context.MODE_PRIVATE);
        username=sharedPref.getString("username","");
        boolean friends=  sharedPref.getBoolean("showFriends", false);
        boolean challenges=  sharedPref.getBoolean("showChallenges", true);
        //BluetoothDevice bd = BluetoothAdapter.get


        Switch sw = findViewById(R.id.switchService);
        if(isMyServiceRunning(MyService.class))
            sw.setChecked(true);
        else
            sw.setChecked(false);



        if (mBluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(SettingsActivity.this, "Device doesn't support Bluetooth", Toast.LENGTH_LONG).show();
        }

        if(mBluetoothAdapter.isEnabled())
        {
            Switch s = (Switch) findViewById(R.id.switch_bluetooth);
            s.setChecked(true);
        }



        users = new ArrayList<User>();
        devices= new ArrayList<BluetoothDevice>();


    Switch s1= findViewById(R.id.switch_friends);
        s1.setChecked(friends);
        Switch s2= findViewById(R.id.switch_challenges);
        s2.setChecked(challenges);



        findViewById(R.id.switch_friends).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Switch s = (Switch) findViewById(R.id.switch_friends);
                SharedPreferences.Editor editor = sharedPref.edit();

                if (s.isChecked() == true)
                    editor.putBoolean("showFriends",true);
                else
                    editor.putBoolean("showFriends",false);

                editor.commit();

            }
        });

        findViewById(R.id.switch_challenges).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Switch s = (Switch) findViewById(R.id.switch_challenges);
                SharedPreferences.Editor editor = sharedPref.edit();

                if (s.isChecked() == true)
                    editor.putBoolean("showChallenges",true);
                else
                    editor.putBoolean("showChallenges",false);

                editor.commit();

            }
        });



        findViewById(R.id.backToSettings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.friendrequest_container).setVisibility(View.INVISIBLE);
                findViewById(R.id.settings_container).setVisibility(View.VISIBLE);

                ListView usersList = (ListView) findViewById(R.id.listViewDevices);
                usersList.setAdapter(null);
                users = new ArrayList<User>();
                mBluetoothAdapter.cancelDiscovery();



                if (clientThread != null)
                    clientThread.cancel();

                    serverThread = new AcceptThread(mHandler,myID);
                    serverThread.mSettingsActivity = SettingsActivity.this;
                    serverThread.start();

                friendRefrence.addValueEventListener(friendsListener);


            }
        });

        i = new Intent(getApplicationContext(),MyService.class);

        findViewById(R.id.switchService).setOnClickListener(new View.OnClickListener() {

            Switch s = (Switch) findViewById(R.id.switchService);
            @Override
            public void onClick(View v) {
                if(s.isChecked() == true)
                {
                    Intent firebase = new Intent(SettingsActivity.this,FirebaseVisibleActivity.class);
                    startActivity(firebase);
                    startService(i);



                }
                else
                {
                    FirebaseDatabase db = FirebaseDatabase.getInstance();
                    DatabaseReference dr = db.getReference("User").child(myID);
                    dr.child("visible").setValue(false);
                    stopService(i);
                }

            }
        });


        findViewById(R.id.switch_bluetooth).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Switch s = (Switch) findViewById(R.id.switch_bluetooth);
                if (s.isChecked() == true) {
                   // Toast.makeText(SettingsActivity.this, "Bluetooth on...", Toast.LENGTH_LONG).show();
                    //visible
                    if (!mBluetoothAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivity(enableBtIntent);

                        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                        startActivity(discoverableIntent);


                    }
                    //server

                } else
                {
                    Toast.makeText(SettingsActivity.this, "Bluetooth off", Toast.LENGTH_LONG).show();
                    mBluetoothAdapter.disable();
                    if (serverThread != null)
                        serverThread.cancel();
                    //iskljuci visible
                    //iskljuci server
                }

                    }
                });


        ListView usersList = (ListView) findViewById(R.id.listViewDevices);
        usersList.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
                User user = users.get(info.position);

                menu.setHeaderTitle(user.username);
                menu.add(0,1,1,"Send Friend Request");
               // menu.add(0,2,2,"Go to profile");

            }
        });


            findViewById(R.id.searchFriends).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setTitle("Found users:");
                    if (serverThread != null) {
                        serverThread.cancel();
                        Toast.makeText(SettingsActivity.this, "Server off", Toast.LENGTH_SHORT).show();
                    }

                    findViewById(R.id.friendrequest_container).setVisibility(View.VISIBLE);
                    findViewById(R.id.settings_container).setVisibility(View.INVISIBLE);
                    users = new ArrayList<User>();

                    //showDevices(deviceName,device);

                    if (!mBluetoothAdapter.isDiscovering()) {
                        mBluetoothAdapter.startDiscovery();
                    }

                }

            });



        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);

        IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter1);

        }


    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        Bundle positionBundle = new Bundle();
        positionBundle.putInt("position", info.position);

        Intent i;

        if(item.getItemId() == 1)
        {
            //i = new Intent(this, ViewMyPlaceActivity.class);
            //i.putExtras(positionBundle);
            //startActivity(i);

            //bluetoothDevice = devices.get(info.position);
            //Gson gson = new Gson();
            //String str = gson.toJson(bluetoothDevice);

            Toast.makeText(SettingsActivity.this,devices.get(info.position).getName(),Toast.LENGTH_SHORT).show();
            clientThread=new ConnectThread(devices.get(info.position),cHandler);
            clientThread.start();
            friendRefrence.removeEventListener(friendsListener);



        }

        return super.onContextItemSelected(item);
    }

    private void showDevices(final String username, final BluetoothDevice device) {

            FirebaseDatabase db = FirebaseDatabase.getInstance();
            DatabaseReference dr = db.getReference("User");
            final Query query = dr.orderByChild("username").equalTo(username);

            final ChildEventListener el = query.addChildEventListener(new ChildEventListener() {
                   @Override
                   public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) { //
                        User user = dataSnapshot.getValue(User.class);
                       if(!friendsUsername.contains(user.username))
                       {

                        boolean isti = false;
                             //prikazi samo user-e koji nam nisu prijatelji i nisu upareni
                        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                        if (pairedDevices.size() > 0) {

                            // There are paired devices. Get the name and address of each paired device.
                             for (BluetoothDevice device1 : pairedDevices) {

                             if (device.getAddress().compareTo(device1.getAddress()) == 0)
                                 {
                                     isti = true;
                                     Toast.makeText(SettingsActivity.this, "Upareni " + device1.getName(), Toast.LENGTH_SHORT).show();
                                 }

                             }

                             if(isti == false)
                                 {

                                     users.add(user);
                                     devices.add(device);
                                     ListView usersList = (ListView) findViewById(R.id.listViewDevices);
                                     usersList.setAdapter(new ArrayAdapter<User>(SettingsActivity.this, android.R.layout.simple_list_item_1, users));
                                     usersList.setVisibility(View.VISIBLE);
                                 }

                        }
                       }

                        query.removeEventListener(this);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                       Toast.makeText(SettingsActivity.this, "onChildChanged: " + username, Toast.LENGTH_LONG).show();
                       }

                       @Override
                       public void onChildRemoved(DataSnapshot dataSnapshot) {
                         Toast.makeText(SettingsActivity.this, "onChildRemoved: " + username, Toast.LENGTH_LONG).show();
                       }

                       @Override
                       public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                        Toast.makeText(SettingsActivity.this, "onChildMoved: " + username, Toast.LENGTH_LONG).show();
                       }

                       @Override
                       public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(SettingsActivity.this, "onCancelled: " + username, Toast.LENGTH_LONG).show();
                       }
                       });

               query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot==null)
                    {
                        query.removeEventListener(el);
                        Toast.makeText(SettingsActivity.this, "kill child", Toast.LENGTH_LONG).show();
                    }
                        query.removeEventListener(this);
                        Toast.makeText(SettingsActivity.this, "kill", Toast.LENGTH_LONG).show();

                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {}

            });
        }





        private void getAllFriends()
        {
            friendRefrence=FirebaseDatabase.getInstance().getReference().child("Friends").child(myID);
           friendsListener=friendRefrence.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    friends =(Map<String, String>) dataSnapshot.getValue();
                    if(friends!=null)
                    friendsUsername=  friends.values();
                    else
                        friendsUsername=new ArrayList<>();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }





    public void ShowMessage(String id )
    {
        Toast.makeText(this,"ID "+id,Toast.LENGTH_SHORT).show();
        Intent i = new Intent(SettingsActivity.this,FriendRequestActivity.class);
        i.putExtra("userID",id);
        startActivityForResult(i,4);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK)
        {
            Toast.makeText(SettingsActivity.this,"Result OK",Toast.LENGTH_SHORT).show();
            serverThread.accept=true;
            serverThread.semaphore.release();
        }
        if(resultCode==RESULT_FIRST_USER)
        {
            Toast.makeText(SettingsActivity.this,"Result CANCELED",Toast.LENGTH_SHORT).show();
            serverThread.accept=false;
            serverThread.semaphore.release();


            //unpairDevice(bluetoothDevice);
        }

    }



    private void unpairDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass()
                    .getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);

            Toast.makeText(SettingsActivity.this,"Unpairing ... ",Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e("Remove bond", e.getMessage());
        }
    }


    protected void onDestroy() {
            super.onDestroy();
            unregisterReceiver(mReceiver);
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
