package com.chatandroid.chats;

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


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    final private String TAG = "kelly ChatsFragment";
    public DatabaseReference RootRef;

    private View PrivateChatsView;
    private RecyclerView chatsList;

    private DatabaseReference FriendsRef, UsersRef;
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
        PrivateChatsView = inflater.inflate(R.layout.fragment_chats, container, false);

        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();
        currentUserID = mAuth.getCurrentUser().getUid();
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUserID);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatsList = (RecyclerView) PrivateChatsView.findViewById(R.id.chats_list);
        chatsList.setHasFixedSize(true);
        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return PrivateChatsView;

    }


    @Override
    public void onStart()
    {
        super.onStart();
        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(FriendsRef, Contacts.class)
                        .build();


        final FirebaseRecyclerAdapter<Contacts, ContactsFragment.ContactsViewHolder> adapter
                = new FirebaseRecyclerAdapter<Contacts, ContactsFragment.ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsFragment.ContactsViewHolder holder, int position, @NonNull Contacts model)
            {

                final String userID = getRef(position).getKey();

                UsersRef.child(userID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {

                        String userImage = Tools.getRefValue(dataSnapshot.child("image"));
                        String uid = Tools.getRefValue(dataSnapshot.child("uid"));
                        String firtname = Tools.getRefValue(dataSnapshot.child("firstname"));
                        String lastname = Tools.getRefValue(dataSnapshot.child("lastname"));
                        String username = Tools.getRefValue(dataSnapshot.child("username"));
                        String name = firtname + " " + lastname;

                        holder.uid.setText(uid);
                        holder.username.setText(username);
                        holder.name.setText(name);
                        holder.device_token.setText(Tools.getRefValue(dataSnapshot.child("device_token")));
                        lastMessage(currentUserID,holder.username,username,holder.mCount,holder.mTime);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent profileIntent = new Intent(getContext(), ChatActivity.class);
                        profileIntent.putExtra("receiver_uid", holder.uid.getText().toString());
                        profileIntent.putExtra("receiver_token", holder.device_token.getText().toString());
                        startActivity(profileIntent);
                    }
                });

            }

            @NonNull
            @Override
            public ContactsFragment.ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
            {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                ContactsFragment.ContactsViewHolder viewHolder = new ContactsFragment.ContactsViewHolder(view);
                return viewHolder;
            }
        };

        chatsList.setAdapter(adapter);
        adapter.startListening();
    }

    private void lastMessage(String user, TextView lastMessageView, String username, TextView mCount, TextView mTime){
        mAuth = FirebaseAuth.getInstance();
        String currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        RootRef.child("Messages").child(currentUserID).child(user)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s)
                    {
                        long mMessages = 0;
                        String time = "";

                        if(dataSnapshot.exists()){
                            mMessages = 0;
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                if(snapshot.exists()){
                                    Messages messages = dataSnapshot.getValue(Messages.class);
                                    lastMessage = messages.getMessage();
                                    if(messages.getFrom().equals(currentUserID) && messages.getTo().equals(user) ||
                                            messages.getFrom().equals(user) && messages.getTo().equals(currentUserID)){
                                        time = messages.getTime();
                                        if(!messages.getSeen()){
                                            mMessages++;
                                        }

                                    }
                                }
                            }

                        }

                        mCount.setText(String.valueOf(mMessages));
                        mTime.setText(time);

                        if(!lastMessage.equals("No message")){
                            mCount.setVisibility(View.VISIBLE);
                            mTime.setVisibility(View.VISIBLE);
                            lastMessageView.setText(lastMessage);
                        }else{
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
