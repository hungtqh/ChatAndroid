package com.chatandroid.chat.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chatandroid.R;
import com.chatandroid.chat.activity.ProfileViewActivity;
import com.chatandroid.chat.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {

    private Context mContext;
    private List<User> mUser;

    public FirebaseAuth mAuth;

    public FriendAdapter(Context mContext, List<User> mUsers) {
        this.mContext = mContext;
        this.mUser = mUsers;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.users_display_layout, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        User user = mUser.get(position);
        holder.username.setText(user.getUsername());
        holder.name.setText(user.getName());
        holder.uid.setText(user.getUid());
        holder.device_token.setText(user.getDevice_token());

        Picasso.get().load(user.getImage()).placeholder(R.mipmap.ic_launcher_round).into(holder.image);

        holder.cv.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, ProfileViewActivity.class);
            intent.putExtra("receiver_uid", holder.uid.getText().toString());
            mContext.startActivity(intent);
        });

        String userStatus = user.getUserState().getStatus();
        if (userStatus.equals("online")) {
            holder.ivOnline.setVisibility(View.VISIBLE);
        } else {
            holder.ivOnline.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mUser.size();
    }

    public class FriendViewHolder extends RecyclerView.ViewHolder {

        public TextView device_token, name, username, statusOffline, statusOnline, uid;
        public View cv;
        private CircleImageView image;
        private ImageView ivOnline;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            username = itemView.findViewById(R.id.username);
            statusOnline = itemView.findViewById(R.id.status_online);
            statusOffline = itemView.findViewById(R.id.status_offline);
            uid = itemView.findViewById(R.id.uid);
            device_token = itemView.findViewById(R.id.device_token);
            cv = itemView.findViewById(R.id.lyt_parent);
            image = itemView.findViewById(R.id.profile_image);
            ivOnline = itemView.findViewById(R.id.online_dot);
        }


    }


}
