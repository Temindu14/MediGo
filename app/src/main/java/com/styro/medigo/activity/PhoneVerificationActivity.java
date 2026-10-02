package com.styro.medigo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import com.styro.medigo.R;

import java.util.concurrent.TimeUnit;

public class PhoneVerificationActivity extends AppCompatActivity {

    private TextView tvPhoneNumber;
    private TextView tvResendTimer;
    private EditText etOtp;
    private Button btnVerify;
    private Button btnResend;
    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    private String phoneNumber;
    private boolean fromRegister;

    private CountDownTimer countDownTimer;
    private static final long RESEND_TIMEOUT = 60000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_verification);
        goToMain();
    }

    private void initViews() {
        tvPhoneNumber = findViewById(R.id.tvPhoneNumber);
        tvResendTimer = findViewById(R.id.tvResendTimer);
        etOtp = findViewById(R.id.etOtp);
        btnVerify = findViewById(R.id.btnVerify);
        btnResend = findViewById(R.id.btnResend);
        progressBar = findViewById(R.id.progressBar);

        tvPhoneNumber.setText("OTP sent to " + phoneNumber);
        btnResend.setEnabled(false);
    }

    private void setClickListeners() {
        btnVerify.setOnClickListener(v -> verifyOtp());
        btnResend.setOnClickListener(v -> {
            btnResend.setEnabled(false);
            sendOtp(phoneNumber, true);
        });
    }

    private void sendOtp(String phone, boolean isResend) {
        showLoading(true);

        PhoneAuthOptions.Builder builder = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        signInWithCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        showLoading(false);
                        Toast.makeText(PhoneVerificationActivity.this,
                                "Failed to send OTP: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                        btnResend.setEnabled(true);
                    }

                    @Override
                    public void onCodeSent(String vId,
                                           PhoneAuthProvider.ForceResendingToken token) {
                        showLoading(false);
                        verificationId = vId;
                        resendToken = token;
                        Toast.makeText(PhoneVerificationActivity.this,
                                "OTP sent!", Toast.LENGTH_SHORT).show();
                        startResendCountdown();
                    }
                });

        if (isResend && resendToken != null) {
            builder.setForceResendingToken(resendToken);
        }

        PhoneAuthProvider.verifyPhoneNumber(builder.build());
    }

    private void verifyOtp() {
        String code = etOtp.getText().toString().trim();

        if (TextUtils.isEmpty(code) || code.length() < 6) {
            etOtp.setError("Enter the 6-digit OTP");
            etOtp.requestFocus();
            return;
        }

        if (verificationId == null) {
            Toast.makeText(this, "Please wait for OTP to be sent", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        firebaseAuth.getCurrentUser()
                .linkWithCredential(credential)
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Phone verified successfully!", Toast.LENGTH_SHORT).show();
                        goToMain();
                    } else {
                        String msg = task.getException() != null
                                ? task.getException().getMessage() : "";
                        if (msg != null && msg.contains("already")) {
                            Toast.makeText(this, "Phone verified!", Toast.LENGTH_SHORT).show();
                            goToMain();
                        } else {
                            Toast.makeText(this, "Invalid OTP. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void goToMain() {
        if (countDownTimer != null) countDownTimer.cancel();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void startResendCountdown() {
        btnResend.setEnabled(false);
        if (countDownTimer != null) countDownTimer.cancel();

        countDownTimer = new CountDownTimer(RESEND_TIMEOUT, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvResendTimer.setText("Resend OTP in " + (millisUntilFinished / 1000) + "s");
                tvResendTimer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFinish() {
                tvResendTimer.setVisibility(View.GONE);
                btnResend.setEnabled(true);
            }
        }.start();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnVerify.setEnabled(!show);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}