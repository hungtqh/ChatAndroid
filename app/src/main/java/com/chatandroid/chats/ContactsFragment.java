package com.chatandroid.chats;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
public class ContactsFragment extends Fragment
{
    private View ContactsView;
    private RecyclerView myContactsList;

    private DatabaseReference ContacsRef, UsersRef;
    private FirebaseAuth mAuth;
    private String currentUserID;


    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ContactsView = inflater.inflate(R.layout.fragment_contacts, container, false);

        myContactsList = (RecyclerView) ContactsView.findViewById(R.id.contacts_list);
        myContactsList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        UsersRef.keepSynced(true);

        return ContactsView;
    }


    @Override
    public void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(UsersRef, Contacts.class)
                .build();


        final FirebaseRecyclerAdapter<Contacts, ContactsViewHolder> adapter
                = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull Contacts model)
            {
                final String userIDs = getRef(position).getKey();


                UsersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.exists())
                        {
                            String userImage = Tools.getRefValue(dataSnapshot.child("image"));
                            String uid = Tools.getRefValue(dataSnapshot.child("uid"));
                            String firtname = Tools.getRefValue(dataSnapshot.child("firstname"));
                            String lastname = Tools.getRefValue(dataSnapshot.child("lastname"));
                            String username = Tools.getRefValue(dataSnapshot.child("username"));
                            String name = firtname + " " + lastname;
                            String profileStatus = Tools.getRefValue(dataSnapshot.child("status"));

                            if (dataSnapshot.child("userState").child("status").exists())
                            {
                                String state = Tools.getRefValue(dataSnapshot.child("userState").child("status"));

                                Log.e("Kelly",currentUserID + " is " + state);

                                if (state.equals("online"))
                                {
                                    holder.statusOnline.setText(state);
                                    holder.statusOnline.setVisibility(View.VISIBLE);
                                    holder.statusOffline.setVisibility(View.GONE);
                                }else{
                                    holder.statusOffline.setText(state);
                                    holder.statusOnline.setVisibility(View.GONE);
                                    holder.statusOffline.setVisibility(View.VISIBLE);
                                }
                            }

                            holder.statusOnline.setVisibility(View.GONE);
                            holder.statusOffline.setVisibility(View.GONE);
                            holder.uid.setText(uid);
                            holder.username.setText(username);
                            holder.name.setText(name);

                            if (dataSnapshot.hasChild("image"))
                            {
                                Picasso.get().load(userImage).placeholder(R.mipmap.ic_launcher_round).into(holder.profileImage);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), ChatActivity.class);
                        intent.putExtra("receiver_uid",holder.uid.getText().toString());
                        intent.putExtra("receiver_username",holder.username.getText().toString());
                        startActivity(intent);
                    }
                });

            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
            {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                return viewHolder;
            }
        };

        myContactsList.setAdapter(adapter);
        adapter.startListening();
    }


    public static class ContactsViewHolder extends RecyclerView.ViewHolder
    {
        TextView name, username, statusOnline, statusOffline, uid, device_token, mTime, mCount;
        CircleImageView profileImage;


        public ContactsViewHolder(@NonNull View itemView)
        {
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


}
