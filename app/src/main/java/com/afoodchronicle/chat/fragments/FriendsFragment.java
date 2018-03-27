package com.afoodchronicle.chat.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afoodchronicle.R;
import com.afoodchronicle.chat.AllUsersDetailsActivity;
import com.afoodchronicle.chat.ChatActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.afoodchronicle.utilities.Static.AGE;
import static com.afoodchronicle.utilities.Static.FIRST_NAME;
import static com.afoodchronicle.utilities.Static.FRIENDS;
import static com.afoodchronicle.utilities.Static.FRIENDS_SINCE;
import static com.afoodchronicle.utilities.Static.FULL_NAME;
import static com.afoodchronicle.utilities.Static.LAST_NAME;
import static com.afoodchronicle.utilities.Static.ONLINE;
import static com.afoodchronicle.utilities.Static.THUMB_PHOTO_URL;
import static com.afoodchronicle.utilities.Static.TRUE;
import static com.afoodchronicle.utilities.Static.USERS;
import static com.afoodchronicle.utilities.Static.VISIT_USER_ID;

public class FriendsFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private RecyclerView myFriendsList;
    private DatabaseReference friendsReference;
    private String online_user_id;
    private DatabaseReference userReference;
    private FirebaseAuth mAuth;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View myMainView = inflater.inflate(R.layout.fragment_friends, container, false);

        myFriendsList = myMainView.findViewById(R.id.friends_list);
        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();

        friendsReference = FirebaseDatabase.getInstance().getReference().child(FRIENDS).child(online_user_id);
        userReference = FirebaseDatabase.getInstance().getReference().child(USERS);

        myFriendsList.setHasFixedSize(true);
        myFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inflate the layout for this fragment
        return myMainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        userReference.child(online_user_id).child(ONLINE).setValue(true);
        FirebaseRecyclerOptions<Friends> options =
                new FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery(friendsReference, Friends.class)
                        .build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder> (options)
        {
            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_users_display_layout, parent, false);

                return new FriendsViewHolder (view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder viewHolder, int position, @NonNull Friends model)
            {
               viewHolder.setFriend_date(FRIENDS_SINCE + model.getDate());

               final String list_user_id = getRef(position).getKey();

               userReference.child(list_user_id).addValueEventListener(new ValueEventListener()
               {
                   @Override
                   public void onDataChange(final DataSnapshot dataSnapshot)
                   {
                       final String fName =dataSnapshot.child(FIRST_NAME).getValue().toString();
                       final String lName =dataSnapshot.child(LAST_NAME).getValue().toString();
                       String thumbImage =dataSnapshot.child(THUMB_PHOTO_URL).getValue().toString();
                       String age =dataSnapshot.child(AGE).getValue().toString();
                       if(dataSnapshot.hasChild(ONLINE))
                       {
                           String online_status = dataSnapshot.child(ONLINE).getValue().toString();
                           viewHolder.setFriendOnline(online_status);
                       }

                       viewHolder.setFriend_age(age);
                       viewHolder.setFriend_name(fName + " " + lName);
                       viewHolder.setFriend_thumbImage(thumbImage, getContext());

                       viewHolder.mView.setOnClickListener(new View.OnClickListener()
                       {
                           @Override
                           public void onClick(View v)
                           {
                               AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                               builder.setTitle("Choose an action");

                               // add the buttons
                               builder.setPositiveButton(fName + "'s Profile" , new DialogInterface.OnClickListener() {
                                   @Override
                                   public void onClick(DialogInterface dialog, int which) {

                                       Intent profileIntent = new Intent(getContext(), AllUsersDetailsActivity.class);
                                       profileIntent.putExtra(VISIT_USER_ID, list_user_id);
                                       startActivity(profileIntent);

                                   }
                               });
                               builder.setNegativeButton("Send message", new DialogInterface.OnClickListener() {
                                   @Override
                                   public void onClick(DialogInterface dialog, int which) {

                                       if(dataSnapshot.child(ONLINE).exists())
                                       {
                                           Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                           chatIntent.putExtra(VISIT_USER_ID, list_user_id);
                                           chatIntent.putExtra(FULL_NAME, fName + " " + lName);
                                           startActivity(chatIntent);
                                       }
                                       else
                                       {
                                           userReference.child(list_user_id).child(ONLINE)
                                                   .setValue(ServerValue.TIMESTAMP).addOnSuccessListener(new OnSuccessListener<Void>() {
                                               @Override
                                               public void onSuccess(Void aVoid)
                                               {
                                                   Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                                   chatIntent.putExtra(VISIT_USER_ID, list_user_id);
                                                   chatIntent.putExtra(FULL_NAME, fName + " " + lName);
                                                   startActivity(chatIntent);
                                               }
                                           });
                                       }
                                   }
                               });
                               builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                                   @Override
                                   public void onClick(DialogInterface dialog, int which) {

                                       // do something like...

                                   }
                               });

                               // create and show the alert dialog
                               AlertDialog dialog = builder.create();
                               dialog.show();

                           }
                       });
                   }

                   @Override
                   public void onCancelled(DatabaseError databaseError) {

                   }
               });
            }
        };
        myFriendsList.setAdapter(adapter);
        adapter.startListening();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mAuth.getCurrentUser().getUid() != null)
        {
            userReference.child(online_user_id).child(ONLINE).setValue(ServerValue.TIMESTAMP);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private static class FriendsViewHolder extends RecyclerView.ViewHolder{
        final View mView;

        FriendsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }
        void setFriend_date(String friends_date)
        {
            TextView friendsSinceDate =  mView.findViewById(R.id.all_users_description);
            friendsSinceDate.setText(friends_date);
        }
        void setFriend_age(String friends_age)
        {
            TextView ageView = mView.findViewById(R.id.all_users_age);
            ageView.setText(friends_age);
        }
        void setFriend_name(String friend_name)
        {
            TextView name = mView.findViewById(R.id.all_users_username);
            name.setText(friend_name);
        }
        void setFriend_thumbImage(final String friend_thumb_image, final Context context)
        {
            final CircleImageView image = mView.findViewById(R.id.all_users_profile_image);
            //
            Picasso.with(context).load(friend_thumb_image).networkPolicy(NetworkPolicy.OFFLINE).into(image, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError()
            {
                Picasso.with(context).load(friend_thumb_image).into(image);
            }
        });
        }

        void setFriendOnline(String online_status)
        {
                {
                    ImageView onlineStatusView = mView.findViewById(R.id.all_users_online_status);
                    if (online_status.equals(TRUE))
                    onlineStatusView.setVisibility(View.VISIBLE);
            }
        }
    }
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
