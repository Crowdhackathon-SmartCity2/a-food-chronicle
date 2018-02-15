package com.afoodchronicle;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FacebookActivity extends AppCompatActivity {
    private static final String TAG = "Facebook";
    private FirebaseAuth mAuth;
    private CallbackManager mCallbackManager;
    private DatabaseReference mDatabase;

    public void initializeFacebookLogin(final Context parentActivity){
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mCallbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = findViewById(R.id.button_facebook_login);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            private ProfileTracker mProfileTracker;
            @Override
            public void onSuccess(LoginResult loginResult) {
                if(Profile.getCurrentProfile() == null) {
                    mProfileTracker = new ProfileTracker() {
                        @Override
                        protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                            String firstName = currentProfile.getFirstName();
                            String lastName = currentProfile.getLastName();
                            String userId = currentProfile.getId();
                            String profileImageUrl = "https://graph.facebook.com/" + userId + "/picture?height=500";
                            writeBasicInfoToDatabaseFacebook(parentActivity, userId, firstName, lastName, profileImageUrl);
                            mProfileTracker.stopTracking();
                        }
                    };
                    // no need to call startTracking() on mProfileTracker
                    // because it is called by its constructor, internally.
                }
                else {
                    Profile mProfile = Profile.getCurrentProfile();
                    String firstName = mProfile.getFirstName();
                    String lastName = mProfile.getLastName();
                    String userId = mProfile.getId();
                    String profileImageUrl = "https://graph.facebook.com/" + userId + "/picture?height=500";
                    writeBasicInfoToDatabaseFacebook(parentActivity, userId, firstName, lastName, profileImageUrl);
                }
                    handleFacebookAccessToken(loginResult.getAccessToken(), parentActivity);
            }


            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                updateUI(null, parentActivity);
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                updateUI(null, parentActivity);
            }

        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void writeBasicInfoToDatabaseFacebook(Context parentActivity, String id, String firstName, String lastName, String photoUrl) {
        User user = new User(firstName, lastName, photoUrl);
        MainActivity.setPreferences("FACEBOOK_FIRST_NAME", firstName, parentActivity);
        MainActivity.setPreferences("FACEBOOK_LAST_NAME", lastName, parentActivity);
        String ifExist = MainActivity.getPreferences("FACEBOOK_PROFILE_PIC",parentActivity);
        if (ifExist == null) {
            MainActivity.setPreferences("FACEBOOK_PROFILE_PIC", photoUrl, parentActivity);
        }
        mDatabase.child("fb_users").child(id).setValue(user);
    }
    public void handleFacebookAccessToken(AccessToken token, final Context parentActivity) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        mAuth = FirebaseAuth.getInstance();

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            try {
                                throw task.getException();
                            } catch (FirebaseNetworkException e) {
                                Toast.makeText(parentActivity,"error_message_failed_sign_in_no_network",
                                        Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Log.e(TAG, e.getMessage());
                            }
                            Log.w(TAG, "signInWithFacebook:failed", task.getException());
                            Toast.makeText(parentActivity, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null, parentActivity);
                        }
                        else{

                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(parentActivity);
                            if (!prefs.getBoolean("firstTime", false)) {
                                Intent facebookIntent = new Intent(parentActivity, ProfileDetailsActivity.class);
                                startActivity(facebookIntent);
                            }
                            else {
                                Intent facebookIntent = new Intent(parentActivity, MainActivity.class);
                                startActivity(facebookIntent);
                            }

                        }


                    }
                });
    }

    public void updateUI(FirebaseUser user, Context packageContext) {
        hideProgressDialog();
        if (user != null) {
            Intent listIntent = new Intent(packageContext, ProfileDetailsActivity.class);

            startActivity(listIntent);
        }
    }
    @VisibleForTesting
    public ProgressDialog mProgressDialog;

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }


    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
    }

}