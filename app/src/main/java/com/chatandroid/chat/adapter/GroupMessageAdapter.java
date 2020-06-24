package com.chatandroid.chat.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
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
import com.chatandroid.chat.activity.ImageViewerActivity;
import com.chatandroid.chat.activity.ProfileViewActivity;
import com.chatandroid.chat.model.GroupMessage;
import com.chatandroid.chat.model.Message;
import com.chatandroid.utils.AppPreference;
import com.chatandroid.utils.Tools;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GroupMessageAdapter extends RecyclerView.Adapter<GroupMessageAdapter.MessageViewHolder> {
    private List<GroupMessage> groupMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private Context mContext;
    private AppPreference preference;

    private final int CHAT_ME = 100;
    private final int CHAT_YOU = 200;

    public GroupMessageAdapter(Context mContext, List<GroupMessage> groupMessagesList) {
        this.groupMessagesList = groupMessagesList;
        this.mContext = mContext;
        mAuth = FirebaseAuth.getInstance();
        preference = new AppPreference(mContext);
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, messageTime, messageDate, username;
        CircularImageView profileImageYou;
        CardView cardView;
        ImageView imageMessage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            messageText = itemView.findViewById(R.id.text_content);
            messageTime = itemView.findViewById(R.id.text_time);
            messageDate = itemView.findViewById(R.id.text_date);
            profileImageYou = itemView.findViewById(R.id.profile_image_group);
            cardView = itemView.findViewById(R.id.card_view_group_message);
            imageMessage = itemView.findViewById(R.id.group_image_message);
        }
    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mAuth = FirebaseAuth.getInstance();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        if (viewType == CHAT_ME) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_chat_me, parent, false);
            return new MessageViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_chat_you, parent, false);
            return new MessageViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, int i) {
        GroupMessage message = groupMessagesList.get(i);
        String fromMessageType = message.getType();

        if (getItemViewType(i) == CHAT_YOU) {
            retrieveUserInfo(message.getFrom(), messageViewHolder);
        }

        if (fromMessageType.equals("text")) {
            messageViewHolder.imageMessage.setVisibility(View.GONE);
            messageViewHolder.cardView.setVisibility(View.VISIBLE);

            messageViewHolder.username.setText(message.getName());
            messageViewHolder.messageText.setText(message.getMessage());

            String selectedLocale = preference.getAppLanguage();
            String messageTime = message.getTime();
            String messageDate = message.getDate();

            if (selectedLocale.equals("vi")) {
                messageTime = messageTime.replace("PM", "CH");
                messageTime = messageTime.replace("AM", "SA");

                messageDate = messageDate.replace("Jan", "Th1");
                messageDate = messageDate.replace("Feb", "Th2");
                messageDate = messageDate.replace("Mar", "Th3");
                messageDate = messageDate.replace("Apr", "Th4");
                messageDate = messageDate.replace("May", "Th5");
                messageDate = messageDate.replace("Jun", "Th6");
                messageDate = messageDate.replace("Jul", "Th7");
                messageDate = messageDate.replace("Aug", "Th8");
                messageDate = messageDate.replace("Sep", "Th9");
                messageDate = messageDate.replace("Oct", "Th10");
                messageDate = messageDate.replace("Nov", "Th11");
                messageDate = messageDate.replace("Dec", "Th12");
            } else {
                messageTime = messageTime.replace("CH", "PM");
                messageTime = messageTime.replace("SA", "AM");

                messageDate = messageDate.replace("Th1", "Jan");
                messageDate = messageDate.replace("Th2", "Feb");
                messageDate = messageDate.replace("Th3", "Mar");
                messageDate = messageDate.replace("Th4", "Apr");
                messageDate = messageDate.replace("Th5", "May");
                messageDate = messageDate.replace("Th6", "Jun");
                messageDate = messageDate.replace("Jul", "Th7");
                messageDate = messageDate.replace("Th8", "Aug");
                messageDate = messageDate.replace("Th9", "Sep");
                messageDate = messageDate.replace("Th10", "Oct");
                messageDate = messageDate.replace("Th11", "Nov");
                messageDate = messageDate.replace("Th12", "Dec");
            }

            messageViewHolder.messageTime.setText(messageTime);
            messageViewHolder.messageDate.setText(messageDate);
        } else if (fromMessageType.equals("image")) {
            messageViewHolder.imageMessage.setVisibility(View.VISIBLE);
            messageViewHolder.cardView.setVisibility(View.GONE);

            Picasso.get().load(message.getMessage()).into(messageViewHolder.imageMessage);
        } else {
            messageViewHolder.imageMessage.setVisibility(View.VISIBLE);
            messageViewHolder.cardView.setVisibility(View.GONE);

            messageViewHolder.imageMessage.setImageResource(R.drawable.file);
        }

        // options with pdf and docx file
        messageViewHolder.itemView.setOnClickListener(v -> {

            if (fromMessageType.equals("pdf") || fromMessageType.equals("docx")) {
                CharSequence[] options = new CharSequence[]{
                        mContext.getString(R.string.download_and_view_document),
                        mContext.getString(R.string.cancel)
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

                builder.setTitle(R.string.select_option);

                builder.setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        downloadContent(message);
                    }
                });

                builder.show();
            } else if (fromMessageType.equals("image")) {
                CharSequence[] options = new CharSequence[]{
                        mContext.getString(R.string.view_this_image),
                        mContext.getString(R.string.download_this_image),
                        mContext.getString(R.string.cancel)
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

                builder.setTitle(R.string.select_option);

                builder.setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        startImageViewerActivity(i);
                    }

                    if (which == 1) {
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
                        mContext.getString(R.string.copy_text),
                        mContext.getString(R.string.cancel)
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

                builder.setTitle(R.string.select_option);

                builder.setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Tools.copyToClipboard(mContext, message.getMessage());
                    }
                });

                builder.show();
            }

            return true;
        });
    }

    @Override
    public int getItemCount() {
        return groupMessagesList.size();
    }

    @Override
    public int getItemViewType(int position) {
        mAuth = FirebaseAuth.getInstance();
        return this.groupMessagesList.get(position).getFrom().equals(mAuth.getCurrentUser().getUid()) ? CHAT_ME : CHAT_YOU;
    }

    private void retrieveUserInfo(String userId, MessageViewHolder messageViewHolder) {
        userRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String userImage = Tools.getRefValue(dataSnapshot.child("image"));

                CircularImageView userProfileImage = messageViewHolder.profileImageYou;

                if (!userImage.isEmpty()) {
                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                }

                messageViewHolder.profileImageYou.setOnClickListener(v -> {
                    Intent intent = new Intent(mContext, ProfileViewActivity.class);
                    intent.putExtra("receiver_uid", userId);
                    mContext.startActivity(intent);
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void startImageViewerActivity(int position) {
        Intent intent = new Intent(mContext, ImageViewerActivity.class);
        intent.putExtra("imageUrl", groupMessagesList.get(position).getMessage());
        mContext.startActivity(intent);
    }

    private void downloadContent(GroupMessage message) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(message.getMessage()));
        mContext.startActivity(intent);
    }
}

