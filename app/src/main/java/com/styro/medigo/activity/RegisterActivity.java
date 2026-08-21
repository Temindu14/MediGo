package com.styro.medigo.activity;

import android.content.Intent;
import android.os.Bundle;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.styro.medigo.R;
import com.styro.medigo.model.User;
import com.styro.medigo.util.Constants;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPhone, etPassword, etConfirmPassword, etAddress;
    private Button btnRegister;
    private TextView tvLogin;
    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registerRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        initFirebase();
        setClickListeners();
    }

    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etAddress = findViewById(R.id.etAddress);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        progressBar = findViewById(R.id.progressBar);
    }

    private void initFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    private void setClickListeners() {
        btnRegister.setOnClickListener(v -> performRegistration());
        tvLogin.setOnClickListener(v -> finish());
    }

    private void performRegistration() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (!validateInputs(fullName, email, phone, password, confirmPassword)) {
            return;
        }

        showLoading(true);

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            saveUserToDatabase(firebaseUser.getUid(), fullName, email, phone, address);

                            firebaseUser.sendEmailVerification()
                                    .addOnCompleteListener(verifyTask -> {
                                        showLoading(false);
                                        if (verifyTask.isSuccessful()) {
                                            Toast.makeText(this, "Verification email sent to " + email, Toast.LENGTH_LONG).show();
                                        }
                                        String formattedPhone = formatPhoneNumber(phone);
                                        Intent intent = new Intent(this, PhoneVerificationActivity.class);
                                        intent.putExtra("phone", formattedPhone);
                                        intent.putExtra("fromRegister", true);
                                        startActivity(intent);
                                        finish();
                                    });
                        }
                    } else {
                        showLoading(false);
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : getString(R.string.error_register_failed);
                        Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToDatabase(String userId, String name, String email, String phone, String address) {
        User user = new User(userId, name, email, phone);
        user.setAddress(address);
        user.setRole(Constants.ROLE_CUSTOMER);

        databaseReference.child(Constants.NODE_USERS)
                .child(userId)
                .setValue(user)
                .addOnFailureListener(e -> Toast.makeText(this, getString(R.string.error_save_failed), Toast.LENGTH_SHORT).show());
    }

    private String formatPhoneNumber(String phone) {
        phone = phone.replaceAll("\\s+", "").replaceAll("-", "");
        if (phone.startsWith("+")) return phone;
        if (phone.startsWith("0")) return "+94" + phone.substring(1);
        return "+94" + phone;
    }

    private boolean validateInputs(String name, String email, String phone, String password, String confirmPassword) {
        if (TextUtils.isEmpty(name)) {
            etFullName.setError(getString(R.string.error_name_required));
            etFullName.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError(getString(R.string.error_email_required));
            etEmail.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(phone) || phone.length() < 9) {
            etPhone.setError(getString(R.string.error_phone_invalid));
            etPhone.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            etPassword.setError(getString(R.string.error_password_short));
            etPassword.requestFocus();
            return false;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.error_passwords_no_match));
            etConfirmPassword.requestFocus();
            return false;
        }
        return true;
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!show);
    }
}