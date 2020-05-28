package com.chatandroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.chatandroid.chats.Chats;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
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

import static android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION;

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
        if (currentUserID == null)
        {
            SendUserToLoginActivity();
        }

        Button lmHumbug = findViewById(R.id.lmHumbug);
        if(lmHumbug != null){
            lmHumbug.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.material_drawer_layout);
                    mDrawerLayout.openDrawer(Gravity.LEFT);
                }
            });
        }

//        Button menu = findViewById(R.id.right_menu);
//        if(menu != null){
//            menu.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    secondaryMenus.openDrawer();
//                }
//            });
//        }
//
//        Button goBack = findViewById(R.id.go_back);
//        if(goBack != null){
//            goBack.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    onBackPressed();
//                }
//            });
//        }
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onResume(){
        super.onResume();
        updateUserStatus("online");
    }

    @Override
    public  void  onPause(){
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
        if(result != null){
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
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);

        //getMenuInflater().inflate(R.menu.options_menu, menu);

        return true;
    }

    protected void primaryMenu(Bundle instance){
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
                        account,
                        new SectionDrawerItem().withName("Tools").withSelectable(false),
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

                        if(drawerItem.getIdentifier() == R.string.dashboard){
                            Intent intent = new Intent(Authenticate.this, MainActivity.class);
                            overridePendingTransition(0, 0);
                            startActivity(intent);
                            overridePendingTransition(0, 0);
                        }

                        if(drawerItem.getIdentifier() == R.string.action_settings){
                            Intent intent = new Intent(Authenticate.this, SettingsActivity.class);
                            startActivity(intent);
                        }

                        if(drawerItem.getIdentifier() == R.string.menu_profile){
                            Intent intent = new Intent(Authenticate.this, ProfileActivity.class);
                            overridePendingTransition(0, 0);
                            startActivity(intent);
                            overridePendingTransition(0, 0);
                        }



                        if(drawerItem.getIdentifier() == R.string.logout){
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
                                    //note don't do this if your badge contains a "+"
                                    //only use toString() if you set the test as String
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
                .withOnDrawerItemLongClickListener(new Drawer.OnDrawerItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(View view, int position, IDrawerItem drawerItem) {

                        return false;
                    }
                })
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

    protected void  rightMenuAccounts(Bundle instance){
        PrimaryDrawerItem home = new PrimaryDrawerItem().withName(R.string.account).withTextColor(getResources().getColor(R.color.defaultDark)).withSelectedTextColor(getResources().getColor(R.color.defaultDark)).withIdentifier(R.string.account).withSelectable(true).withIcon(ContextCompat.getDrawable(this, R.drawable.ic_person_outline_black_24dp)).withIconColor(getResources().getColor(R.color.defaultDark)).withIconTintingEnabled(true).withSelectedIconColor(getResources().getColor(R.color.defaultDark)).withSelectedColor(getResources().getColor(R.color.primary));
        SecondaryDrawerItem privacy = new SecondaryDrawerItem().withName(R.string.privacy).withTextColor(getResources().getColor(R.color.defaultDark)).withSelectedTextColor(getResources().getColor(R.color.defaultDark)).withIdentifier(R.string.privacy).withSelectable(true).withIcon(ContextCompat.getDrawable(this, R.drawable.ic_security_black_24dp)).withIconColor(getResources().getColor(R.color.defaultDark)).withIconTintingEnabled(true).withSelectedIconColor(getResources().getColor(R.color.defaultDark)).withSelectedColor(getResources().getColor(R.color.primary));
        SecondaryDrawerItem security = new SecondaryDrawerItem().withName(R.string.security).withTextColor(getResources().getColor(R.color.defaultDark)).withSelectedTextColor(getResources().getColor(R.color.defaultDark)).withIdentifier(R.string.security).withSelectable(true).withIcon(ContextCompat.getDrawable(this, R.drawable.ic_lock_outline_black_24dp)).withIconColor(getResources().getColor(R.color.defaultDark)).withIconTintingEnabled(true).withSelectedIconColor(getResources().getColor(R.color.defaultDark)).withSelectedColor(getResources().getColor(R.color.primary));
        secondaryMenus = new DrawerBuilder()
                .withActivity(this)
                .withDisplayBelowStatusBar(true)
                .withSavedInstance(instance)
                .addDrawerItems(
                        home,
                        privacy,
                        security

                ).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {


//                if(drawerItem.getIdentifier() == R.string.account){
//                    Intent intent = new Intent(Lockminds.this, LmAccount.class);
//                    intent.addFlags(FLAG_ACTIVITY_NO_ANIMATION);
//                    startActivity(intent);
//                }
//
//                if(drawerItem.getIdentifier() == R.string.security){
//                    Intent intent = new Intent(Lockminds.this, LmChangePassword.class);
//                    intent.addFlags(FLAG_ACTIVITY_NO_ANIMATION);
//                    startActivity(intent);
//                }
                        return false;
                    }
                })
                .withDrawerGravity(Gravity.END)
                .append(drawerReference());
    }

    protected void  rightMenuChats(Bundle instance){
        PrimaryDrawerItem home = new PrimaryDrawerItem().withName(R.string.menu_home).withTextColor(getResources().getColor(R.color.defaultDark)).withSelectedTextColor(getResources().getColor(R.color.defaultDark)).withIdentifier(R.string.menu_home).withSelectable(true).withIcon(ContextCompat.getDrawable(this, R.drawable.ic_home_black_24dp)).withIconColor(getResources().getColor(R.color.defaultDark)).withIconTintingEnabled(true).withSelectedIconColor(getResources().getColor(R.color.defaultDark)).withSelectedColor(getResources().getColor(R.color.primary));
        secondaryMenus = new DrawerBuilder()
                .withActivity(this)
                .withDisplayBelowStatusBar(true)
                .withSavedInstance(instance)
                .addDrawerItems(
                        home

                ).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                        if(drawerItem.getIdentifier() == R.string.menu_home){
                            Intent intent = new Intent(Authenticate.this, MainActivity.class);
                            intent.addFlags(FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(intent);
                        }

                        return false;
                    }
                })
                .withDrawerGravity(Gravity.END)
                .append(drawerReference());
    }

    public Drawer drawerReference(){
        return  result;
    }

    private void VerifyUserExistance() {


    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(Authenticate.this, LoginActivity.class);
        overridePendingTransition(0, 0);
        startActivity(loginIntent);
        overridePendingTransition(0, 0);

    }

    private void SendUserToSettingsActivity() {
        Intent settingsIntent = new Intent(Authenticate.this, SettingsActivity.class);
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

