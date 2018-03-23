package com.afoodchronicle.chat;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afoodchronicle.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {
    private final List<Messages> userMessagesList;
    private FirebaseAuth mAuth;

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
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position)
    {
        String message_sender_id = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(position);
        String fromUserId = messages.getFrom();
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

    @Override
    public int getItemCount() {
        return this.userMessagesList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public final TextView messageText;
        public final CircleImageView profileImage;

        public MessageViewHolder(View view) {
            super(view);
            messageText = view.findViewById(R.id.message_text_sender);
            profileImage = view.findViewById(R.id.message_profile_image);
        }
    }
}
