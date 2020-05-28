package com.chatandroid;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.widget.Toolbar;

import com.chatandroid.databinding.ActivitySettingsBinding;
import com.chatandroid.utils.Tools;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;


public class ConfigurationActivity extends Authenticate
{
    private Button UpdateAccountSettings;
    private EditText userName, userStatus;
    private CircularImageView userProfileImage;

    private static final int GalleryPick = 1;
    private StorageReference UserProfileImagesRef;
    private ProgressDialog loadingBar;

    private Toolbar toolbar;
    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        InitializeFields();
        primaryMenu(savedInstanceState);
    }



    private void InitializeFields()
    {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Account Settings");
        Tools.setSystemBarColor(this, R.color.grey_5);
        Tools.setSystemBarLight(this);

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        if(bottomNavigationView != null){
            for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
                bottomNavigationView.getMenu().getItem(i).setChecked(false);
            }
            bottomNavigationView.getMenu().findItem(R.id.navigation_home).setChecked(true);
        }
    }


}
