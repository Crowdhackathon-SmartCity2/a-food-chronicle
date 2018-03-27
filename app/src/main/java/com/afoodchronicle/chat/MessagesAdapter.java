package com.afoodchronicle.chat;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afoodchronicle.R;
import com.afoodchronicle.utilities.ImageLoadedCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.afoodchronicle.utilities.Static.FIRST_NAME;
import static com.afoodchronicle.utilities.Static.TEXT;
import static com.afoodchronicle.utilities.Static.THUMB_PHOTO_URL;
import static com.afoodchronicle.utilities.Static.USERS;


public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {
    private final List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersReference;

    public MessagesAdapter(List<Messages> userMessagesList)
    {
        this.userMessagesList=userMessagesList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.messages_layout_user, parent, false);
        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position)
    {
        String message_sender_id = mAuth.getCurrentUser().getUid();
        final Messages messages = userMessagesList.get(position);
        String fromUserId = messages.getFrom();
        String fromMessageType = messages.getType();

        usersReference = FirebaseDatabase.getInstance().getReference().child(USERS).child(fromUserId);
        usersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String userName = dataSnapshot.child(FIRST_NAME).getValue().toString();
                String userImage = dataSnapshot.child(THUMB_PHOTO_URL).getValue().toString();
                Picasso.with(holder.profileImage.getContext()).load(userImage).into(holder.profileImage,
                        new ImageLoadedCallback(holder.progressBar)
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

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        if (fromMessageType.equals(TEXT))
        {
            holder.chatImage.setVisibility(View.GONE);
            if (fromUserId.equals(message_sender_id))
            {
                holder.messageText.setBackgroundResource(R.drawable.sender_message_text_background);
                holder.messageText.setGravity(Gravity.END);
            }
            else
            {
                holder.messageText.setBackgroundResource(R.drawable.receiver_message_text_background);
                holder.messageText.setGravity(Gravity.START);
            }
            holder.messageText.setText(messages.getMessages());
        }
        else
        {

            holder.progressBar.setVisibility(View.VISIBLE);
            holder.messageText.setVisibility(View.GONE);
            Picasso.with(holder.profileImage.getContext()).load(messages.getMessages()).into(holder.chatImage,
                    new ImageLoadedCallback(holder.progressBar)
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

    }

    @Override
    public int getItemCount() {
        return this.userMessagesList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public final TextView messageText;
        public final CircleImageView profileImage;
        public final ImageView chatImage;
        public ProgressBar progressBar;

        public MessageViewHolder(View view) {
            super(view);
            messageText = view.findViewById(R.id.message_text_sender);
            profileImage = view.findViewById(R.id.message_profile_image);
            chatImage = view.findViewById(R.id.message_chat_image);
            progressBar = view.findViewById(R.id.message_progressbar);
        }
    }
}
