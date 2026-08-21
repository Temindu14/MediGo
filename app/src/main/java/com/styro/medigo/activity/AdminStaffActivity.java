package com.styro.medigo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import com.styro.medigo.R;
import com.styro.medigo.adapter.OrderAdapter;
import com.styro.medigo.model.Order;
import com.styro.medigo.util.Constants;
import com.styro.medigo.util.SessionManager;

public class AdminStaffActivity extends AppCompatActivity implements OrderAdapter.OnOrderClickListener {

    private TextView tvStaffName;
    private TextView tvPendingCount;
    private TextView tvProcessingCount;
    private CardView cardManageMedicines;
    private CardView cardViewOrders;
    private CardView cardUploadPrescription;
    private RecyclerView rvPendingOrders;
    private SwipeRefreshLayout swipeRefresh;
    private View progressBar;

    private DatabaseReference databaseReference;
    private SessionManager sessionManager;
    private OrderAdapter orderAdapter;
    private List<Order> pendingOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_staff);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.staffRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sessionManager = new SessionManager(this);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        initViews();
        setupRecyclerView();
        setClickListeners();
        loadDashboardData();
    }

    private void initViews() {
        tvStaffName = findViewById(R.id.tvStaffName);
        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvProcessingCount = findViewById(R.id.tvProcessingCount);
        cardManageMedicines = findViewById(R.id.cardManageMedicines);
        cardViewOrders = findViewById(R.id.cardViewOrders);
        cardUploadPrescription = findViewById(R.id.cardUploadPrescription);
        rvPendingOrders = findViewById(R.id.rvPendingOrders);
        swipeRefresh = findViewById(R.id.swipeRefreshLayout);
        progressBar = findViewById(R.id.progressBar);

        tvStaffName.setText(getString(R.string.label_welcome,
                sessionManager.getUserName().split(" ")[0]));
    }

    private void setupRecyclerView() {
        pendingOrders = new ArrayList<>();
        orderAdapter = new OrderAdapter(this, pendingOrders, this);
        rvPendingOrders.setLayoutManager(new LinearLayoutManager(this));
        rvPendingOrders.setAdapter(orderAdapter);
    }

    private void setClickListeners() {
        cardManageMedicines.setOnClickListener(v ->
                startActivity(new Intent(this, ManageMedicinesActivity.class)));

        cardViewOrders.setOnClickListener(v ->
                startActivity(new Intent(this, ManageOrdersActivity.class)));

        cardUploadPrescription.setOnClickListener(v ->
                startActivity(new Intent(this, PrescriptionUploadActivity.class)));

        swipeRefresh.setOnRefreshListener(this::loadDashboardData);

        findViewById(R.id.btnLogout).setOnClickListener(v -> confirmLogout());
    }

    private void loadDashboardData() {
        progressBar.setVisibility(View.VISIBLE);

        databaseReference.child(Constants.NODE_ORDERS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);
                        pendingOrders.clear();

                        int pendingCount = 0;
                        int processingCount = 0;

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Order order = dataSnapshot.getValue(Order.class);
                            if (order != null) {
                                if ("pending".equals(order.getStatus())) {
                                    pendingCount++;
                                    pendingOrders.add(order);
                                } else if ("processing".equals(order.getStatus())) {
                                    processingCount++;
                                }
                            }
                        }

                        tvPendingCount.setText(String.valueOf(pendingCount));
                        tvProcessingCount.setText(String.valueOf(processingCount));
                        orderAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);
                    }
                });
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    sessionManager.clearSession();
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onOrderClick(Order order) {
        Intent intent = new Intent(this, OrderDetailActivity.class);
        intent.putExtra(Constants.KEY_ORDER_ID, order.getOrderId());
        startActivity(intent);
    }
}