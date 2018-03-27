package com.afoodchronicle.chat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.afoodchronicle.R;
import com.afoodchronicle.firebase.ProfileDetailsActivity;
import com.afoodchronicle.utilities.FacebookUtils;
import com.afoodchronicle.utilities.LastSeenTime;
import com.afoodchronicle.utilities.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

import static com.afoodchronicle.utilities.Static.EMAIL_THUMB_PROFILE_PIC;
import static com.afoodchronicle.utilities.Static.FACEBOOK_THUMB_PROFILE_PIC;
import static com.afoodchronicle.utilities.Static.FROM;
import static com.afoodchronicle.utilities.Static.FULL_NAME;
import static com.afoodchronicle.utilities.Static.IMAGE;
import static com.afoodchronicle.utilities.Static.IMAGES;
import static com.afoodchronicle.utilities.Static.JPG;
import static com.afoodchronicle.utilities.Static.MESSAGE;
import static com.afoodchronicle.utilities.Static.MESSAGES;
import static com.afoodchronicle.utilities.Static.MESSAGES_PICTURES;
import static com.afoodchronicle.utilities.Static.ONLINE;
import static com.afoodchronicle.utilities.Static.PICK_IMAGE_REQUEST;
import static com.afoodchronicle.utilities.Static.SEEN;
import static com.afoodchronicle.utilities.Static.TEXT;
import static com.afoodchronicle.utilities.Static.THUMB_IMAGES;
import static com.afoodchronicle.utilities.Static.THUMB_PHOTO_URL;
import static com.afoodchronicle.utilities.Static.TIME;
import static com.afoodchronicle.utilities.Static.TRUE;
import static com.afoodchronicle.utilities.Static.TYPE;
import static com.afoodchronicle.utilities.Static.UPLOAD;
import static com.afoodchronicle.utilities.Static.UPLOAD_ERROR;
import static com.afoodchronicle.utilities.Static.USERS;
import static com.afoodchronicle.utilities.Static.VISIT_USER_ID;


public class ChatActivity extends AppCompatActivity implements View.OnClickListener{
    private TextView userLastSeen;
    private CircleImageView userChatProfileImage;
    private EditText inputMessageText;
    private ImageButton sendMessageButton;
    private ImageButton selectImageButton;
    private String messageSenderId;
    private String messageReceiverId;
    private String messageReceiverName;
    private DatabaseReference rootRef;
    private final List<Messages> messagesList = new ArrayList<>();
    private MessagesAdapter messagesAdapter;
    private StorageReference messageImageStorageRef;
    private ProgressDialog loadingBar;
    private Bitmap thumb_bitmap;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_window);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        messageSenderId= mAuth.getCurrentUser().getUid();

        messageReceiverId = getIntent().getExtras().get(VISIT_USER_ID).toString();
        messageReceiverName = getIntent().getExtras().get(FULL_NAME).toString();
        messageImageStorageRef = FirebaseStorage.getInstance().getReference().child(MESSAGES_PICTURES);
        Toolbar chatToolBar = findViewById(R.id.chat_custom_bar);
        setSupportActionBar(chatToolBar);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final ViewGroup nullParent = null;
        View actionBarView  = layoutInflater.inflate(R.layout.chat_custom_bar, nullParent);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setCustomView(actionBarView);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        loadingBar = new ProgressDialog(this);


        TextView userNameTitle = findViewById(R.id.custom_chat_username);
        userLastSeen = findViewById(R.id.custom_user_last_seen);
        userChatProfileImage = findViewById(R.id.custom_user_prof_image);
        inputMessageText = findViewById(R.id.messageEt);
        sendMessageButton = findViewById(R.id.send_button);
        selectImageButton = findViewById(R.id.add_photo);
        RecyclerView userMessageList = findViewById(R.id.recycler_view_messages);
        findViewById(R.id.send_button).setOnClickListener(this);
        findViewById(R.id.add_photo).setOnClickListener(this);

        messagesAdapter = new MessagesAdapter(messagesList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
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
                String lastSeenDisplayTime = LastSeenTime.getTimeAgo(lastSeen);
                userLastSeen.setText(lastSeenDisplayTime);
            }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                    && data != null && data.getData() != null) {
                loadingBar.setTitle("Sending chat image");
                loadingBar.setMessage("Please wait...");
                loadingBar.show();
                Uri filePath = data.getData();

                final String messageSenderRef = MESSAGES + "/" + messageSenderId + "/" + messageReceiverId;
                final String messageReceiverRef = MESSAGES + "/" + messageReceiverId + "/" + messageSenderId;
                DatabaseReference userMessageKey = rootRef.child(MESSAGES).child(messageSenderId)
                        .child(messageReceiverId).push();
                final String messagePushId = userMessageKey.getKey();

                StorageReference filePathStorage = messageImageStorageRef.child(messagePushId + ".jpg");
                if (resultCode == RESULT_OK)

                {

                    try {

                        thumb_bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), filePath);


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    Utils.resize(thumb_bitmap, 1000, 1000).compress(Bitmap.CompressFormat.JPEG, 40, byteArrayOutputStream);
                    final byte[] thumb_byte = byteArrayOutputStream.toByteArray();

                    UploadTask uploadTask = filePathStorage.putBytes(thumb_byte);
                    uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {


                            if (thumb_task.isSuccessful()) {
                                String downloadUrl = thumb_task.getResult().getDownloadUrl().toString();


                                Map messageTextBody = new HashMap();
                                messageTextBody.put(MESSAGE, downloadUrl);
                                messageTextBody.put(SEEN, false);
                                messageTextBody.put(TYPE, IMAGE);
                                messageTextBody.put(TIME, ServerValue.TIMESTAMP);
                                messageTextBody.put(FROM, messageSenderId);

                                Map messageBodyDetails = new HashMap();
                                messageBodyDetails.put(messageSenderRef + "/" + messagePushId, messageTextBody);
                                messageBodyDetails.put(messageReceiverRef + "/" + messagePushId, messageTextBody);

                                rootRef.updateChildren(messageBodyDetails, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                        inputMessageText.setText("");
                                        loadingBar.dismiss();
                                    }
                                });
                                loadingBar.dismiss();
                            }

                        }
                    });
                }

            }
        }
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
            selectImage();
        }
    }

    private void selectImage() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);

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
            messageTextBody.put(FROM, messageSenderId);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/"+ messagePushId, messageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/" + messagePushId, messageTextBody);

            rootRef.updateChildren(messageBodyDetails, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference)
                {
                    if (databaseError != null)
                    {
                        Log.d("Chat Log", databaseError.getMessage());
                    }

                    inputMessageText.setText("");
                }
            });
        }
    }
}
