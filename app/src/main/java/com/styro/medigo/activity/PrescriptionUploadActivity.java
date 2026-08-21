package com.styro.medigo.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

import com.styro.medigo.R;
import com.styro.medigo.util.Constants;
import com.styro.medigo.util.SessionManager;

public class PrescriptionUploadActivity extends AppCompatActivity {

    private ImageView ivPrescriptionPreview;
    private TextView tvUploadHint;
    private Button btnChooseFromGallery;
    private Button btnTakePhoto;
    private Button btnUpload;
    private View progressBar;

    private FirebaseStorage firebaseStorage;
    private SessionManager sessionManager;

    private Uri selectedImageUri;
    private String medicineId;

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    previewImage(selectedImageUri);
                }
            }
    );

    private final ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            granted -> {
                if (granted) {
                    openGallery();
                } else {
                    Toast.makeText(this,
                            getString(R.string.error_permission_denied),
                            Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescription_upload);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.prescriptionRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        medicineId = getIntent().getStringExtra(Constants.KEY_MEDICINE_ID);

        initViews();
        initFirebase();
        setupToolbar();
        setClickListeners();
    }

    private void initViews() {
        ivPrescriptionPreview = findViewById(R.id.ivPrescriptionPreview);
        tvUploadHint = findViewById(R.id.tvUploadHint);
        btnChooseFromGallery = findViewById(R.id.btnChooseFromGallery);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnUpload = findViewById(R.id.btnUpload);
        progressBar = findViewById(R.id.progressBar);
    }

    private void initFirebase() {
        firebaseStorage = FirebaseStorage.getInstance();
        sessionManager = new SessionManager(this);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.title_prescription_upload));
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setClickListeners() {
        btnChooseFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndRequestPermission();
            }
        });

        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(PrescriptionUploadActivity.this,
                        getString(R.string.msg_camera_coming_soon),
                        Toast.LENGTH_SHORT).show();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadPrescription();
            }
        });
    }

    private void checkAndRequestPermission() {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission)
                == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            permissionLauncher.launch(permission);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private void previewImage(Uri imageUri) {
        tvUploadHint.setVisibility(View.GONE);
        ivPrescriptionPreview.setVisibility(View.VISIBLE);

        Glide.with(this)
                .load(imageUri)
                .into(ivPrescriptionPreview);

        btnUpload.setEnabled(true);
    }

    private void uploadPrescription() {
        if (selectedImageUri == null) {
            Toast.makeText(this,
                    getString(R.string.error_no_image_selected),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        String fileName = Constants.STORAGE_PRESCRIPTIONS
                + sessionManager.getUserId()
                + "_"
                + UUID.randomUUID().toString()
                + ".jpg";

        StorageReference storageRef = firebaseStorage.getReference().child(fileName);

        storageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        showLoading(false);
                        Toast.makeText(PrescriptionUploadActivity.this,
                                getString(R.string.msg_prescription_uploaded),
                                Toast.LENGTH_SHORT).show();
                        finish();
                    });
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(PrescriptionUploadActivity.this,
                            getString(R.string.error_upload_failed),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnUpload.setEnabled(!show);
        btnChooseFromGallery.setEnabled(!show);
        btnTakePhoto.setEnabled(!show);
    }

}