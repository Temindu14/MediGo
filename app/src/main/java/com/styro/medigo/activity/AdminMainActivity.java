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

public class AdminMainActivity extends AppCompatActivity {

    private TextView tvAdminName, tvTotalOrders, tvTotalRevenue, tvTotalUsers, tvTotalMedicines, tvTotalOutlets;
    private CardView cardManageAdmins, cardManageManagers, cardManageMedicines, cardManageOutlets, cardViewAllOrders, cardViewReports;
    private SwipeRefreshLayout swipeRefresh;
    private View progressBar;

    private DatabaseReference databaseReference;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainAdminRoot), (v, insets) -> {
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
        tvAdminName = findViewById(R.id.tvAdminName);
        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvTotalMedicines = findViewById(R.id.tvTotalMedicines);
        tvTotalOutlets = findViewById(R.id.tvTotalOutlets);
        cardManageAdmins = findViewById(R.id.cardManageAdmins);
        cardManageManagers = findViewById(R.id.cardManageManagers);
        cardManageMedicines = findViewById(R.id.cardManageMedicines);
        cardManageOutlets = findViewById(R.id.cardManageOutlets);
        cardViewAllOrders = findViewById(R.id.cardViewAllOrders);
        cardViewReports = findViewById(R.id.cardViewReports);
        swipeRefresh = findViewById(R.id.swipeRefreshLayout);
        progressBar = findViewById(R.id.progressBar);

        tvAdminName.setText(getString(R.string.label_welcome, sessionManager.getUserName().split(" ")[0]));
    }

    private void setClickListeners() {
        cardManageAdmins.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterAdminActivity.class);
            intent.putExtra("allowed_role", Constants.ROLE_MAIN_ADMIN);
            startActivity(intent);
        });

        cardManageManagers.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterAdminActivity.class);
            intent.putExtra("allowed_role", Constants.ROLE_OUTLET_MANAGER);
            startActivity(intent);
        });

        cardManageMedicines.setOnClickListener(v -> startActivity(new Intent(this, ManageMedicinesActivity.class)));
        cardManageOutlets.setOnClickListener(v -> startActivity(new Intent(this, ManageOutletsActivity.class)));
        cardViewAllOrders.setOnClickListener(v -> startActivity(new Intent(this, ManageOrdersActivity.class)));
        cardViewReports.setOnClickListener(v -> startActivity(new Intent(this, ReportsActivity.class)));

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
                        double revenue = 0;
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Order order = dataSnapshot.getValue(Order.class);
                            if (order != null) {
                                total++;
                                revenue += order.getTotalAmount();
                            }
                        }
                        tvTotalOrders.setText(String.valueOf(total));
                        tvTotalRevenue.setText(String.format("Rs. %.2f", revenue));
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);
                    }
                });

        databaseReference.child(Constants.NODE_USERS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        tvTotalUsers.setText(String.valueOf(snapshot.getChildrenCount()));
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {}
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

        databaseReference.child(Constants.NODE_OUTLETS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        tvTotalOutlets.setText(String.valueOf(snapshot.getChildrenCount()));
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