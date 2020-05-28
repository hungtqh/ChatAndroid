package com.chatandroid;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.chatandroid.chats.FindFriendsActivity;
import com.chatandroid.utils.Tools;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


public class MainActivity extends Authenticate
{
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        primaryMenu(savedInstanceState);
        initToolbar();

    }

    @Override
    protected void onStart(){
        super.onStart();
//        RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot)
//            {
//                if (!(dataSnapshot.child("configured").exists()))
//                {
//                    Intent intent = new Intent(MainActivity.this, ProfileEditActivity.class);
//                    overridePendingTransition(0, 0);
//                    startActivity(intent);
//                    overridePendingTransition(0, 0);
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Communications");
        Tools.setSystemBarColorInt(this, getResources().getColor(R.color.default_status_color));
    }

    private void RequestNewGroup()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.Dialog_Theme);
        builder.setTitle("Enter Group Name :");

        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("e.g Coding Cafe");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                String groupName = groupNameField.getText().toString();

                if (TextUtils.isEmpty(groupName))
                {
                    Toast.makeText(MainActivity.this, "Please write Group Name...", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    CreateNewGroup(groupName);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                dialogInterface.cancel();
            }
        });

        builder.show();
    }

    private void CreateNewGroup(final String groupName)
    {
        RootRef.child("Groups").child(groupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(MainActivity.this, groupName + " group is Created Successfully...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void SendUserToFindFriendsActivity()
    {
        Intent findFriendsIntent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(findFriendsIntent);
    }


}

