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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import com.styro.medigo.R;
import com.styro.medigo.util.SessionManager;

public class OTPVerificationActivity extends AppCompatActivity {

    private TextView tvPhoneNumber;
    private EditText etOtp;
    private Button btnVerify;
    private TextView tvResendOtp;
    private TextView tvCountdown;
    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;
    private SessionManager sessionManager;

    private String verificationId;
    private String phoneNumber;
    private CountDownTimer countDownTimer;

    private static final long OTP_TIMEOUT_SECONDS = 60L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.otpRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        phoneNumber = getIntent().getStringExtra("phone");

        initViews();
        initFirebase();
        setClickListeners();
        sendOtp();
    }

    private void initViews() {
        tvPhoneNumber = findViewById(R.id.tvPhoneNumber);
        etOtp = findViewById(R.id.etOtp);
        btnVerify = findViewById(R.id.btnVerify);
        tvResendOtp = findViewById(R.id.tvResendOtp);
        tvCountdown = findViewById(R.id.tvCountdown);
        progressBar = findViewById(R.id.progressBar);

        tvPhoneNumber.setText(getString(R.string.label_sent_to, phoneNumber));
    }

    private void initFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(this);
    }

    private void setClickListeners() {
        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyOtp();
            }
        });

        tvResendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendOtp();
            }
        });
    }

    private void sendOtp() {
        showLoading(true);
        tvResendOtp.setEnabled(false);

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber("+94" + phoneNumber)
                .setTimeout(OTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .setActivity(this)

                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        // Auto-retrieval or instant verification
                        showLoading(false);
                        signInWithCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        showLoading(false);
                        Toast.makeText(OTPVerificationActivity.this,
                                getString(R.string.error_otp_failed),
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(String id, PhoneAuthProvider.ForceResendingToken token) {
                        super.onCodeSent(id, token);
                        verificationId = id;
                        showLoading(false);
                        startCountdown();
                    }

                })
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyOtp() {
        String otp = etOtp.getText().toString().trim();

        if (TextUtils.isEmpty(otp) || otp.length() < 6) {
            etOtp.setError(getString(R.string.error_otp_invalid));
            etOtp.requestFocus();
            return;
        }

        if (verificationId == null) {
            Toast.makeText(this,
                    getString(R.string.error_otp_not_sent),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(OTPVerificationActivity.this, task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        navigateToMain();
                    } else {
                        Toast.makeText(OTPVerificationActivity.this,
                                getString(R.string.error_otp_incorrect),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startCountdown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        tvCountdown.setVisibility(View.VISIBLE);
        tvResendOtp.setEnabled(false);

        countDownTimer = new CountDownTimer(OTP_TIMEOUT_SECONDS * 1000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                long secondsLeft = millisUntilFinished / 1000;
                tvCountdown.setText(getString(R.string.label_resend_in, secondsLeft));
            }

            @Override
            public void onFinish() {
                tvCountdown.setVisibility(View.GONE);
                tvResendOtp.setEnabled(true);
            }

        }.start();
    }

    private void navigateToMain() {
        Intent intent = new Intent(OTPVerificationActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnVerify.setEnabled(!show);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

}