package com.chatandroid.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.chatandroid.R;
import com.chatandroid.chat.Chats;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends Authentication {
    private Button btnSendCode, btnVerifyCode;
    private EditText etPhoneLogin;
    private TextView tvEmailLogin, tvPhoneCode, tvResend, tvSendWait;
    private Spinner spCountryCode;
    private ProgressDialog loadingBar;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String mVerificationId;
    String countryCode;
    String phoneNumber;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        initializeFields();

        tvEmailLogin.setOnClickListener(v -> sendUserToEmailLoginActivity());
        btnSendCode.setOnClickListener(v1 -> sendVerifyCode());
        btnVerifyCode.setOnClickListener(v2 -> verifyCode());
        tvResend.setOnClickListener(v12 -> {
            resendCode();
            setResendFields();
        });
        onCallBacks();
    }

    private void initializeFields() {
        btnSendCode = findViewById(R.id.send_verification_code);
        btnVerifyCode = findViewById(R.id.verify_code);
        etPhoneLogin = findViewById(R.id.phone_login_input);
        tvEmailLogin = findViewById(R.id.email_login);
        tvPhoneCode = findViewById(R.id.phone_code);
        tvSendWait = findViewById(R.id.send_code_wait);
        tvResend = findViewById(R.id.resend_code);

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
        countryCode = spCountryCode.getSelectedItem().toString();
        phoneNumber = etPhoneLogin.getText().toString();

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

    private void resendCode() {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                countryCode + phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                PhoneLoginActivity.this,               // Activity (for callback binding)
                callbacks, // OnVerificationStateChangedCallbacks
                mResendToken);
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

            @Override
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

                setResendFields();
            }
        };
    }

    private void setResendFields() {
        tvResend.setVisibility(View.GONE);
        tvSendWait.setVisibility(View.VISIBLE);
        new CountDownTimer(30000, 1000) {
            public void onTick(long millisUntilFinished) {
                tvSendWait.setText(getString(R.string.send_code_again_after) +
                        millisUntilFinished / 1000 + " " + getString(R.string.second));
            }

            public void onFinish() {
                tvSendWait.setText(R.string.resend_code);
                tvResend.setVisibility(View.VISIBLE);
            }

        }.start();
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        loadingBar.dismiss();
                        Toast.makeText(PhoneLoginActivity.this, R.string.logged_in_successful, Toast.LENGTH_SHORT).show();
                        sendUserToChatActivity();
                    } else {
                        String message = task.getException().toString();
                        Toast.makeText(PhoneLoginActivity.this, R.string.error_message + message, Toast.LENGTH_SHORT).show();
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
