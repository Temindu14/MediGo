package com.styro.medigo.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.styro.medigo.R;
import com.styro.medigo.model.User;
import com.styro.medigo.util.Constants;
import com.styro.medigo.util.SessionManager;

public class ProfileActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPhone, etPrimaryAddress, etSecondaryAddress;
    private Button btnSaveChanges;
    private View progressBar;

    private DatabaseReference databaseReference;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profileRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        initFirebase();
        setupToolbar();
        loadUserProfile();
        setClickListeners();
    }

    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPrimaryAddress = findViewById(R.id.etPrimaryAddress);
        etSecondaryAddress = findViewById(R.id.etSecondaryAddress);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        progressBar = findViewById(R.id.progressBar);

        etEmail.setEnabled(false);
    }

    private void initFirebase() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        sessionManager = new SessionManager(this);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_edit_profile);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadUserProfile() {
        progressBar.setVisibility(View.VISIBLE);

        databaseReference.child(Constants.NODE_USERS)
                .child(sessionManager.getUserId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        progressBar.setVisibility(View.GONE);
                        if (snapshot.exists()) {
                            User user = snapshot.getValue(User.class);
                            if (user != null) populateFields(user);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ProfileActivity.this, getString(R.string.error_generic), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void populateFields(User user) {
        etFullName.setText(user.getName());
        etEmail.setText(user.getEmail());
        etPhone.setText(user.getPhone());
        etPrimaryAddress.setText(user.getAddress());
        etSecondaryAddress.setText(user.getSecondaryAddress());
    }

    private void setClickListeners() {
        btnSaveChanges.setOnClickListener(v -> saveChanges());
    }

    private void saveChanges() {
        String name = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String primaryAddress = etPrimaryAddress.getText().toString().trim();
        String secondaryAddress = etSecondaryAddress.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etFullName.setError(getString(R.string.error_name_required));
            etFullName.requestFocus();
            return;
        }

        showLoading(true);

        DatabaseReference userRef = databaseReference.child(Constants.NODE_USERS).child(sessionManager.getUserId());
        userRef.child("name").setValue(name);
        userRef.child("phone").setValue(phone);
        userRef.child("address").setValue(primaryAddress);
        userRef.child("secondaryAddress").setValue(secondaryAddress)
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        sessionManager.saveUserSession(sessionManager.getUserId(), name, sessionManager.getUserEmail(), phone, sessionManager.getUserRole());
                        Toast.makeText(this, getString(R.string.msg_profile_updated), Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, getString(R.string.error_generic), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSaveChanges.setEnabled(!show);
    }
}