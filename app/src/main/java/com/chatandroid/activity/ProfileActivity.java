package com.chatandroid.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.appcompat.widget.Toolbar;

import com.chatandroid.R;
import com.chatandroid.chat.activity.ImageViewerActivity;
import com.chatandroid.databinding.ActivityProfileBinding;
import com.chatandroid.utils.Tools;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends Authenticate {
    private DatabaseReference userRef;

    private ActivityProfileBinding binding;
    private String imageUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        primaryMenu(savedInstanceState);
        initToolbar();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        if (mAuth.getCurrentUser() != null) {
            retrieveUserInfo();
        }

        boolean nightMode = preference.getNightMode();
        toggleNightMode(view, nightMode);

        binding.floatingActionButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ProfileEditActivity.class);
            startActivity(intent);
        });
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.menu_profile));
        Tools.setSystemBarColorInt(this, getResources().getColor(R.color.default_status_color));
    }

    private void retrieveUserInfo() {
        userRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String firtname = Tools.getRefValue(dataSnapshot.child("firstname"));
                String lastname = Tools.getRefValue(dataSnapshot.child("lastname"));
                String name = firtname + " " + lastname;
                imageUrl = Tools.getRefValue(dataSnapshot.child("image"));
                String nickName = Tools.getRefValue(dataSnapshot.child("username"));
                String phone = Tools.getRefValue(dataSnapshot.child("phonenumber"));
                String location = Tools.getRefValue(dataSnapshot.child("location"));
                String gender = Tools.getRefValue(dataSnapshot.child("gender"));
                String email = Tools.getRefValue(dataSnapshot.child("email"));
                String dateOfBirth = Tools.getRefValue(dataSnapshot.child("dateOfBirth"));

                binding.profileName.setText(name);
                binding.nickname.setText(nickName);
                binding.location.setText(location);
                binding.email.setText(email);

                if (mAuth.getCurrentUser() != null) {
                    if (!TextUtils.isEmpty(mAuth.getCurrentUser().getPhoneNumber())) {
                        phone = mAuth.getCurrentUser().getPhoneNumber();
                    } else {
                        binding.email.setText(mAuth.getCurrentUser().getEmail());
                    }
                }

                binding.phone.setText(phone);

                if (selectedLocale.equals("vi")) {
                    if (gender.equals("Male")) {
                        gender = "Nam";
                    } else if (gender.equals("Female")) {
                        gender = "Nữ";
                    }
                }

                binding.gender.setText(gender);
                binding.dateOfBirth.setText(dateOfBirth);

                CircularImageView userProfileImage = binding.image;

                if (!imageUrl.isEmpty()) {
                    Picasso.get().load(imageUrl).placeholder(R.drawable.profile_image).into(userProfileImage);
                    userProfileImage.setOnClickListener(view -> startImageViewerActivity());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void startImageViewerActivity() {
        Intent intent = new Intent(ProfileActivity.this, ImageViewerActivity.class);
        intent.putExtra("imageUrl", imageUrl);
        startActivity(intent);
    }
}
