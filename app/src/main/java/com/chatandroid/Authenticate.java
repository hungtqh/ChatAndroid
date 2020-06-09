package com.chatandroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.chatandroid.activity.LoginActivity;
import com.chatandroid.activity.ProfileActivity;
import com.chatandroid.activity.SettingActivity;
import com.chatandroid.chat.Chats;
import com.chatandroid.chat.activity.ChatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Badgeable;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class Authenticate extends AppCompatActivity {

    public FirebaseAuth mAuth;
    public DatabaseReference RootRef;
    public String currentUserID;
    public String mName = null;
    public String mEmail = null;

    Drawer secondaryMenus;
    protected Drawer result = null;
    protected Drawer resultAppended = null;
    private AccountHeader headerResult = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUserID == null) {
            SendUserToLoginActivity();
        }

        Button lmHumbug = findViewById(R.id.lmHumbug);
        if (lmHumbug != null) {
            lmHumbug.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.material_drawer_layout);
                    mDrawerLayout.openDrawer(Gravity.LEFT);
                }
            });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserStatus("online");
    }

    @Override
    public void onPause() {
        super.onPause();
        updateUserStatus("offline");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //add the values which need to be saved from the drawer to the bundle
        if (result != null) {
            outState = result.saveInstanceState(outState);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (result != null && result.isDrawerOpen()) {
            result.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        //getMenuInflater().inflate(R.menu.options_menu, menu);

        return true;
    }

    protected void primaryMenu(Bundle instance) {
        PrimaryDrawerItem chat = new PrimaryDrawerItem().withName("Chat").withTextColor(getResources().getColor(R.color.defaultDark)).withSelectedTextColor(getResources().getColor(R.color.defaultDark)).withIdentifier(R.string.menu_chat).withSelectable(true).withIcon(ContextCompat.getDrawable(this, R.drawable.ic_chat)).withIconColor(getResources().getColor(R.color.defaultDark)).withIconTintingEnabled(true).withSelectedIconColor(getResources().getColor(R.color.defaultDark)).withSelectedColor(getResources().getColor(R.color.defaultWhite));

        SecondaryDrawerItem account = new SecondaryDrawerItem().withName(R.string.menu_profile).withTextColor(getResources().getColor(R.color.defaultDark)).withSelectedTextColor(getResources().getColor(R.color.defaultDark)).withIdentifier(R.string.menu_profile).withSelectable(true).withIcon(ContextCompat.getDrawable(this, R.drawable.ic_person_outline_black_24dp)).withIconColor(getResources().getColor(R.color.defaultDark)).withIconTintingEnabled(true).withSelectedIconColor(getResources().getColor(R.color.defaultDark)).withSelectedColor(getResources().getColor(R.color.defaultWhite));
        SecondaryDrawerItem settings = new SecondaryDrawerItem().withName(R.string.action_settings).withTextColor(getResources().getColor(R.color.defaultDark)).withSelectedTextColor(getResources().getColor(R.color.defaultDark)).withIdentifier(R.string.action_settings).withSelectable(true).withIcon(ContextCompat.getDrawable(this, R.drawable.ic_settings)).withIconColor(getResources().getColor(R.color.defaultDark)).withIconTintingEnabled(true).withSelectedIconColor(getResources().getColor(R.color.defaultDark)).withSelectedColor(getResources().getColor(R.color.defaultWhite));
        SecondaryDrawerItem logout = new SecondaryDrawerItem().withName(R.string.logout).withTextColor(getResources().getColor(R.color.defaultDark)).withSelectedTextColor(getResources().getColor(R.color.defaultDark)).withIdentifier(R.string.logout).withSelectable(true).withIcon(ContextCompat.getDrawable(this, R.drawable.ic_lock_outline_black_24dp)).withIconColor(getResources().getColor(R.color.defaultDark)).withIconTintingEnabled(true).withSelectedIconColor(getResources().getColor(R.color.defaultDark)).withSelectedColor(getResources().getColor(R.color.defaultWhite));

        final IProfile profile = new ProfileDrawerItem().withName(mName).withEmail(mEmail).withIcon(R.mipmap.ic_launcher);
        headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.color.app_bar)
                .withCompactStyle(true)
                .addProfiles(
                        profile
                )
                .withSavedInstance(instance)
                .build();

        result = new DrawerBuilder()
                .withActivity(this)
                .withAccountHeader(headerResult)
                .withSavedInstance(instance)
                .addDrawerItems(
                        chat,
                        new SectionDrawerItem().withName("Tools").withSelectable(false),
                        account,
                        settings,
                        logout
                )
                .withOnDrawerListener(new Drawer.OnDrawerListener() {
                    @Override
                    public void onDrawerOpened(View drawerView) {

                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {

                    }

                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {

                    }
                })
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                        if (drawerItem.getIdentifier() == R.string.menu_chat) {
                            Intent intent = new Intent(Authenticate.this, Chats.class);
                            startActivity(intent);
                        }

                        if (drawerItem.getIdentifier() == R.string.action_settings) {
                            Intent intent = new Intent(Authenticate.this, SettingActivity.class);
                            startActivity(intent);
                        }

                        if (drawerItem.getIdentifier() == R.string.menu_profile) {
                            Intent intent = new Intent(Authenticate.this, ProfileActivity.class);
                            overridePendingTransition(0, 0);
                            startActivity(intent);
                            overridePendingTransition(0, 0);
                        }

                        if (drawerItem.getIdentifier() == R.string.logout) {
                            mAuth.signOut();
                            Intent intent = new Intent(Authenticate.this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            overridePendingTransition(0, 0);
                            startActivity(intent);
                            overridePendingTransition(0, 0);
                            finish();
                        }

                        if (drawerItem != null) {

                            if (drawerItem instanceof Badgeable) {
                                Badgeable badgeable = (Badgeable) drawerItem;
                                if (badgeable.getBadge() != null) {
                                    int badge = Integer.valueOf(badgeable.getBadge().toString());
                                    if (badge > 0) {
                                        badgeable.withBadge(String.valueOf(badge - 1));
                                        result.updateItem(drawerItem);
                                    }
                                }
                            }
                        }

                        return false;
                    }
                })
                .withOnDrawerItemLongClickListener((view, position, drawerItem) -> false)
                .withOnDrawerListener(new Drawer.OnDrawerListener() {
                    @Override
                    public void onDrawerOpened(View drawerView) {

                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {

                    }

                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {

                    }
                })
                .build();

    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(Authenticate.this, LoginActivity.class);
        overridePendingTransition(0, 0);
        startActivity(loginIntent);
        overridePendingTransition(0, 0);

    }

    private void SendUserToSettingsActivity() {
        Intent settingsIntent = new Intent(Authenticate.this, SettingActivity.class);
        startActivity(settingsIntent);
    }

    private void updateUserStatus(String state) {
        String saveCurrentTime, saveCurrentDate;

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date", saveCurrentDate);
        onlineStateMap.put("status", state);

        RootRef.child("Users").child(currentUserID).child("userState")
                .updateChildren(onlineStateMap);
    }
}

