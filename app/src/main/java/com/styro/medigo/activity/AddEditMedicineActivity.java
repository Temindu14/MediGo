package com.styro.medigo.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.styro.medigo.R;
import com.styro.medigo.model.Medicine;
import com.styro.medigo.util.Constants;

public class AddEditMedicineActivity extends AppCompatActivity {

    private TextView tvTitle;
    private EditText etName, etBrand, etCategory, etStrength, etPrice, etStock, etDescription, etUsageGuidelines;
    private CheckBox cbRequiresPrescription, cbFeatured, cbAvailable;
    private Button btnSave;
    private ProgressBar progressBar;

    private DatabaseReference databaseReference;
    private String medicineId;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_medicine);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.addMedicineRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseReference = FirebaseDatabase.getInstance().getReference();
        medicineId = getIntent().getStringExtra(Constants.KEY_MEDICINE_ID);
        isEditMode = medicineId != null;

        initViews();
        setClickListeners();

        if (isEditMode) {
            loadMedicineData();
        }
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvTitle);
        etName = findViewById(R.id.etName);
        etBrand = findViewById(R.id.etBrand);
        etCategory = findViewById(R.id.etCategory);
        etStrength = findViewById(R.id.etStrength);
        etPrice = findViewById(R.id.etPrice);
        etStock = findViewById(R.id.etStock);
        etDescription = findViewById(R.id.etDescription);
        etUsageGuidelines = findViewById(R.id.etUsageGuidelines);
        cbRequiresPrescription = findViewById(R.id.cbRequiresPrescription);
        cbFeatured = findViewById(R.id.cbFeatured);
        cbAvailable = findViewById(R.id.cbAvailable);
        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);

        tvTitle.setText(isEditMode ? "Edit Medicine" : "Add Medicine");
        cbAvailable.setChecked(true);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setClickListeners() {
        btnSave.setOnClickListener(v -> saveMedicine());
    }

    private void loadMedicineData() {
        progressBar.setVisibility(View.VISIBLE);

        databaseReference.child(Constants.NODE_MEDICINES).child(medicineId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        progressBar.setVisibility(View.GONE);
                        Medicine medicine = snapshot.getValue(Medicine.class);
                        if (medicine != null) {
                            etName.setText(medicine.getName());
                            etBrand.setText(medicine.getBrand());
                            etCategory.setText(medicine.getCategory());
                            etStrength.setText(medicine.getStrength());
                            etPrice.setText(String.valueOf(medicine.getPrice()));
                            etStock.setText(String.valueOf(medicine.getStockQuantity()));
                            etDescription.setText(medicine.getDescription());
                            etUsageGuidelines.setText(medicine.getUsageGuidelines());
                            cbRequiresPrescription.setChecked(medicine.isRequiresPrescription());
                            cbFeatured.setChecked(medicine.isFeatured());
                            cbAvailable.setChecked(medicine.isAvailable());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void saveMedicine() {
        String name = etName.getText().toString().trim();
        String brand = etBrand.getText().toString().trim();
        String category = etCategory.getText().toString().trim();
        String strength = etStrength.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String stockStr = etStock.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String usageGuidelines = etUsageGuidelines.getText().toString().trim();

        if (TextUtils.isEmpty(name)) { etName.setError("Required"); etName.requestFocus(); return; }
        if (TextUtils.isEmpty(brand)) { etBrand.setError("Required"); etBrand.requestFocus(); return; }
        if (TextUtils.isEmpty(category)) { etCategory.setError("Required"); etCategory.requestFocus(); return; }
        if (TextUtils.isEmpty(priceStr)) { etPrice.setError("Required"); etPrice.requestFocus(); return; }
        if (TextUtils.isEmpty(stockStr)) { etStock.setError("Required"); etStock.requestFocus(); return; }

        double price = Double.parseDouble(priceStr);
        int stock = Integer.parseInt(stockStr);

        String id = isEditMode ? medicineId : databaseReference.child(Constants.NODE_MEDICINES).push().getKey();

        Medicine medicine = new Medicine(id, name, category, price);
        medicine.setBrand(brand);
        medicine.setStrength(strength);
        medicine.setStockQuantity(stock);
        medicine.setDescription(description);
        medicine.setUsageGuidelines(usageGuidelines);
        medicine.setRequiresPrescription(cbRequiresPrescription.isChecked());
        medicine.setFeatured(cbFeatured.isChecked());
        medicine.setAvailable(cbAvailable.isChecked());
        medicine.setOutletId("outlet001");
        medicine.setUpdatedAt(System.currentTimeMillis());

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        databaseReference.child(Constants.NODE_MEDICINES).child(id)
                .setValue(medicine)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, isEditMode ? "Medicine updated!" : "Medicine added!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to save medicine", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}