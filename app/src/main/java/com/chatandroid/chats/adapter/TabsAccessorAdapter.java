package com.chatandroid.chats.adapter;


import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.chatandroid.chats.ChatsFragment;
import com.chatandroid.chats.FriendsFragment;
import com.chatandroid.chats.GroupsFragment;
import com.chatandroid.chats.RequestsFragment;

public class TabsAccessorAdapter extends FragmentPagerAdapter
{

    public TabsAccessorAdapter(FragmentManager fm)
    {
        super(fm);
    }

    @Override
    public Fragment getItem(int i)
    {
        switch (i)
        {
            case 0:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;

            case 1:
                GroupsFragment groupsFragment = new GroupsFragment();
                return groupsFragment;

            case 2:
                FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;

            case 3:
                RequestsFragment requestsFragment = new RequestsFragment();
                return requestsFragment;

            default:
                return null;
        }
    }


    @Override
    public int getCount()
    {
        return 3;
    }


    @Nullable
    @Override
    public CharSequence getPageTitle(int position)
    {
        switch (position)
        {
            case 0:
                return "CHATS";

            case 1:
                return "GROUPS";

            case 2:
                return "USERS";

            case 3:
                return "REQUESTS";

            default:
                return null;
        }
    }
}
