package com.afoodchronicle.utilities;

import android.annotation.SuppressLint;
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

import com.afoodchronicle.MainActivity;
import com.afoodchronicle.firebase.ProfileDetailsActivity;
import com.afoodchronicle.R;
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
import com.google.firebase.iid.FirebaseInstanceId;

import static com.afoodchronicle.utilities.Static.FACEBOOK_DEVICE_TOKEN;
import static com.afoodchronicle.utilities.Static.FACEBOOK_FIRST_NAME;
import static com.afoodchronicle.utilities.Static.FACEBOOK_LAST_NAME;
import static com.afoodchronicle.utilities.Static.FACEBOOK_PROFILE_PIC;

@SuppressLint("Registered")
public class FacebookUtils extends AppCompatActivity {
    private static final String TAG = "Facebook";


    private FirebaseAuth mAuth;
    private CallbackManager mCallbackManager;

    protected void initializeFacebookLogin(final Context parentActivity){
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

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
                            writeInfoToPreferencesFacebook(parentActivity, firstName, lastName);
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
                    writeInfoToPreferencesFacebook(parentActivity, firstName, lastName);
                    writePhotoToDatabaseFacebook(parentActivity, profileImageUrl);

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

    private void writeInfoToPreferencesFacebook(Context parentActivity, String firstName, String lastName) {
        Utils.setPreferences(FACEBOOK_FIRST_NAME, firstName, parentActivity);
        Utils.setPreferences(FACEBOOK_LAST_NAME, lastName, parentActivity);
    }
    private void writePhotoToDatabaseFacebook(Context parentActivity, String photoUrl) {
        Utils.setPreferences(FACEBOOK_PROFILE_PIC, photoUrl, parentActivity);
    }
    private void handleFacebookAccessToken(AccessToken token, final Context parentActivity) {
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
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();
                            Utils.setPreferences(FACEBOOK_DEVICE_TOKEN, deviceToken, parentActivity);
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

    protected void updateUI(FirebaseUser user, Context packageContext) {
        hideProgressDialog();
        if (user != null) {
            Intent listIntent = new Intent(packageContext, ProfileDetailsActivity.class);

            startActivity(listIntent);
        }
    }
    @VisibleForTesting
    private ProgressDialog mProgressDialog;

    protected void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }


    protected void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public static boolean isLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }

    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
    }

}