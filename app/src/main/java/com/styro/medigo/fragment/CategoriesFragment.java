package com.styro.medigo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
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
import com.styro.medigo.activity.ProductDetailsActivity;
import com.styro.medigo.activity.SearchActivity;
import com.styro.medigo.adapter.CategoryAdapter;
import com.styro.medigo.adapter.MedicineAdapter;
import com.styro.medigo.model.Category;
import com.styro.medigo.model.Medicine;
import com.styro.medigo.util.Constants;

public class CategoriesFragment extends Fragment implements MedicineAdapter.OnMedicineClickListener {

    private RecyclerView rvCategories;
    private RecyclerView rvMedicines;
    private View progressBar;

    private DatabaseReference databaseReference;

    private CategoryAdapter categoryAdapter;
    private MedicineAdapter medicineAdapter;

    private List<Category> categoryList;
    private List<Medicine> medicineList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_categories, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initFirebase();
        setupRecyclerViews();
        loadCategories();
        loadAllMedicines();
    }

    private void initViews(View view) {
        rvCategories = view.findViewById(R.id.rvCategories);
        rvMedicines = view.findViewById(R.id.rvMedicines);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void initFirebase() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    private void setupRecyclerViews() {
        categoryList = new ArrayList<>();
        medicineList = new ArrayList<>();

        categoryAdapter = new CategoryAdapter(requireContext(), categoryList);
        categoryAdapter.setOnCategoryClickListener(new CategoryAdapter.OnCategoryClickListener() {
            @Override
            public void onCategoryClick(Category category) {
                filterByCategory(category.getName());
            }
        });
        rvCategories.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);

        medicineAdapter = new MedicineAdapter(requireContext(), medicineList, this);
        rvMedicines.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        rvMedicines.setAdapter(medicineAdapter);
    }

    private void loadCategories() {
        databaseReference.child(Constants.NODE_CATEGORIES)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        categoryList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Category category = dataSnapshot.getValue(Category.class);
                            if (category != null && category.isActive()) {
                                categoryList.add(category);
                            }
                        }
                        categoryAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                    }

                });
    }

    private void loadAllMedicines() {
        progressBar.setVisibility(View.VISIBLE);

        databaseReference.child(Constants.NODE_MEDICINES)
                .limitToFirst(50)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        progressBar.setVisibility(View.GONE);
                        medicineList.clear();

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Medicine medicine = dataSnapshot.getValue(Medicine.class);
                            if (medicine != null && medicine.isAvailable()) {
                                medicineList.add(medicine);
                            }
                        }

                        medicineAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                    }

                });
    }

    public void filterByCategory(String categoryName) {
        progressBar.setVisibility(View.VISIBLE);

        databaseReference.child(Constants.NODE_MEDICINES)
                .orderByChild("category")
                .equalTo(categoryName)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        progressBar.setVisibility(View.GONE);
                        medicineList.clear();

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Medicine medicine = dataSnapshot.getValue(Medicine.class);
                            if (medicine != null && medicine.isAvailable()) {
                                medicineList.add(medicine);
                            }
                        }

                        medicineAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                    }

                });
    }

    @Override
    public void onMedicineClick(Medicine medicine) {
        Intent intent = new Intent(requireContext(), ProductDetailsActivity.class);
        intent.putExtra(Constants.KEY_MEDICINE_ID, medicine.getMedicineId());
        startActivity(intent);
    }

}