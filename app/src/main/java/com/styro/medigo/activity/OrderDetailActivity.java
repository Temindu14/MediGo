package com.styro.medigo.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
import com.styro.medigo.model.Order;
import com.styro.medigo.util.Constants;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView tvOrderId, tvDeliveryAddress, tvPaymentMethod, tvTotalAmount, tvCurrentStatus;
    private Spinner spinnerStatus;
    private Button btnUpdateStatus;
    private ProgressBar progressBar;

    private DatabaseReference databaseReference;
    private String orderId;
    private Order currentOrder;

    private static final String[] STATUS_OPTIONS = {
            "pending", "processing", "out_for_delivery", "delivered", "cancelled"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.orderDetailRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        orderId = getIntent().getStringExtra(Constants.KEY_ORDER_ID);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        initViews();
        loadOrderData();
    }

    private void initViews() {
        tvOrderId = findViewById(R.id.tvOrderId);
        tvDeliveryAddress = findViewById(R.id.tvDeliveryAddress);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvCurrentStatus = findViewById(R.id.tvCurrentStatus);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        btnUpdateStatus = findViewById(R.id.btnUpdateStatus);
        progressBar = findViewById(R.id.progressBar);

        String[] displayStatuses = {"Pending", "Processing", "Out for Delivery", "Delivered", "Cancelled"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, displayStatuses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnUpdateStatus.setOnClickListener(v -> updateOrderStatus());
    }

    private void loadOrderData() {
        progressBar.setVisibility(View.VISIBLE);

        databaseReference.child(Constants.NODE_ORDERS).child(orderId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        progressBar.setVisibility(View.GONE);
                        currentOrder = snapshot.getValue(Order.class);
                        if (currentOrder != null) {
                            tvOrderId.setText("Order #" + currentOrder.getOrderId().substring(0, 8).toUpperCase());
                            tvDeliveryAddress.setText(currentOrder.getDeliveryAddress());
                            tvPaymentMethod.setText(currentOrder.getPaymentMethod());
                            tvTotalAmount.setText(String.format("Rs. %.2f", currentOrder.getTotalAmount()));
                            tvCurrentStatus.setText(currentOrder.getStatus().toUpperCase());

                            for (int i = 0; i < STATUS_OPTIONS.length; i++) {
                                if (STATUS_OPTIONS[i].equals(currentOrder.getStatus())) {
                                    spinnerStatus.setSelection(i);
                                    break;
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void updateOrderStatus() {
        int selectedIndex = spinnerStatus.getSelectedItemPosition();
        String newStatus = STATUS_OPTIONS[selectedIndex];

        progressBar.setVisibility(View.VISIBLE);
        btnUpdateStatus.setEnabled(false);

        databaseReference.child(Constants.NODE_ORDERS).child(orderId)
                .child("status")
                .setValue(newStatus)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    btnUpdateStatus.setEnabled(true);
                    if (task.isSuccessful()) {
                        tvCurrentStatus.setText(newStatus.toUpperCase());
                        Toast.makeText(this, "Order status updated to " + newStatus, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}