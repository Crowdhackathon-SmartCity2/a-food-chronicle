package com.afoodchronicle.chat;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.afoodchronicle.chat.fragments.ChatsFragment;
import com.afoodchronicle.chat.fragments.FriendsFragment;
import com.afoodchronicle.chat.fragments.RequestsFragment;

import static com.afoodchronicle.utilities.Static.FRAGMENT_TITLE_CHATS;
import static com.afoodchronicle.utilities.Static.FRAGMENT_TITLE_FRIENDS;
import static com.afoodchronicle.utilities.Static.FRAGMENT_TITLE_REQUESTS;

public class TabsPagerAdapter extends FragmentPagerAdapter {

    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position)
    {
        switch (position)
        {
            case 0:
                return new RequestsFragment();
            case 1:
                return new ChatsFragment();
            case 2:
                return new FriendsFragment();
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
