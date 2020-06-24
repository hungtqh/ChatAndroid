package com.chatandroid.chat.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chatandroid.R;
import com.chatandroid.chat.activity.ChatActivity;
import com.chatandroid.chat.adapter.ContactsViewHolder;
import com.chatandroid.chat.model.Contact;
import com.chatandroid.chat.model.Message;
import com.chatandroid.utils.Tools;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    public DatabaseReference rootRef;

    private View privateChatsView;
    private RecyclerView chatsList;
    private CircleImageView image;

    private DatabaseReference friendsRef, usersRef;
    private FirebaseAuth mAuth;
    private String currentUserID = "";


    private String lastMessage = "No message";

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        privateChatsView = inflater.inflate(R.layout.fragment_chats, container, false);

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        currentUserID = mAuth.getCurrentUser().getUid();
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUserID);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatsList = (RecyclerView) privateChatsView.findViewById(R.id.chats_list);
        chatsList.setHasFixedSize(true);
        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return privateChatsView;

    }


    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Contact>()
                        .setQuery(friendsRef, Contact.class)
                        .build();


        final FirebaseRecyclerAdapter<Contact, ContactsViewHolder> adapter
                = new FirebaseRecyclerAdapter<Contact, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull Contact model) {

                final String userID = getRef(position).getKey();

                usersRef.child(userID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String uid = Tools.getRefValue(dataSnapshot.child("uid"));
                        String firtname = Tools.getRefValue(dataSnapshot.child("firstname"));
                        String lastname = Tools.getRefValue(dataSnapshot.child("lastname"));
                        String username = Tools.getRefValue(dataSnapshot.child("username"));
                        String name = firtname + " " + lastname;

                        image = holder.profileImage;

                        if (dataSnapshot.child("image").exists()) {
                            String userImage = Tools.getRefValue(dataSnapshot.child("image"));
                            Picasso.get().load(userImage).placeholder(R.mipmap.ic_launcher_round).into(image);
                        }

                        if (dataSnapshot.child("userState").hasChild("status")) {
                            String status = Tools.getRefValue(dataSnapshot.child("userState").child("status"));

                            if (status.equals("online")) {
                                holder.onlineGreen.setVisibility(View.VISIBLE);
                            } else {
                                holder.onlineGreen.setVisibility(View.INVISIBLE);
                            }
                        }

                        holder.uid.setText(uid);
                        holder.username.setText(username);
                        holder.name.setText(name);
                        holder.device_token.setText(Tools.getRefValue(dataSnapshot.child("device_token")));

                        if (mAuth.getCurrentUser() != null) {
                            lastMessage(currentUserID, holder.username, username, holder.mCount, holder.mTime);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                holder.itemView.setOnClickListener(v -> {
                    Intent profileIntent = new Intent(getContext(), ChatActivity.class);
                    profileIntent.putExtra("receiver_uid", holder.uid.getText().toString());
                    profileIntent.putExtra("receiver_token", holder.device_token.getText().toString());
                    startActivity(profileIntent);
                });
            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                return viewHolder;
            }
        };

        chatsList.setAdapter(adapter);
        adapter.startListening();
    }

    private void lastMessage(String user, TextView lastMessageView, String username, TextView mCount, TextView mTime) {
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages").child(currentUserID).child(user)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        long mMessages = 0;
                        String time = "";

                        if (dataSnapshot.exists()) {
                            mMessages = 0;
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                if (snapshot.exists()) {
                                    Message message = dataSnapshot.getValue(Message.class);
                                    lastMessage = message.getMessage();
                                    if (message.getFrom().equals(currentUserID) && message.getTo().equals(user) ||
                                            message.getFrom().equals(user) && message.getTo().equals(currentUserID)) {
                                        time = message.getTime();
                                        if (!message.getSeen()) {
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
}
