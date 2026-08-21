package com.styro.medigo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.styro.medigo.R;
import com.styro.medigo.adapter.CartAdapter;
import com.styro.medigo.database.DatabaseHelper;
import com.styro.medigo.model.CartItem;
import com.styro.medigo.util.Constants;

public class CartActivity extends AppCompatActivity implements CartAdapter.CartActionListener {

    private RecyclerView rvCartItems;
    private TextView tvSubtotal, tvDeliveryFee, tvTotal;
    private Button btnCheckout;
    private View layoutEmptyCart, layoutCartContent;

    private DatabaseHelper databaseHelper;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.cartRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupToolbar();
        initDatabase();
        loadCartItems();
        setClickListeners();
    }

    private void initViews() {
        rvCartItems = findViewById(R.id.rvCartItems);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvDeliveryFee = findViewById(R.id.tvDeliveryFee);
        tvTotal = findViewById(R.id.tvTotal);
        btnCheckout = findViewById(R.id.btnCheckout);
        layoutEmptyCart = findViewById(R.id.layoutEmptyCart);
        layoutCartContent = findViewById(R.id.layoutCartContent);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_cart);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initDatabase() {
        databaseHelper = new DatabaseHelper(this);
    }

    private void loadCartItems() {
        cartItems = databaseHelper.getAllCartItems();

        if (cartItems.isEmpty()) {
            layoutEmptyCart.setVisibility(View.VISIBLE);
            layoutCartContent.setVisibility(View.GONE);
            return;
        }

        layoutEmptyCart.setVisibility(View.GONE);
        layoutCartContent.setVisibility(View.VISIBLE);

        cartAdapter = new CartAdapter(this, cartItems, this);
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        rvCartItems.setAdapter(cartAdapter);

        updateOrderSummary();
    }

    private void updateOrderSummary() {
        double subtotal = databaseHelper.getCartTotal();
        double deliveryFee = subtotal >= Constants.FREE_DELIVERY_THRESHOLD ? 0.0 : Constants.DELIVERY_FEE;
        double total = subtotal + deliveryFee;

        tvSubtotal.setText(getString(R.string.price_format, subtotal));
        tvDeliveryFee.setText(deliveryFee == 0 ? getString(R.string.label_free) : getString(R.string.price_format, deliveryFee));
        tvTotal.setText(getString(R.string.price_format, total));
    }

    private void setClickListeners() {
        btnCheckout.setOnClickListener(v -> startActivity(new Intent(this, CheckoutActivity.class)));
    }

    @Override
    public void onQuantityChanged(int itemId, int newQuantity) {
        if (newQuantity <= 0) {
            databaseHelper.removeCartItem(itemId);
        } else {
            databaseHelper.updateCartItemQuantity(itemId, newQuantity);
        }
        loadCartItems();
    }

    @Override
    public void onItemRemoved(int itemId) {
        databaseHelper.removeCartItem(itemId);
        loadCartItems();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCartItems();
    }
}