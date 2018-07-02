package com.example.fitchallenger.fitchallenger;

import android.app.ActionBar;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.solver.widgets.WidgetContainer;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.Layout;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.content.Intent;
import android.app.Activity;
import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.IOException;
import java.io.*;
import android.content.ClipData.Item;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.tasks.*;
import com.google.firebase.auth.*;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;
import com.google.firebase.database.Query;
import com.google.firebase.storage.*;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.database.ValueEventListener;
import java.net.URL;
import java.util.HashMap;

import java.util.Map;




public class MyProfileActivity extends AppCompatActivity {

    private TextView mTextMessage;
    FirebaseDatabase storage;
    FirebaseDatabase storageReference;
    FirebaseAuth mAuth;

    Uri downloadUrl;
    private int mMenuID;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            switch (item.getItemId()) {
                case R.id.about_me:
                    mTextMessage.setText("About me");

                    return true;
                case R.id.my_challenges:
                 mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.friends:
                    mTextMessage.setText(R.string.title_notifications);

                    return true;
            }
            return false;
        }
    };

    public static final int GET_FROM_GALLERY = 3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_my_profile);


        mAuth=FirebaseAuth.getInstance();



        mTextMessage = (TextView) findViewById(R.id.message);
        final BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);




       findViewById(R.id.uploadPicture).setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
           }
       });


       findViewById(R.id.my_challenges).setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {


               View l=(View)findViewById(R.id.about_container);
               l.setVisibility(View.INVISIBLE);
               navigation.getMenu().findItem(R.id.my_challenges).setChecked(true);



           }
       });
        findViewById(R.id.about_me).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                View l=(View)findViewById(R.id.about_container);
                l.setVisibility(View.VISIBLE);
                navigation.getMenu().findItem(R.id.about_me).setChecked(true);



            }
        });
        findViewById(R.id.friends).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                View l=(View)findViewById(R.id.about_container);
                l.setVisibility(View.INVISIBLE);
                navigation.getMenu().findItem(R.id.friends).setChecked(true);



            }
        });


        ShowPicture();
        ShowInfo();



    }

    private void ShowInfo() {
        FirebaseUser user = mAuth.getCurrentUser();
        SharedPreferences sharedPref = getSharedPreferences("CurrentUser", Context.MODE_PRIVATE);
        String username=sharedPref.getString("username","");
        setTitle(username);
        String name=sharedPref.getString("name","");
        String phone=sharedPref.getString("phone","");
        String lastname=sharedPref.getString("lastname","");
        int age = sharedPref.getInt("age",0);
        String s = Integer.toString(age);
        int points = sharedPref.getInt("points",0);
        String s1 = Integer.toString(points);
        final TextView t1 = (TextView)findViewById(R.id.textView1);
        final TextView t2 = (TextView)findViewById(R.id.textView2);
        final TextView t3 = (TextView)findViewById(R.id.textView3);
        final TextView t4 = (TextView)findViewById(R.id.textView4);

        final TextView t5 = (TextView)findViewById(R.id.textView5);
        final TextView t7 = (TextView)findViewById(R.id.textView7);

        t1.setText(name);
        t2.setText(lastname);
        t3.setText(phone);
        t4.setText(user.getEmail());
        t5.setText(s);
        t7.setText(s1);
        //DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("User").child(user.getUid().toString());
        /*

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference.child("User").orderByChild("id").equalTo(user.getUid().toString());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "issue" node with all children with id 0
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        t2.setText(issue.child("lastname").toString());
                        t1.setText(issue.child("name").toString());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/
    }


    private void ShowPicture()
   {
       ImageView i = (ImageView) findViewById(R.id.imageView);
       SharedPreferences sharedPref = getSharedPreferences("CurrentUser", Context.MODE_PRIVATE);
       String picture=sharedPref.getString("picture","");
     //  Toast.makeText(MyProfileActivity.this,picture,Toast.LENGTH_LONG).show();

       if(picture!="")
      {
          Picasso.with(this)
                  .load(picture)
                  .into(i);
      }
      else
      {
          FirebaseUser user = mAuth.getCurrentUser();

          if(user != null)
          {
              if(user.getPhotoUrl() != null)
              {
                  //Toast.makeText(MyProfileActivity.this,user.getPhotoUrl().toString(),Toast.LENGTH_LONG).show();
                  Picasso.with(this)
                          .load(user.getPhotoUrl().toString())
                          .into(i);

              }
          }
      }


   }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        //Detects request codes
        if(requestCode==GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            Bitmap bitmap = null;
            try {
                ImageView i=(ImageView)findViewById(R.id.imageView);
                Picasso.with(MyProfileActivity.this).load(selectedImage).into(i);
                // UpdatePicture(selectedImage);

                uploadImgToFirebaseStorage(selectedImage);
            }
            catch (Exception e)
            {
                Toast.makeText(MyProfileActivity.this,e.toString(),Toast.LENGTH_LONG).show();
            }
        }
    }

    private void UpdatePicture( Uri s) {
        if(s != null)
        {

            final FirebaseUser user = mAuth.getCurrentUser();
            SharedPreferences sharedPref = getSharedPreferences("CurrentUser", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("picture",s.toString());
            editor.commit();
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(s).build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("User").child(user.getUid().toString());
                                mDatabase.child("picture").setValue(user.getPhotoUrl().toString());


                                //vraca sliku
                                //@string/title_home
                               // String p = mDatabase.child("picture").toString();
                               // Toast.makeText(MyProfileActivity.this,p,Toast.LENGTH_LONG).show();
                            }
                        }
                    });


        }
    }



    private void uploadImgToFirebaseStorage(Uri uriProfileImage) {

        final StorageReference profileImgRef = FirebaseStorage.getInstance().getReference("picture/" + System.currentTimeMillis() + ".jpg");


        final @SuppressWarnings("VisibleForTests")Uri profileImageUrl;
        if(uriProfileImage != null)
        {
            profileImgRef.putFile(uriProfileImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                {
                    profileImgRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            downloadUrl = uri;

                            UpdatePicture(downloadUrl);
                            //Do what you want with the url

                        }
                            //Toast.MakeText(MyProfileActivity.this, "Upload Done", Toast.LENGTH_LONG).show();
                    });
                }

            });

        }
    }




}
