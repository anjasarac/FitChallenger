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
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class HomeScreenActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref = getSharedPreferences("CurrentUser", Context.MODE_PRIVATE);
        String currentID =sharedPref.getString("myID","");
        mAuth= FirebaseAuth.getInstance();
        if(currentID.compareTo(mAuth.getUid())!=0)
            getUserData();
        setContentView(R.layout.activity_home_screen);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
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
}
