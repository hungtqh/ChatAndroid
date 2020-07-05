package com.chatandroid.chat.fragment;


import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chatandroid.R;
import com.chatandroid.chat.model.Contact;
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
public class RequestsFragment extends Fragment {
    private View requestsFragmentView;
    private RecyclerView myRequestsList;

    private DatabaseReference chatRequestsRef, usersRef, contactsRef;
    private FirebaseAuth mAuth;
    private String currentUserID;


    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        requestsFragmentView = inflater.inflate(R.layout.fragment_requests, container, false);


        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestsRef = FirebaseDatabase.getInstance().getReference().child("Friend Requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Friends");


        myRequestsList = requestsFragmentView.findViewById(R.id.chat_requests_list);
        myRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));


        return requestsFragmentView;
    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contact> options =
                new FirebaseRecyclerOptions.Builder<Contact>()
                        .setQuery(chatRequestsRef.child(currentUserID), Contact.class)
                        .build();


        FirebaseRecyclerAdapter<Contact, RequestsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contact, RequestsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final RequestsViewHolder holder, int position, @NonNull Contact model) {

                        final String list_user_id = getRef(position).getKey();

                        DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();

                        getTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    String type = dataSnapshot.getValue().toString();

                                    if (type.equals("received")) {
                                        usersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                String firtname = Tools.getRefValue(dataSnapshot.child("firstname"));
                                                String lastname = Tools.getRefValue(dataSnapshot.child("lastname"));
                                                String requestUserName = firtname + " " + lastname;

                                                holder.userName.setText(requestUserName);
                                                holder.userStatus.setText(R.string.friend_request_msg);

                                                if (dataSnapshot.child("image").exists()) {
                                                    String userImage = Tools.getRefValue(dataSnapshot.child("image"));
                                                    Picasso.get().load(userImage).placeholder(R.mipmap.ic_launcher_round).into(holder.image);
                                                }

                                                holder.itemView.setOnClickListener(view -> {
                                                    CharSequence options12[] = new CharSequence[]
                                                            {
                                                                    getString(R.string.accept_request),
                                                                    getString(R.string.cancel)
                                                            };

                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                    builder.setTitle(requestUserName + " - " + getString(R.string.friend_request));

                                                    builder.setItems(options12, (dialogInterface, i) -> {
                                                        if (i == 0) {
                                                            contactsRef.child(currentUserID).child(list_user_id).child("Friend")
                                                                    .setValue("Saved").addOnCompleteListener(task -> {
                                                                if (task.isSuccessful()) {
                                                                    contactsRef.child(list_user_id).child(currentUserID).child("Friend")
                                                                            .setValue("Saved").addOnCompleteListener(task1 -> {
                                                                        if (task1.isSuccessful()) {
                                                                            chatRequestsRef.child(currentUserID).child(list_user_id)
                                                                                    .removeValue()
                                                                                    .addOnCompleteListener(task112 -> {
                                                                                        if (task112.isSuccessful()) {
                                                                                            chatRequestsRef.child(list_user_id).child(currentUserID)
                                                                                                    .removeValue()
                                                                                                    .addOnCompleteListener(task11 -> {
                                                                                                        if (task11.isSuccessful()) {
                                                                                                            Toast.makeText(getContext(), getString(R.string.request_accepted), Toast.LENGTH_SHORT).show();
                                                                                                        }
                                                                                                    });
                                                                                        }
                                                                                    });
                                                                        }
                                                                    });
                                                                }
                                                            });
                                                        }
                                                        if (i == 1) {
                                                            chatRequestsRef.child(currentUserID).child(list_user_id)
                                                                    .removeValue()
                                                                    .addOnCompleteListener(task -> {
                                                                        if (task.isSuccessful()) {
                                                                            chatRequestsRef.child(list_user_id).child(currentUserID)
                                                                                    .removeValue()
                                                                                    .addOnCompleteListener(task12 -> {
                                                                                        if (task12.isSuccessful()) {
                                                                                            Toast.makeText(getContext(), getString(R.string.request_canceled), Toast.LENGTH_SHORT).show();
                                                                                        }
                                                                                    });
                                                                        }
                                                                    });
                                                        }
                                                    });
                                                    builder.show();
                                                });

                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    } else if (type.equals("sent")) {
                                        usersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                String firtname = Tools.getRefValue(dataSnapshot.child("firstname"));
                                                String lastname = Tools.getRefValue(dataSnapshot.child("lastname"));
                                                String requestUserName = firtname + " " + lastname;

                                                holder.userName.setText(requestUserName);
                                                holder.userStatus.setText(getString(R.string.request_sent));

                                                if (dataSnapshot.child("image").exists()) {
                                                    String userImage = Tools.getRefValue(dataSnapshot.child("image"));
                                                    Picasso.get().load(userImage).placeholder(R.mipmap.ic_launcher_round).into(holder.image);
                                                }

                                                holder.itemView.setOnClickListener(view -> {
                                                    CharSequence options1[] = new CharSequence[]
                                                            {
                                                                    getString(R.string.cancel_request)
                                                            };

                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                    builder.setTitle(getString(R.string.friend_request));

                                                    builder.setItems(options1, (dialogInterface, i) -> {
                                                        if (i == 0) {
                                                            chatRequestsRef.child(currentUserID).child(list_user_id)
                                                                    .removeValue()
                                                                    .addOnCompleteListener(task -> {
                                                                        if (task.isSuccessful()) {
                                                                            chatRequestsRef.child(list_user_id).child(currentUserID)
                                                                                    .removeValue()
                                                                                    .addOnCompleteListener(task13 -> {
                                                                                        if (task13.isSuccessful()) {
                                                                                            Toast.makeText(getContext(), getString(R.string.request_canceled), Toast.LENGTH_SHORT).show();
                                                                                        }
                                                                                    });
                                                                        }
                                                                    });
                                                        }
                                                    });
                                                    builder.show();
                                                });

                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                        RequestsViewHolder holder = new RequestsViewHolder(view);
                        return holder;
                    }
                };

        myRequestsList.setAdapter(adapter);
        adapter.startListening();
    }


    public static class RequestsViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userStatus;
        private CircleImageView image;

        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.name);
            userStatus = itemView.findViewById(R.id.username);
            image = itemView.findViewById(R.id.profile_image);
        }
    }
}
