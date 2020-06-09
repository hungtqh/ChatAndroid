package com.chatandroid.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.chatandroid.Authenticate;
import com.chatandroid.R;
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
    private DatabaseReference UserRef;

    private ActivityProfileBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        primaryMenu(savedInstanceState);
        initToolbar();

        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        if (mAuth.getCurrentUser() != null) {
            RetrieveUserInfo();
        }
        binding.floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, ProfileEditActivity.class);
                overridePendingTransition(0, 0);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Messaging App");
        Tools.setSystemBarColorInt(this, getResources().getColor(R.color.default_status_color));
    }

    private void RetrieveUserInfo() {
        UserRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String firtname = Tools.getRefValue(dataSnapshot.child("firstname"));
                String lastname = Tools.getRefValue(dataSnapshot.child("lastname"));
                String name = firtname + " " + lastname;
                String userImage = Tools.getRefValue(dataSnapshot.child("image"));
                String username = Tools.getRefValue(dataSnapshot.child("username"));
                String phone = Tools.getRefValue(dataSnapshot.child("phonenumber"));
                String location = Tools.getRefValue(dataSnapshot.child("location"));
                String gender = Tools.getRefValue(dataSnapshot.child("gender"));
                String dateOfBirth = Tools.getRefValue(dataSnapshot.child("dateOfBirth"));

                binding.profileName.setText(name);
                binding.nickname.setText(username);
                binding.location.setText(location);
                binding.phone.setText(phone);
                binding.gender.setText(gender);
                binding.dateOfBirth.setText(dateOfBirth);

                CircularImageView userProfileImage = binding.image;

                if (!userImage.isEmpty()) {
                    Picasso.get().load(userImage).placeholder(R.drawable.photo_male_8).into(userProfileImage);
                }

                if (mAuth.getCurrentUser() != null) {
                    binding.username.setText(mAuth.getCurrentUser().getEmail());
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
