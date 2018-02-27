package com.afoodchronicle;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.afoodchronicle.utilities.FacebookUtils;
import com.afoodchronicle.utilities.Utils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static com.afoodchronicle.utilities.Static.EMAIL_FIRST_NAME;
import static com.afoodchronicle.utilities.Static.EMAIL_LAST_NAME;
import static com.afoodchronicle.utilities.Static.FACEBOOK_PROFILE_PIC;
import static com.afoodchronicle.utilities.Static.PASSWORD_DONT_MATCH;
import static com.afoodchronicle.utilities.Static.PHOTO;
import static com.afoodchronicle.utilities.Static.REQUIRED;
import static com.afoodchronicle.utilities.Static.USERS;

public class CreateUserActivity extends FacebookUtils implements View.OnClickListener {

    public static final String TAG = CreateUserActivity.class.getSimpleName();
    private FirebaseAuth mAuth;
    private EditText mEmailField;
    private EditText mPasswordField;
    private EditText mConfirmPasswordField;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private LoginManager mAuthFacebook;
    private EditText mFirstName;
    private EditText mLastName;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);

        // Views
        mEmailField = findViewById(R.id.etEmail);
        mPasswordField = findViewById(R.id.etPassword);
        mFirstName = findViewById(R.id.etFirstName);
        mLastName = findViewById(R.id.etLastName);
        mConfirmPasswordField = findViewById(R.id.etConfirmPassword);

        // Buttons
        findViewById(R.id.sign_up_button).setOnClickListener(this);
        findViewById(R.id.mLoginTextView).setOnClickListener(this);

        mAuthFacebook = LoginManager.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initializeFacebookLogin(CreateUserActivity.this);

        createAuthStateListener();

        mDatabase = FirebaseDatabase.getInstance().getReference();

    }

    private void writeBasicInfoToDatabaseEmail(String firstName, String lastName, String id) {
        User userBasicInfo = new User(firstName, lastName,1);
        Utils.setPreferences(EMAIL_FIRST_NAME, firstName, CreateUserActivity.this);
        Utils.setPreferences(EMAIL_LAST_NAME, lastName, CreateUserActivity.this);
        mDatabase.child(USERS).child(id).setValue(userBasicInfo);
    }

    private void createAccount(String email, String password)
    {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm())
        {
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener
                (this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {


                        if (task.isSuccessful())
                        {
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        }

                        else
                        {
                            Toast.makeText(CreateUserActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                    }
                });
    }

    private void createAuthStateListener() {
        mAuthListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Intent intent = new Intent(CreateUserActivity.this, ProfileDetailsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }

        };}


    private boolean validateForm() {
        boolean valid = true;

        String fName = mFirstName.getText().toString();
        if (TextUtils.isEmpty(fName)) {
            mFirstName.setError(REQUIRED);
            mFirstName.requestFocus();
            valid = false;
        } else {
            mFirstName.setError(null);
        }
        String lName = mLastName.getText().toString();
        if (TextUtils.isEmpty(lName)) {
            mLastName.setError(REQUIRED);
            mLastName.requestFocus();
            valid = false;
        } else {
            mLastName.setError(null);
        }
        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError(REQUIRED);
            mLastName.requestFocus();
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError(REQUIRED);
            mLastName.requestFocus();
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        String confirmPassword = mConfirmPasswordField.getText().toString();
        if (password.equals(confirmPassword)) {
            mPasswordField.setError(null);
            mLastName.requestFocus();
        } else {
            mPasswordField.setError(PASSWORD_DONT_MATCH);
            valid = false;
        }

        return valid;
    }

    private void sendEmailVerification() {


        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // [START_EXCLUDE]
                        // Re-enable button
                        if (task.isSuccessful()) {
                            Toast.makeText(CreateUserActivity.this,
                                    "Verification email sent to " + user.getEmail(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "sendEmailVerification", task.getException());
                            Toast.makeText(CreateUserActivity.this,
                                    "Failed to send verification email.",
                                    Toast.LENGTH_SHORT).show();
                        }
                      }
                });
        }

    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
        if (user != null) {
            writeBasicInfoToDatabaseEmail(mFirstName.getText().toString(), mLastName.getText().toString(), mAuth.getUid());
            Intent listIntent = new Intent(CreateUserActivity.this, ProfileDetailsActivity.class);
            sendEmailVerification();
            startActivity(listIntent);
        }
    }


    @Override
    public void onClick(View view) {
        int i = view.getId();

        if (i == R.id.sign_up_button) {
            createAccount(mEmailField.getText().toString().trim().toLowerCase(), mPasswordField.getText().toString().trim());
        }

        if (i == R.id.mLoginTextView) {
            Intent intent = new Intent(CreateUserActivity.this, LogInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
}}