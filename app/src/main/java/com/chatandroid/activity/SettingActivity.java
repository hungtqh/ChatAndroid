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
import com.chatandroid.databinding.ActivitySettingBinding;
import com.chatandroid.utils.Tools;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;


public class SettingActivity extends Authenticate {
    private Button UpdateAccountSettings;
    private EditText userName, userStatus;
    private CircularImageView userProfileImage;

    private static final int GalleryPick = 1;
    private StorageReference UserProfileImagesRef;
    private ProgressDialog loadingBar;

    private Toolbar toolbar;
    private ActivitySettingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        primaryMenu(savedInstanceState);
        InitializeFields();
    }


    private void InitializeFields() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Settings");
        Tools.setSystemBarColorInt(this, getResources().getColor(R.color.default_status_color));
    }
}