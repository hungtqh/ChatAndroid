package com.chatandroid.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.chatandroid.Authenticate;
import com.chatandroid.R;
import com.chatandroid.chat.Chats;
import com.chatandroid.databinding.ActivityLoginBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends Authenticate {

    private DatabaseReference usersRef;

    private ActivityLoginBinding binding;
    private ProgressDialog loadingBar;
    private BottomSheetBehavior mBehavior;
    private BottomSheetDialog mBottomSheetDialog;
    private View bottom_sheet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        initializeFields();

        binding.signUp.setOnClickListener(view1 -> sendUserToRegisterActivity());

        binding.login.setOnClickListener(view12 -> allowUserToLogin());

        binding.forgotPassword.setOnClickListener(v -> showBottomSheetDialog());

        binding.phoneLogin.setOnClickListener(v112 -> sendUserToPhoneLoginActivity());
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null && (currentUser.isEmailVerified() || !TextUtils.isEmpty(currentUser.getPhoneNumber()))) {
            sendUserToChatActivity();
        }
    }


    private void allowUserToLogin() {
        String email = binding.email.getText().toString();
        String password = binding.password.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, R.string.enter_all_fields, Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setTitle(getString(R.string.sign_in));
            loadingBar.setMessage(getString(R.string.please_wait));
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String currentUserId = mAuth.getCurrentUser().getUid();
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();

                            usersRef.child(currentUserId).child("device_token")
                                    .setValue(deviceToken)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful() && mAuth.getCurrentUser().isEmailVerified()) {
                                            sendUserToChatActivity();
                                            Toast.makeText(LoginActivity.this, R.string.logged_in_successful, Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        } else {
                                            Toast.makeText(this, R.string.please_verify_email, Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }
                                    });
                        } else {
                            String message = task.getException().toString();
                            Toast.makeText(LoginActivity.this, getString(R.string.error_message) + message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    });
        }
    }

    private void initializeFields() {
        loadingBar = new ProgressDialog(this);
        bottom_sheet = findViewById(R.id.bottom_sheet);
        mBehavior = BottomSheetBehavior.from(bottom_sheet);
    }

    private void sendUserToChatActivity() {
        Intent mainIntent = new Intent(LoginActivity.this, Chats.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void sendUserToRegisterActivity() {
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
    }

    private void sendUserToPhoneLoginActivity() {
        Intent intent = new Intent(this, PhoneLoginActivity.class);
        startActivity(intent);
    }

    private void showBottomSheetDialog() {
        if (mBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        final View view = getLayoutInflater().inflate(R.layout.sheet_basic, null);
        (view.findViewById(R.id.cancel)).setOnClickListener(view1 -> mBottomSheetDialog.dismiss());

        (view.findViewById(R.id.reset)).setOnClickListener(v -> {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            EditText emailAddress = view.findViewById(R.id.email);
            TextView results = view.findViewById(R.id.results);
            String email = emailAddress.getText().toString();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(getApplicationContext(), getString(R.string.must_provide_email), Toast.LENGTH_SHORT).show();
                return;
            }
            mBottomSheetDialog.cancel();
            loadingBar.setTitle(getString(R.string.account_recovery_msg));
            loadingBar.setMessage(getString(R.string.please_wait));
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            loadingBar.cancel();
                            Toast.makeText(getApplicationContext(), getString(R.string.instruction_email_sent), Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(e -> {
                Toast.makeText(getApplicationContext(), getString(R.string.error_message) + e.getMessage(), Toast.LENGTH_SHORT).show();
                loadingBar.cancel();
            });

        });

        mBottomSheetDialog = new BottomSheetDialog(this);
        mBottomSheetDialog.setContentView(view);

        mBottomSheetDialog.show();
        mBottomSheetDialog.setOnDismissListener(dialog -> mBottomSheetDialog = null);
    }
}
