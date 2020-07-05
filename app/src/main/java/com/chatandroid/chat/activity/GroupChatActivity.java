package com.chatandroid.chat.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chatandroid.activity.Authentication;
import com.chatandroid.R;
import com.chatandroid.chat.adapter.GroupMessageAdapter;
import com.chatandroid.chat.model.GroupMessage;
import com.chatandroid.databinding.ActivityGroupChatBinding;
import com.chatandroid.utils.Tools;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;

public class GroupChatActivity extends Authentication {
    private Toolbar mToolbar;
    private ImageView sendMessageButton;
    private ImageView sendFileButton;
    private EditText userMessageInput;
    private ActivityGroupChatBinding binding;

    private EmojIconActions emojIcon;

    private RecyclerView userMessagesList;
    private LinearLayoutManager linearLayoutManager;
    private GroupMessageAdapter messageAdapter;

    private String checker = "";

    private FirebaseAuth mAuth;

    private ProgressDialog loadingBar;

    private Uri fileUri;

    private final List<GroupMessage> messagesList = new ArrayList<>();

    private DatabaseReference usersRef, groupNameRef, groupMessageKeyRef;

    private String currentGroupName, currentUserID, currentUsername, currentDate, currentTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityGroupChatBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        currentGroupName = getIntent().getExtras().get("groupName").toString();

        groupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        initializeFields();

        getUserInfo();

        (binding.lytBack).setOnClickListener(v -> onBackPressed());

        sendMessageButton.setOnClickListener(view1 -> {
            saveTextMessageInfoToDatabase();

            userMessageInput.setText("");
        });

        sendFileButton.setOnClickListener(view2 -> {
            sendFile();
        });

        boolean nightMode = preference.getNightMode();
        toggleNightMode(view, nightMode);

        groupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                GroupMessage message = dataSnapshot.getValue(GroupMessage.class);
                messagesList.add(message);
                messageAdapter.notifyDataSetChanged();
                userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        emojIcon = new EmojIconActions(this, binding.rootView, binding.textContent, binding.emojiBtn);
        emojIcon.ShowEmojIcon();
        emojIcon.setKeyboardListener(new EmojIconActions.KeyboardListener() {
            @Override
            public void onKeyboardOpen() {
                Log.e("Keyboard", "open");
            }

            @Override
            public void onKeyboardClose() {
                Log.e("Keyboard", "close");
            }
        });
    }

    private void sendFile() {
        CharSequence[] options = new CharSequence[]{
                getString(R.string.images),
                getString(R.string.pdf_files),
                getString(R.string.ms_word_files)
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(GroupChatActivity.this);
        builder.setTitle(R.string.select_file);

        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                checker = "image";

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent.createChooser(intent, getString(R.string.select_image)), 1);
            }

            if (which == 1) {
                checker = "pdf";

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("application/pdf");
                startActivityForResult(intent.createChooser(intent, getString(R.string.select_pdf)), 1);
            }

            if (which == 2) {
                checker = "docx";

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                String[] mimetypes = {"application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/msword"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                startActivityForResult(intent.createChooser(intent, getString(R.string.select_word)), 1);
            }
        });

        builder.show();
    }

    private void initializeFields() {
        mToolbar = binding.toolbar;
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);

        Tools.setSystemBarColorInt(this, getResources().getColor(R.color.default_status_color));

        loadingBar = new ProgressDialog(this);

        messageAdapter = new GroupMessageAdapter(GroupChatActivity.this, messagesList);
        userMessagesList = binding.recyclerView;
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);

        sendMessageButton = binding.btnSend;
        sendFileButton = binding.btnSendFile;

        userMessageInput = binding.textContent;
    }

    private void getUserInfo() {
        usersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String firtname = Tools.getRefValue(dataSnapshot.child("firstname"));
                    String lastname = Tools.getRefValue(dataSnapshot.child("lastname"));
                    currentUsername = firtname + " " + lastname;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void saveTextMessageInfoToDatabase() {
        String message = userMessageInput.getText().toString();
        String messageKey = groupNameRef.push().getKey();
        String type = "text";

        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, R.string.please_write_msg, Toast.LENGTH_SHORT).show();
        } else {
            saveMessage(message, messageKey, type);
        }
    }

    private void saveMessage(String message, String messageKey, String type) {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
        currentDate = currentDateFormat.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
        currentTime = currentTimeFormat.format(calForTime.getTime());

        HashMap<String, Object> groupMessageKey = new HashMap<>();
        groupNameRef.updateChildren(groupMessageKey);

        groupMessageKeyRef = groupNameRef.child(messageKey);

        HashMap<String, Object> messageInfoMap = new HashMap<>();
        messageInfoMap.put("name", currentUsername);
        messageInfoMap.put("message", message);
        messageInfoMap.put("date", currentDate);
        messageInfoMap.put("time", currentTime);
        messageInfoMap.put("from", currentUserID);
        messageInfoMap.put("type", type);

        groupMessageKeyRef.updateChildren(messageInfoMap);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {

            loadingBar.setTitle(getString(R.string.send_image));
            loadingBar.setMessage(getString(R.string.sending_image));
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            fileUri = data.getData();

            if (checker.equals("image")) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Group Files");

                String messageKey = groupNameRef.push().getKey();

                StorageReference filePath = storageReference.child(messageKey + ".jpg");

                filePath.putFile(fileUri).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        filePath.getDownloadUrl().addOnSuccessListener(uri -> {
                            String downloadedUrl = uri.toString();

                            saveMessage(downloadedUrl, messageKey, checker);
                            Toast.makeText(GroupChatActivity.this, R.string.image_sent, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        });
                    } else {
                        String message = task.getException().toString();
                        Toast.makeText(GroupChatActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });

            } else if (checker.equals("pdf") || checker.equals("docx")) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Group Document Files");

                String messageKey = groupNameRef.push().getKey();

                StorageReference filePath = storageReference.child(messageKey + "." + checker);

                filePath.putFile(fileUri).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        filePath.getDownloadUrl().addOnSuccessListener(uri -> {
                            String downloadedUrl = uri.toString();

                            saveMessage(downloadedUrl, messageKey, checker);
                            Toast.makeText(GroupChatActivity.this, R.string.file_sent, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        });
                    } else {
                        String message = task.getException().toString();
                        Toast.makeText(GroupChatActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(taskSnapshot -> {
                    double p = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    loadingBar.setMessage((int) p + getString(R.string.percent_upload));
                });
            } else {
                Toast.makeText(this, getString(R.string.nothing_selected), Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }
}
