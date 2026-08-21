package com.styro.medigo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

public class ManageMedicinesActivity extends AppCompatActivity implements MedicineAdapter.OnMedicineClickListener {

    private RecyclerView rvMedicines;
    private SwipeRefreshLayout swipeRefresh;
    private View progressBar;
    private FloatingActionButton fabAddMedicine;

    private DatabaseReference databaseReference;
    private MedicineAdapter medicineAdapter;
    private List<Medicine> medicineList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_medicines);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.manageMedicinesRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseReference = FirebaseDatabase.getInstance().getReference();

        initViews();
        setupRecyclerView();
        setClickListeners();
        loadMedicines();
    }

    private void initViews() {
        rvMedicines = findViewById(R.id.rvMedicines);
        swipeRefresh = findViewById(R.id.swipeRefreshLayout);
        progressBar = findViewById(R.id.progressBar);
        fabAddMedicine = findViewById(R.id.fabAddMedicine);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        medicineList = new ArrayList<>();
        medicineAdapter = new MedicineAdapter(this, medicineList, this);
        rvMedicines.setLayoutManager(new LinearLayoutManager(this));
        rvMedicines.setAdapter(medicineAdapter);
    }

    private void setClickListeners() {
        fabAddMedicine.setOnClickListener(v ->
                startActivity(new Intent(this, AddEditMedicineActivity.class)));

        swipeRefresh.setOnRefreshListener(this::loadMedicines);
    }

    private void loadMedicines() {
        progressBar.setVisibility(View.VISIBLE);

        databaseReference.child(Constants.NODE_MEDICINES)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);
                        medicineList.clear();

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Medicine medicine = dataSnapshot.getValue(Medicine.class);
                            if (medicine != null) {
                                medicineList.add(medicine);
                            }
                        }
                        medicineAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);
                    }
                });
    }

    @Override
    public void onMedicineClick(Medicine medicine) {
        new AlertDialog.Builder(this)
                .setTitle(medicine.getName())
                .setItems(new String[]{"Edit", "Delete"}, (dialog, which) -> {
                    if (which == 0) {
                        Intent intent = new Intent(this, AddEditMedicineActivity.class);
                        intent.putExtra(Constants.KEY_MEDICINE_ID, medicine.getMedicineId());
                        startActivity(intent);
                    } else {
                        confirmDelete(medicine);
                    }
                })
                .show();
    }

    private void confirmDelete(Medicine medicine) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Medicine")
                .setMessage("Are you sure you want to delete " + medicine.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    databaseReference.child(Constants.NODE_MEDICINES)
                            .child(medicine.getMedicineId())
                            .removeValue()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(this, "Medicine deleted", Toast.LENGTH_SHORT).show();
                                    loadMedicines();
                                } else {
                                    Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}