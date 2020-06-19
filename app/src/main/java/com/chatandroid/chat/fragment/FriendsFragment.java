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
import com.chatandroid.chat.adapter.ContactsViewHolder;
import com.chatandroid.chat.model.Contact;
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
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {
    private View privateChatsView;
    private RecyclerView friendList;
    private CircleImageView image;

    private DatabaseReference friendRef, usersRef;
    private FirebaseAuth mAuth;
    private String currentUserID = "";

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        privateChatsView = inflater.inflate(R.layout.fragment_friends, container, false);


        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        friendRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUserID);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        friendList = privateChatsView.findViewById(R.id.chats_list);
        friendList.setHasFixedSize(true);
        friendList.setLayoutManager(new LinearLayoutManager(getContext()));


        FloatingActionButton floatingActionButton = privateChatsView.findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(v -> {
            Intent findFriendsIntent = new Intent(getContext(), FindFriendsActivity.class);
            startActivity(findFriendsIntent);
        });
        return privateChatsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Contact>()
                        .setQuery(friendRef, Contact.class)
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



                        if (dataSnapshot.child("userState").hasChild("status")) {
                            String status = Tools.getRefValue(dataSnapshot.child("userState").child("status"));

                            if (status.equals("online")) {
                                holder.onlineGreen.setVisibility(View.VISIBLE);
                            } else {
                                holder.onlineGreen.setVisibility(View.INVISIBLE);
                            }
                        }

                        image = holder.profileImage;

                        if (dataSnapshot.child("image").exists()) {
                            String userImage = Tools.getRefValue(dataSnapshot.child("image"));
                            Picasso.get().load(userImage).placeholder(R.mipmap.ic_launcher_round).into(image);
                        }

                        holder.uid.setText(uid);
                        holder.username.setText(username);
                        holder.name.setText(name);
                        holder.device_token.setText(Tools.getRefValue(dataSnapshot.child("device_token")));

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                holder.itemView.setOnClickListener(v -> {
                    Intent profileIntent = new Intent(getContext(), ProfileViewActivity.class);
                    profileIntent.putExtra("receiver_uid", holder.uid.getText().toString());
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

        friendList.setAdapter(adapter);
        adapter.startListening();
    }
}
