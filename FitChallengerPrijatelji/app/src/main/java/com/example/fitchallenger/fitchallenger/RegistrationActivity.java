package com.example.fitchallenger.fitchallenger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import android.widget.Toast;
import  android.app.Activity;
import  android.content.Context;

public class RegistrationActivity extends AppCompatActivity implements View.OnClickListener {

    private AutoCompleteTextView editTextName, editTextUsername, editTextPhone,editTextLastName, editTextEmail,editTextAge;
    private EditText mPasswordView;
    private FirebaseAuth mAuth;
    private String mPicture;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        editTextEmail = findViewById(R.id.email_registration);
        editTextName = findViewById(R.id.name);
        editTextLastName = findViewById(R.id.lastname);
        editTextUsername = findViewById(R.id.username_registration);
        mPasswordView = findViewById(R.id.password_registration);
        editTextPhone = findViewById(R.id.phone);
        editTextAge = findViewById(R.id.age);

        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.finish_button).setOnClickListener(this);


    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            Intent i = new Intent(RegistrationActivity.this,HomeScreenActivity.class);
            startActivity(i);
        }
    }

    private void registerUser() {
        final String email = editTextEmail.getText().toString().trim();
        final String name = editTextName.getText().toString().trim();
        final String username = editTextUsername.getText().toString().trim();
        final String password = mPasswordView.getText().toString().trim();
        final String phone = editTextPhone.getText().toString().trim();
        final String lastname = editTextLastName.getText().toString().trim();



        String age1= editTextAge.getText().toString();
        final int age=Integer.parseInt(age1);

        final String picture = mPicture;


        if (email.isEmpty())
        {
            editTextEmail.setError(getString(R.string.input_error_email));
            editTextEmail.requestFocus();
            return;
        }

        if (name.isEmpty()) {
            editTextName.setError(getString(R.string.input_error_name));
            editTextName.requestFocus();
            return;
        }
        if (lastname.isEmpty()) {
            editTextLastName.setError(getString(R.string.input_error_name));
            editTextLastName.requestFocus();
            return;
        }

        if (username.isEmpty()) {
            editTextUsername.setError(getString(R.string.input_error_email));
            editTextUsername.requestFocus();
            return;
        }

        if (editTextAge.toString().isEmpty()) {
            editTextName.setError(getString(R.string.input_error_age));
            editTextName.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            mPasswordView.setError(getString(R.string.input_error_password));
            mPasswordView.requestFocus();
            return;
        }

        if (password.length() < 6) {
            mPasswordView.setError(getString(R.string.input_error_password_length));
            mPasswordView.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            editTextPhone.setError(getString(R.string.input_error_phone));
            editTextPhone.requestFocus();
            return;
        }

        if (phone.length() < 9) {
            editTextPhone.setError(getString(R.string.input_error_phone_invalid));
            editTextPhone.requestFocus();
            return;
        }


        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            User user = new User(email,username,name,lastname,phone,"https://firebasestorage.googleapis.com/v0/b/fitchallenger-e6fb5.appspot.com/o/picture%2F1530732973113.jpg?alt=media&token=c1fd19bc-591e-42a5-b56b-a5e4f64d6f9c",age);
                            SharedPreferences sharedPref = getSharedPreferences("CurrentUser",Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("username",username);
                            editor.putString("name",name);
                            editor.putString("lastname",lastname);
                            editor.putString("phone",phone);
                            editor.putLong("age",age);
                            editor.putLong("points",0);
                            editor.putString("picture","https://firebasestorage.googleapis.com/v0/b/fitchallenger-e6fb5.appspot.com/o/picture%2F1530732973113.jpg?alt=media&token=c1fd19bc-591e-42a5-b56b-a5e4f64d6f9c");

                            editor.commit();
                            FirebaseDatabase.getInstance().getReference("User")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {
                                        Toast.makeText(RegistrationActivity.this, getString(R.string.registration_success), Toast.LENGTH_LONG).show();
                                        Intent resultIntent = new Intent();
                                        resultIntent.putExtra("email", email);
                                        resultIntent.putExtra("password", password);
                                        SharedPreferences sharedPref = getSharedPreferences("CurrentUser",Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPref.edit();
                                        editor.putString("myID",mAuth.getUid());
                                        editor.commit();
                                        setResult(Activity.RESULT_OK, resultIntent);
                                        finish();
                                    }
                                    else
                                        {
                                        Toast.makeText(RegistrationActivity.this, "Username already exists", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                        } else {
                            Toast.makeText(RegistrationActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }

    @Override
    public void onClick(View v) {

        registerUser();


    }

}
