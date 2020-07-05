package com.chatandroid.chat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.chatandroid.R;
import com.chatandroid.activity.Authentication;
import com.chatandroid.activity.ImageViewerActivity;
import com.chatandroid.activity.ProfileEditActivity;
import com.chatandroid.chat.model.NotificationDataModel;
import com.chatandroid.databinding.ActivityProfileViewBinding;
import com.chatandroid.notification.APIService;
import com.chatandroid.notification.Client;
import com.chatandroid.notification.MyResponse;
import com.chatandroid.notification.Sender;
import com.chatandroid.utils.Config;
import com.chatandroid.utils.Tools;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileViewActivity extends Authentication {

    private ActivityProfileViewBinding binding;
    private CircularImageView image;
    private String receiverUserID, senderUserID, currentState;
    private DatabaseReference userRef, chatRequestRef, contactsRef;
    private Toolbar toolbar;
    private APIService apiService;
    private String token;
    private String senderName;
    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileViewBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        primaryMenu(savedInstanceState);

        toolbar = findViewById(R.id.toolbar);
        initToolbar();

        apiService = Client.getClient(Config.NOTIFICATION_URI).create(APIService.class);
        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend Requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        image = binding.image;

        receiverUserID = getIntent().getExtras().get("receiver_uid").toString();
        senderUserID = mAuth.getCurrentUser().getUid();

        retrieveCurrentUserName();
        retrieveUserInfo();

        boolean nightMode = preference.getNightMode();
        toggleNightMode(view, nightMode);
    }

    private void initToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.profile_view_title);
        Tools.setSystemBarColorInt(this, getResources().getColor(R.color.default_status_color));
    }

    private void retrieveUserInfo() {
        userRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String firtname = Tools.getRefValue(dataSnapshot.child("firstname"));
                String lastname = Tools.getRefValue(dataSnapshot.child("lastname"));
                String location = Tools.getRefValue(dataSnapshot.child("location"));
                String phone = Tools.getRefValue(dataSnapshot.child("phonenumber"));
                String email = Tools.getRefValue(dataSnapshot.child("email"));
                String gender = Tools.getRefValue(dataSnapshot.child("gender"));
                String dateOfBirth = Tools.getRefValue(dataSnapshot.child("dateOfBirth"));
                String name = firtname + " " + lastname;
                String username = Tools.getRefValue(dataSnapshot.child("username"));
                token = Tools.getRefValue(dataSnapshot.child("device_token"));

                binding.profileName.setText(name);
                binding.nickname.setText(username);
                binding.email.setText(email);

                if (selectedLocale.equals("vi")) {
                    if (gender.equals("Male")) {
                        gender = "Nam";
                    } else if (gender.equals("Female")) {
                        gender = "Nữ";
                    }
                }

                binding.gender.setText(gender);
                binding.dateOfBirth.setText(dateOfBirth);
                binding.location.setText(location);
                binding.phone.setText(phone);
                toolbar.setTitle(name);

                if (dataSnapshot.child("image").exists()) {
                    imageUrl = dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(imageUrl).placeholder(R.mipmap.ic_launcher_round).into(image);

                    binding.image.setOnClickListener(view -> startImageViewerActivity());
                }

                manageChatRequests();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void manageChatRequests() {

        chatRequestRef.child(senderUserID) // has sent request
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(receiverUserID)) {
                            String request_type = dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();

                            if (request_type.equals("sent")) {
                                currentState = "request_sent";
                                binding.requestFriendship.setText(getString(R.string.requested));
                                binding.cancelFriendship.setText(getString(R.string.cancel));
                                binding.requestFriendship.setEnabled(false);
                                binding.cancelFriendship.setEnabled(true);
                                binding.cancelFriendship.setOnClickListener(view -> {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ProfileViewActivity.this);
                                    builder.setTitle(R.string.app_name);
                                    builder.setMessage(R.string.want_to_cancel_request);
                                    builder.setIcon(R.mipmap.ic_launcher_round);
                                    builder.setPositiveButton(R.string.yes, (dialog, id) -> {
                                        removeChatRequest();
                                        Toast.makeText(ProfileViewActivity.this, R.string.request_canceled, Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    });
                                    builder.setNegativeButton(R.string.no, (dialog, id) -> dialog.dismiss());
                                    AlertDialog alert = builder.create();
                                    alert.show();
                                });
                            } else { // need to accept, type receiver
                                currentState = "request_received";
                                binding.requestFriendship.setText(R.string.accept_request);
                                binding.cancelFriendship.setText(R.string.cancel);
                                binding.requestFriendship.setEnabled(true);
                                binding.cancelFriendship.setEnabled(true);
                                binding.requestFriendship.setOnClickListener(view -> acceptRequest());
                                binding.cancelFriendship.setOnClickListener(view -> {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ProfileViewActivity.this);
                                    builder.setTitle(R.string.app_name);
                                    builder.setMessage(R.string.want_to_cancel_request);
                                    builder.setIcon(R.mipmap.ic_launcher_round);
                                    builder.setPositiveButton(R.string.yes, (dialog, id) -> {
                                        removeChatRequest();
                                        Toast.makeText(ProfileViewActivity.this, R.string.request_canceled, Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    });
                                    builder.setNegativeButton(R.string.no, (dialog, id) -> dialog.dismiss());
                                    AlertDialog alert = builder.create();
                                    alert.show();
                                });
                            }
                        } else {
                            contactsRef.child(senderUserID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(receiverUserID)) {
                                                currentState = "friends";
                                                binding.requestFriendship.setText(R.string.send_message);
                                                binding.requestFriendship.setEnabled(true);
                                                binding.cancelFriendship.setEnabled(true);
                                                binding.cancelFriendship.setText(R.string.unfollow);

                                                binding.requestFriendship.setOnClickListener(view -> {
                                                    startChatActivity(receiverUserID, token);
                                                });

                                                binding.cancelFriendship.setOnClickListener(view -> {

                                                    AlertDialog.Builder builder = new AlertDialog.Builder(ProfileViewActivity.this);
                                                    builder.setTitle(R.string.app_name);
                                                    builder.setMessage(R.string.want_to_unfriend);
                                                    builder.setIcon(R.mipmap.ic_launcher_round);
                                                    builder.setPositiveButton(R.string.yes, (dialog, id) -> {
                                                        removeFriend();
                                                        Toast.makeText(ProfileViewActivity.this, R.string.unfriend_successfully, Toast.LENGTH_SHORT).show();
                                                        dialog.dismiss();

                                                        binding.requestFriendship.setOnClickListener(v12 -> sendChatRequest());
                                                    });
                                                    builder.setNegativeButton(R.string.no, (dialog, id) -> dialog.dismiss());
                                                    AlertDialog alert = builder.create();
                                                    alert.show();
                                                });
                                            } else {
                                                binding.requestFriendship.setText(R.string.request);
                                                binding.cancelFriendship.setText(R.string.cancel);
                                                binding.cancelFriendship.setEnabled(false);
                                                binding.requestFriendship.setEnabled(true);
                                                currentState = "new";
                                                binding.requestFriendship.setOnClickListener(view -> {
                                                    if (TextUtils.isEmpty(senderName.trim())) {
                                                        Toast.makeText(ProfileViewActivity.this, R.string.update_info_first, Toast.LENGTH_SHORT).show();
                                                        sendUserToProfileEditActivity();
                                                        return;
                                                    }
                                                    sendChatRequest();
                                                });
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
    }

    private void sendUserToProfileEditActivity() {
        Intent intent = new Intent(ProfileViewActivity.this, ProfileEditActivity.class);
        startActivity(intent);
    }

    private void startChatActivity(String receiverUserID, String token) {
        Intent profileIntent = new Intent(ProfileViewActivity.this, ChatActivity.class);
        profileIntent.putExtra("receiver_uid", receiverUserID);
        profileIntent.putExtra("receiver_token", token);
        startActivity(profileIntent);
    }

    private void acceptRequest() {
        contactsRef.child(currentUserID).child(receiverUserID).child("Friend")
                .setValue("Saved").addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                contactsRef.child(receiverUserID).child(currentUserID).child("Friend")
                        .setValue("Saved").addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        chatRequestRef.child(currentUserID).child(receiverUserID)
                                .removeValue()
                                .addOnCompleteListener(task112 -> {
                                    if (task112.isSuccessful()) {
                                        chatRequestRef.child(receiverUserID).child(currentUserID)
                                                .removeValue()
                                                .addOnCompleteListener(task11 -> {
                                                    if (task11.isSuccessful()) {
                                                        Toast.makeText(ProfileViewActivity.this, R.string.request_accepted, Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                });
                    }
                });
            }
        });
    }


    private void removeFriend() {

        contactsRef.child(receiverUserID).child(senderUserID)
                .removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        binding.requestFriendship.setEnabled(true);
                        currentState = "new";
                        binding.requestFriendship.setText(getString(R.string.request));
                        binding.cancelFriendship.setEnabled(false);
                        binding.cancelFriendship.setText(getString(R.string.cancel));
                    }
                });

        contactsRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        binding.requestFriendship.setEnabled(true);
                        currentState = "new";
                        binding.requestFriendship.setText(getString(R.string.request));
                        binding.cancelFriendship.setEnabled(false);
                        binding.cancelFriendship.setText(getString(R.string.cancel));
                    }
                });
    }

    private void removeChatRequest() {
        chatRequestRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        binding.requestFriendship.setEnabled(true);
                        currentState = "new";
                        binding.requestFriendship.setText(getString(R.string.request));
                        binding.cancelFriendship.setEnabled(false);
                        binding.cancelFriendship.setText(getString(R.string.cancel));
                    }
                });

        chatRequestRef.child(receiverUserID).child(senderUserID)
                .removeValue()
                .addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        binding.requestFriendship.setEnabled(true);
                        currentState = "new";
                        binding.requestFriendship.setText(getString(R.string.request));
                        binding.cancelFriendship.setEnabled(false);
                        binding.cancelFriendship.setText(getString(R.string.cancel));
                    }
                });
    }


    private void sendChatRequest() {
        chatRequestRef.child(senderUserID).child(receiverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        chatRequestRef.child(receiverUserID).child(senderUserID)
                                .child("request_type").setValue("received")
                                .addOnCompleteListener(task12 -> {
                                    if (task12.isSuccessful()) {
                                        currentState = "request_sent";
                                        binding.requestFriendship.setText(R.string.requested);
                                        binding.requestFriendship.setEnabled(false);
                                        binding.cancelFriendship.setEnabled(true);

                                        sendNotification(senderName + getString(R.string.sent_u_a_friend_request), getString(R.string.friend_request));
                                        Toast.makeText(ProfileViewActivity.this, R.string.request_sent, Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
    }

    private void sendNotification(String message, String username) {
        NotificationDataModel data = new NotificationDataModel(currentUserID, R.mipmap.ic_launcher_round, username, message, receiverUserID, token, "ProfileViewActivity");
        Sender sender = new Sender(token, data);
        apiService.sendNotification(sender)
                .enqueue(new Callback<MyResponse>() {
                    @Override
                    public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                        if (response.code() == 200) {
                            if (response.body().success == 1) {
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<MyResponse> call, Throwable t) {
                    }

                });

    }

    private void retrieveCurrentUserName() {
        userRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String firstname = Tools.getRefValue(dataSnapshot.child("firstname"));
                String lastname = Tools.getRefValue(dataSnapshot.child("lastname"));

                senderName = firstname + " " + lastname;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void startImageViewerActivity() {
        Intent intent = new Intent(ProfileViewActivity.this, ImageViewerActivity.class);
        intent.putExtra("imageUrl", imageUrl);
        startActivity(intent);
    }
}
