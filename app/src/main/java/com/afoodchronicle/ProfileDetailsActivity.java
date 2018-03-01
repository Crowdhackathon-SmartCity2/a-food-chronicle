package com.afoodchronicle;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afoodchronicle.utilities.FacebookUtils;
import com.afoodchronicle.utilities.ImageLoadedCallback;
import com.afoodchronicle.utilities.Utils;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.Locale;
import java.util.prefs.PreferenceChangeListener;

import static com.afoodchronicle.utilities.Static.EMAIL_AGE;
import static com.afoodchronicle.utilities.Static.EMAIL_BIRTHDAY;
import static com.afoodchronicle.utilities.Static.EMAIL_DESCRIPTION;
import static com.afoodchronicle.utilities.Static.EMAIL_FIRST_NAME;
import static com.afoodchronicle.utilities.Static.EMAIL_LAST_NAME;
import static com.afoodchronicle.utilities.Static.EMAIL_PROFILE_PIC;
import static com.afoodchronicle.utilities.Static.FACEBOOK_AGE;
import static com.afoodchronicle.utilities.Static.FACEBOOK_BIRTHDAY;
import static com.afoodchronicle.utilities.Static.FACEBOOK_DESCRIPTION;
import static com.afoodchronicle.utilities.Static.FACEBOOK_FIRST_NAME;
import static com.afoodchronicle.utilities.Static.FACEBOOK_LAST_NAME;
import static com.afoodchronicle.utilities.Static.FACEBOOK_PROFILE_PIC;
import static com.afoodchronicle.utilities.Static.IMAGES;
import static com.afoodchronicle.utilities.Static.JPG;
import static com.afoodchronicle.utilities.Static.PHOTO_URL;
import static com.afoodchronicle.utilities.Static.REQUIRED;
import static com.afoodchronicle.utilities.Static.UPLOAD;
import static com.afoodchronicle.utilities.Static.UPLOAD_ERROR;
import static com.afoodchronicle.utilities.Static.USERS;

public class ProfileDetailsActivity extends FacebookUtils implements View.OnClickListener {

    //Views
    private TextView fullName;
    private TextView birthdayEt;
    private TextView descriptionEt;
    private ImageView profileImage;

    //Networking
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private LoginManager mAuthFacebook;
    private Uri filePath;

    //Strings
    private final int PICK_IMAGE_REQUEST = 71;
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
    private String profileImageLink;


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent listIntent = new Intent(ProfileDetailsActivity.this, MainActivity.class);
        startActivity(listIntent);
    }

//

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

        profileImage = findViewById(R.id.profile_pic_details);
        profilePicBackground = findViewById(R.id.profile_pic_background);
        fullName = findViewById(R.id.full_name);

        birthdayEt = findViewById(R.id.etBirthday);

        descriptionEt = findViewById(R.id.etDescription);
        ProgressBar progressBar = null;
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        // Firebase Database

        mDatabase = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        // [START initialize_auth]
        initializeAuth();

        final ProgressBar finalProgressBar = progressBar;
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                profileImageLink = dataSnapshot.child(USERS).child(mAuth.getUid()).child(PHOTO_URL).getValue().toString();

                if (!profileImageLink.equals("default_profile")) {
                    Picasso.with(ProfileDetailsActivity.this).load(profileImageLink).into(profileImage,
                            new ImageLoadedCallback(finalProgressBar) {
                                @Override
                                public void onSuccess() {
                                    if (this.progressBar != null) {
                                        this.progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    private void initializeAuth(){
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    if (FacebookUtils.isLoggedIn()) {
                        fullName.setText(Utils.getPreferences(FACEBOOK_FIRST_NAME,ProfileDetailsActivity.this)
                                + " "
                                + Utils.getPreferences(FACEBOOK_LAST_NAME,
                                ProfileDetailsActivity.this));
                        //Email

                    } else {
                        fullName.setText(Utils.getPreferences(EMAIL_FIRST_NAME,ProfileDetailsActivity.this)
                                + " "
                                + Utils.getPreferences(EMAIL_LAST_NAME,
                                ProfileDetailsActivity.this));
                                           }
                }
               else
                {
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
                if(FacebookUtils.isLoggedIn())
                {
                    Utils.setPreferences(FACEBOOK_AGE,  Utils.getAge(year,monthOfYear, dayOfMonth),
                            ProfileDetailsActivity.this);
                }
                else
                {
                    Utils.setPreferences(EMAIL_AGE,  Utils.getAge(year,monthOfYear, dayOfMonth),
                        ProfileDetailsActivity.this);
                }
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

        birthday = birthdayEt.getText().toString().trim();
        description = descriptionEt.getText().toString().trim();

        // Facebook
        if (FacebookUtils.isLoggedIn())
        {
            writeInfoToDatabaseFacebook(birthday, description);
        }

        // Email
        else
        {
            writeInfoToDatabaseEmail(birthday, description);
        }
    }

    private void writeInfoToDatabaseFacebook(String birthday, String description) {
        String firstName = Utils.getPreferences(FACEBOOK_FIRST_NAME, ProfileDetailsActivity.this);
        String lastName = Utils.getPreferences(FACEBOOK_LAST_NAME, ProfileDetailsActivity.this);
        String photoUrl = Utils.getPreferences(FACEBOOK_PROFILE_PIC, ProfileDetailsActivity.this);
        String age = Utils.getPreferences(FACEBOOK_AGE, ProfileDetailsActivity.this);
        Utils.setPreferences(FACEBOOK_BIRTHDAY, birthday, ProfileDetailsActivity.this);
        Utils.setPreferences(FACEBOOK_DESCRIPTION, description, ProfileDetailsActivity.this);
        User user = new User(firstName,lastName, photoUrl, birthday, description, age);
        mDatabase.child(USERS).child(mAuth.getUid()).setValue(user);
    }

    private void writeInfoToDatabaseEmail(String birthday, String description) {
        String firstName = Utils.getPreferences(EMAIL_FIRST_NAME, ProfileDetailsActivity.this);
        String lastName = Utils.getPreferences(EMAIL_LAST_NAME, ProfileDetailsActivity.this);
        String photoUrl = Utils.getPreferences(EMAIL_PROFILE_PIC, ProfileDetailsActivity.this);
        String age = Utils.getPreferences(EMAIL_AGE, ProfileDetailsActivity.this);
        Utils.setPreferences(EMAIL_BIRTHDAY, birthday, ProfileDetailsActivity.this);
        Utils.setPreferences(EMAIL_DESCRIPTION, description, ProfileDetailsActivity.this);
        User user = new User(firstName,lastName, photoUrl, birthday, description, age);
        mDatabase.child(USERS).child(mAuth.getUid()).setValue(user);
    }

    public void writePhotoToPreferencesFacebook(String photoUrl) {
        Utils.setPreferences(FACEBOOK_PROFILE_PIC, photoUrl, ProfileDetailsActivity.this);
    }

    public void writePhotoToDatabaseEmail(String photoUrl) {
        Utils.setPreferences(EMAIL_PROFILE_PIC, photoUrl, ProfileDetailsActivity.this);
    }
    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null ) {
            filePath = data.getData();

            CropImage.activity(filePath)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);

                if (resultCode == RESULT_OK)

                {
                    Uri resultUri = result.getUri();
                    try {

                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri);
                            Picasso.with(ProfileDetailsActivity.this)
                                .load(Utils.getImageUri(ProfileDetailsActivity.this, bitmap))
                                .into(profileImage);


                            final StorageReference ref = storageReference.child
                                    (IMAGES + mAuth.getUid()+ JPG);
                            ref.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    if(task.isSuccessful())
                                    {
                                        Context context = getApplicationContext();
                                        CharSequence text = UPLOAD;
                                        int duration = Toast.LENGTH_SHORT;

                                        Toast toast = Toast.makeText(context, text, duration);
                                        toast.show();

                                        String downloadUrl = task.getResult().getDownloadUrl().toString();
                                        if (FacebookUtils.isLoggedIn())
                                        {
                                            writePhotoToPreferencesFacebook(downloadUrl);
                                        }
                                        else
                                        {
                                            writePhotoToDatabaseEmail(downloadUrl);
                                        }
                                    }

                                    else
                                    {
                                        Context context = getApplicationContext();
                                        CharSequence text = UPLOAD_ERROR;
                                        int duration = Toast.LENGTH_SHORT;

                                        Toast toast = Toast.makeText(context, text, duration);
                                        toast.show();
                                    }
                                }
                            });
                        }

                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
               }
               else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
               {
                    Exception error = result.getError();
               }
            }
    }

    private boolean validateForm() {
        boolean valid = true;

        String birthday = birthdayEt.getText().toString();
        if (TextUtils.isEmpty(birthday))
        {
            birthdayEt.setError(REQUIRED);
            valid = false;
        }
        else
        {
            birthdayEt.setError(null);
        }
        if (valid)
        {
        Intent i = new Intent(ProfileDetailsActivity.this, MainActivity.class);
        startActivity(i);
        }
        return valid;
    }
    @Override
    protected void onStart()
    {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    @Override
    public void onResume()
    {
        super.onResume();

        initializeAuth();

    }

    @Override
    public void onRestart()
    {
        super.onRestart();
        initializeAuth();
    }
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (mAuthListener != null)
        {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onClick(View view)
    {

        int i = view.getId();
        if (i == R.id.btnLog_out)
        {
            mAuth.signOut();
            mAuthFacebook.logOut();
            Intent intent = new Intent(ProfileDetailsActivity.this, MainActivity.class);
            startActivity(intent);
        }
        else if(i == R.id.btnChoose)
        {
            chooseImage();
        }
        else if(i == R.id.btnSave)
        {
            saveDetails();
        }
        else if(i == R.id.etBirthday)
        {
            setBirthday();
        }
        else if (i == R.id.btnCancel)
        {
            Intent intent = new Intent(ProfileDetailsActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
//
}
