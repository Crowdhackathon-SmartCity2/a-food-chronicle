package com.afoodchronicle.chat;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.afoodchronicle.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import com.afoodchronicle.User;

import static com.afoodchronicle.utilities.Static.USERS;


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

        FirebaseRecyclerAdapter<User, AllUsersViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<User, AllUsersViewHolder>
                (
                        User.class,
                        R.layout.all_users_display_layout,
                        AllUsersViewHolder.class,
                        allDatabaseUsersReference

                ) {
            @Override
            protected void populateViewHolder(AllUsersViewHolder viewHolder, User model, int position)
            {
                viewHolder.setUser_name(model.getFirstName()+ " " + model.getLastName());
                viewHolder.setUser_description(model.getDescription());
                viewHolder.setUser_image(getApplicationContext(), model.getPhotoUrl());
                viewHolder.setUser_age("Age: " + model.getAge());
            }
        };
        allUsersList.setAdapter(firebaseRecyclerAdapter);
    }

    private static class AllUsersViewHolder extends RecyclerView.ViewHolder{

        private View mView;
        private TextView name;
        private TextView description;
        private CircleImageView image;
        private TextView age;

        public AllUsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setUser_name(String user_name)
        {
            name = mView.findViewById(R.id.all_users_username);
            name.setText(user_name);
        }
        public void setUser_description(String user_description)
        {
            description = mView.findViewById(R.id.all_users_description);
            description.setText(user_description);
        }

        public void setUser_image(Context context, String user_image)
        {
            image = mView.findViewById(R.id.all_users_profile_image);
            Picasso.with(context).load(user_image).into(image);

        }
        public void setUser_age(String user_age)
        {
            age = mView.findViewById(R.id.all_users_age);
            age.setText(user_age);
        }

    }
}
