package com.chatandroid.chat.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.chatandroid.Authenticate;
import com.chatandroid.R;
import com.chatandroid.databinding.ActivityProfileViewBinding;
import com.chatandroid.utils.Tools;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class ProfileViewActivity extends Authenticate {

    private ActivityProfileViewBinding binding;
    private String receiverUserID, senderUserID, Current_State;
    private DatabaseReference UserRef, ChatRequestRef, ContactsRef, NotificationRef;
    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileViewBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        primaryMenu(savedInstanceState);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        initToolbar();

        mAuth = FirebaseAuth.getInstance();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend Requests");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        NotificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        receiverUserID = getIntent().getExtras().get("user_uid").toString();
        senderUserID = mAuth.getCurrentUser().getUid();

        RetrieveUserInfo();
    }

    private void initToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile View");
        Tools.setSystemBarColorInt(this, getResources().getColor(R.color.default_status_color));
    }

    private void RetrieveUserInfo() {
        UserRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String firtname = Tools.getRefValue(dataSnapshot.child("firstname"));
                String lastname = Tools.getRefValue(dataSnapshot.child("lastname"));
                String location = Tools.getRefValue(dataSnapshot.child("location"));
                String phone = Tools.getRefValue(dataSnapshot.child("phonenumber"));
                String name = firtname + " " + lastname;
                String username = Tools.getRefValue(dataSnapshot.child("username"));

                binding.profileName.setText(name);
                binding.username.setText(mAuth.getCurrentUser().getEmail());
                binding.nickname.setText(username);
                binding.location.setText(location);
                binding.phone.setText(phone);
                toolbar.setTitle(name);

                ManageChatRequests();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void ManageChatRequests() {

        ChatRequestRef.child(senderUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(receiverUserID)) {
                            String request_type = dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();

                            if (request_type.equals("sent")) {
                                Current_State = "request_sent";
                                binding.requestFriendship.setText("Requested");
                                binding.cancelFriendship.setEnabled(true);
                                binding.cancelFriendship.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        CancelChatRequest();
                                    }
                                });
                            } else {
                                binding.requestFriendship.setText("Following");
                                binding.requestFriendship.setEnabled(false);
                                binding.cancelFriendship.setEnabled(true);
                                binding.cancelFriendship.setText("Unfollow");
                                binding.cancelFriendship.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        CancelChatRequest();
                                    }
                                });
                            }
                        } else {
                            ContactsRef.child(senderUserID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(receiverUserID)) {
                                                Current_State = "friends";
                                                binding.requestFriendship.setText("Following");
                                                binding.requestFriendship.setEnabled(false);
                                                binding.cancelFriendship.setEnabled(true);
                                                binding.cancelFriendship.setText("Unfollow");
                                                binding.cancelFriendship.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        CancelChatRequest();
                                                    }
                                                });
                                            } else {
                                                binding.requestFriendship.setEnabled(true);
                                                Current_State = "new";
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


        binding.requestFriendship.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.requestFriendship.setEnabled(false);

                if (Current_State.equals("new")) {
                    SendChatRequest();
                }
            }
        });

    }


    private void CancelChatRequest() {
        Log.i("kelly", "click");

        ContactsRef.child(receiverUserID).child(senderUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            binding.requestFriendship.setEnabled(true);
                            Current_State = "new";
                            binding.requestFriendship.setText("Request");
                            binding.cancelFriendship.setEnabled(false);
                            binding.cancelFriendship.setText("Cancel");
                        }
                    }
                });

        ContactsRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            binding.requestFriendship.setEnabled(true);
                            Current_State = "new";
                            binding.requestFriendship.setText("Request");
                            binding.cancelFriendship.setEnabled(false);
                            binding.cancelFriendship.setText("Cancel");
                        }
                    }
                });

        ChatRequestRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            ChatRequestRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                binding.requestFriendship.setEnabled(true);
                                                Current_State = "new";
                                                binding.requestFriendship.setText("Request");
                                                binding.cancelFriendship.setEnabled(false);
                                                binding.cancelFriendship.setText("Cancel");
                                            }
                                        }
                                    });


                        }
                    }
                });
    }


    private void SendChatRequest() {
        ChatRequestRef.child(senderUserID).child(receiverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            ChatRequestRef.child(receiverUserID).child(senderUserID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                HashMap<String, String> chatNotificationMap = new HashMap<>();
                                                chatNotificationMap.put("from", senderUserID);
                                                chatNotificationMap.put("type", "request");

                                                NotificationRef.child(receiverUserID).push()
                                                        .setValue(chatNotificationMap)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    binding.requestFriendship.setEnabled(true);
                                                                    Current_State = "request_sent";
                                                                    binding.requestFriendship.setText("Requested");
                                                                    binding.requestFriendship.setEnabled(false);
                                                                    binding.cancelFriendship.setEnabled(true);
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

}
