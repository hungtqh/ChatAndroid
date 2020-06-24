package com.chatandroid.chat.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chatandroid.Authenticate;
import com.chatandroid.R;
import com.chatandroid.activity.ProfileEditActivity;
import com.chatandroid.chat.adapter.MessageAdapter;
import com.chatandroid.chat.model.Message;
import com.chatandroid.chat.model.NotificationDataModel;
import com.chatandroid.databinding.ActivityChatTelegramBinding;
import com.chatandroid.notification.APIService;
import com.chatandroid.notification.Client;
import com.chatandroid.notification.MyResponse;
import com.chatandroid.notification.Sender;
import com.chatandroid.utils.AppPreference;
import com.chatandroid.utils.Config;
import com.chatandroid.utils.Tools;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ChatActivity extends Authenticate {
    private String messageReceiverID, messageSenderID;
    private AppPreference preference;
    private DatabaseReference usersRef;

    private final List<Message> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;
    private ProgressDialog loadingBar;

    private EmojIconActions emojIcon;

    private String saveCurrentTime, saveCurrentDate;
    private ActivityChatTelegramBinding binding;
    private String checker = "";
    private String senderName = "";

    private boolean notify = false;
    private boolean mTyping = false;
    private Handler mTypingHandler = new Handler();
    private static final int TYPING_TIMER_LENGTH = 1000;

    private Uri fileUri;

    String token = null;

    APIService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatTelegramBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        primaryMenu(savedInstanceState);

        messageSenderID = currentUserID;
        messageReceiverID = getIntent().getExtras().get("receiver_uid").toString();
        token = getIntent().getExtras().get("receiver_token").toString();
        preference = new AppPreference(ChatActivity.this);

        initializeControllers();

        apiService = Client.getClient(Config.NOTIFICATION_URI).create(APIService.class);

        binding.btnSend.setOnClickListener(view1 -> {
            notify = true;
            sendTextMessage();
        });

        binding.btnSendFile.setOnClickListener(view2 -> {
            notify = true;
            sendFile();
        });

        // current user
        rootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Message message = dataSnapshot.getValue(Message.class);
                        messagesList.add(message);
                        messageAdapter.notifyDataSetChanged();
                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        Message message = dataSnapshot.getValue(Message.class);
                        messagesList.remove(message);
                        messageAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        retrieveUserInfo();

        seenMessage();

        myTypingState();
        getTypingState();

        displayLastSeen();
    }

    @Override
    public void onResume() {
        super.onResume();
        preference.setCurrentChattingUser(messageReceiverID);
    }

    @Override
    public void onPause() {
        super.onPause();
        preference.setCurrentChattingUser("none");
    }

    private void initializeControllers() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);

        Tools.setSystemBarColorInt(this, getResources().getColor(R.color.default_status_color));

        loadingBar = new ProgressDialog(this);
        messageAdapter = new MessageAdapter(ChatActivity.this, messagesList);
        userMessagesList = binding.recyclerView;

        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        usersRef.keepSynced(true);

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        binding.image.setOnClickListener(v -> startProfileViewActivity(messageReceiverID));

        binding.lytBack.setOnClickListener(v -> onBackPressed());

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

    private void startProfileViewActivity(String messageReceiverID) {
        Intent profileIntent = new Intent(ChatActivity.this, ProfileViewActivity.class);
        profileIntent.putExtra("receiver_uid", messageReceiverID);
        startActivity(profileIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat_telegram, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_more) {
            Intent profileIntent = new Intent(ChatActivity.this, ProfileViewActivity.class);
            profileIntent.putExtra("receiver_uid", messageReceiverID);
            startActivity(profileIntent);
        }

        return true;
    }

    private void displayLastSeen() {
        rootRef.child("Users").child(messageReceiverID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        binding.username.setText(Tools.getRefValue(dataSnapshot.child("username")));

                        if (dataSnapshot.child("userState").hasChild("status")) {
                            String state = dataSnapshot.child("userState").child("status").getValue().toString();

                            binding.status.setText(state);

                            if (dataSnapshot.child("image").exists()) {
                                String userImage = dataSnapshot.child("image").getValue().toString();
                                Picasso.get().load(userImage).placeholder(R.mipmap.ic_launcher_round).into(binding.image);
                            }

                            if (state.equals("online")) {
                                binding.status.setTextColor(getResources().getColor(R.color.green_50));
                            } else if (state.equals("offline")) {
                                binding.status.setTextColor(getResources().getColor(R.color.red_500));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void sendTextMessage() {
        String messageText = binding.textContent.getText().toString();

        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, R.string.please_write_msg, Toast.LENGTH_SHORT).show();
        } else {
            String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
            String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

            DatabaseReference userMessageKeyRef = rootRef.child("Messages")
                    .child(messageSenderID).child(messageReceiverID).push();

            String messagePushID = userMessageKeyRef.getKey();
            String type = "text";

            saveMessageToDB(messageSenderRef, messageReceiverRef, messageText, messagePushID, type);
            binding.textContent.setText("");

            final String msg = messageText;
            if (notify) {
                sendNotification(msg, senderName + getString(R.string.sent_you_a_message));
                notify = false;
            }
        }
    }

    private void retrieveUserInfo() {
        usersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
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

    private void saveMessageToDB(String messageSenderRef, String messageReceiverRef, String messageText, String messagePushID, String type) {
        Map messageTextBody = new HashMap();
        messageTextBody.put("message", messageText);
        messageTextBody.put("type", type);
        messageTextBody.put("from", messageSenderID);
        messageTextBody.put("to", messageReceiverID);
        messageTextBody.put("messageID", messagePushID);
        messageTextBody.put("time", saveCurrentTime);
        messageTextBody.put("date", saveCurrentDate);
        messageTextBody.put("seen", false);

        Map messageBodyDetails = new HashMap();
        messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
        messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);

        rootRef.updateChildren(messageBodyDetails);
    }


    private void sendFile() {
        CharSequence[] options = new CharSequence[]{
                getString(R.string.images),
                getString(R.string.pdf_files),
                getString(R.string.ms_word_files)
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {

            loadingBar.setTitle(getString(R.string.send_file));
            loadingBar.setMessage(getString(R.string.sending_your_file));
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            fileUri = data.getData();
            if (checker.equals("image")) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

                String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = rootRef.child("Messages")
                        .child(messageSenderID).child(messageReceiverID).push();

                String messagePushID = userMessageKeyRef.getKey();

                StorageReference filePath = storageReference.child(messagePushID + ".jpg");

                filePath.putFile(fileUri).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        filePath.getDownloadUrl().addOnSuccessListener(uri -> {
                            String downloadedUrl = uri.toString();

                            saveMessageToDB(messageSenderRef, messageReceiverRef, downloadedUrl, messagePushID, checker);
                            Toast.makeText(ChatActivity.this, R.string.image_sent, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();

                            if (notify) {
                                sendNotification(senderName + getString(R.string.sent_you_a_photo), getString(R.string.new_mess_notification));
                                notify = false;
                            }
                        });
                    } else {
                        String message = task.getException().toString();
                        Toast.makeText(ChatActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });

            } else if (!checker.equals("image")) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");

                String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = rootRef.child("Messages")
                        .child(messageSenderID).child(messageReceiverID).push();

                String messagePushID = userMessageKeyRef.getKey();

                StorageReference filePath = storageReference.child(messagePushID + "." + checker);

                filePath.putFile(fileUri).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        filePath.getDownloadUrl().addOnSuccessListener(uri -> {
                            String downloadedUrl = uri.toString();

                            saveMessageToDB(messageSenderRef, messageReceiverRef, downloadedUrl, messagePushID, checker);
                            Toast.makeText(ChatActivity.this, R.string.file_sent, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();

                            if (notify) {
                                sendNotification(senderName + getString(R.string.sent_you_a_file), getString(R.string.new_mess_notification));
                                notify = false;
                            }
                        });
                    } else {
                        String message = task.getException().toString();
                        Toast.makeText(ChatActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(taskSnapshot -> {
                    double p = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    loadingBar.setMessage((int) p + getString(R.string.percent_upload));
                });

            } else {
                Toast.makeText(this, R.string.nothing_selected, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendNotification(String message, String username) {
        NotificationDataModel data = new NotificationDataModel(currentUserID, R.mipmap.ic_launcher_round, username, message, messageReceiverID, token, "ChatActivity");
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

    private void seenMessage() {
        // user at another device
        rootRef.child("Messages").child(messageReceiverID).child(messageSenderID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Message message = dataSnapshot.getValue(Message.class);

                        if (message.getTo().equals(currentUserID)) {
                            HashMap<String, Object> data = new HashMap<>();
                            data.put("seen", true);
                            dataSnapshot.getRef().updateChildren(data);
                            messageAdapter.notifyDataSetChanged();
                        }
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

    }

    private void myTypingState() {
        binding.textContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (!mTyping) {
                    mTyping = true;

                    updateTypingState("typing...");

                }

                mTypingHandler.removeCallbacks(onTypingTimeout);
                mTypingHandler.postDelayed(onTypingTimeout, TYPING_TIMER_LENGTH);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private Runnable onTypingTimeout = () -> {
        if (!mTyping) return;

        mTyping = false;

        updateTypingState("");

    };

    private void updateTypingState(String state) {

        HashMap<String, String> data = new HashMap<>();
        data.put("typingState", state);

        rootRef.child("Typing State").child(currentUserID).child(messageReceiverID)
                .setValue(data);
    }

    private void getTypingState() {
        rootRef.child("Typing State").child(messageReceiverID).child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String state = Tools.getRefValue(dataSnapshot.child("typingState"));
                        if (!state.equals("")) {
                            binding.status.setVisibility(View.GONE);
                            binding.typingStatus.setText(state);
                            binding.typingStatus.setVisibility(View.VISIBLE);
                        } else {
                            binding.status.setVisibility(View.VISIBLE);
                            binding.typingStatus.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}
