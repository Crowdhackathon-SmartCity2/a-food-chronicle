package com.afoodchronicle.chat;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

import com.afoodchronicle.R;
import com.afoodchronicle.utilities.LastSeenTime;
import com.afoodchronicle.utilities.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.PendingIntent.getActivity;
import static com.afoodchronicle.utilities.Static.FULL_NAME;
import static com.afoodchronicle.utilities.Static.MESSAGE;
import static com.afoodchronicle.utilities.Static.MESSAGES;
import static com.afoodchronicle.utilities.Static.ONLINE;
import static com.afoodchronicle.utilities.Static.SEEN;
import static com.afoodchronicle.utilities.Static.TEXT;
import static com.afoodchronicle.utilities.Static.THUMB_PHOTO_URL;
import static com.afoodchronicle.utilities.Static.TIME;
import static com.afoodchronicle.utilities.Static.TRUE;
import static com.afoodchronicle.utilities.Static.TYPE;
import static com.afoodchronicle.utilities.Static.USERS;
import static com.afoodchronicle.utilities.Static.VISIT_USER_ID;


public class ChatActivity extends AppCompatActivity implements View.OnClickListener{
    private TextView userLastSeen;
    private CircleImageView userChatProfileImage;
    private EditText inputMessageText;
    private ImageButton sendMessageButton;
    private ImageButton selectImageButton;
    private String messageSenderId;
    private FirebaseAuth mAuth;
    private String messageReceiverId;
    private String messageReceiverName;
    private DatabaseReference rootRef;
    private RecyclerView userMessageList;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_window);
        mAuth = FirebaseAuth.getInstance();
        messageSenderId= mAuth.getCurrentUser().getUid();

        messageReceiverId = getIntent().getExtras().get(VISIT_USER_ID).toString();
        messageReceiverName = getIntent().getExtras().get(FULL_NAME).toString();

        Toolbar chatToolBar = findViewById(R.id.chat_custom_bar);
        setSupportActionBar(chatToolBar);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView  = layoutInflater.inflate(R.layout.chat_custom_bar, null);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setCustomView(actionBarView);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);



        TextView userNameTitle = findViewById(R.id.custom_chat_username);
        userLastSeen = findViewById(R.id.custom_user_last_seen);
        userChatProfileImage = findViewById(R.id.custom_user_profile_image);
        inputMessageText = findViewById(R.id.messageEt);
        sendMessageButton = findViewById(R.id.send_button);
        selectImageButton = findViewById(R.id.add_photo);
        userMessageList = findViewById(R.id.recycler_view_messages);
        findViewById(R.id.send_button).setOnClickListener(this);
        findViewById(R.id.add_photo).setOnClickListener(this);

        messagesAdapter = new MessagesAdapter(messagesList);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessageList.setHasFixedSize(true);
        userMessageList.setLayoutManager(linearLayoutManager);
        userMessageList.setAdapter(messagesAdapter);

        fetchMessages();

        userNameTitle.setText(messageReceiverName);
        rootRef = FirebaseDatabase.getInstance().getReference();

        
        rootRef.child(USERS).child(messageReceiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String online = dataSnapshot.child(ONLINE).getValue().toString();
                final String userThumb = dataSnapshot.child(THUMB_PHOTO_URL).getValue().toString();
                Picasso.with(ChatActivity.this).load(userThumb).networkPolicy(NetworkPolicy.OFFLINE).into(userChatProfileImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError()
                    {
                        Picasso.with(ChatActivity.this).load(userThumb).into(userChatProfileImage);
                    }
                });
            if (online.equals(TRUE))
            {
                userLastSeen.setText(ONLINE);
            }
            else
            {
                LastSeenTime getTime = new LastSeenTime();
                long lastSeen = Long.parseLong(online);
                String lastSeenDisplayTime = getTime.getTimeAgo(lastSeen, getApplicationContext()).toString();
                userLastSeen.setText(lastSeenDisplayTime);
            }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void fetchMessages()
    {
        FirebaseDatabase.getInstance().getReference().child(MESSAGES).child(messageSenderId).child(messageReceiverId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Messages messages = dataSnapshot.getValue(Messages.class);
                    messagesList.add(messages);
                    messagesAdapter.notifyDataSetChanged();


            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View view) {

        int i = view.getId();
        if (i == R.id.send_button)
        {
           sendMessage();
        }
        else if (i == R.id.add_photo)
        {

        }
    }

    private void sendMessage()
    {
        String messageText = inputMessageText.getText().toString();
        if (TextUtils.isEmpty(messageText))
        {
            sendMessageButton.setEnabled(false);
        }
        else
        {
            String messageSenderRef = MESSAGES + "/" +messageSenderId + "/" + messageReceiverId;
            String messageReceiverRef = MESSAGES +"/" + messageReceiverId + "/" + messageSenderId;
            DatabaseReference userMessageKey =  rootRef.child(MESSAGES).child(messageSenderId)
                    .child(messageReceiverId).push();
            String messagePushId=  userMessageKey.getKey();


            Map messageTextBody = new HashMap();
            messageTextBody.put(MESSAGE, messageText);
            messageTextBody.put(SEEN, false);
            messageTextBody.put(TYPE, TEXT);
            messageTextBody.put(TIME, ServerValue.TIMESTAMP);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/"+ messagePushId, messageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/" + messagePushId, messageTextBody);

            rootRef.updateChildren(messageBodyDetails, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference)
                {
                    if (databaseError != null)
                    {
                        Log.d("Chat Log", databaseError.getMessage().toString());
                    }

                    inputMessageText.setText("");
                }
            });


        }
    }
}
