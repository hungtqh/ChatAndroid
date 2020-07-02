package com.chatandroid.chat.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chatandroid.activity.Authenticate;
import com.chatandroid.R;
import com.chatandroid.chat.adapter.FriendAdapter;
import com.chatandroid.chat.model.User;
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
    private RecyclerView findFriendsRecyclerList;
    private DatabaseReference usersRef;
    private ActivityFindFriendsBinding binding;
    private FriendAdapter usersAdapter;
    private List<User> mUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFindFriendsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.find_friend);

        Tools.setSystemBarColorInt(this, getResources().getColor(R.color.default_status_color));

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        findFriendsRecyclerList = findViewById(R.id.find_friends_recycler_list);
        findFriendsRecyclerList.setLayoutManager(new LinearLayoutManager(this));
        mUsers = new ArrayList<>();

        binding.lytBack.setOnClickListener(v -> onBackPressed());
        binding.searchFriend.setOnClickListener(v1 -> {
            String searchInput = binding.searchField.getText().toString();

            if (TextUtils.isEmpty(searchInput)) {
                Toast.makeText(this, R.string.enter_mail_phone, Toast.LENGTH_SHORT).show();
                return;
            }

            if (searchInput.startsWith("0")) {
                searchInput = "+84" + searchInput.substring(1);
            }

            findFriend(searchInput.trim());
        });
        boolean nightMode = preference.getNightMode();
        toggleNightMode(view, nightMode);
    }

    private void findFriend(String searchInput) {
        binding.searchResult.setVisibility(View.VISIBLE);
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (dataSnapshot.exists()) {
                        if (user.getUid() != null) {
                            if (!user.getUid().equals(currentUserID) &&
                                    (user.getPhonenumber().equals(searchInput) || user.getEmail().equals(searchInput))) {
                                mUsers.add(user);
                            }
                        }

                    }
                }

                usersAdapter = new FriendAdapter(FindFriendsActivity.this, mUsers);
                findFriendsRecyclerList.setAdapter(usersAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
