package com.styro.medigo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import com.styro.medigo.R;
import com.styro.medigo.adapter.MedicineAdapter;
import com.styro.medigo.model.Medicine;
import com.styro.medigo.util.Constants;

public class SearchActivity extends AppCompatActivity implements MedicineAdapter.OnMedicineClickListener {

    private EditText etSearch;
    private ImageButton btnClear;
    private RecyclerView rvSearchResults;
    private TextView tvNoResults;
    private View progressBar;

    private DatabaseReference databaseReference;
    private MedicineAdapter medicineAdapter;
    private List<Medicine> searchResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.searchRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        initFirebase();
        setupRecyclerView();
        setupSearchListener();

        String categoryName = getIntent().getStringExtra(Constants.KEY_CATEGORY_NAME);
        if (categoryName != null && !categoryName.isEmpty()) {
            etSearch.setText(categoryName);
            etSearch.setSelection(categoryName.length());
        }
    }

    private void initViews() {
        etSearch = findViewById(R.id.etSearch);
        btnClear = findViewById(R.id.btnClear);
        rvSearchResults = findViewById(R.id.rvSearchResults);
        tvNoResults = findViewById(R.id.tvNoResults);
        progressBar = findViewById(R.id.progressBar);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnClear.setOnClickListener(v -> {
            etSearch.setText("");
            searchResults.clear();
            medicineAdapter.notifyDataSetChanged();
            tvNoResults.setVisibility(View.GONE);
        });
    }

    private void initFirebase() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    private void setupRecyclerView() {
        searchResults = new ArrayList<>();
        medicineAdapter = new MedicineAdapter(this, searchResults, this);
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        rvSearchResults.setAdapter(medicineAdapter);
    }

    private void setupSearchListener() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                btnClear.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);

                if (query.length() >= 2) {
                    searchMedicines(query);
                } else {
                    searchResults.clear();
                    medicineAdapter.notifyDataSetChanged();
                    tvNoResults.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etSearch.requestFocus();
    }

    private void searchMedicines(String query) {
        progressBar.setVisibility(View.VISIBLE);
        String lowerQuery = query.toLowerCase();

        databaseReference.child(Constants.NODE_MEDICINES)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        progressBar.setVisibility(View.GONE);
                        searchResults.clear();

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Medicine medicine = dataSnapshot.getValue(Medicine.class);
                            if (medicine != null && medicine.isAvailable()) {
                                if (medicine.getName().toLowerCase().contains(lowerQuery) ||
                                        medicine.getCategory().toLowerCase().contains(lowerQuery) ||
                                        medicine.getBrand().toLowerCase().contains(lowerQuery)) {
                                    searchResults.add(medicine);
                                }
                            }
                        }

                        medicineAdapter.notifyDataSetChanged();
                        tvNoResults.setVisibility(searchResults.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public void onMedicineClick(Medicine medicine) {
        Intent intent = new Intent(SearchActivity.this, ProductDetailsActivity.class);
        intent.putExtra(Constants.KEY_MEDICINE_ID, medicine.getMedicineId());
        startActivity(intent);
    }
}