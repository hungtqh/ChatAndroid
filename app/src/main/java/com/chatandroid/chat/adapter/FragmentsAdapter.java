package com.chatandroid.chat.adapter;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.chatandroid.R;
import com.chatandroid.chat.fragment.ChatsFragment;
import com.chatandroid.chat.fragment.FriendsFragment;
import com.chatandroid.chat.fragment.GroupsFragment;
import com.chatandroid.chat.fragment.RequestsFragment;

public class FragmentsAdapter extends FragmentPagerAdapter {
    private Context context;

    public FragmentsAdapter(Context context, FragmentManager manager) {
        super(manager);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                ChatsFragment chats = new ChatsFragment();
                return chats;

            case 1:
                GroupsFragment groups = new GroupsFragment();
                return groups;

            case 2:
                FriendsFragment friends = new FriendsFragment();
                return friends;

            case 3:
                RequestsFragment requests = new RequestsFragment();
                return requests;

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return context.getString(R.string.chats);

            case 1:
                return context.getString(R.string.groups);

            case 2:
                return context.getString(R.string.friends);

            case 3:
                return context.getString(R.string.requests);

            default:
                return null;
        }
    }

}

