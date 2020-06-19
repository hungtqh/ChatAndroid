package com.chatandroid.chat.fragment;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.chatandroid.R;
import com.chatandroid.chat.activity.FindFriendsActivity;
import com.chatandroid.chat.activity.GroupChatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends Fragment {
    private View groupFragmentView;
    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> groupsList = new ArrayList<>();

    private DatabaseReference groupsRef;


    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        groupFragmentView = inflater.inflate(R.layout.fragment_groups, container, false);

        groupsRef = FirebaseDatabase.getInstance().getReference().child("Groups");

        initializeFields();

        retrieveAndDisplayGroups();

        listView.setOnItemClickListener((adapterView, view, position, id) -> {
            String currentGroupName = adapterView.getItemAtPosition(position).toString();

            Intent groupChatIntent = new Intent(getContext(), GroupChatActivity.class);
            groupChatIntent.putExtra("groupName", currentGroupName);
            startActivity(groupChatIntent);
        });

        FloatingActionButton floatingActionButton = groupFragmentView.findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(v -> requestNewGroup());

        listView.setOnItemClickListener((adapterView, view, position, id) -> {
            String currentGroupName = adapterView.getItemAtPosition(position).toString();

            Intent groupChatIntent = new Intent(getContext(), GroupChatActivity.class);
            groupChatIntent.putExtra("groupName", currentGroupName);
            startActivity(groupChatIntent);
        });

        return groupFragmentView;
    }


    private void initializeFields() {
        listView = groupFragmentView.findViewById(R.id.list_view);
        arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, groupsList);
        listView.setAdapter(arrayAdapter);
    }


    private void retrieveAndDisplayGroups() {
        groupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Set<String> set = new HashSet<>();
                Iterator iterator = dataSnapshot.getChildren().iterator();

                while (iterator.hasNext()) {
                    set.add(((DataSnapshot) iterator.next()).getKey());
                }

                groupsList.clear();
                groupsList.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void requestNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Enter Group Name :");

        final EditText groupNameField = new EditText(getContext());
        groupNameField.setGravity(Gravity.CENTER);
        groupNameField.setHint("e.g Guitar");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", (dialogInterface, i) -> {
            String groupName = groupNameField.getText().toString();

            if (TextUtils.isEmpty(groupName)) {
                Toast.makeText(getContext(), "Please write Group Name...", Toast.LENGTH_SHORT).show();
            } else {
                createNewGroup(groupName);
            }
        });

        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());

        builder.show();
    }


    private void createNewGroup(final String groupName) {
        groupsRef.child(groupName).setValue("")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), groupName + " group is Created Successfully...", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
