package com.styro.medigo.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

import android.widget.ProgressBar;
import android.widget.TextView;

import com.styro.medigo.R;
import com.styro.medigo.activity.DiagnosticBookingActivity;
import com.styro.medigo.activity.LoginActivity;
import com.styro.medigo.activity.NotificationsActivity;
import com.styro.medigo.activity.PrescriptionUploadActivity;
import com.styro.medigo.activity.ProfileActivity;
import com.styro.medigo.util.SessionManager;

public class ProfileFragment extends Fragment {

    private CircleImageView ivProfilePicture;
    private TextView tvUserName, tvUserEmail, tvUserPhone;
    private ProgressBar progressBar;

    private SessionManager sessionManager;
    private String currentUserId;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) uploadProfilePicture(uri);
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager  = new SessionManager(requireContext());
        currentUserId   = sessionManager.getUserId();

        bindViews(view);
        populateUserInfo();
        loadProfilePicture();
        setClickListeners(view);
    }

    private void bindViews(View view) {
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        tvUserName       = view.findViewById(R.id.tvUserName);
        tvUserEmail      = view.findViewById(R.id.tvUserEmail);
        tvUserPhone      = view.findViewById(R.id.tvUserPhone);
        progressBar      = view.findViewById(R.id.progressBar);
    }

    private void populateUserInfo() {
        tvUserName.setText(sessionManager.getUserName());
        tvUserEmail.setText(sessionManager.getUserEmail());
        tvUserPhone.setText(sessionManager.getUserPhone());
    }

    private void loadProfilePicture() {
        String imageUrl = sessionManager.getProfileImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_profile)
                    .error(R.drawable.placeholder_profile)
                    .circleCrop()
                    .into(ivProfilePicture);
        }
    }

    private void setClickListeners(View view) {
        ivProfilePicture.setOnClickListener(v ->
                imagePickerLauncher.launch("image/*"));

        view.findViewById(R.id.layoutEditProfile).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ProfileActivity.class)));

        view.findViewById(R.id.layoutUploadPrescription).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), PrescriptionUploadActivity.class)));

        view.findViewById(R.id.layoutBookDiagnostic).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), DiagnosticBookingActivity.class)));

        view.findViewById(R.id.layoutNotifications).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), NotificationsActivity.class)));

        view.findViewById(R.id.layoutLogout).setOnClickListener(v ->
                showLogoutDialog());
    }

    private void uploadProfilePicture(Uri imageUri) {
        if (currentUserId == null || currentUserId.isEmpty()) return;

        showLoading(true);

        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference("profile_pictures/" + currentUserId + ".jpg");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        storageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                            String url = downloadUri.toString();
                            saveProfilePictureUrl(url);
                        }))
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(requireContext(),
                            "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveProfilePictureUrl(String url) {
        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUserId)
                .child("profileImageUrl")
                .setValue(url)
                .addOnSuccessListener(unused -> {
                    showLoading(false);
                    sessionManager.updateProfileImageUrl(url);
                    Glide.with(this)
                            .load(url)
                            .placeholder(R.drawable.placeholder_profile)
                            .circleCrop()
                            .into(ivProfilePicture);
                    Toast.makeText(requireContext(),
                            "Profile picture updated!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(requireContext(),
                            "Failed to save picture.", Toast.LENGTH_SHORT).show();
                });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.dialog_logout_title))
                .setMessage(getString(R.string.dialog_logout_message))
                .setPositiveButton(getString(R.string.action_logout), (dialog, which) -> performLogout())
                .setNegativeButton(getString(R.string.action_cancel), null)
                .show();
    }

    private void performLogout() {
        FirebaseAuth.getInstance().signOut();
        sessionManager.clearSession();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void showLoading(boolean show) {
        if (progressBar != null)
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        populateUserInfo();
        loadProfilePicture();
    }
}