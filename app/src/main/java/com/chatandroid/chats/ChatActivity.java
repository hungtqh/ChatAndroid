package com.chatandroid.chats;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chatandroid.Authenticate;
import com.chatandroid.R;
import com.chatandroid.chats.adapter.MessageAdapter;
import com.chatandroid.chats.model.NotificationDataModel;
import com.chatandroid.chats.model.NotificationModel;
import com.chatandroid.databinding.ActivityChatTelegramBinding;
import com.chatandroid.notifications.APIService;
import com.chatandroid.notifications.Client;
import com.chatandroid.notifications.MyResponse;
import com.chatandroid.notifications.Sender;
import com.chatandroid.utils.AppPreference;
import com.chatandroid.utils.Config;
import com.chatandroid.utils.Tools;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ChatActivity extends Authenticate
{
    private String messageReceiverID, messageSenderID;
    private ActionBar actionBar;
    private AppPreference preference;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;

    private String saveCurrentTime, saveCurrentDate;
    private ActivityChatTelegramBinding binding;

    public ChildEventListener seenListener;

    private  boolean notify = false;
    private boolean mTyping = false;
    private Handler mTypingHandler = new Handler();
    private static final int TYPING_TIMER_LENGTH = 1000;

    String token = null;

   APIService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityChatTelegramBinding.inflate(getLayoutInflater());
        View  view  = binding.getRoot();
        setContentView(view);
        primaryMenu(savedInstanceState);

        messageSenderID = currentUserID;
        messageReceiverID = getIntent().getExtras().get("receiver_uid").toString();
        token = getIntent().getExtras().get("receiver_token").toString();
        preference = new AppPreference(ChatActivity.this);

        IntializeControllers();

        apiService = Client.getClient(Config.NOTIFICATION_URI).create(APIService.class);

        binding.btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                notify = true;
                SendMessage();
            }
        });

        DisplayLastSeen();

    }

    @Override
    public  void onStop(){
        super.onStop();
    }

    @Override
    public void  onResume(){
        super.onResume();
        preference.setCurrentChatingUser(messageReceiverID);
    }

    @Override
    public  void  onPause(){
        super.onPause();
        preference.setCurrentChatingUser("none");
    }

    private void IntializeControllers()
    {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setTitle(null);

        Tools.setSystemBarColorInt(this, getResources().getColor(R.color.default_status_color));

        messageAdapter = new MessageAdapter(messagesList);
        userMessagesList = (RecyclerView) binding.recyclerView;
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        (binding.lytBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.textContent.addTextChangedListener(contentWatcher);
    }

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        hideKeyboard();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat_telegram, menu);
        return true;
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private TextWatcher contentWatcher = new TextWatcher() {
        @Override
        public void afterTextChanged(Editable etd) {
            if (etd.toString().trim().length() == 0) {
                binding.btnSend.setImageResource(R.drawable.ic_mic);
            } else {
                binding.btnSend.setImageResource(R.drawable.ic_send);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }
    };

    private void DisplayLastSeen()
    {
        RootRef.child("Users").child(messageReceiverID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        binding.username.setText(Tools.getRefValue(dataSnapshot.child("username")));

                        if (dataSnapshot.child("userState").hasChild("status"))
                        {
                            String state = dataSnapshot.child("userState").child("status").getValue().toString();
                            String date = dataSnapshot.child("userState").child("date").getValue().toString();
                            String time = dataSnapshot.child("userState").child("time").getValue().toString();

                            binding.status.setText(state);

                            if (state.equals("online"))
                            {
                                binding.status.setTextColor(getResources().getColor(R.color.green_50));
                            }
                            else if (state.equals("offline"))
                            {
                                binding.status.setTextColor(getResources().getColor(R.color.red_500));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    @Override
    protected void onStart()
    {
        super.onStart();

       RootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s)
                    {
                        Messages messages = dataSnapshot.getValue(Messages.class);
                        messagesList.add(messages);
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
                seenMessage();

        myTypingState();
        getTypingState();
    }

    private void SendMessage()
    {
        String messageText = binding.textContent.getText().toString();

        if (TextUtils.isEmpty(messageText))
        {
            Toast.makeText(this, "first write your message...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
            String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

            DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                    .child(messageSenderID).child(messageReceiverID).push();

            String messagePushID = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderID);
            messageTextBody.put("to", messageReceiverID);
            messageTextBody.put("messageID", messagePushID);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);
            messageTextBody.put("seen", false);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
            messageBodyDetails.put( messageReceiverRef + "/" + messagePushID, messageTextBody);

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task)
                {
                    binding.textContent.setText("");

                    if (task.isSuccessful())
                    {
                      final   DatabaseReference reference = RootRef.child("Friends").child(currentUserID).child(messageReceiverID);
                      reference.addValueEventListener(new ValueEventListener() {
                          @Override
                          public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                              if(!dataSnapshot.exists()){
                                  reference.child("id").setValue(messageReceiverID);
                              }
                          }

                          @Override
                          public void onCancelled(@NonNull DatabaseError databaseError) {

                          }
                      });
                    }
                }
            });

            final String msg = messageText;
            if(notify){
                sendNotification("New message",msg);
                notify = false;
            }
        }
    }

    private  void sendNotification(String message, String username){
        NotificationModel notification = new NotificationModel("New message", message);
        NotificationDataModel data = new NotificationDataModel(currentUserID,R.mipmap.ic_launcher_round, username,message,messageReceiverID,token);
        Sender sender = new Sender(token, data);
        apiService.sendNotification(sender)
                .enqueue(new Callback<MyResponse>() {
                    @Override
                    public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                        if(response.code() == 200){
                            if(response.body().success == 1){
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call<MyResponse> call, Throwable t) {
                    }

                });

    }

    private void seenMessage(){

        seenListener =  RootRef.child("Messages").child(messageReceiverID).child(messageSenderID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s)
                    {
                        Messages messages = dataSnapshot.getValue(Messages.class);

                        if(messages.getTo().equals(currentUserID)){
                                HashMap<String, Object> data = new HashMap<>();
                                data.put("seen",true);
                                dataSnapshot.getRef().updateChildren(data);
                            }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                        Messages messages = dataSnapshot.getValue(Messages.class);

                        if(messages.getTo().equals(currentUserID)){
                            HashMap<String, Object> data = new HashMap<>();
                            data.put("seen",true);
                            dataSnapshot.getRef().updateChildren(data);
                            messageAdapter.notifyDataSetChanged();
                        }

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

    private void myTypingState(){
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

    private Runnable onTypingTimeout = new Runnable() {
        @Override
        public void run() {
            if (!mTyping) return;

            mTyping = false;

            updateTypingState("");

        }
    };

    private void updateTypingState(String state){

        HashMap <String, String> data = new HashMap<>();
                        data.put("typingState",state);

        RootRef.child("Typing State").child(currentUserID).child(messageReceiverID)
                .setValue(data)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {

                    }
                });
    }

    private void getTypingState(){
        RootRef.child("Typing State").child(messageReceiverID).child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String state = Tools.getRefValue(dataSnapshot.child("typingState"));
                        if(!state.equals("")){
                            binding.status.setVisibility(View.GONE);
                            binding.typingStatus.setText(state);
                            binding.typingStatus.setVisibility(View.VISIBLE);
                        }else{
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
