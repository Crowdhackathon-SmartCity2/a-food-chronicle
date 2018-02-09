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


public class ProfileDetailsActivity extends AppCompatActivity implements View.OnClickListener {

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
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        mAuthFacebook = LoginManager.getInstance();
        findViewById(R.id.btnLog_out).setOnClickListener(this);
        findViewById(R.id.btnChoose).setOnClickListener(this);
        findViewById(R.id.btnUpload).setOnClickListener(this);
        findViewById(R.id.btnSave).setOnClickListener(this);
        findViewById(R.id.etBirthday).setOnClickListener(this);

        profilePic = findViewById(R.id.profile_pic_details);

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
                    if (MainActivity.isLoggedIn()) {

                        firstNameEt.setVisibility(View.GONE);
                        facebookFirstName.setVisibility(View.VISIBLE);
                        lastNameEt.setVisibility(View.GONE);
                        facebookLastName.setVisibility(View.VISIBLE);

                        DatabaseReference facebookRef = FirebaseDatabase.getInstance().getReference("fb_users");
                        facebookRef.addValueEventListener(new ValueEventListener()

                        {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                for (DataSnapshot ds : dataSnapshot.getChildren())

                                {
                                    String firstName = ds.child("firstName").getValue(String.class);
                                    String lastName = ds.child("lastName").getValue(String.class);
                                    String facebookPhotoUrl = ds.child("photoUrl").getValue(String.class);
                                    facebookFirstName.setText(firstName);
                                    facebookLastName.setText(lastName);
                                    Picasso.with(ProfileDetailsActivity.this).load(facebookPhotoUrl).into(profilePic);

                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                } else {
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
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String myFormat = "MM/dd/yy"; //In which you need put here
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

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

    private void saveDetails(){

        firstNameEmail =firstNameEt.getText().toString().trim();
        firstNameFacebook = facebookFirstName.getText().toString().trim();

        lastNameEmail = lastNameEt.getText().toString().trim();
        lastNameFacebook = facebookLastName.getText().toString().trim();

        birthday = birthdayEt.getText().toString().trim();
        description= descriptionEt.getText().toString().trim();

//        emailPhotoUrl = String.valueOf(storageReference.child("images/"+ mAuth.getUid() +"/profilePicture.png").getDownloadUrl().getResult());

        // Facebook
        if(MainActivity.isLoggedIn()){
            writeExtraInfoToDatabaseFacebook(birthday, description);
        }
        // Email
        else
        {
            writeBasicInfoToDatabaseEmail(firstNameEmail, lastNameEmail, emailPhotoUrl);
            writeExtraInfoToDatabaseEmail(birthday, description);
        }
        Intent i = new Intent(ProfileDetailsActivity.this, MainActivity.class);
        startActivity(i);
    }

    private void writeExtraInfoToDatabaseFacebook(String birthday, String description) {
        User userExtraInfo = new User(birthday, description);
        mDatabase.child("fb_users").child(Profile.getCurrentProfile().getId()).child("extra").setValue(userExtraInfo);
    }

    private void writeBasicInfoToDatabaseEmail(String firstName, String lastName, String photoUrl) {
        User userBasicInfo = new User(firstName, lastName, photoUrl);
        mDatabase.child("email_users").child(mAuth.getUid()).setValue(userBasicInfo);
    }
    private void writeExtraInfoToDatabaseEmail(String birthday, String description) {
        User userExtraInfo = new User(birthday, description);
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
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = storageReference.child("images/"+mAuth.getUid()+"/"+ "profilePicture.png".toString());
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(ProfileDetailsActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(ProfileDetailsActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                profilePic.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
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
        else if(i == R.id.btnUpload){
            uploadImage();
        }
        else if(i == R.id.btnSave){
            saveDetails();
        }
        else if(i == R.id.etBirthday){
            setBirthday();
        }
    }
}
