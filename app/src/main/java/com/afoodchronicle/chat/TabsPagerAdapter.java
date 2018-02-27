package com.afoodchronicle.chat;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import static com.afoodchronicle.utilities.Static.FRAGMENT_TITLE_CHATS;
import static com.afoodchronicle.utilities.Static.FRAGMENT_TITLE_FRIENDS;
import static com.afoodchronicle.utilities.Static.FRAGMENT_TITLE_REQUESTS;

class TabsPagerAdapter extends FragmentPagerAdapter {

    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position)
    {
        switch (position)
        {
            case 0:
                RequestsFragment requestsFragment = new RequestsFragment();
                return requestsFragment;
            case 1:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;
            case 2:
                FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;
            default:
                return null;

        }
    }

    @Override
    public int getCount()
    {
        return 3;
    }
    public CharSequence getPageTitle (int position)
    {
        switch (position)
        {
            case 0:
                return FRAGMENT_TITLE_REQUESTS;
            case 1:
                return FRAGMENT_TITLE_CHATS;
            case 2:
                return FRAGMENT_TITLE_FRIENDS;
            default:
                return null;
        }
    }
}
