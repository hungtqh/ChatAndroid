package com.chatandroid.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.chatandroid.Authenticate;
import com.chatandroid.R;
import com.chatandroid.databinding.ActivitySettingBinding;
import com.chatandroid.utils.AppPreference;
import com.chatandroid.utils.Tools;


public class SettingActivity extends Authenticate {
    private Toolbar toolbar;
    private ActivitySettingBinding binding;

    // variable for language change
    private int checkedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        primaryMenu(savedInstanceState);
        initializeFields();

        if (selectedLocale.equals("en")) {
            binding.language.setText(R.string.english);
            checkedItem = 0;
        } else {
            binding.language.setText(R.string.vietnamese);
            checkedItem = 1;
        }

        binding.switchDisplayMode.setOnClickListener(v12 -> toggleTheme());

        binding.selectLanguageOpt.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
            builder.setTitle(getString(R.string.select_language));
            CharSequence[] items = new CharSequence[]{getString(R.string.english), getString(R.string.vietnamese)};

            builder.setSingleChoiceItems(items, checkedItem, (dialog, which) -> {
                if (which == 0) {
                    selectedLocale = "en";
                }

                if (which == 1) {
                    selectedLocale = "vi";
                }
            });

            builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            });
            builder.setPositiveButton(R.string.save, (dialog, which) -> {
            });

            final AlertDialog dialog = builder.create();
            dialog.show();

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v11 -> {
                setAppLanguage();
                Toast.makeText(this, R.string.language_changed, Toast.LENGTH_SHORT).show();

                dialog.dismiss();
                refreshApp();
            });
        });
    }

    private void refreshApp() {
        Intent refresh = new Intent(this, SettingActivity.class);
        startActivity(refresh);
        this.finish();
    }

    private void initializeFields() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.action_settings));
        Tools.setSystemBarColorInt(this, getResources().getColor(R.color.default_status_color));

        preference = new AppPreference(SettingActivity.this);
    }

    private void toggleTheme() {

//        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
//        } else {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//        }
//
//        refreshApp();
    }
}