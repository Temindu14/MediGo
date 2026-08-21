package com.styro.medigo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.styro.medigo.R;
import com.styro.medigo.model.Order;
import com.styro.medigo.util.Constants;
import com.styro.medigo.util.SessionManager;

public class AdminManagerActivity extends AppCompatActivity {

    private TextView tvManagerName;
    private TextView tvTotalOrders;
    private TextView tvTotalRevenue;
    private TextView tvPendingOrders;
    private TextView tvTotalMedicines;
    private CardView cardManageStaff;
    private CardView cardManageMedicines;
    private CardView cardViewOrders;
    private CardView cardViewReports;
    private SwipeRefreshLayout swipeRefresh;
    private View progressBar;

    private DatabaseReference databaseReference;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manager);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.managerRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sessionManager = new SessionManager(this);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        initViews();
        setClickListeners();
        loadDashboardData();
    }

    private void initViews() {
        tvManagerName = findViewById(R.id.tvManagerName);
        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvPendingOrders = findViewById(R.id.tvPendingOrders);
        tvTotalMedicines = findViewById(R.id.tvTotalMedicines);
        cardManageStaff = findViewById(R.id.cardManageStaff);
        cardManageMedicines = findViewById(R.id.cardManageMedicines);
        cardViewOrders = findViewById(R.id.cardViewOrders);
        cardViewReports = findViewById(R.id.cardViewReports);
        swipeRefresh = findViewById(R.id.swipeRefreshLayout);
        progressBar = findViewById(R.id.progressBar);

        tvManagerName.setText(getString(R.string.label_welcome,
                sessionManager.getUserName().split(" ")[0]));
    }

    private void setClickListeners() {
        cardManageStaff.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterAdminActivity.class);
            intent.putExtra("allowed_role", Constants.ROLE_OUTLET_STAFF);
            startActivity(intent);
        });

        cardManageMedicines.setOnClickListener(v ->
                startActivity(new Intent(this, ManageMedicinesActivity.class)));

        cardViewOrders.setOnClickListener(v ->
                startActivity(new Intent(this, ManageOrdersActivity.class)));

        cardViewReports.setOnClickListener(v ->
                startActivity(new Intent(this, ReportsActivity.class)));

        swipeRefresh.setOnRefreshListener(this::loadDashboardData);

        findViewById(R.id.btnLogout).setOnClickListener(v -> confirmLogout());
    }

    private void loadDashboardData() {
        progressBar.setVisibility(View.VISIBLE);

        databaseReference.child(Constants.NODE_ORDERS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        int total = 0;
                        int pending = 0;
                        double revenue = 0;

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Order order = dataSnapshot.getValue(Order.class);
                            if (order != null) {
                                total++;
                                revenue += order.getTotalAmount();
                                if ("pending".equals(order.getStatus())) {
                                    pending++;
                                }
                            }
                        }

                        tvTotalOrders.setText(String.valueOf(total));
                        tvPendingOrders.setText(String.valueOf(pending));
                        tvTotalRevenue.setText(String.format("Rs. %.2f", revenue));
                        swipeRefresh.setRefreshing(false);
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);
                    }
                });

        databaseReference.child(Constants.NODE_MEDICINES)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        tvTotalMedicines.setText(String.valueOf(snapshot.getChildrenCount()));
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
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
}