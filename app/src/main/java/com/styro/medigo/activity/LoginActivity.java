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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.styro.medigo.R;
import com.styro.medigo.model.User;
import com.styro.medigo.util.Constants;
import com.styro.medigo.util.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister, tvForgotPassword;
    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        initFirebase();
        setClickListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        progressBar = findViewById(R.id.progressBar);
    }

    private void initFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        sessionManager = new SessionManager(this);
    }

    private void setClickListeners() {
        btnLogin.setOnClickListener(v -> performLogin());
        tvRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        tvForgotPassword.setOnClickListener(v -> sendPasswordResetEmail());
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError(getString(R.string.error_email_required));
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError(getString(R.string.error_password_required));
            etPassword.requestFocus();
            return;
        }

        showLoading(true);

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            if (!firebaseUser.isEmailVerified()) {
                                showLoading(false);
                                showEmailNotVerifiedDialog(firebaseUser, email);
                            } else {
                                fetchUserData(firebaseUser.getUid());
                            }
                        }
                    } else {
                        showLoading(false);
                        Toast.makeText(this, getString(R.string.error_login_failed), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showEmailNotVerifiedDialog(FirebaseUser user, String email) {
        new AlertDialog.Builder(this)
                .setTitle("Email Not Verified")
                .setMessage("Please verify your email address before logging in.\n\nCheck your inbox for a verification link.")
                .setPositiveButton("Resend Email", (dialog, which) -> {
                    user.sendEmailVerification()
                            .addOnSuccessListener(unused -> {
                                firebaseAuth.signOut();
                                Toast.makeText(this, "Verification email sent to " + email, Toast.LENGTH_LONG).show();
                            })
                            .addOnFailureListener(e -> {
                                firebaseAuth.signOut();
                                Toast.makeText(this, "Failed to send email: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .setNegativeButton("OK", (dialog, which) -> firebaseAuth.signOut())
                .setOnCancelListener(d -> firebaseAuth.signOut())
                .show();
    }

    private void fetchUserData(String userId) {
        databaseReference.child(Constants.NODE_USERS)
                .child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        showLoading(false);

                        if (snapshot.exists()) {
                            User user = new User();
                            user.setUserId(getStringValue(snapshot, "userId"));
                            user.setName(getStringValue(snapshot, "name"));
                            user.setEmail(getStringValue(snapshot, "email"));
                            user.setPhone(getStringValue(snapshot, "phone"));
                            user.setAddress(getStringValue(snapshot, "address"));
                            user.setSecondaryAddress(getStringValue(snapshot, "secondaryAddress"));
                            user.setProfileImageUrl(getStringValue(snapshot, "profileImageUrl"));
                            user.setRole(getStringValue(snapshot, "role"));
                            user.setOutletId(getStringValue(snapshot, "outletId"));

                            if (snapshot.child("isVerified").exists()) {
                                Object val = snapshot.child("isVerified").getValue();
                                if (val instanceof Boolean) user.setVerified((Boolean) val);
                            }
                            if (snapshot.child("createdAt").exists()) {
                                Object val = snapshot.child("createdAt").getValue();
                                if (val instanceof Long) user.setCreatedAt((Long) val);
                                else if (val instanceof Number) user.setCreatedAt(((Number) val).longValue());
                            }

                            String role = user.getRole();
                            if (role == null || role.isEmpty()) role = "customer";

                            sessionManager.saveUserSession(
                                    user.getUserId(),
                                    user.getName(),
                                    user.getEmail(),
                                    user.getPhone(),
                                    role
                            );
                            navigateByRole(role);
                        } else {
                            Toast.makeText(LoginActivity.this, getString(R.string.error_user_not_found), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        showLoading(false);
                        Toast.makeText(LoginActivity.this, getString(R.string.error_generic), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getStringValue(DataSnapshot snapshot, String field) {
        if (!snapshot.child(field).exists()) return "";
        Object value = snapshot.child(field).getValue();
        return value == null ? "" : String.valueOf(value);
    }

    private void navigateByRole(String role) {
        Intent intent;
        switch (role) {
            case Constants.ROLE_MAIN_ADMIN:
                intent = new Intent(this, AdminMainActivity.class);
                break;
            case Constants.ROLE_OUTLET_MANAGER:
                intent = new Intent(this, AdminManagerActivity.class);
                break;
            case Constants.ROLE_OUTLET_STAFF:
                intent = new Intent(this, AdminStaffActivity.class);
                break;
            default:
                intent = new Intent(this, MainActivity.class);
                break;
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void sendPasswordResetEmail() {
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError(getString(R.string.error_email_required));
            etEmail.requestFocus();
            return;
        }

        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, getString(R.string.msg_reset_email_sent), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, getString(R.string.error_reset_failed), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
    }
}