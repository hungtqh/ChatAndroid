package com.chatandroid.chat.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chatandroid.R;
import com.chatandroid.chat.activity.ChatActivity;
import com.chatandroid.chat.model.Messages;
import com.chatandroid.chat.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    private Context mContext;
    private List<User> mUser;
    private String lastMessage = "No message";

    public FirebaseAuth mAuth;
    public DatabaseReference RootRef;

    public UsersAdapter(Context mContext, List<User> mUsers) {
        this.mContext = mContext;
        this.mUser = mUsers;
    }

    @NonNull
    @Override
    public UsersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.users_display_layout, parent, false);
        return new UsersAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersAdapter.ViewHolder holder, int position) {
        User user = mUser.get(position);
        holder.username.setText(user.getUsername());
        holder.name.setText(user.getName());
        holder.statusOffline.setText(user.getStatus());
        holder.statusOffline.setText(user.getStatus());
        holder.uid.setText(user.getUid());
        holder.device_token.setText(user.getDevice_token());
        lastMessage(user.getUid(), holder.username, user.getUsername(), holder.mCount, holder.mTime);
        holder.cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ChatActivity.class);
                intent.putExtra("receiver_uid", holder.uid.getText().toString());
                intent.putExtra("receiver_token", holder.device_token.getText().toString());
                intent.putExtra("receiver_username", holder.username.getText().toString());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUser.size();
    }


    private void lastMessage(String user, TextView lastMessageView, String username, TextView mCount, TextView mTime) {
        mAuth = FirebaseAuth.getInstance();
        String currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        RootRef.child("Messages").child(currentUserID).child(user)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        long mMessages = 0;
                        String time = "";

                        if (dataSnapshot.exists()) {
                            mMessages = 0;
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                if (snapshot.exists()) {
                                    Messages messages = dataSnapshot.getValue(Messages.class);
                                    lastMessage = messages.getMessage();
                                    if (messages.getFrom().equals(currentUserID) && messages.getTo().equals(user) ||
                                            messages.getFrom().equals(user) && messages.getTo().equals(currentUserID)) {
                                        time = messages.getTime();
                                        if (!messages.getSeen()) {
                                            mMessages++;
                                        }

                                    }
                                }
                            }

                        }

                        mCount.setText(String.valueOf(mMessages));
                        mTime.setText(time);

                        if (!lastMessage.equals("No message")) {
                            mCount.setVisibility(View.VISIBLE);
                            mTime.setVisibility(View.VISIBLE);
                            lastMessageView.setText(lastMessage);
                        } else {
                            lastMessageView.setText(username);
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

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView device_token, name, username, statusOffline, statusOnline, uid, mTime, mCount;
        public View cv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTime = (TextView) itemView.findViewById(R.id.message_time);
            mCount = (TextView) itemView.findViewById(R.id.messages_count);
            name = (TextView) itemView.findViewById(R.id.name);
            username = (TextView) itemView.findViewById(R.id.username);
            statusOnline = (TextView) itemView.findViewById(R.id.status_online);
            statusOffline = (TextView) itemView.findViewById(R.id.status_offline);
            uid = (TextView) itemView.findViewById(R.id.uid);
            device_token = (TextView) itemView.findViewById(R.id.device_token);
            cv = (View) itemView.findViewById(R.id.lyt_parent);
        }


    }


}
