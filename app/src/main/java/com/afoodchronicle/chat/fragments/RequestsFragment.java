package com.afoodchronicle.chat.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afoodchronicle.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.afoodchronicle.utilities.Static.DATE;
import static com.afoodchronicle.utilities.Static.DESCRIPTION;
import static com.afoodchronicle.utilities.Static.FIRST_NAME;
import static com.afoodchronicle.utilities.Static.FRIENDS;
import static com.afoodchronicle.utilities.Static.FRIEND_REQUEST;
import static com.afoodchronicle.utilities.Static.LAST_NAME;
import static com.afoodchronicle.utilities.Static.ONLINE;
import static com.afoodchronicle.utilities.Static.RECEIVED;
import static com.afoodchronicle.utilities.Static.REQUEST_TYPE;
import static com.afoodchronicle.utilities.Static.SENT;
import static com.afoodchronicle.utilities.Static.THUMB_PHOTO_URL;
import static com.afoodchronicle.utilities.Static.USERS;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RequestsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class RequestsFragment extends Fragment {

    private FriendsFragment.OnFragmentInteractionListener mListener;
    private RecyclerView myRequestsList;
    private DatabaseReference friendsNodeReference;
    private String online_user_id;
    private DatabaseReference userReference;
    private DatabaseReference friendRequestsDetailedReference;
    private DatabaseReference friendRequestsGeneralReference;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View myMainView = inflater.inflate(R.layout.fragment_requests, container, false);

        myRequestsList = myMainView.findViewById(R.id.requests_list);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();

        friendsNodeReference = FirebaseDatabase.getInstance().getReference().child(FRIENDS);
        userReference = FirebaseDatabase.getInstance().getReference().child(USERS);
        friendRequestsDetailedReference = FirebaseDatabase.getInstance().getReference().child(FRIEND_REQUEST).child(online_user_id);
        friendRequestsGeneralReference = FirebaseDatabase.getInstance().getReference().child(FRIEND_REQUEST);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager((getContext()));
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        myRequestsList.setLayoutManager(linearLayoutManager);

        // Inflate the layout for this fragment
        return myMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        userReference.child(online_user_id).child(ONLINE).setValue(true);
        FirebaseRecyclerOptions<Requests> options =
                new FirebaseRecyclerOptions.Builder<Requests>()
                        .setQuery(friendRequestsDetailedReference, Requests.class)
                        .build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Requests, RequestsFragment.RequestsViewHolder> (options)
        {
            @NonNull
            @Override
            public RequestsFragment.RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.request_users_display_layout, parent, false);

                return new RequestsFragment.RequestsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final RequestsFragment.RequestsViewHolder viewHolder, int position,
                                            @NonNull Requests model)
            {

                final String list_user_id = getRef(position).getKey();

                DatabaseReference getTypeRef = getRef(position).child(REQUEST_TYPE).getRef();
                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists())
                        {
                            String requestType = dataSnapshot.getValue().toString();
                            if (requestType.equals(RECEIVED))
                            {


                                userReference.child(list_user_id).addValueEventListener(new ValueEventListener()
                                {
                                    @Override
                                    public void onDataChange(final DataSnapshot dataSnapshot)
                                    {
                                        final String fName =dataSnapshot.child(FIRST_NAME).getValue().toString();
                                        final String lName =dataSnapshot.child(LAST_NAME).getValue().toString();
                                        String thumbImage =dataSnapshot.child(THUMB_PHOTO_URL).getValue().toString();
                                        String description = dataSnapshot.child(DESCRIPTION).getValue().toString();
                                        if (!description.equals("")) {
                                            viewHolder.setFriend_status(description);
                                        }
                                        viewHolder.setFriend_name(fName + " " + lName);
                                        viewHolder.setFriend_thumbImage(thumbImage, getContext());

                                        Button acceptBtn =viewHolder.mView.findViewById(R.id.requests_accept_button);
                                        Button declineBtn =viewHolder.mView.findViewById(R.id.requests_decline_button);

                                        acceptBtn.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        Calendar cal = Calendar.getInstance();
                                                        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-YYYY");
                                                        final String saveCurrentDate = currentDate.format(cal.getTime());

                                                        friendsNodeReference.child(online_user_id).child(list_user_id).child(DATE).setValue(saveCurrentDate)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid)
                                                                    {
                                                                        friendsNodeReference.child(list_user_id).child(online_user_id).child(DATE).setValue(saveCurrentDate)
                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void aVoid)
                                                                                    {
                                                                                        friendRequestsGeneralReference.child(online_user_id).child(list_user_id).removeValue()
                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                        friendRequestsGeneralReference.child(list_user_id).child(online_user_id).removeValue()
                                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                                                                {
                                                                                                                    @Override
                                                                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                                                                    {
                                                                                                                        if(task.isSuccessful())
                                                                                                                        {
//                                                                                                                            Context context = getContext();
//                                                                                                                            CharSequence text = "Friend accepted.";
//                                                                                                                            int duration = Toast.LENGTH_SHORT;
//
//                                                                                                                            Toast toast = Toast.makeText(context, text, duration);
//                                                                                                                            toast.show();
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
                                                });
                                        declineBtn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                friendRequestsGeneralReference.child(online_user_id).child(list_user_id).removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                friendRequestsGeneralReference.child(list_user_id).child(online_user_id).removeValue()
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                        {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task)
                                                                            {
                                                                                if(task.isSuccessful())
                                                                                {
                                                                                    Toast.makeText(getContext(), "Friend request canceled.", Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            }
                                                                        });
                                                            }
                                                        });
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });


                            }
                            else if (requestType.equals(SENT))
                            {
                                final Button reqSentBtn = viewHolder.mView.findViewById(R.id.requests_accept_button);
                                reqSentBtn.setText("Request Sent");
                                viewHolder.mView.findViewById(R.id.requests_decline_button).setVisibility(View.GONE);

                                userReference.child(list_user_id).addValueEventListener(new ValueEventListener()
                                {
                                    @Override
                                    public void onDataChange(final DataSnapshot dataSnapshot)
                                    {
                                        final String fName =dataSnapshot.child(FIRST_NAME).getValue().toString();
                                        final String lName =dataSnapshot.child(LAST_NAME).getValue().toString();
                                        String thumbImage =dataSnapshot.child(THUMB_PHOTO_URL).getValue().toString();
                                        String description = dataSnapshot.child(DESCRIPTION).getValue().toString();
                                        if (!description.equals("")) {
                                            viewHolder.setFriend_status(description);
                                        }
                                        viewHolder.setFriend_name(fName + " " + lName);
                                        viewHolder.setFriend_thumbImage(thumbImage, getContext());

                                        reqSentBtn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {


                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle("Choose an action");

                                                builder.setNeutralButton("Cancel Friend Request", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                        friendRequestsGeneralReference.child(online_user_id).child(list_user_id).removeValue()
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        friendRequestsGeneralReference.child(list_user_id).child(online_user_id).removeValue()
                                                                                .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                                {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                                    {
                                                                                        if(task.isSuccessful())
                                                                                        {
                                                                                            Toast.makeText(getContext(), "Friend request canceled.", Toast.LENGTH_SHORT).show();
                                                                                        }
                                                                                    }
                                                                                });
                                                                    }
                                                                });
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
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        myRequestsList.setAdapter(adapter);
        adapter.startListening();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FriendsFragment.OnFragmentInteractionListener) {
            mListener = (FriendsFragment.OnFragmentInteractionListener) context;
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

    private static class RequestsViewHolder extends RecyclerView.ViewHolder{
        final View mView;

        RequestsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

         void setFriend_name(String friend_name)
        {
            TextView name = mView.findViewById(R.id.requests_profile_name);
            name.setText(friend_name);
        }
        void setFriend_thumbImage(final String friend_thumb_image, final Context context)
        {
            final CircleImageView image = mView.findViewById(R.id.requests_profile_image);
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

        public void setFriend_status(String userStatus) {
            TextView userStatusTV = mView.findViewById(R.id.requests_profile_desc);
            userStatusTV.setText(userStatus);
        }
    }
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
