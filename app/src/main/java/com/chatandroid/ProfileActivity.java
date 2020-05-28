package com.chatandroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.widget.Toolbar;

import com.chatandroid.chats.Chats;
import com.chatandroid.databinding.ActivityProfileBinding;
import com.chatandroid.utils.Tools;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


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
                String firtname = (dataSnapshot.child("firstname").exists()) ? dataSnapshot.child("firstname").getValue().toString() : "";
                String lastname = (dataSnapshot.child("lastname").exists()) ? dataSnapshot.child("lastname").getValue().toString() : "";
                String name = firtname + " " + lastname;
                String username = (dataSnapshot.child("username").exists()) ? dataSnapshot.child("username").getValue().toString() : "";

                binding.profileName.setText(name);
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
