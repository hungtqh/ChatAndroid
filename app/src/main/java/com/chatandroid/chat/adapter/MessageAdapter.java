package com.chatandroid.chat.adapter;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.chatandroid.R;
import com.chatandroid.chat.activity.ChatActivity;
import com.chatandroid.chat.activity.ImageViewerActivity;
import com.chatandroid.chat.activity.ProfileViewActivity;
import com.chatandroid.chat.model.Message;
import com.chatandroid.utils.Tools;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.List;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Message> userMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private Context mContext;

    private final int CHAT_ME = 100;
    private final int CHAT_YOU = 200;

    public MessageAdapter(Context mContext, List<Message> userMessageList) {
        this.mContext = mContext;
        this.userMessageList = userMessageList;
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageText, messageTime;
        public ImageView workStatus, workStatusDone, imageMessage;
        public CardView cardView;
        public CircularImageView profileImageChat;


        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.text_content);
            messageTime = itemView.findViewById(R.id.text_time);
            workStatus = itemView.findViewById(R.id.work_status);
            workStatusDone = itemView.findViewById(R.id.work_status_done);
            imageMessage = itemView.findViewById(R.id.image_message);
            cardView = itemView.findViewById(R.id.card_view_text_message);
            profileImageChat = itemView.findViewById(R.id.profile_image_chat);
        }
    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        if (viewType == CHAT_ME) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_telegram_me, parent, false);
            return new MessageViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_telegram_you, parent, false);
            return new MessageViewHolder(v);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, int i) {
        Message message = userMessageList.get(i);

        String fromMessageType = message.getType();

        if (fromMessageType.equals("text")) {
            messageViewHolder.cardView.setVisibility(View.VISIBLE);
            messageViewHolder.imageMessage.setVisibility(View.GONE);

            messageViewHolder.messageText.setText(message.getMessage());
            messageViewHolder.messageTime.setText(message.getTime());

            if (message.getSeen()) {
                if (messageViewHolder.workStatusDone != null && messageViewHolder.workStatus != null) {
                    messageViewHolder.workStatusDone.setVisibility(View.VISIBLE);
                    messageViewHolder.workStatus.setVisibility(View.INVISIBLE);
                }

            } else {
                if (messageViewHolder.workStatusDone != null && messageViewHolder.workStatus != null) {
                    messageViewHolder.workStatus.setVisibility(View.VISIBLE);
                    messageViewHolder.workStatusDone.setVisibility(View.INVISIBLE);
                }
            }
        } else if (fromMessageType.equals("image")) {
            messageViewHolder.imageMessage.setVisibility(View.VISIBLE);
            messageViewHolder.cardView.setVisibility(View.GONE);

            Picasso.get().load(message.getMessage()).into(messageViewHolder.imageMessage);
        } else {
            messageViewHolder.imageMessage.setVisibility(View.VISIBLE);
            messageViewHolder.cardView.setVisibility(View.GONE);

            messageViewHolder.imageMessage.setImageResource(R.drawable.file);
        }

        // user can only delete their own message
        if (getItemViewType(i) == CHAT_ME) {

            // options with pdf and docx file
            messageViewHolder.itemView.setOnClickListener(v -> {

                if (fromMessageType.equals("pdf") || fromMessageType.equals("docx")) {
                    CharSequence[] options = new CharSequence[]{
                            "Delete for me",
                            "Delete for everyone",
                            "Download and view this document",
                            "Cancel"
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

                    builder.setTitle("Select option");

                    builder.setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            deleteSenderMessage(i);
                        }

                        if (which == 1) {
                            deleteMessageForEveryone(i);
                        }

                        if (which == 2) {
                            downloadContent(message);
                        }
                    });

                    builder.show();
                } else if (fromMessageType.equals("image")) {
                    CharSequence[] options = new CharSequence[]{
                            "Delete for me",
                            "Delete for everyone",
                            "View this image",
                            "Download this image",
                            "Cancel"
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

                    builder.setTitle("Select option");

                    builder.setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            deleteSenderMessage(i);
                        }

                        if (which == 1) {
                            deleteMessageForEveryone(i);
                        }

                        if (which == 2) {
                            startImageViewerActivity(i);
                        }

                        if (which == 3) {
                            downloadContent(message);
                        }
                    });

                    builder.show();
                }
            });

            // options with text message
            messageViewHolder.itemView.setOnLongClickListener(v -> {

                if (fromMessageType.equals("text")) {
                    CharSequence[] options = new CharSequence[]{
                            "Copy text",
                            "Delete for me",
                            "Delete for everyone",
                            "Cancel"
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

                    builder.setTitle("Select option");

                    builder.setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            Tools.copyToClipboard(mContext, message.getMessage());
                        }

                        if (which == 1) {
                            deleteSenderMessage(i);
                        }

                        if (which == 2) {
                            deleteMessageForEveryone(i);
                        }
                    });

                    builder.show();
                }

                return true;
            });
        } else { // receiver
            String receiverId = message.getFrom();
            retrieveUserInfo(receiverId, messageViewHolder);

            messageViewHolder.profileImageChat.setOnClickListener(v -> {
                startProfileViewActivity(receiverId);
            });

            // options with pdf and docx file
            messageViewHolder.imageMessage.setOnClickListener(v -> {

                if (fromMessageType.equals("pdf") || fromMessageType.equals("docx")) {
                    CharSequence[] options = new CharSequence[]{
                            "Delete for me",
                            "Download and view this document",
                            "Cancel"
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

                    builder.setTitle("Select option");

                    builder.setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            deleteReceiverMessage(i);
                        }

                        if (which == 1) {
                            downloadContent(message);
                        }

                    });

                    builder.show();
                } else if (fromMessageType.equals("image")) {
                    CharSequence[] options = new CharSequence[]{
                            "Delete for me",
                            "View this image",
                            "Download this image",
                            "Cancel"
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

                    builder.setTitle("Select option");

                    builder.setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            deleteReceiverMessage(i);
                        }

                        if (which == 1) {
                            startImageViewerActivity(i);
                        }

                        if (which == 2) {
                            downloadContent(message);
                        }
                    });

                    builder.show();
                }
            });

            // options with text message
            messageViewHolder.itemView.setOnLongClickListener(v -> {

                if (fromMessageType.equals("text")) {
                    CharSequence[] options = new CharSequence[]{
                            "Copy text",
                            "Delete for me",
                            "Cancel"
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

                    builder.setTitle("Select option");

                    builder.setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            Tools.copyToClipboard(mContext, message.getMessage());
                        }

                        if (which == 1) {
                            deleteReceiverMessage(i);
                        }
                    });

                    builder.show();
                }

                return true;
            });
        }
    }

    private void startProfileViewActivity(String receiverId) {
        Intent profileIntent = new Intent(mContext, ProfileViewActivity.class);
        profileIntent.putExtra("receiver_uid", receiverId);
        mContext.startActivity(profileIntent);
    }

    @Override
    public int getItemCount() {
        return userMessageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        mAuth = FirebaseAuth.getInstance();
        return this.userMessageList.get(position).getFrom().equals(mAuth.getCurrentUser().getUid()) ? CHAT_ME : CHAT_YOU;
    }

    private void retrieveUserInfo(String userId, MessageViewHolder messageViewHolder) {
        userRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String userImage = Tools.getRefValue(dataSnapshot.child("image"));

                CircularImageView userProfileImage = messageViewHolder.profileImageChat;

                if (!userImage.isEmpty()) {
                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void startImageViewerActivity(int position) {
        Intent intent = new Intent(mContext, ImageViewerActivity.class);
        intent.putExtra("imageUrl", userMessageList.get(position).getMessage());
        mContext.startActivity(intent);
    }

    private void deleteSenderMessage(final int position) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        rootRef.child("Messages").child(userMessageList.get(position).getFrom())
                .child(userMessageList.get(position).getTo())
                .child(userMessageList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(mContext, "Deleted successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, "Error occurred while deleting the message.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteReceiverMessage(final int position) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        rootRef.child("Messages").child(userMessageList.get(position).getTo())
                .child(userMessageList.get(position).getFrom())
                .child(userMessageList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(mContext, "Deleted successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, "Error occurred while deleting the message.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteMessageForEveryone(final int position) {
        deleteSenderMessage(position);
        deleteReceiverMessage(position);
    }

    private void downloadContent(Message message) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(message.getMessage()));
        mContext.startActivity(intent);
    }
}
