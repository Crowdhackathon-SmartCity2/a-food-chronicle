package com.afoodchronicle.chat.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.afoodchronicle.R;
import com.afoodchronicle.chat.TabsPagerAdapter;
import com.google.firebase.auth.FirebaseAuth;

public class ChatFragmentsListActivity extends AppCompatActivity implements
        ChatsFragment.OnFragmentInteractionListener,
        FriendsFragment.OnFragmentInteractionListener,
        RequestsFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ViewPager mViewPager = findViewById(R.id.main_tabs_pager);
        TabsPagerAdapter mTabsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mTabsPagerAdapter);
        TabLayout mTabLayout = findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onFragmentInteraction() {

    }
}
