package com.chatandroid.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.chatandroid.Authenticate;
import com.chatandroid.R;
import com.chatandroid.chat.Chats;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends Authenticate {
    private Button btnSendCode, btnVerifyCode;
    private EditText etPhoneLogin;
    private TextView tvEmailLogin, tvPhoneCode;
    private Spinner spCountryCode;
    private ProgressDialog loadingBar;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        initializeFields();

        tvEmailLogin.setOnClickListener(v -> sendUserToEmailLoginActivity());
        btnSendCode.setOnClickListener(v1 -> sendVerifyCode());
        btnVerifyCode.setOnClickListener(v2 -> verifyCode());
        onCallBacks();
    }

    private void initializeFields() {
        btnSendCode = findViewById(R.id.send_verification_code);
        btnVerifyCode = findViewById(R.id.verify_code);
        etPhoneLogin = findViewById(R.id.phone_login_input);
        tvEmailLogin = findViewById(R.id.email_login);
        tvPhoneCode = findViewById(R.id.phone_code);

        spCountryCode = findViewById(R.id.country_code);
        ArrayAdapter<String> countryCodeAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_custom_layout, new String[]{"+84", "+82"});
        spCountryCode.setAdapter(countryCodeAdapter);


        loadingBar = new ProgressDialog(this);
    }

    private void sendUserToEmailLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void sendVerifyCode() {
        String countryCode = spCountryCode.getSelectedItem().toString();
        String phoneNumber = etPhoneLogin.getText().toString();

        if (phoneNumber.startsWith("0")) {
            phoneNumber = phoneNumber.substring(1);
        }

        if (TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(PhoneLoginActivity.this, R.string.enter_phone_first, Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setTitle(getString(R.string.phone_verification));
            loadingBar.setMessage(getString(R.string.wait_verify_phone));
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    countryCode + phoneNumber,        // Phone number to verify
                    60,                 // Timeout duration
                    TimeUnit.SECONDS,   // Unit of timeout
                    PhoneLoginActivity.this,               // Activity (for callback binding)
                    callbacks);        // OnVerificationStateChangedCallbacks
        }
    }

    private void verifyCode() {

        String verificationCode = etPhoneLogin.getText().toString();

        if (TextUtils.isEmpty(verificationCode)) {
            Toast.makeText(PhoneLoginActivity.this, R.string.verify_code_first, Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setTitle(getString(R.string.verification_code));
            loadingBar.setMessage(getString(R.string.wait_verify_phone_code));
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
            signInWithPhoneAuthCredential(credential);
        }
    }

    private void onCallBacks() {
        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, R.string.invalid_phone, Toast.LENGTH_SHORT).show();

                btnSendCode.setVisibility(View.VISIBLE);
                btnVerifyCode.setVisibility(View.GONE);
            }

            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, R.string.code_sent, Toast.LENGTH_SHORT).show();

                btnSendCode.setVisibility(View.GONE);
                spCountryCode.setVisibility(View.GONE);
                tvPhoneCode.setText(R.string.verification_code);
                btnVerifyCode.setVisibility(View.VISIBLE);
                etPhoneLogin.setText("");
                etPhoneLogin.setGravity(Gravity.CENTER);
            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loadingBar.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, R.string.logged_in_successful, Toast.LENGTH_SHORT).show();
                            sendUserToChatActivity();
                        } else {
                            String message = task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, R.string.error_message + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendUserToChatActivity() {
        Intent mainIntent = new Intent(PhoneLoginActivity.this, Chats.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
