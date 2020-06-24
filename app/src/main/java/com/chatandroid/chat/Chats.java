package com.chatandroid.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.chatandroid.Authenticate;
import com.chatandroid.R;
import com.chatandroid.chat.adapter.ChatsFragmentsAdapter;
import com.chatandroid.chat.fragment.ChatsFragment;
import com.chatandroid.chat.fragment.FriendsFragment;
import com.chatandroid.chat.fragment.GroupsFragment;
import com.chatandroid.chat.fragment.RequestsFragment;
import com.chatandroid.utils.Tools;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class Chats extends Authenticate {

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);
        primaryMenu(savedInstanceState);

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.chatting);
        Tools.setSystemBarColorInt(this, getResources().getColor(R.color.default_status_color));
    }

    private void initComponent() {
        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);

        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user_setting, menu);
        Tools.changeMenuIconColor(menu, getResources().getColor(R.color.defaultWhite));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.change_password) {
            LayoutInflater factory = LayoutInflater.from(this);
            final View textEntryView = factory.inflate(R.layout.alert_change_password, null);

            final EditText oldPass = textEntryView.findViewById(R.id.old_password);
            final EditText newPass = textEntryView.findViewById(R.id.new_password);
            final EditText confirmPass = textEntryView.findViewById(R.id.confirm_password);

            oldPass.setHint(R.string.enter_old_password);
            newPass.setHint(R.string.enter_new_password);
            confirmPass.setHint(R.string.confirm_new_password);

            final AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setIcon(R.mipmap.ic_launcher_round)
                    .setTitle(R.string.change_user_password)
                    .setView(textEntryView)
                    .setPositiveButton(R.string.save,
                            (dialog, whichButton) -> {
                            })
                    .setNegativeButton(R.string.cancel,
                            (dialog, whichButton) -> {
                            });

            final AlertDialog dialog = alert.create();
            dialog.show();

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                if (TextUtils.isEmpty(oldPass.getText().toString())
                        || TextUtils.isEmpty(newPass.getText().toString())
                        || TextUtils.isEmpty(confirmPass.getText().toString())) {
                    Toast.makeText(this, R.string.enter_all_fields, Toast.LENGTH_SHORT).show();
                } else {
                    AuthCredential credential = EmailAuthProvider
                            .getCredential(mAuth.getCurrentUser().getEmail(), oldPass.getText().toString());

                    mAuth.getCurrentUser().reauthenticate(credential)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    if (newPass.getText().toString().equals(confirmPass.getText().toString())) {
                                        mAuth.getCurrentUser().updatePassword(newPass.getText().toString());
                                        Toast.makeText(this, R.string.password_changed, Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    } else {
                                        Toast.makeText(this, R.string.password_not_match, Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(this, R.string.wrong_password, Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            });
        }

        return true;
    }

    private void setupViewPager(ViewPager viewPager) {
        ChatsFragmentsAdapter adapter = new ChatsFragmentsAdapter(getSupportFragmentManager());
        adapter.addFragment(new ChatsFragment(), getString(R.string.chats));
        adapter.addFragment(new GroupsFragment(), getString(R.string.groups));
        adapter.addFragment(new FriendsFragment(), getString(R.string.friends));
        adapter.addFragment(new RequestsFragment(), getString(R.string.requests));
        viewPager.setAdapter(adapter);
    }
}