package com.chatandroid.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.chatandroid.Authenticate;
import com.chatandroid.R;
import com.chatandroid.databinding.ActivityRegisterBinding;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;


public class RegisterActivity extends Authenticate {
    private ProgressDialog loadingBar;

    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        initializeFields();

        binding.login.setOnClickListener(view1 -> sendUserToLoginActivity());

        binding.register.setOnClickListener(view12 -> createNewAccount());
    }


    private void createNewAccount() {
        String email = binding.email.getText().toString();
        String password = binding.password.getText().toString();

        if (TextUtils.isEmpty(password) || TextUtils.isEmpty(email)) {
            Toast.makeText(this, R.string.enter_all_fields, Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setTitle(getString(R.string.create_new_account));
            loadingBar.setMessage(getString(R.string.wait_creating_account));
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();

                            String currentUserID = mAuth.getCurrentUser().getUid();
                            rootRef.child("Users").child(currentUserID).setValue("");


                            rootRef.child("Users").child(currentUserID).child("device_token")
                                    .setValue(deviceToken);

                            FirebaseUser user = mAuth.getCurrentUser();
                            user.sendEmailVerification();

                            Toast.makeText(RegisterActivity.this, R.string.check_verification_email, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        } else {
                            String message = task.getException().getMessage();
                            Toast.makeText(RegisterActivity.this, getString(R.string.error_message) + message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    });
        }
    }


    private void initializeFields() {
        loadingBar = new ProgressDialog(this);
    }


    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(loginIntent);
    }
}
