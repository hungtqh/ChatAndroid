package com.chatandroid.chats;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.chatandroid.Authenticate;
import com.chatandroid.R;
import com.chatandroid.chats.adapter.ChatsFragmentsAdapter;
import com.chatandroid.utils.Tools;
import com.google.android.material.tabs.TabLayout;


public class Chats extends Authenticate {

    private ViewPager view_pager;
    private TabLayout tab_layout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);
        primaryMenu(savedInstanceState);

        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Chat");
        Tools.setSystemBarColorInt(this, getResources().getColor(R.color.default_status_color));
    }

    private void initComponent() {
        view_pager = (ViewPager) findViewById(R.id.view_pager);
        tab_layout = (TabLayout) findViewById(R.id.tab_layout);
        tab_layout.setupWithViewPager(view_pager);

        setupViewPager(view_pager);
        tab_layout.setupWithViewPager(view_pager);
        tab_layout.setTabGravity(TabLayout.GRAVITY_FILL);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_setting, menu);
        Tools.changeMenuIconColor(menu, getResources().getColor(R.color.defaultWhite));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViewPager(ViewPager viewPager) {
        ChatsFragmentsAdapter adapter = new ChatsFragmentsAdapter(getSupportFragmentManager());
        adapter.addFragment(new ChatsFragment(), "Chats");
        adapter.addFragment(new GroupsFragment(), "Groups");
        adapter.addFragment(new FriendsFragment(), "Friends");
        adapter.addFragment(new RequestsFragment(), "Requests");
        viewPager.setAdapter(adapter);
    }

}