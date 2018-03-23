package com.afoodchronicle.firebase;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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

import com.afoodchronicle.MainActivity;
import com.afoodchronicle.R;
import com.afoodchronicle.utilities.FacebookUtils;
import com.afoodchronicle.utilities.ImageLoadedCallback;
import com.afoodchronicle.utilities.Utils;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

import id.zelory.compressor.Compressor;

import static com.afoodchronicle.utilities.Static.EMAIL_AGE;
import static com.afoodchronicle.utilities.Static.EMAIL_BIRTHDAY;
import static com.afoodchronicle.utilities.Static.EMAIL_DESCRIPTION;
import static com.afoodchronicle.utilities.Static.EMAIL_DEVICE_TOKEN;
import static com.afoodchronicle.utilities.Static.EMAIL_FIRST_NAME;
import static com.afoodchronicle.utilities.Static.EMAIL_LAST_NAME;
import static com.afoodchronicle.utilities.Static.EMAIL_PROFILE_PIC;
import static com.afoodchronicle.utilities.Static.EMAIL_THUMB_PROFILE_PIC;
import static com.afoodchronicle.utilities.Static.FACEBOOK_AGE;
import static com.afoodchronicle.utilities.Static.FACEBOOK_BIRTHDAY;
import static com.afoodchronicle.utilities.Static.FACEBOOK_DESCRIPTION;
import static com.afoodchronicle.utilities.Static.FACEBOOK_DEVICE_TOKEN;
import static com.afoodchronicle.utilities.Static.FACEBOOK_FIRST_NAME;
import static com.afoodchronicle.utilities.Static.FACEBOOK_LAST_NAME;
import static com.afoodchronicle.utilities.Static.FACEBOOK_PROFILE_PIC;
import static com.afoodchronicle.utilities.Static.FACEBOOK_THUMB_PROFILE_PIC;
import static com.afoodchronicle.utilities.Static.IMAGES;
import static com.afoodchronicle.utilities.Static.JPG;
import static com.afoodchronicle.utilities.Static.PICK_IMAGE_REQUEST;
import static com.afoodchronicle.utilities.Static.REQUIRED;
import static com.afoodchronicle.utilities.Static.THUMB_IMAGES;
import static com.afoodchronicle.utilities.Static.THUMB_PHOTO_URL;
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
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private DatabaseReference mDatabase;
    private String profileImageLink;
    private Bitmap thumb_bitmap;
    private StorageReference thumbPhotoUrlReference;
    private String online_user_id;


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

        mAuthFacebook = LoginManager.getInstance();
        findViewById(R.id.btnChoose).setOnClickListener(this);
        findViewById(R.id.btnSave).setOnClickListener(this);
        findViewById(R.id.etBirthday).setOnClickListener(this);
        findViewById(R.id.btnCancel).setOnClickListener(this);

        profileImage = findViewById(R.id.profile_pic_details);
        fullName = findViewById(R.id.full_name);

        birthdayEt = findViewById(R.id.etBirthday);

        descriptionEt = findViewById(R.id.etDescription);
        ProgressBar progressBar;
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        // Firebase Database

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();
        initializeAuth();

        final ProgressBar finalProgressBar = progressBar;
        if ( online_user_id != null)
        {
            mDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(USERS).child(online_user_id).child(THUMB_PHOTO_URL).exists() ) {
                        profileImageLink = dataSnapshot.child(USERS).child(online_user_id).child(THUMB_PHOTO_URL).getValue().toString();
                        Picasso.with(ProfileDetailsActivity.this).load(profileImageLink).networkPolicy(NetworkPolicy.OFFLINE)
                                .into(profileImage, new Callback() {
                                    @Override
                                    public void onSuccess()
                                    {

                                    }

                                    @Override
                                    public void onError()
                                    {
                                        Picasso.with(ProfileDetailsActivity.this).load(profileImageLink).into(profileImage,
                                                new ImageLoadedCallback(finalProgressBar)
                                                {
                                                    @Override
                                                    public void onSuccess()
                                                    {
                                                        if (this.progressBar != null)
                                                        {
                                                            this.progressBar.setVisibility(View.GONE);
                                                        }
                                                    }
                                                });
                                    }
                                });
                    }
                    else
                    {
                        if (FacebookUtils.isLoggedIn())
                        {
                            profileImageLink= Utils.getPreferences(FACEBOOK_PROFILE_PIC, ProfileDetailsActivity.this);
                            Picasso.with(ProfileDetailsActivity.this).load(profileImageLink).networkPolicy(NetworkPolicy.OFFLINE)
                                    .into(profileImage, new Callback()
                                    {
                                        @Override
                                        public void onSuccess()
                                        {
                                        }

                                        @Override
                                        public void onError() {
                                            Picasso.with(ProfileDetailsActivity.this).load(profileImageLink).into(profileImage,
                                                    new ImageLoadedCallback(finalProgressBar)
                                                    {
                                                        @Override
                                                        public void onSuccess()
                                                        {
                                                            if (this.progressBar != null)
                                                            {
                                                                this.progressBar.setVisibility(View.GONE);
                                                            }
                                                        }
                                                    });
                                        }
                                    });
                        }
                        else
                        {
                            profileImageLink = Utils.getPreferences(EMAIL_PROFILE_PIC, ProfileDetailsActivity.this);
                            if (profileImageLink.equals(""))
                            {
                                Picasso.with(ProfileDetailsActivity.this).load(R.drawable.default_profile).into(profileImage);

                            }
                            else
                            {
                                Picasso.with(ProfileDetailsActivity.this).load(profileImageLink).networkPolicy(NetworkPolicy.OFFLINE)
                                        .into(profileImage, new Callback()
                                        {
                                            @Override
                                            public void onSuccess()
                                            {

                                            }

                                            @Override
                                            public void onError()
                                            {
                                                Picasso.with(ProfileDetailsActivity.this).load(profileImageLink).into(profileImage,
                                                        new ImageLoadedCallback(finalProgressBar)
                                                        {
                                                            @Override
                                                            public void onSuccess()
                                                            {
                                                                if (this.progressBar != null)
                                                                {
                                                                    this.progressBar.setVisibility(View.GONE);
                                                                }
                                                            }
                                                        });
                                            }
                                        });
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });
        }

    }



    private void initializeAuth(){
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                com.google.firebase.auth.FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                if (firebaseUser != null)
                {
                    if (FacebookUtils.isLoggedIn())
                        {
                        fullName.setText(Utils.getPreferences(FACEBOOK_FIRST_NAME,ProfileDetailsActivity.this)
                                + " "
                                + Utils.getPreferences(FACEBOOK_LAST_NAME,
                                ProfileDetailsActivity.this));
                        //Email

                        }
                    else
                        {
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
    private void setBirthday()
    {
        final Calendar myCalendar = Calendar.getInstance();

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener()
        {

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

    private void saveDetails()
    {

        validateForm();

        String birthday = birthdayEt.getText().toString().trim();
        String description = descriptionEt.getText().toString().trim();

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

    private void writeInfoToDatabaseFacebook(String birthday, String description)
    {
        String firstName = Utils.getPreferences(FACEBOOK_FIRST_NAME, ProfileDetailsActivity.this);
        String lastName = Utils.getPreferences(FACEBOOK_LAST_NAME, ProfileDetailsActivity.this);
        String photoUrl = Utils.getPreferences(FACEBOOK_PROFILE_PIC, ProfileDetailsActivity.this);
        String thumbPhotoUrl = Utils.getPreferences(FACEBOOK_THUMB_PROFILE_PIC, ProfileDetailsActivity.this);
        String age = Utils.getPreferences(FACEBOOK_AGE, ProfileDetailsActivity.this);
        String deviceToken = Utils.getPreferences(FACEBOOK_DEVICE_TOKEN, ProfileDetailsActivity.this);
        Utils.setPreferences(FACEBOOK_BIRTHDAY, birthday, ProfileDetailsActivity.this);
        Utils.setPreferences(FACEBOOK_DESCRIPTION, description, ProfileDetailsActivity.this);
        FirebaseUser firebaseUser = new FirebaseUser(firstName,lastName, photoUrl, thumbPhotoUrl, birthday, description, age, deviceToken);
        mDatabase.child(USERS).child(mAuth.getUid()).setValue(firebaseUser);
    }

    private void writeInfoToDatabaseEmail(String birthday, String description)
    {
        String firstName = Utils.getPreferences(EMAIL_FIRST_NAME, ProfileDetailsActivity.this);
        String lastName = Utils.getPreferences(EMAIL_LAST_NAME, ProfileDetailsActivity.this);
        String photoUrl = Utils.getPreferences(EMAIL_PROFILE_PIC, ProfileDetailsActivity.this);
        String thumbPhotoUrl = Utils.getPreferences(EMAIL_THUMB_PROFILE_PIC, ProfileDetailsActivity.this);
        String deviceToken = Utils.getPreferences(EMAIL_DEVICE_TOKEN, ProfileDetailsActivity.this);
        String age = Utils.getPreferences(EMAIL_AGE, ProfileDetailsActivity.this);
        Utils.setPreferences(EMAIL_BIRTHDAY, birthday, ProfileDetailsActivity.this);
        Utils.setPreferences(EMAIL_DESCRIPTION, description, ProfileDetailsActivity.this);
        FirebaseUser firebaseUser = new FirebaseUser(firstName,lastName, photoUrl, thumbPhotoUrl, birthday, description, age, deviceToken);
        mDatabase.child(USERS).child(mAuth.getUid()).setValue(firebaseUser);
    }

    private void writePhotoToPreferencesFacebook(String photoUrl)
    {
        Utils.setPreferences(FACEBOOK_PROFILE_PIC, photoUrl, ProfileDetailsActivity.this);
    }

    private void writePhotoToDatabaseEmail(String photoUrl)
    {
        Utils.setPreferences(EMAIL_PROFILE_PIC, photoUrl, ProfileDetailsActivity.this);
    }
    private void chooseImage()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            Uri filePath = data.getData();

            CropImage.activity(filePath)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
            {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);

                if (resultCode == RESULT_OK)

                {
                    Uri resultUri = result.getUri();
                    final File thumb_filePathUri = new File(resultUri.getPath());
                    try
                      {

                            thumb_bitmap = new Compressor(this)
                                    .setMaxWidth(200)
                                    .setMaxHeight(200)
                                    .setQuality(50)
                                    .compressToBitmap(thumb_filePathUri);

        //                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri);
                            Picasso.with(ProfileDetailsActivity.this)
                                .load(Utils.getImageUri(ProfileDetailsActivity.this, thumb_bitmap))
                                .into(profileImage);

                            showProgressDialog();

                       }

                    catch (IOException e)
                      {
                        e.printStackTrace();
                      }
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
                    final byte[] thumb_byte = byteArrayOutputStream.toByteArray();


                    StorageReference photoUrlReference = storageReference.child
                            (IMAGES + mAuth.getUid() + JPG);
                    thumbPhotoUrlReference = storageReference.child
                            (THUMB_IMAGES + mAuth.getUid()+ JPG);

                    photoUrlReference.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful())
                            {
                                UploadTask uploadTask = thumbPhotoUrlReference.putBytes(thumb_byte);
                                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {
                                        String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();
                                        if (task.isSuccessful())
                                        {
                                            if (FacebookUtils.isLoggedIn())
                                            {
                                                Utils.setPreferences(FACEBOOK_THUMB_PROFILE_PIC, thumb_downloadUrl, ProfileDetailsActivity.this);
                                            }
                                            else
                                            {
                                                Utils.setPreferences(EMAIL_THUMB_PROFILE_PIC, thumb_downloadUrl, ProfileDetailsActivity.this);
                                            }
                                        }
                                    }
                                });
                                Context context = getApplicationContext();
                                int duration = Toast.LENGTH_SHORT;

                                Toast toast = Toast.makeText(context, UPLOAD, duration);
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
                                int duration = Toast.LENGTH_SHORT;

                                Toast toast = Toast.makeText(context, UPLOAD_ERROR, duration);
                                toast.show();
                            }
                            hideProgressDialog();
                        }
                    });
               }
               else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
               {
                    Exception error = result.getError();
               }
            }
    }

    private void validateForm()
    {
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

        if(i == R.id.btnChoose)
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
}
