package com.chatandroid.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.chatandroid.R;
import com.chatandroid.chat.Chats;
import com.chatandroid.utils.AppPreference;
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
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class Authentication extends AppCompatActivity {

    public FirebaseAuth mAuth;
    public DatabaseReference rootRef;
    public String currentUserID;
    public String mName;
    public String mEmail;

    protected Drawer result = null;
    private AccountHeader headerResult = null;

    private Locale mLocale;
    protected AppPreference preference;
    protected String selectedLocale;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();

        if (mAuth.getCurrentUser() != null) {
            currentUserID = mAuth.getCurrentUser().getUid();

            String phoneNumber = mAuth.getCurrentUser().getPhoneNumber();
            if (!TextUtils.isEmpty(phoneNumber)) {
                mEmail = phoneNumber;
            } else {
                mEmail = mAuth.getCurrentUser().getEmail();
            }

            mName = getString(R.string.app_name);
        }

        preference = new AppPreference(this);
        selectedLocale = preference.getAppLanguage();
        setAppLanguage();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() != null) {
            Button lmHumbug = findViewById(R.id.lmHumbug);
            if (lmHumbug != null) {
                lmHumbug.setOnClickListener(v -> {
                    DrawerLayout mDrawerLayout = findViewById(R.id.material_drawer_layout);
                    mDrawerLayout.openDrawer(Gravity.LEFT);
                });
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentUserID != null) {
            updateUserStatus("online");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (currentUserID != null) {
            updateUserStatus("offline");
        }
    }

    protected void primaryMenu(Bundle instance) {
        PrimaryDrawerItem chat = new PrimaryDrawerItem().
                withName(R.string.menu_chat).
                withTextColor(getResources().getColor(R.color.defaultDark)).
                withSelectedTextColor(getResources().getColor(R.color.defaultDark)).
                withIdentifier(R.string.menu_chat).
                withSelectable(true).
                withIcon(ContextCompat.getDrawable(this, R.drawable.ic_chat)).
                withIconColor(getResources().getColor(R.color.defaultDark)).
                withIconTintingEnabled(true).
                withSelectedIconColor(getResources().getColor(R.color.defaultDark)).
                withSelectedColor(getResources().getColor(R.color.defaultWhite));

        SecondaryDrawerItem account = new SecondaryDrawerItem().
                withName(R.string.menu_profile).
                withTextColor(getResources().getColor(R.color.defaultDark)).
                withSelectedTextColor(getResources().getColor(R.color.defaultDark)).
                withIdentifier(R.string.menu_profile).
                withSelectable(true).
                withIcon(ContextCompat.getDrawable(this, R.drawable.ic_person_outline_black_24dp)).
                withIconColor(getResources().getColor(R.color.defaultDark)).
                withIconTintingEnabled(true).
                withSelectedIconColor(getResources().getColor(R.color.defaultDark)).
                withSelectedColor(getResources().getColor(R.color.defaultWhite));

        SecondaryDrawerItem settings = new SecondaryDrawerItem().
                withName(R.string.action_settings).
                withTextColor(getResources().getColor(R.color.defaultDark)).
                withSelectedTextColor(getResources().getColor(R.color.defaultDark)).
                withIdentifier(R.string.action_settings).
                withSelectable(true).
                withIcon(ContextCompat.getDrawable(this, R.drawable.ic_settings)).
                withIconColor(getResources().getColor(R.color.defaultDark)).
                withIconTintingEnabled(true).
                withSelectedIconColor(getResources().getColor(R.color.defaultDark)).
                withSelectedColor(getResources().getColor(R.color.defaultWhite));

        SecondaryDrawerItem logout = new SecondaryDrawerItem().withName(R.string.logout).
                withTextColor(getResources().getColor(R.color.defaultDark)).
                withSelectedTextColor(getResources().getColor(R.color.defaultDark)).
                withIdentifier(R.string.logout).
                withSelectable(true).
                withIcon(ContextCompat.getDrawable(this, R.drawable.ic_lock_outline_black_24dp)).
                withIconColor(getResources().getColor(R.color.defaultDark)).
                withIconTintingEnabled(true).
                withSelectedIconColor(getResources().getColor(R.color.defaultDark)).
                withSelectedColor(getResources().getColor(R.color.defaultWhite));

        final IProfile profile = new ProfileDrawerItem().withName(mName).withEmail(mEmail).withIcon(R.mipmap.ic_launcher);

        headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.color.app_bar)
                .withTextColor(getColor(R.color.blue_grey_50))
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
                        new SectionDrawerItem().withName(R.string.tools).withSelectable(false),
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
                .withOnDrawerItemClickListener((view, position, drawerItem) -> {

                    if (drawerItem.getIdentifier() == R.string.menu_chat) {
                        Intent intent = new Intent(Authentication.this, Chats.class);
                        startActivity(intent);
                    }

                    if (drawerItem.getIdentifier() == R.string.action_settings) {
                        Intent intent = new Intent(Authentication.this, SettingActivity.class);
                        startActivity(intent);
                    }

                    if (drawerItem.getIdentifier() == R.string.menu_profile) {
                        Intent intent = new Intent(Authentication.this, ProfileActivity.class);
                        startActivity(intent);
                    }

                    if (drawerItem.getIdentifier() == R.string.logout) {
                        mAuth.signOut();
                        Intent intent = new Intent(Authentication.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //add the values which need to be saved from the drawer to the bundle
        if (result != null) {
            outState = result.saveInstanceState(outState);
        }

        super.onSaveInstanceState(outState);
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

        rootRef.child("Users").child(currentUserID).child("userState")
                .updateChildren(onlineStateMap);
    }

    protected void setAppLanguage() {
        mLocale = new Locale(selectedLocale);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = mLocale;
        res.updateConfiguration(conf, dm);

        preference.setAppLanguage(selectedLocale);
    }

    protected void toggleNightMode(View view, boolean nightMode) {
        if (nightMode) {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            view.setBackgroundColor(getColor(R.color.grey_400));
        } else {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}

