package com.chatandroid.chat.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chatandroid.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsViewHolder extends RecyclerView.ViewHolder {
    public TextView name, username, statusOnline, statusOffline, uid, device_token, mTime, mCount;
    public CircleImageView profileImage;

    public ContactsViewHolder(@NonNull View itemView) {
        super(itemView);

        uid = itemView.findViewById(R.id.uid);
        name = itemView.findViewById(R.id.name);
        username = itemView.findViewById(R.id.username);
        statusOnline = itemView.findViewById(R.id.status_online);
        statusOffline = itemView.findViewById(R.id.status_online);
        profileImage = itemView.findViewById(R.id.profile_image);
        device_token = itemView.findViewById(R.id.device_token);
        mTime = (TextView) itemView.findViewById(R.id.message_time);
        mCount = (TextView) itemView.findViewById(R.id.messages_count);
    }
}
