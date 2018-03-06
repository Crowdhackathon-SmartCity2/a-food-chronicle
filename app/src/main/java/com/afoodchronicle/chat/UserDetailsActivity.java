package com.afoodchronicle.chat;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afoodchronicle.R;
import com.afoodchronicle.utilities.ImageLoadedCallback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import static com.afoodchronicle.utilities.Static.AGE;
import static com.afoodchronicle.utilities.Static.DESCRIPTION;
import static com.afoodchronicle.utilities.Static.EMAIL_DESCRIPTION;
import static com.afoodchronicle.utilities.Static.FIRST_NAME;
import static com.afoodchronicle.utilities.Static.LAST_NAME;
import static com.afoodchronicle.utilities.Static.PHOTO_URL;
import static com.afoodchronicle.utilities.Static.USERS;
import static com.afoodchronicle.utilities.Static.VISIT_USER_ID;


public class UserDetailsActivity extends AppCompatActivity {

    private String visit_user_id;
    private Button sendFriendRequest;
    private Button declineFriendRequest;
    private TextView profileName;
    private TextView profileDescription;
    private ImageView profilePhoto;
    private TextView profileAge;
    private DatabaseReference userReference;
    private String firstName;
    private String lastName;
    private String name;
    private String description;
    private String age;
    private String photoUrl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);


        visit_user_id =getIntent().getExtras().get(VISIT_USER_ID).toString();

        userReference = FirebaseDatabase.getInstance().getReference().child(USERS).child(visit_user_id);

        sendFriendRequest = findViewById(R.id.profile_visit_user_friend_request);
        declineFriendRequest = findViewById(R.id.profile_visit_user_decline_request);
        profileName = findViewById(R.id.profile_visit_user_name);
        profileAge = findViewById(R.id.profile_visit_user_age);
        profilePhoto = findViewById(R.id.profile_visit_user_image);
        profileDescription = findViewById(R.id.profile_visit_user_description);

        ProgressBar progressBar = null;
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        final ProgressBar finalProgressBar1 = progressBar;
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                firstName = dataSnapshot.child(FIRST_NAME).getValue().toString();
                lastName = dataSnapshot.child(LAST_NAME).getValue().toString();
                name = firstName + " " + lastName;
                description = dataSnapshot.child(DESCRIPTION).getValue().toString();
                age = dataSnapshot.child(AGE).getValue().toString();
                photoUrl = dataSnapshot.child(PHOTO_URL).getValue().toString();

                profileName.setText(name);
                profileAge.setText(age);
                profileDescription.setText(description);
                    Picasso.with(UserDetailsActivity.this).load(photoUrl).into(profilePhoto,
                            new ImageLoadedCallback(finalProgressBar1) {
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
}
