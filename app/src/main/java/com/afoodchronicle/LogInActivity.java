package com.afoodchronicle;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afoodchronicle.utilities.FacebookUtils;
import com.afoodchronicle.utilities.Utils;
import com.facebook.CallbackManager;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import static com.afoodchronicle.utilities.Static.DEVICE_TOKEN;
import static com.afoodchronicle.utilities.Static.EMAIL_DEVICE_TOKEN;
import static com.afoodchronicle.utilities.Static.EMAIL_FIRST_NAME;
import static com.afoodchronicle.utilities.Static.EMAIL_LAST_NAME;
import static com.afoodchronicle.utilities.Static.EMAIL_PROFILE_PIC;
import static com.afoodchronicle.utilities.Static.FIRST_NAME;
import static com.afoodchronicle.utilities.Static.LAST_NAME;
import static com.afoodchronicle.utilities.Static.PHOTO;
import static com.afoodchronicle.utilities.Static.PHOTO_URL;
import static com.afoodchronicle.utilities.Static.USERS;

public class LogInActivity extends FacebookUtils implements
        View.OnClickListener {

    private static final String TAG = "EmailPassword";


    // Views
    private TextView mStatusTextView;
    private EditText mEmailField;
    private EditText mPasswordField;

    static final String LOG_TAG = LogInActivity.class.getSimpleName();


    // Facebook
    private CallbackManager mCallbackManager;
    private LoginManager mAuthFacebook;

    //Firebase
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference getNameFromDatabaseReference;
    private DatabaseReference userReference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Views
        mStatusTextView = findViewById(R.id.status);
        mEmailField = findViewById(R.id.etEmail);
        mPasswordField = findViewById(R.id.etPassword);

        // Buttons
        findViewById(R.id.email_sign_in_button).setOnClickListener(this);
        findViewById(R.id.email_create_account_button).setOnClickListener(this);
        findViewById(R.id.tvForgotPassword).setOnClickListener(this);


        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        mAuthFacebook = LoginManager.getInstance();
        userReference = FirebaseDatabase.getInstance().getReference().child(USERS);

        // [END initialize_auth]

        // [START auth_state_listener] ,this method execute as soon as there is a change in Auth status , such as user sign in or sign out.
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    Intent intent = new Intent(LogInActivity.this, MainActivity.class);
                    startActivity(intent);

                }
            }
        };
        // [END auth_state_listener]

        // Firebase Database

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        // Initialize Facebook Login button
        initializeFacebookLogin(LogInActivity.this);
    }

    // [START on_activity_result]


    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser, LogInActivity.this);
    }
    // [END on_start_check_user]

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if(task.isSuccessful())
                        {
                            downloadUserDataFromDatabaseToPreferences(mAuth.getUid());
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();
                            Utils.setPreferences(EMAIL_DEVICE_TOKEN, deviceToken, LogInActivity.this);

                        }
                        else
                        {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidUserException e) {
                                mStatusTextView.setError("Invalid Emaild Id");
                                mStatusTextView.requestFocus();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                              //  Log.d(LOG_TAG , "email :" + email);
                                mStatusTextView.setError("Invalid Password");
                                mStatusTextView.requestFocus();
                            } catch (FirebaseNetworkException e) {
                                Toast.makeText(LogInActivity.this,"error_message_failed_sign_in_no_network",
                                        Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Log.e(LOG_TAG, e.getMessage());
                            }
                            Log.w(LOG_TAG, "signInWithEmail:failed", task.getException());
                            Toast.makeText(LogInActivity.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null, LogInActivity.this);
                        }


                }});
    }

    private void downloadUserDataFromDatabaseToPreferences(final String id){
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {

                    getNameFromDatabaseReference = FirebaseDatabase.getInstance().getReference().child(USERS).child(id);
                    getNameFromDatabaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String firstName = dataSnapshot.child(FIRST_NAME).getValue().toString();
                            String lastName = dataSnapshot.child(LAST_NAME).getValue().toString();
                            String photoUrl = dataSnapshot.child(PHOTO).getValue().toString();
                            Utils.setPreferences(EMAIL_FIRST_NAME, firstName, LogInActivity.this);
                            Utils.setPreferences(EMAIL_LAST_NAME, lastName, LogInActivity.this);
                            Utils.setPreferences(EMAIL_PROFILE_PIC, photoUrl, LogInActivity.this);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }


            }
        };
    }


    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            mEmailField.requestFocus();
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            mPasswordField.requestFocus();
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.email_create_account_button) {
            Intent listIntent = new Intent(LogInActivity.this, CreateUserActivity.class);

            startActivity(listIntent);
        }
        else if (i == R.id.email_sign_in_button) {
            signIn(mEmailField.getText().toString().trim().toLowerCase(), mPasswordField.getText().toString().trim());
        }
        else if (i == R.id.tvForgotPassword) {
            Intent listIntent = new Intent(LogInActivity.this, ResetPasswordActivity.class);
            startActivity(listIntent);
    }}

}

