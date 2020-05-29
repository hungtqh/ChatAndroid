package com.chatandroid.chat.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chatandroid.R;
import com.chatandroid.chat.activity.FindFriendsActivity;
import com.chatandroid.chat.activity.ProfileViewActivity;
import com.chatandroid.chat.model.Contacts;
import com.chatandroid.utils.Tools;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment
{
    private View PrivateChatsView;
    private RecyclerView friendList;

    private DatabaseReference FriendRef, UsersRef;
    private FirebaseAuth mAuth;
    private String currentUserID="";

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        PrivateChatsView = inflater.inflate(R.layout.fragment_friends, container, false);


        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        FriendRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUserID);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        friendList = (RecyclerView) PrivateChatsView.findViewById(R.id.chats_list);
        friendList.setHasFixedSize(true);
        friendList.setLayoutManager(new LinearLayoutManager(getContext()));


        FloatingActionButton floatingActionButton = (FloatingActionButton) PrivateChatsView.findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent findFriendsIntent = new Intent(getContext(), FindFriendsActivity.class);
                startActivity(findFriendsIntent);
            }
        });
        return PrivateChatsView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(FriendRef, Contacts.class)
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

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent profileIntent = new Intent(getContext(), ProfileViewActivity.class);
                            profileIntent.putExtra("user_uid", holder.uid.getText().toString());
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

        friendList.setAdapter(adapter);
        adapter.startListening();
    }


}
