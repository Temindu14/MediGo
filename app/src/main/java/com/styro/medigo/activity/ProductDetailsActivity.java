package com.styro.medigo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.styro.medigo.R;
import com.styro.medigo.database.DatabaseHelper;
import com.styro.medigo.model.CartItem;
import com.styro.medigo.model.Medicine;
import com.styro.medigo.util.Constants;

public class ProductDetailsActivity extends AppCompatActivity {

    private ImageView ivProductImage;
    private TextView tvProductName, tvProductPrice, tvProductCategory, tvProductBrand, tvProductStrength, tvStockStatus, tvDescription, tvUsageGuidelines;
    private Chip chipPrescription;
    private Button btnAddToCart, btnUploadPrescription;
    private View progressBar;

    private DatabaseReference databaseReference;
    private DatabaseHelper databaseHelper;

    private String medicineId;
    private Medicine currentMedicine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.productDetailsRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        medicineId = getIntent().getStringExtra(Constants.KEY_MEDICINE_ID);

        initViews();
        initDatabase();
        setupToolbar();
        loadProductDetails();
    }

    private void initViews() {
        ivProductImage = findViewById(R.id.ivProductImage);
        tvProductName = findViewById(R.id.tvProductName);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        tvProductCategory = findViewById(R.id.tvProductCategory);
        tvProductBrand = findViewById(R.id.tvProductBrand);
        tvProductStrength = findViewById(R.id.tvProductStrength);
        tvStockStatus = findViewById(R.id.tvStockStatus);
        tvDescription = findViewById(R.id.tvDescription);
        tvUsageGuidelines = findViewById(R.id.tvUsageGuidelines);
        chipPrescription = findViewById(R.id.chipPrescription);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnUploadPrescription = findViewById(R.id.btnUploadPrescription);
        progressBar = findViewById(R.id.progressBar);
    }

    private void initDatabase() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseHelper = new DatabaseHelper(this);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadProductDetails() {
        progressBar.setVisibility(View.VISIBLE);

        databaseReference.child(Constants.NODE_MEDICINES)
                .child(medicineId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        progressBar.setVisibility(View.GONE);
                        if (snapshot.exists()) {
                            currentMedicine = snapshot.getValue(Medicine.class);
                            if (currentMedicine != null) populateUI(currentMedicine);
                        } else {
                            Toast.makeText(ProductDetailsActivity.this, getString(R.string.error_product_not_found), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ProductDetailsActivity.this, getString(R.string.error_generic), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void populateUI(Medicine medicine) {
        Glide.with(this)
                .load(medicine.getImageUrl())
                .placeholder(R.drawable.placeholder_medicine)
                .error(R.drawable.placeholder_medicine)
                .into(ivProductImage);

        tvProductName.setText(medicine.getName());
        tvProductPrice.setText(getString(R.string.price_format, medicine.getPrice()));
        tvProductCategory.setText(medicine.getCategory());
        tvProductBrand.setText(medicine.getBrand());
        tvProductStrength.setText(medicine.getStrength());
        tvDescription.setText(medicine.getDescription());
        tvUsageGuidelines.setText(medicine.getUsageGuidelines());

        if (medicine.isAvailable() && medicine.getStockQuantity() > 0) {
            tvStockStatus.setText(getString(R.string.status_in_stock));
            tvStockStatus.setTextColor(getColor(R.color.green_success));
        } else {
            tvStockStatus.setText(getString(R.string.status_out_of_stock));
            tvStockStatus.setTextColor(getColor(R.color.red_error));
            btnAddToCart.setEnabled(false);
        }

        if (medicine.isRequiresPrescription()) {
            chipPrescription.setVisibility(View.VISIBLE);
            btnUploadPrescription.setVisibility(View.VISIBLE);
        } else {
            chipPrescription.setVisibility(View.GONE);
            btnUploadPrescription.setVisibility(View.GONE);
        }

        setButtonListeners();
    }

    private void setButtonListeners() {
        btnAddToCart.setOnClickListener(v -> addToCart());
        btnUploadPrescription.setOnClickListener(v -> {
            Intent intent = new Intent(this, PrescriptionUploadActivity.class);
            intent.putExtra(Constants.KEY_MEDICINE_ID, medicineId);
            startActivity(intent);
        });
    }

    private void addToCart() {
        if (currentMedicine == null) return;

        CartItem cartItem = new CartItem(
                currentMedicine.getMedicineId(),
                currentMedicine.getName(),
                currentMedicine.getImageUrl(),
                currentMedicine.getPrice(),
                1,
                currentMedicine.isRequiresPrescription()
        );

        long result = databaseHelper.addToCart(cartItem);
        if (result != -1) {
            Toast.makeText(this, getString(R.string.msg_added_to_cart), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.error_add_to_cart), Toast.LENGTH_SHORT).show();
        }
    }
}