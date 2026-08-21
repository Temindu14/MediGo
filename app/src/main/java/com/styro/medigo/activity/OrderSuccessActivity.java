package com.styro.medigo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.styro.medigo.R;
import com.styro.medigo.util.Constants;

import java.text.DecimalFormat;

public class OrderSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_success);

        String orderId       = getIntent().getStringExtra(Constants.KEY_ORDER_ID);
        String paymentMethod = getIntent().getStringExtra("PAYMENT_METHOD");
        double total         = getIntent().getDoubleExtra("TOTAL", 0.0);

        DecimalFormat df = new DecimalFormat("0.00");

        TextView tvOrderId       = findViewById(R.id.tvSuccessOrderId);
        TextView tvPaymentMethod = findViewById(R.id.tvSuccessPaymentMethod);
        TextView tvTotal         = findViewById(R.id.tvSuccessTotal);
        Button   btnTrackOrder   = findViewById(R.id.btnTrackOrder);
        Button   btnContinue     = findViewById(R.id.btnContinueShopping);

        if (orderId != null) {
            tvOrderId.setText("Order ID: " + orderId.substring(0, 8).toUpperCase());
        }
        tvPaymentMethod.setText("Payment: " + (paymentMethod != null ? paymentMethod : ""));
        tvTotal.setText("Total: LKR " + df.format(total));

        btnTrackOrder.setOnClickListener(v -> {
            Intent intent = new Intent(this, OrderTrackingActivity.class);
            intent.putExtra(Constants.KEY_ORDER_ID, orderId);
            startActivity(intent);
            finish();
        });

        btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}