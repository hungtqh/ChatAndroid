package com.chatandroid.chats.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chatandroid.R;
import com.chatandroid.chats.Messages;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>
{
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;

    private final int CHAT_ME = 100;
    private final int CHAT_YOU = 200;

    public MessageAdapter (List<Messages> userMessagesList)
    {
        this.userMessagesList = userMessagesList;
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView messageText, messageTime;
        public ImageView workStatus, workStatusDone;


        public MessageViewHolder(@NonNull View itemView)
        {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.text_content);
            messageTime = (TextView) itemView.findViewById(R.id.text_time);
            workStatus = (ImageView) itemView.findViewById(R.id.work_status);
            workStatusDone = (ImageView) itemView.findViewById(R.id.work_status_done);
        }
    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        mAuth = FirebaseAuth.getInstance();

        if (viewType == CHAT_ME) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_telegram_me, parent, false);
            return new MessageViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_whatsapp_telegram_you, parent, false);
            return new MessageViewHolder(v);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, int i)
    {
        Messages messages = userMessagesList.get(i);

        String fromMessageType = messages.getType();


        if (fromMessageType.equals("text"))
        {
            messageViewHolder.messageText.setText(messages.getMessage());
            messageViewHolder.messageTime.setText(messages.getTime());

            if(messages.getSeen()){
                if(messageViewHolder.workStatusDone != null && messageViewHolder.workStatus != null){
                    messageViewHolder.workStatusDone.setVisibility(View.VISIBLE);
                    messageViewHolder.workStatus.setVisibility(View.INVISIBLE);
                }

            }else{
                if(messageViewHolder.workStatusDone != null && messageViewHolder.workStatus != null) {
                    messageViewHolder.workStatus.setVisibility(View.VISIBLE);
                    messageViewHolder.workStatusDone.setVisibility(View.INVISIBLE);
                }
            }


        }

    }



    @Override
    public int getItemCount()
    {
        return userMessagesList.size();
    }

    @Override
    public int getItemViewType(int position) {
        mAuth = FirebaseAuth.getInstance();
        return this.userMessagesList.get(position).getFrom().equals(mAuth.getCurrentUser().getUid()) ? CHAT_ME : CHAT_YOU;
    }

}
