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
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Locale;
import java.util.prefs.PreferenceChangeListener;

import com.afoodchronicle.utilities.FacebookUtils;
import com.afoodchronicle.utilities.Utils;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import static com.afoodchronicle.utilities.Static.EMAIL_BIRTHDAY;
import static com.afoodchronicle.utilities.Static.EMAIL_DESCRIPTION;
import static com.afoodchronicle.utilities.Static.EMAIL_FIRST_NAME;
import static com.afoodchronicle.utilities.Static.EMAIL_LAST_NAME;
import static com.afoodchronicle.utilities.Static.EMAIL_PROFILE_EDIT_PIC;
import static com.afoodchronicle.utilities.Static.EMAIL_PROFILE_PIC;
import static com.afoodchronicle.utilities.Static.EXTRA;
import static com.afoodchronicle.utilities.Static.FACEBOOK_BIRTHDAY;
import static com.afoodchronicle.utilities.Static.FACEBOOK_DESCRIPTION;
import static com.afoodchronicle.utilities.Static.FACEBOOK_FIRST_NAME;
import static com.afoodchronicle.utilities.Static.FACEBOOK_LAST_NAME;
import static com.afoodchronicle.utilities.Static.FACEBOOK_PROFILE_EDIT_PIC;
import static com.afoodchronicle.utilities.Static.FACEBOOK_PROFILE_PIC;
import static com.afoodchronicle.utilities.Static.IMAGES;
import static com.afoodchronicle.utilities.Static.JPG;
import static com.afoodchronicle.utilities.Static.PHOTO;
import static com.afoodchronicle.utilities.Static.REQUIRED;
import static com.afoodchronicle.utilities.Static.UPLOAD;
import static com.afoodchronicle.utilities.Static.UPLOAD_ERROR;
import static com.afoodchronicle.utilities.Static.USERS;

public class ProfileDetailsActivity extends FacebookUtils implements View.OnClickListener {

    //Views
    private TextView fullName;
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

    //Preferences



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

        profilePic = findViewById(R.id.profile_pic_details);
        profilePicBackground = findViewById(R.id.profile_pic_background);
        fullName = findViewById(R.id.full_name);

        birthdayEt = findViewById(R.id.etBirthday);

        descriptionEt = findViewById(R.id.etDescription);

        // Firebase Database

        mDatabase = FirebaseDatabase.getInstance().getReference();

        if(FacebookUtils.isLoggedIn())
        {
            if (Utils.getPreferences(FACEBOOK_PROFILE_PIC, ProfileDetailsActivity.this) != null)
            {
                Picasso.with(ProfileDetailsActivity.this).load(Utils.getPreferences
                        (FACEBOOK_PROFILE_PIC,
                                ProfileDetailsActivity.this)).into(profilePic);
            }
        }
        else
        {
            if (Utils.getPreferences(EMAIL_PROFILE_PIC, ProfileDetailsActivity.this) != null)
            {
                Picasso.with(ProfileDetailsActivity.this).load(Utils.getPreferences
                        (EMAIL_PROFILE_PIC,
                                ProfileDetailsActivity.this)).into(profilePic);
            }
        }
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        // [START initialize_auth]
        initializeAuth();
        if (FacebookUtils.isLoggedIn())
        {
            writeBasicInfoToDatabaseFacebook();
        }
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
            writeExtraInfoToDatabaseFacebook(birthday, description);
        }

        // Email
        else
        {
            writeExtraInfoToDatabaseEmail(birthday, description);
        }
    }

    public void writeBasicInfoToDatabaseFacebook() {
        User user = new User(Utils.getPreferences(FACEBOOK_FIRST_NAME, ProfileDetailsActivity.this),
                Utils.getPreferences(FACEBOOK_LAST_NAME, ProfileDetailsActivity.this),
                1);
        mDatabase.child(USERS).child(mAuth.getUid()).setValue(user);
    }
    private void writeExtraInfoToDatabaseFacebook(String birthday, String description) {
        User userExtraInfo = new User(birthday, description);
        Utils.setPreferences(FACEBOOK_BIRTHDAY, birthday, ProfileDetailsActivity.this);
        Utils.setPreferences(FACEBOOK_DESCRIPTION, description, ProfileDetailsActivity.this);
        mDatabase.child(USERS).child(mAuth.getUid()).child(EXTRA).setValue(userExtraInfo);
    }

    private void writeExtraInfoToDatabaseEmail(String birthday, String description) {
        User userExtraInfo = new User(birthday, description);
        Utils.setPreferences(EMAIL_BIRTHDAY, birthday, ProfileDetailsActivity.this);
        Utils.setPreferences(EMAIL_DESCRIPTION, description, ProfileDetailsActivity.this);
        mDatabase.child(USERS).child(mAuth.getUid()).child(EXTRA).setValue(userExtraInfo);
    }

    public void writePhotoToDatabaseFacebook(String photoUrl, String id) {
        User user = new User(photoUrl);
        Utils.setPreferences(FACEBOOK_PROFILE_PIC, photoUrl, ProfileDetailsActivity.this);
        mDatabase.child(USERS).child(id).child(PHOTO).setValue(user);
    }

    public void writePhotoToDatabaseEmail(String photoUrl, String id) {
        User user = new User(photoUrl);
        Utils.setPreferences(EMAIL_PROFILE_PIC, photoUrl, ProfileDetailsActivity.this);
        mDatabase.child(USERS).child(id).child(PHOTO).setValue(user);
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
                                .into(profilePic);


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
                                            writePhotoToDatabaseFacebook(downloadUrl, mAuth.getUid());
                                        }
                                        else
                                        {
                                            writePhotoToDatabaseEmail(downloadUrl, mAuth.getUid());
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
