package com.styro.medigo.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
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

import java.util.ArrayList;
import java.util.List;

import com.styro.medigo.R;
import com.styro.medigo.model.User;
import com.styro.medigo.util.Constants;
import com.styro.medigo.util.SessionManager;

public class RegisterAdminActivity extends AppCompatActivity {

    private TextView tvTitle;
    private EditText etFullName;
    private EditText etEmail;
    private EditText etPhone;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private Spinner spinnerRole;
    private Button btnRegister;
    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private SessionManager sessionManager;

    private String allowedRole;
    private List<String> roleLabels;
    private List<String> roleValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_admin);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registerAdminRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        allowedRole = getIntent().getStringExtra("allowed_role");

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        sessionManager = new SessionManager(this);

        initViews();
        setupRoleSpinner();
        setClickListeners();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvTitle);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        spinnerRole = findViewById(R.id.spinnerRole);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);

        if (Constants.ROLE_MAIN_ADMIN.equals(allowedRole)) {
            tvTitle.setText("Register Main Admin");
        } else if (Constants.ROLE_OUTLET_MANAGER.equals(allowedRole)) {
            tvTitle.setText("Register Outlet Manager");
        } else {
            tvTitle.setText("Register Outlet Staff");
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupRoleSpinner() {
        roleLabels = new ArrayList<>();
        roleValues = new ArrayList<>();

        String currentRole = sessionManager.getUserRole();

        if (Constants.ROLE_MAIN_ADMIN.equals(currentRole)) {
            if (Constants.ROLE_MAIN_ADMIN.equals(allowedRole)) {
                roleLabels.add("Main Admin");
                roleValues.add(Constants.ROLE_MAIN_ADMIN);
            } else {
                roleLabels.add("Outlet Manager");
                roleValues.add(Constants.ROLE_OUTLET_MANAGER);
            }
        } else if (Constants.ROLE_OUTLET_MANAGER.equals(currentRole)) {
            roleLabels.add("Outlet Staff");
            roleValues.add(Constants.ROLE_OUTLET_STAFF);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, roleLabels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);
    }

    private void setClickListeners() {
        btnRegister.setOnClickListener(v -> performRegistration());
    }

    private void performRegistration() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        int selectedIndex = spinnerRole.getSelectedItemPosition();

        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Name is required");
            etFullName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(phone) || phone.length() < 9) {
            etPhone.setError("Valid phone number required");
            etPhone.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        String selectedRole = roleValues.get(selectedIndex);
        showLoading(true);

        FirebaseAuth tempAuth = FirebaseAuth.getInstance();
        String currentAdminEmail = sessionManager.getUserEmail();
        String currentAdminId = sessionManager.getUserId();

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser newUser = firebaseAuth.getCurrentUser();
                        if (newUser != null) {
                            String newUserId = newUser.getUid();
                            User user = new User(newUserId, fullName, email, phone);
                            user.setRole(selectedRole);

                            databaseReference.child(Constants.NODE_USERS)
                                    .child(newUserId)
                                    .setValue(user)
                                    .addOnCompleteListener(dbTask -> {
                                        showLoading(false);
                                        if (dbTask.isSuccessful()) {
                                            Toast.makeText(this,
                                                    fullName + " registered successfully!",
                                                    Toast.LENGTH_SHORT).show();
                                            clearForm();
                                        } else {
                                            Toast.makeText(this,
                                                    "Failed to save user data",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        showLoading(false);
                        Toast.makeText(this,
                                "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void clearForm() {
        etFullName.setText("");
        etEmail.setText("");
        etPhone.setText("");
        etPassword.setText("");
        etConfirmPassword.setText("");
        etFullName.requestFocus();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!show);
    }
}