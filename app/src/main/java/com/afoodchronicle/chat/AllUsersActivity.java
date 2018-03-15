package com.afoodchronicle.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afoodchronicle.R;
import com.afoodchronicle.firebase.FirebaseUser;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.afoodchronicle.utilities.Static.USERS;
import static com.afoodchronicle.utilities.Static.VISIT_USER_ID;


public class AllUsersActivity extends AppCompatActivity {

    private RecyclerView allUsersList;
    private DatabaseReference allDatabaseUsersReference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);
        mAuth = FirebaseAuth.getInstance();
        allUsersList = findViewById(R.id.all_users_list);
        allUsersList.setHasFixedSize(true);
        allUsersList.setLayoutManager(new LinearLayoutManager(this));
        allDatabaseUsersReference = FirebaseDatabase.getInstance().getReference().child(USERS);
    }

    @Override
    protected void onStart(){
        super.onStart();

        FirebaseRecyclerOptions<FirebaseUser> options =
                new FirebaseRecyclerOptions.Builder<FirebaseUser>()
                        .setQuery(allDatabaseUsersReference, FirebaseUser.class)
                        .build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<FirebaseUser, AllUsersViewHolder> (options)
            {
            @NonNull
            @Override
            public AllUsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_users_display_layout, parent, false);

                return new AllUsersViewHolder (view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final AllUsersViewHolder viewHolder, int position, @NonNull FirebaseUser model)
            {
                viewHolder.setUser_name(model.getFirstName()+ " " + model.getLastName());
                viewHolder.setUser_description(model.getDescription());
                viewHolder.setUser_thumbImage(getApplicationContext(), model.getThumbPhotoUrl());
                viewHolder.setUser_age("Age: " + model.getAge());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        String visitUserId = getRef(viewHolder.getAdapterPosition()).getKey();
                        Intent userDetailsIntent = new Intent (AllUsersActivity.this, AllUsersDetailsActivity.class);
                        userDetailsIntent.putExtra(VISIT_USER_ID, visitUserId);
                        startActivity(userDetailsIntent);
                    }
                });
            }
        };
        allUsersList.setAdapter(adapter);
        adapter.startListening();
    }

    private static class AllUsersViewHolder extends RecyclerView.ViewHolder{

        private View mView;
        private TextView name;
        private TextView description;

        private TextView age;

        AllUsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }
        void setUser_name(String user_name)
        {
            name = mView.findViewById(R.id.all_users_username);
            name.setText(user_name);
        }
        void setUser_description(String user_description)
        {
            description = mView.findViewById(R.id.all_users_description);
            description.setText(user_description);
        }

        void setUser_thumbImage(final Context context, final String user_thumb_image)
        {
            final CircleImageView image = mView.findViewById(R.id.all_users_profile_image);
           //
            Picasso.with(context).load(user_thumb_image).networkPolicy(NetworkPolicy.OFFLINE).into(image, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError()
                {
                    Picasso.with(context).load(user_thumb_image).into(image);
                }
            });

        }
        void setUser_age(String user_age)
        {
            age = mView.findViewById(R.id.all_users_age);
            age.setText(user_age);
        }

    }
}
