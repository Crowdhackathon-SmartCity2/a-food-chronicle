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

import com.facebook.CallbackManager;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.afoodchronicle.utilities.FacebookUtils;

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
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        Log.d(LOG_TAG, " Verification : signIn With Email:onComplete:" + task.isSuccessful());
                        //  If sign in succeeds i.e if task.isSuccessful(); returns true then the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        // If sign in fails, display a message to the user.
                        if (!task.isSuccessful()) {
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

