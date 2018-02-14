package com.afoodchronicle;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
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
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Locale;
import java.util.UUID;


public class ProfileDetailsActivity extends FacebookActivity implements View.OnClickListener {

    private EditText firstNameEt;
    private TextView facebookFirstName;
    private EditText lastNameEt;
    private TextView facebookLastName;
    private TextView birthdayEt;
    private TextView descriptionEt;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ImageView profilePic;
    private LoginManager mAuthFacebook;
    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 71;


    //Strings
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
        findViewById(R.id.btnLog_out).setOnClickListener(this);
        findViewById(R.id.btnChoose).setOnClickListener(this);
        findViewById(R.id.btnSave).setOnClickListener(this);
        findViewById(R.id.etBirthday).setOnClickListener(this);

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


        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    if (MainActivity.isLoggedIn())
                    {
                        firstNameEt.setVisibility(View.GONE);
                        facebookFirstName.setVisibility(View.VISIBLE);
                        lastNameEt.setVisibility(View.GONE);
                        facebookLastName.setVisibility(View.VISIBLE);
                        facebookFirstName.setText(MainActivity.getPreferences("FACEBOOK_FIRST_NAME",
                                ProfileDetailsActivity.this));
                        facebookLastName.setText(MainActivity.getPreferences("FACEBOOK_LAST_NAME",
                                ProfileDetailsActivity.this));
                        Picasso.with(ProfileDetailsActivity.this).load(MainActivity.getPreferences("FACEBOOK_PROFILE_PIC",
                                ProfileDetailsActivity.this)).into(profilePic);
                        final BlurredAsynctask task = new BlurredAsynctask(ProfileDetailsActivity.this,
                        profilePicBackground, 15);
                        task.execute(MainActivity.getPreferences("FACEBOOK_PROFILE_PIC",
                                ProfileDetailsActivity.this));
                    }
                    //Email
                    {

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
        showProgressDialog();
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
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
                // TODO Auto-generated method stub
                new DatePickerDialog(ProfileDetailsActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private void saveDetails() {

        validateForm();
        firstNameEmail = firstNameEt.getText().toString().trim();
        firstNameFacebook = facebookFirstName.getText().toString().trim();

        lastNameEmail = lastNameEt.getText().toString().trim();
        lastNameFacebook = facebookLastName.getText().toString().trim();

        birthday = birthdayEt.getText().toString().trim();
        description = descriptionEt.getText().toString().trim();
        // Facebook
        if (MainActivity.isLoggedIn()) {
            writeExtraInfoToDatabaseFacebook(birthday, description);
        }
        // Email
        else {
            writeBasicInfoToDatabaseEmail(firstNameEmail, lastNameEmail, emailPhotoUrl);
            writeExtraInfoToDatabaseEmail(birthday, description);
        }

    }

    private void writeExtraInfoToDatabaseFacebook(String birthday, String description) {
        User userExtraInfo = new User(birthday, description);
        MainActivity.setPreferences("FACEBOOK_BIRTHDAY", birthday, ProfileDetailsActivity.this);
        MainActivity.setPreferences("FACEBOOK_DESCRIPTION", description, ProfileDetailsActivity.this);
        mDatabase.child("fb_users").child(Profile.getCurrentProfile().getId()).child("extra").setValue(userExtraInfo);
    }

    private void writeBasicInfoToDatabaseEmail(String firstName, String lastName, String photoUrl) {
        User userBasicInfo = new User(firstName, lastName, photoUrl);
        MainActivity.setPreferences("EMAIL_FIRST_NAME", firstName, ProfileDetailsActivity.this);
        MainActivity.setPreferences("EMAIL_LAST_NAME", lastName, ProfileDetailsActivity.this);
        mDatabase.child("email_users").child(mAuth.getUid()).setValue(userBasicInfo);
    }
    private void writeExtraInfoToDatabaseEmail(String birthday, String description) {
        User userExtraInfo = new User(birthday, description);
        MainActivity.setPreferences("EMAIL_BIRTHDAY", birthday, ProfileDetailsActivity.this);
        MainActivity.setPreferences("EMAIL_DESCRIPTION", description, ProfileDetailsActivity.this);
        mDatabase.child("email_users").child(mAuth.getUid()).child("extra").setValue(userExtraInfo);
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
//            final ProgressDialog progressDialog = new ProgressDialog(this);
//            progressDialog.setTitle("Uploading...");
//            progressDialog.show();

            final StorageReference ref = storageReference.child("images/"+mAuth.getUid()+"/"+ "profilePicture.png");
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                            if(MainActivity.isLoggedIn()){
                                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        // Got the download URL for 'users/me/profile.png'
                                        MainActivity.setPreferences("FACEBOOK_PROFILE_PIC",taskSnapshot.getMetadata().getDownloadUrl().toString(),
                                                ProfileDetailsActivity.this);
//                                        Picasso.with(ProfileDetailsActivity.this).load(MainActivity.getPreferences("FACEBOOK_PROFILE_PIC",
//                                                ProfileDetailsActivity.this)).into(profilePic);
//                                        final BlurredAsynctask task = new BlurredAsynctask(ProfileDetailsActivity.this,
//                                                profilePicBackground, 15);
//                                        task.execute(MainActivity.getPreferences("FACEBOOK_PROFILE_PIC",
//                                                ProfileDetailsActivity.this));
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Handle any errors
                                    }
                                });


                            }
//                            progressDialog.dismiss();
                            Toast.makeText(ProfileDetailsActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
//                            progressDialog.dismiss();
                            Toast.makeText(ProfileDetailsActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
//                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
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
                uploadImage();
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                profilePic.setImageBitmap(bitmap);
                profilePicBackground.setImageBitmap(BlurredAsynctask.CreateBlurredImage(bitmap, 15, this));

            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    private boolean validateForm() {
        boolean valid = true;

        if(!MainActivity.isLoggedIn()) {

            String fName = firstNameEt.getText().toString();
            if (TextUtils.isEmpty(fName)) {
                firstNameEt.setError("Required.");
                valid = false;
            } else {
                firstNameEt.setError(null);

            }

            String lName = lastNameEt.getText().toString();
            if (TextUtils.isEmpty(lName)) {
                lastNameEt.setError("Required.");
                valid = false;
            } else {
                lastNameEt.setError(null);
            }
        }


        String birthday = birthdayEt.getText().toString();
        if (TextUtils.isEmpty(birthday)) {
            birthdayEt.setError("Required.");
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
    }
}
