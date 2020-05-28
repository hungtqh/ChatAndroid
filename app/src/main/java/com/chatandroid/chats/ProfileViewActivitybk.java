package com.chatandroid.chats;

import android.os.Bundle;
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

public class ProfileViewActivitybk extends Authenticate {

    private ActivityProfileViewBinding binding;
    private String receiverUserID, senderUserID, Current_State;
    private DatabaseReference UserRef, ChatRequestRef, ContactsRef, NotificationRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileViewBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        primaryMenu(savedInstanceState);
        initToolbar();

        mAuth = FirebaseAuth.getInstance();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        NotificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        receiverUserID = getIntent().getExtras().get("user_uid").toString();
        senderUserID = mAuth.getCurrentUser().getUid();

        RetrieveUserInfo();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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
                String name = firtname + " " + lastname;
                String username = Tools.getRefValue(dataSnapshot.child("username"));
                binding.profileName.setText(name);
                binding.username.setText(mAuth.getCurrentUser().getEmail());

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
                            } else if (request_type.equals("received")) {
                                Current_State = "request_received";
                                binding.requestFriendship.setText("Accept Chat Request");

                                binding.cancelFriendship.setVisibility(View.VISIBLE);
                                binding.cancelFriendship.setEnabled(true);

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
                                                binding.requestFriendship.setText("Unfollow");
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


        if (!senderUserID.equals(receiverUserID)) {
            binding.requestFriendship.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    binding.requestFriendship.setEnabled(false);

                    if (Current_State.equals("new")) {
                        SendChatRequest();
                    }
                    if (Current_State.equals("request_sent")) {
                        CancelChatRequest();
                    }
                    if (Current_State.equals("request_received")) {
                        AcceptChatRequest();
                    }
                    if (Current_State.equals("friends")) {
                        RemoveSpecificContact();
                    }
                }
            });
        } else {
            binding.requestFriendship.setVisibility(View.INVISIBLE);
        }
    }


    private void RemoveSpecificContact() {
        ContactsRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            ContactsRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                binding.requestFriendship.setEnabled(true);
                                                Current_State = "new";
                                                binding.requestFriendship.setText("Send Message");

                                                binding.cancelFriendship.setVisibility(View.INVISIBLE);
                                                binding.cancelFriendship.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptChatRequest() {
        ContactsRef.child(senderUserID).child(receiverUserID)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            ContactsRef.child(receiverUserID).child(senderUserID)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
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
                                                                                    binding.requestFriendship.setEnabled(true);
                                                                                    Current_State = "friends";
                                                                                    binding.requestFriendship.setText("Unfollow");

                                                                                    binding.cancelFriendship.setVisibility(View.INVISIBLE);
                                                                                    binding.cancelFriendship.setEnabled(false);
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
                });
    }

    private void CancelChatRequest() {
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
                                                binding.requestFriendship.setText("Send Message");

                                                binding.cancelFriendship.setVisibility(View.INVISIBLE);
                                                binding.cancelFriendship.setEnabled(false);
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
                                                                    binding.requestFriendship.setText("Cancel Chat Request");
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
