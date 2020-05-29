package com.chatandroid.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.chatandroid.Authenticate;
import com.chatandroid.R;
import com.chatandroid.chat.adapter.SettingsFragmentsAdapter;
import com.chatandroid.databinding.ActivitySettingsBinding;
import com.chatandroid.utils.Tools;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;


public class SettingsActivity extends Authenticate {
    private Button UpdateAccountSettings;
    private EditText userName, userStatus;
    private CircularImageView userProfileImage;

    private static final int GalleryPick = 1;
    private StorageReference UserProfileImagesRef;
    private ProgressDialog loadingBar;

    private Toolbar toolbar;
    private ActivitySettingsBinding binding;
    private ViewPager view_pager;
    private TabLayout tab_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        primaryMenu(savedInstanceState);
        InitializeFields();
        initComponent();
    }


    private void InitializeFields() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Settings");
        Tools.setSystemBarColorInt(this, getResources().getColor(R.color.default_status_color));
    }

    private void initComponent() {
        view_pager = findViewById(R.id.view_pager);
        tab_layout = findViewById(R.id.tab_layout);
        tab_layout.setupWithViewPager(view_pager);

        setupViewPager(view_pager);
        tab_layout.setupWithViewPager(view_pager);
        tab_layout.setTabGravity(TabLayout.GRAVITY_FILL);
    }

    private void setupViewPager(ViewPager viewPager) {
        SettingsFragmentsAdapter adapter = new SettingsFragmentsAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
    }


}
