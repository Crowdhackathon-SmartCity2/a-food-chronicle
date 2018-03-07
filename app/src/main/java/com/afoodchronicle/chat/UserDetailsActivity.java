package com.afoodchronicle.chat;

import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afoodchronicle.R;
import com.afoodchronicle.utilities.ImageLoadedCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import static com.afoodchronicle.utilities.Static.ACCEPT_FRIEND_REQUEST;
import static com.afoodchronicle.utilities.Static.AGE;
import static com.afoodchronicle.utilities.Static.CANCEL_FRIEND_REQUEST;
import static com.afoodchronicle.utilities.Static.DESCRIPTION;
import static com.afoodchronicle.utilities.Static.FIRST_NAME;
import static com.afoodchronicle.utilities.Static.FRIENDS;
import static com.afoodchronicle.utilities.Static.FRIEND_REQUEST;
import static com.afoodchronicle.utilities.Static.LAST_NAME;
import static com.afoodchronicle.utilities.Static.NOT_FRIENDS;
import static com.afoodchronicle.utilities.Static.PHOTO_URL;
import static com.afoodchronicle.utilities.Static.RECEIVED;
import static com.afoodchronicle.utilities.Static.REQUEST_RECEIVED;
import static com.afoodchronicle.utilities.Static.REQUEST_SENT;
import static com.afoodchronicle.utilities.Static.REQUEST_TYPE;
import static com.afoodchronicle.utilities.Static.SEND_FRIEND_REQUEST;
import static com.afoodchronicle.utilities.Static.SENT;
import static com.afoodchronicle.utilities.Static.UNFRIEND;
import static com.afoodchronicle.utilities.Static.USERS;
import static com.afoodchronicle.utilities.Static.VISIT_USER_ID;


public class UserDetailsActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView profileName;
    private TextView profileDescription;
    private ImageView profilePhoto;
    private TextView profileAge;
    private String firstName;
    private String lastName;
    private String name;
    private String description;
    private String age;
    private String photoUrl;
    private String CURRENT_STATE;
    private DatabaseReference FriendRequestReference;
    private FirebaseAuth mAuth;
    String sender_user_id;
    String receiver_user_id;
    Button sendFriendRequestBtn;
    private DatabaseReference FriendsReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_user_details);

        mAuth = FirebaseAuth.getInstance();
        receiver_user_id = getIntent().getExtras().get(VISIT_USER_ID).toString();
        sender_user_id = mAuth.getCurrentUser().getUid();
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child(USERS).child(receiver_user_id);
        FriendRequestReference = FirebaseDatabase.getInstance().getReference().child(FRIEND_REQUEST);
        FriendsReference = FirebaseDatabase.getInstance().getReference().child(FRIENDS);

        CURRENT_STATE = NOT_FRIENDS;


        findViewById(R.id.profile_visit_user_friend_request).setOnClickListener(this);
        findViewById(R.id.profile_visit_user_decline_request).setOnClickListener(this);
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



                FriendRequestReference.child(sender_user_id).
                        addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    if (dataSnapshot.hasChild(receiver_user_id)) {
                                        String req_type = dataSnapshot.child(receiver_user_id).child(REQUEST_TYPE).getValue().toString();

                                        if (req_type.equals(SENT)) {
                                            CURRENT_STATE = REQUEST_SENT;
                                            sendFriendRequestBtn = findViewById(R.id.profile_visit_user_friend_request);
                                            sendFriendRequestBtn.setText(CANCEL_FRIEND_REQUEST);

                                        } else if (req_type.equals(RECEIVED)) {
                                            CURRENT_STATE = REQUEST_RECEIVED;
                                            sendFriendRequestBtn = findViewById(R.id.profile_visit_user_friend_request);
                                            sendFriendRequestBtn.setText(ACCEPT_FRIEND_REQUEST);
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

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onClick(View v)
    {
        int i = v.getId();
        if (i == R.id.profile_visit_user_friend_request)
        {
            sendFriendRequestBtn = findViewById(R.id.profile_visit_user_friend_request);
            sendFriendRequestBtn.setEnabled(false);

            if(CURRENT_STATE.equals(NOT_FRIENDS))
            {
                sendFriendRequestToAPerson();
            }
            if (CURRENT_STATE.equals(REQUEST_SENT))
            {
                cancelFriendRequest();
            }
            if (CURRENT_STATE.equals(REQUEST_RECEIVED))
            {
                acceptFriendRequest();
            }
        }
        else if(i == R.id.profile_visit_user_decline_request)
        {
        }
    }

    private void acceptFriendRequest()
    {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-YYYY");
        final String saveCurrentDate = currentDate.format(cal.getTime());

        FriendsReference.child(sender_user_id).child(receiver_user_id).setValue(saveCurrentDate)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid)
                    {
                        FriendsReference.child(receiver_user_id).child(sender_user_id).setValue(saveCurrentDate)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid)
                                    {
                                        FriendRequestReference.child(sender_user_id).child(receiver_user_id).removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        FriendRequestReference.child(receiver_user_id).child(sender_user_id).removeValue()
                                                                .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                    {
                                                                        if(task.isSuccessful())
                                                                        {
                                                                            sendFriendRequestBtn.setEnabled(true);
                                                                            CURRENT_STATE= FRIENDS;
                                                                            sendFriendRequestBtn.setText(UNFRIEND);
                                                                        }
                                                                    }
                                                                });
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }

    private void cancelFriendRequest()
    {
        FriendRequestReference.child(sender_user_id).child(receiver_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        FriendRequestReference.child(receiver_user_id).child(sender_user_id).removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>()
                                {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        if(task.isSuccessful())
                                        {
                                            sendFriendRequestBtn.setEnabled(true);
                                            CURRENT_STATE= NOT_FRIENDS;
                                            sendFriendRequestBtn.setText(SEND_FRIEND_REQUEST);
                                        }
                                    }
                                });
                    }
                });
    }

    private void sendFriendRequestToAPerson()
    {
        FriendRequestReference.child(sender_user_id).child(receiver_user_id).child(REQUEST_TYPE).setValue(SENT)
        .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
            if (task.isSuccessful())
             {
                FriendRequestReference.child(receiver_user_id).child(sender_user_id).child(REQUEST_TYPE).setValue(RECEIVED)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task)
                            {
                                if(task.isSuccessful())
                                {
                                    sendFriendRequestBtn.setEnabled(true);
                                    CURRENT_STATE = REQUEST_SENT;
                                    sendFriendRequestBtn.setText(CANCEL_FRIEND_REQUEST);
                                }
                            }
                        });
             }
            }
        });
    }
}
