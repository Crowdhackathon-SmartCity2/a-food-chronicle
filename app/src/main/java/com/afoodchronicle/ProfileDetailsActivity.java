package com.afoodchronicle;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Locale;
import java.util.prefs.PreferenceChangeListener;

import com.afoodchronicle.utilities.FacebookUtils;
import com.afoodchronicle.utilities.PermissionUtils;
import com.afoodchronicle.utilities.PreferenceUtils;

import static com.afoodchronicle.utilities.Static.EMAIL_BIRTHDAY;
import static com.afoodchronicle.utilities.Static.EMAIL_DESCRIPTION;
import static com.afoodchronicle.utilities.Static.EMAIL_FIRST_NAME;
import static com.afoodchronicle.utilities.Static.EMAIL_LAST_NAME;
import static com.afoodchronicle.utilities.Static.EMAIL_PROFILE_EDIT_PIC;
import static com.afoodchronicle.utilities.Static.EMAIL_PROFILE_PIC;
import static com.afoodchronicle.utilities.Static.EMAIL_USERS;
import static com.afoodchronicle.utilities.Static.EXTRA;
import static com.afoodchronicle.utilities.Static.FACEBOOK_BIRTHDAY;
import static com.afoodchronicle.utilities.Static.FACEBOOK_DESCRIPTION;
import static com.afoodchronicle.utilities.Static.FACEBOOK_FIRST_NAME;
import static com.afoodchronicle.utilities.Static.FACEBOOK_LAST_NAME;
import static com.afoodchronicle.utilities.Static.FACEBOOK_PROFILE_EDIT_PIC;
import static com.afoodchronicle.utilities.Static.FACEBOOK_PROFILE_PIC;
import static com.afoodchronicle.utilities.Static.FB_USERS;
import static com.afoodchronicle.utilities.Static.PROFILE_PIC_PNG;
import static com.afoodchronicle.utilities.Static.REQUIRED;

public class ProfileDetailsActivity extends FacebookUtils implements View.OnClickListener {

    //Views
    private EditText firstNameEt;
    private TextView facebookFirstName;
    private EditText lastNameEt;
    private TextView facebookLastName;
    private TextView birthdayEt;
    private TextView descriptionEt;
    private ImageView profilePic;

    //Networking
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private LoginManager mAuthFacebook;
    private Uri filePath;

    //Strings
    private final int PICK_IMAGE_REQUEST = 71;
    private String firstNameEmail;
    private String firstNameFacebook;
    private String lastNameEmail;
    private String lastNameFacebook;
    private String birthday;
    private String description;
    private String emailPhotoUrl;

    //Firebase
    FirebaseStorage storage;
    StorageReference storageReference;
    private DatabaseReference mDatabase;
    private ImageView profilePicBackground;
    private PreferenceChangeListener mPreferenceListener;
    private SharedPreferences mPrefs;
    private String fbEditpic;
    private String emailEditPic;
    private String fbPic;

    //Preferences



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent listIntent = new Intent(ProfileDetailsActivity.this, MainActivity.class);
        startActivity(listIntent);
    }

//    protected void onSaveInstanceState(Bundle outState)
//    {
//        super.onSaveInstanceState(outState);
//        if(!MainActivity.isLoggedIn()) {
//            firstNameEt = findViewById(R.id.etFirstName);
//            lastNameEt = findViewById(R.id.etLastName);
//            CharSequence fName = firstNameEt.getText();
//            CharSequence lName = lastNameEt.getText();
//            outState.putCharSequence("fName", fName);
//            outState.putCharSequence("lName", lName);
//        }
//        birthdayEt = findViewById(R.id.etBirthday);
//        descriptionEt = findViewById(R.id.etDescription);
//        CharSequence birthday = birthdayEt.getText();
//        CharSequence description = descriptionEt.getText();
//        outState.putCharSequence("birthday", birthday);
//        outState.putCharSequence("description", description);
//    }
//
//
//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState)
//    {
//        super.onRestoreInstanceState(savedInstanceState);
//       if(!MainActivity.isLoggedIn()){
//           firstNameEt = findViewById(R.id.etFirstName);
//           lastNameEt = findViewById(R.id.etLastName);
//           CharSequence fName = savedInstanceState.getCharSequence("fName");
//           CharSequence lName = savedInstanceState.getCharSequence("lName");
//           firstNameEt.setText(fName);// set the text that is retrieved from bundle object
//           lastNameEt.setText(lName);// set the text that is retrieved from bundle object
//       }
//        birthdayEt = findViewById(R.id.etBirthday);
//        descriptionEt = findViewById(R.id.etDescription);
//        CharSequence birthday = savedInstanceState.getCharSequence("birthday");
//        CharSequence description = savedInstanceState.getCharSequence("description");
//        birthdayEt.setText(birthday);// set the text that is retrieved from bundle object
//        lastNameEt.setText(description);// set the text that is retrieved from bundle object
//    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_details);

        mAuthFacebook = LoginManager.getInstance();
        findViewById(R.id.btnLog_out).setOnClickListener(this);
        findViewById(R.id.btnChoose).setOnClickListener(this);
        findViewById(R.id.btnSave).setOnClickListener(this);
        findViewById(R.id.etBirthday).setOnClickListener(this);
        findViewById(R.id.btnCancel).setOnClickListener(this);

        profilePic = findViewById(R.id.profile_pic_details);
        profilePicBackground = findViewById(R.id.profile_pic_background);
        firstNameEt = findViewById(R.id.etFirstName);
        facebookFirstName = findViewById(R.id.tvFacebookFirstName);

        lastNameEt = findViewById(R.id.etLastName);
        facebookLastName = findViewById(R.id.tvFacebookLastName);

        birthdayEt = findViewById(R.id.etBirthday);

        descriptionEt = findViewById(R.id.etDescription);

        // Firebase Database

        mDatabase = FirebaseDatabase.getInstance().getReference();

        final String profileEditPic = PreferenceUtils.getPreferences(FACEBOOK_PROFILE_PIC,
                ProfileDetailsActivity.this);
        PreferenceUtils.setPreferences(FACEBOOK_PROFILE_EDIT_PIC,profileEditPic,
                ProfileDetailsActivity.this);


        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        // [START initialize_auth]
        initializeAuth();
    }

    private void initializeAuth(){
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    if (FacebookUtils.isLoggedIn()) {
                        firstNameEt.setVisibility(View.GONE);
                        facebookFirstName.setVisibility(View.VISIBLE);
                        lastNameEt.setVisibility(View.GONE);
                        facebookLastName.setVisibility(View.VISIBLE);
                        facebookFirstName.setText(PreferenceUtils.getPreferences(FACEBOOK_FIRST_NAME,
                                ProfileDetailsActivity.this));
                        facebookLastName.setText(PreferenceUtils.getPreferences(FACEBOOK_LAST_NAME,
                                ProfileDetailsActivity.this));


                        Picasso.with(ProfileDetailsActivity.this).load(PreferenceUtils.getPreferences
                                (FACEBOOK_PROFILE_EDIT_PIC,
                                        ProfileDetailsActivity.this)).into(profilePic);

                        //Email

                    } else {
                        firstNameEt.setText(PreferenceUtils.getPreferences(EMAIL_FIRST_NAME,
                                ProfileDetailsActivity.this));
                        lastNameEt.setText(PreferenceUtils.getPreferences(EMAIL_LAST_NAME,
                                ProfileDetailsActivity.this));
                        Picasso.with(ProfileDetailsActivity.this).load(PreferenceUtils.getPreferences
                                (EMAIL_PROFILE_EDIT_PIC,
                                        ProfileDetailsActivity.this)).into(profilePic);

                    }
                }
               else{
                    Intent i = new Intent(ProfileDetailsActivity.this, MainActivity.class);
                    startActivity(i);
                }

            }


        };

    }
    private void setBirthday(){
        final Calendar myCalendar = Calendar.getInstance();

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String myFormat = "dd/MM/yy"; //In which you need put here
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.GERMAN);

                birthdayEt.setText(sdf.format(myCalendar.getTime()));
            }


        };

        birthdayEt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                 new DatePickerDialog(ProfileDetailsActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private void saveDetails() {

        validateForm();
        uploadImage();

        fbEditpic = PreferenceUtils.getPreferences
                (FACEBOOK_PROFILE_EDIT_PIC,
                        ProfileDetailsActivity.this);
        emailEditPic = PreferenceUtils.getPreferences
                (EMAIL_PROFILE_EDIT_PIC,
                        ProfileDetailsActivity.this);
        firstNameEmail = firstNameEt.getText().toString().trim();
        firstNameFacebook = facebookFirstName.getText().toString().trim();

        lastNameEmail = lastNameEt.getText().toString().trim();
        lastNameFacebook = facebookLastName.getText().toString().trim();

        birthday = birthdayEt.getText().toString().trim();
        description = descriptionEt.getText().toString().trim();
        // Facebook
        if (FacebookUtils.isLoggedIn()) {
            PreferenceUtils.setPreferences(FACEBOOK_PROFILE_PIC, fbEditpic,
                    ProfileDetailsActivity.this);

            writeExtraInfoToDatabaseFacebook(birthday, description, fbEditpic);

        }
        // Email
        else {
            PreferenceUtils.setPreferences(EMAIL_PROFILE_PIC, emailEditPic,
                    ProfileDetailsActivity.this);
            writeExtraInfoToDatabaseEmail(birthday, description, emailEditPic);

        }



    }

    private void writeExtraInfoToDatabaseFacebook(String birthday, String description, String photoUrl) {
        User userExtraInfo = new User(birthday, description, photoUrl);
        PreferenceUtils.setPreferences(FACEBOOK_BIRTHDAY, birthday, ProfileDetailsActivity.this);
        PreferenceUtils.setPreferences(FACEBOOK_DESCRIPTION, description, ProfileDetailsActivity.this);
        mDatabase.child(FB_USERS).child(Profile.getCurrentProfile().getId()).child(EXTRA).setValue(userExtraInfo);
    }

    private void writeExtraInfoToDatabaseEmail(String birthday, String description, String photoUrl) {
        User userExtraInfo = new User(birthday, description, photoUrl);
        PreferenceUtils.setPreferences(EMAIL_BIRTHDAY, birthday, ProfileDetailsActivity.this);
        PreferenceUtils.setPreferences(EMAIL_DESCRIPTION, description, ProfileDetailsActivity.this);
        mDatabase.child(EMAIL_USERS).child(mAuth.getUid()).child(EXTRA).setValue(userExtraInfo);
    }
    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void uploadImage() {


        if(filePath != null)
        {
            final StorageReference ref = storageReference.child("images/"+mAuth.getUid()+"/"+ PROFILE_PIC_PNG);
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                            if(FacebookUtils.isLoggedIn()){
                                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {

                                        PreferenceUtils.setPreferences(FACEBOOK_PROFILE_EDIT_PIC,taskSnapshot.getMetadata().getDownloadUrl().toString(),
                                               ProfileDetailsActivity.this);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Handle any errors
                                    }
                                });


                            }
                            //Email
                            else
                            {
                                PreferenceUtils.setPreferences(EMAIL_PROFILE_EDIT_PIC,taskSnapshot.getMetadata().getDownloadUrl().toString(),
                                        ProfileDetailsActivity.this);
                            }
                            Toast.makeText(ProfileDetailsActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ProfileDetailsActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                profilePic.setImageBitmap(bitmap);
                PreferenceUtils.setPreferences(FACEBOOK_PROFILE_EDIT_PIC,filePath.toString(),
                        ProfileDetailsActivity.this);
                PreferenceUtils.setPreferences(EMAIL_PROFILE_EDIT_PIC,filePath.toString(),
                        ProfileDetailsActivity.this);

            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private boolean validateForm() {
        boolean valid = true;

        if(!FacebookUtils.isLoggedIn()) {

            String fName = firstNameEt.getText().toString();
            if (TextUtils.isEmpty(fName)) {
                firstNameEt.setError(REQUIRED);
                valid = false;
            } else {
                firstNameEt.setError(null);

            }

            String lName = lastNameEt.getText().toString();
            if (TextUtils.isEmpty(lName)) {
                lastNameEt.setError(REQUIRED);
                valid = false;
            } else {
                lastNameEt.setError(null);
            }
        }


        String birthday = birthdayEt.getText().toString();
        if (TextUtils.isEmpty(birthday)) {
            birthdayEt.setError(REQUIRED);
            valid = false;
        } else {
            birthdayEt.setError(null);
        }
        if (valid){
        Intent i = new Intent(ProfileDetailsActivity.this, MainActivity.class);
        startActivity(i);}
        return valid;
    }
    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    @Override
    public void onResume() {
        super.onResume();

        initializeAuth();

    }

    @Override
    public void onRestart() {
        super.onRestart();
        initializeAuth();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onClick(View view) {

        int i = view.getId();
        if (i == R.id.btnLog_out) {
            mAuth.signOut();
            mAuthFacebook.logOut();
            Intent intent = new Intent(ProfileDetailsActivity.this, MainActivity.class);
            startActivity(intent);
             }
        else if(i == R.id.btnChoose){
            chooseImage();
        }
        else if(i == R.id.btnSave){
            saveDetails();
        }
        else if(i == R.id.etBirthday){
            setBirthday();
        }
        else if (i == R.id.btnCancel){
            Intent intent = new Intent(ProfileDetailsActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

//
}
