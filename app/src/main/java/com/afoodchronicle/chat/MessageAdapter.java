package com.afoodchronicle.chat;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.afoodchronicle.R;
import com.afoodchronicle.User;
import com.afoodchronicle.utilities.FacebookUtils;
import com.afoodchronicle.utilities.PreferenceUtils;

import java.util.List;

import static com.afoodchronicle.utilities.Static.EMAIL_FIRST_NAME;
import static com.afoodchronicle.utilities.Static.EMAIL_LAST_NAME;
import static com.afoodchronicle.utilities.Static.FACEBOOK_FIRST_NAME;
import static com.afoodchronicle.utilities.Static.FACEBOOK_LAST_NAME;

class MessageAdapter extends ArrayAdapter<User> {

    public MessageAdapter(Context context, int resource, List<User> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_message, parent, false);
        }

       // ImageView photoImageView = convertView.findViewById(R.id.photoImageView);
        TextView messageTextView = convertView.findViewById(R.id.messageTextView);


        User message = getItem(position);

        boolean isPhoto = message.getPhotoUrl() != null;
        if (isPhoto) {
            messageTextView.setVisibility(View.GONE);
//            photoImageView.setVisibility(View.VISIBLE);
//            Glide.with(photoImageView.getContext())
//                    .load(message.getPhotoUrl())
//                    .into(photoImageView);
        } else {
            messageTextView.setVisibility(View.VISIBLE);
//            photoImageView.setVisibility(View.GONE);
            messageTextView.setText(message.getText());
        }
        if(FacebookUtils.isLoggedIn()){

            TextView fbTextView = convertView.findViewById(R.id.username);
            fbTextView.setText(PreferenceUtils.getPreferences(FACEBOOK_FIRST_NAME, getContext()) + " "
                    + PreferenceUtils.getPreferences(FACEBOOK_LAST_NAME, getContext()) );
        }
        else {

            TextView emailTextView = convertView.findViewById(R.id.username);
            emailTextView.setText(PreferenceUtils.getPreferences(EMAIL_FIRST_NAME, getContext()) + " "
                    + PreferenceUtils.getPreferences(EMAIL_LAST_NAME, getContext()));
        }
        return convertView;
    }
}