package com.afoodchronicle;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class ProfileDetailsActivity extends AppCompatActivity {

    private EditText firstName;
    private TextView facebookFirstName;
    private EditText lastName;
    private TextView facebookLastName;
    private TextView birthday;
    private TextView description;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String profileFirstName;
    private String profileLastName;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent listIntent = new Intent(ProfileDetailsActivity.this, MainActivity.class);
        startActivity(listIntent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_details);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        firstName = findViewById(R.id.etFirstName);
        facebookFirstName = findViewById(R.id.tvFacebookFirstName);

        lastName = findViewById(R.id.etLastName);
        facebookLastName = findViewById(R.id.tvFacebookLastName);

        birthday = findViewById(R.id.etBirthday);

        description = findViewById(R.id.etDescription);

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                  return;

                } else {
                    Intent i = new Intent(ProfileDetailsActivity.this, LogInActivity.class);
                    startActivity(i);
                }

            }
        };
        if (MainActivity.isLoggedIn()){
            firstName.setVisibility(View.GONE);
            facebookFirstName.setVisibility(View.VISIBLE);
            lastName.setVisibility(View.GONE);
            facebookLastName.setVisibility(View.VISIBLE);

            profileFirstName = preferences.getString(MainActivity.PROFILE_FIRST_NAME, "");
            profileLastName = preferences.getString(MainActivity.PROFILE_LAST_NAME,"");

            facebookFirstName.setText(profileFirstName);
            facebookLastName.setText(profileLastName);
         }

    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
