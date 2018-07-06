package com.example.fitchallenger.fitchallenger;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.example.fitchallenger.fitchallenger.R.drawable.bckg;

public class UserBoardActivity extends AppCompatActivity {

    private TableLayout usersTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_user_board);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_user_board);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        usersTable = (TableLayout)findViewById(R.id.tableUserBoard);
        usersTable.setStretchAllColumns(true);
        usersTable.bringToFront();

        setTitle("USER BOARD");






        initBoard();


    }

    private void initBoard() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference dr = db.getReference("User");
        final Query query = dr.orderByChild("points").limitToFirst(100);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<User> peopleList = new ArrayList<User>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    //Getting the data from snapshot
                    User person = postSnapshot.getValue(User.class);

                    //add person to your list
                    peopleList.add(person);
                    //create a list view, and add the apapter, passing in your list

                }

                init(peopleList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public void init(ArrayList<User> peopleList){


       Collections.sort(peopleList);

        for(int i = 0; i < peopleList.size(); i++){
            User user= peopleList.get(i);
            TableRow tr =  new TableRow(this);


            ImageView c1 = new ImageView(this);
            c1.setPadding(0,3,0,3);

            //LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(width,height);
            //c1.setLayoutParams(parms);

            if(user.picture.compareTo("")!=0)
            Picasso.with(this)
                    .load(user.picture)
                    .resize(65,65)
                    .into(c1);


            TextView c2 = new TextView(this);
            c2.setText(String.valueOf(i+1));
            c2.setTextSize(24);
            //c2.setGravity(Gravity.CENTER);

            TextView c3 = new TextView(this);
            c3.setText(user.username);
            c3.setTextSize(24);
            //c3.setGravity(Gravity.CENTER);


            TextView c4 = new TextView(this);
            c4.setText(String.valueOf(user.age));
            c4.setTextSize(24);
            //c4.setGravity(Gravity.CENTER);

            TextView c5 = new TextView(this);
            c5.setText(String.valueOf(user.points));
            c5.setTextSize(24);
            //c5.setGravity(Gravity.CENTER);


            tr.setPadding(-3,2,0,2);
            tr.addView(c1);
            tr.addView(c2);
            tr.addView(c3);
            tr.addView(c4);
            tr.addView(c5);
            //tr.setBackgroundResource(R.drawable.bckg);
            usersTable.addView(tr);
        }
    }


}
