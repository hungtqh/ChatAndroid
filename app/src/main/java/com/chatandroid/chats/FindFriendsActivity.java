package com.chatandroid.chats;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chatandroid.Authenticate;
import com.chatandroid.R;
import com.chatandroid.chats.adapter.FriendAdapter;
import com.chatandroid.chats.model.User;
import com.chatandroid.databinding.ActivityFindFriendsBinding;
import com.chatandroid.utils.Tools;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class FindFriendsActivity extends Authenticate {
    private RecyclerView FindFriendsRecyclerList;
    private DatabaseReference UsersRef;
    private ActionBar actionBar;
    private ActivityFindFriendsBinding binding;
    private FriendAdapter usersAdapter;
    private List<User> mUsers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFindFriendsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setTitle("Find Friend");
        Tools.setSystemBarColorInt(this, getResources().getColor(R.color.default_status_color));

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        FindFriendsRecyclerList = (RecyclerView) findViewById(R.id.find_friends_recycler_list);
        FindFriendsRecyclerList.setLayoutManager(new LinearLayoutManager(this));
        mUsers = new ArrayList<>();

        (binding.lytBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (dataSnapshot.exists()) {
                        if (user.getUid() != null) {
                            if (!user.getUid().equals(currentUserID)) {
                                mUsers.add(user);
                            }
                        }

                    }
                }

                usersAdapter = new FriendAdapter(FindFriendsActivity.this, mUsers);
                FindFriendsRecyclerList.setAdapter(usersAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
