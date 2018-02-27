package com.afoodchronicle.chat;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.afoodchronicle.LogInActivity;
import com.afoodchronicle.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChatActivity extends AppCompatActivity implements
        ChatsFragment.OnFragmentInteractionListener,
        FriendsFragment.OnFragmentInteractionListener,
        RequestsFragment.OnFragmentInteractionListener {

    private FirebaseAuth mAuth;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private TabsPagerAdapter mTabsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mViewPager = findViewById(R.id.main_tabs_pager);
        mTabsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mTabsPagerAdapter);
        mTabLayout = findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null){
            Intent goToLogIn = new Intent(ChatActivity.this, LogInActivity.class);
            goToLogIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(goToLogIn);
            finish();
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
