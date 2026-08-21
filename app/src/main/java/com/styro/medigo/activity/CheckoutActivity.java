package com.styro.medigo.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.styro.medigo.R;
import com.styro.medigo.database.DatabaseHelper;
import com.styro.medigo.model.CartItem;
import com.styro.medigo.model.Order;
import com.styro.medigo.util.Constants;
import com.styro.medigo.util.SessionManager;

import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.Item;
import lk.payhere.androidsdk.model.StatusResponse;

public class CheckoutActivity extends AppCompatActivity {

    private static final int PAYHERE_REQUEST_CODE = 1001;
    private static final int MAP_PICKER_REQUEST_CODE = 1002;
    private static final String MERCHANT_ID = "1231610";
    private static final String MERCHANT_SECRET = "MzA4Mzk4MTIyNTQ1OTc5OTA1MzEyMTQ4NDc5MDExOTA5NDQwMjYx";

    private EditText etDeliveryAddress;
    private RadioGroup rgPaymentMethod;
    private RadioButton rbCashOnDelivery, rbCard;
    private TextView tvSubtotal, tvDeliveryFee, tvTotal;
    private Button btnPlaceOrder, btnPickOnMap, btnUploadPrescription;
    private ImageView ivPrescriptionPreview;
    private TextView tvPrescriptionStatus;
    private View progressBar;

    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    private String pendingOrderId, pendingAddress;
    private double deliveryLatitude = 0.0, deliveryLongitude = 0.0;
    private Uri prescriptionUri = null, cameraImageUri = null;
    private String prescriptionUrl = "";

    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && cameraImageUri != null) {
                    prescriptionUri = cameraImageUri;
                    showPrescriptionPreview(prescriptionUri);
                }
            });

    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    prescriptionUri = uri;
                    showPrescriptionPreview(prescriptionUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.checkoutRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        initDatabase();
        setupToolbar();
        populateOrderSummary();
        setClickListeners();
    }

    private void initViews() {
        etDeliveryAddress = findViewById(R.id.etDeliveryAddress);
        rgPaymentMethod = findViewById(R.id.rgPaymentMethod);
        rbCashOnDelivery = findViewById(R.id.rbCashOnDelivery);
        rbCard = findViewById(R.id.rbCard);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvDeliveryFee = findViewById(R.id.tvDeliveryFee);
        tvTotal = findViewById(R.id.tvTotal);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        btnPickOnMap = findViewById(R.id.btnPickOnMap);
        btnUploadPrescription = findViewById(R.id.btnUploadPrescription);
        ivPrescriptionPreview = findViewById(R.id.ivPrescriptionPreview);
        tvPrescriptionStatus = findViewById(R.id.tvPrescriptionStatus);
        progressBar = findViewById(R.id.progressBar);
    }

    private void initDatabase() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_checkout);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void populateOrderSummary() {
        double subtotal = databaseHelper.getCartTotal();
        double deliveryFee = subtotal >= Constants.FREE_DELIVERY_THRESHOLD ? 0.0 : Constants.DELIVERY_FEE;
        double total = subtotal + deliveryFee;

        tvSubtotal.setText(getString(R.string.price_format, subtotal));
        tvDeliveryFee.setText(deliveryFee == 0 ? getString(R.string.label_free) : getString(R.string.price_format, deliveryFee));
        tvTotal.setText(getString(R.string.price_format, total));
    }

    private void setClickListeners() {
        btnPlaceOrder.setOnClickListener(v -> placeOrder());
        btnPickOnMap.setOnClickListener(v -> startActivityForResult(new Intent(this, MapPickerActivity.class), MAP_PICKER_REQUEST_CODE));
        btnUploadPrescription.setOnClickListener(v -> showImageSourceDialog());
    }

    private void showImageSourceDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Upload Prescription")
                .setItems(new String[]{"Take a Photo", "Choose from Gallery"}, (dialog, which) -> {
                    if (which == 0) openCamera();
                    else galleryLauncher.launch("image/*");
                })
                .show();
    }

    private void openCamera() {
        try {
            File photoFile = createImageFile();
            cameraImageUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
            cameraLauncher.launch(cameraImageUri);
        } catch (IOException e) {
            Toast.makeText(this, "Could not open camera.", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return File.createTempFile("PRESCRIPTION_" + timeStamp, ".jpg", getExternalFilesDir(null));
    }

    private void showPrescriptionPreview(Uri uri) {
        ivPrescriptionPreview.setVisibility(View.VISIBLE);
        tvPrescriptionStatus.setText("Prescription selected");
        tvPrescriptionStatus.setVisibility(View.VISIBLE);
        Glide.with(this).load(uri).into(ivPrescriptionPreview);
        btnUploadPrescription.setText("Change Prescription");
    }

    private void placeOrder() {
        String address = etDeliveryAddress.getText().toString().trim();
        if (address.isEmpty()) {
            etDeliveryAddress.setError(getString(R.string.error_address_required));
            etDeliveryAddress.requestFocus();
            return;
        }

        showLoading(true);
        if (prescriptionUri != null) uploadPrescriptionAndPlaceOrder(address);
        else proceedWithOrder(address);
    }

    private void uploadPrescriptionAndPlaceOrder(String address) {
        String fileName = "prescriptions/" + sessionManager.getUserId() + "_" + System.currentTimeMillis() + ".jpg";
        StorageReference ref = storageReference.child(fileName);

        tvPrescriptionStatus.setText("Uploading prescription...");
        ref.putFile(prescriptionUri)
                .addOnSuccessListener(snapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    prescriptionUrl = uri.toString();
                    tvPrescriptionStatus.setText("Prescription uploaded");
                    showLoading(false);
                    proceedWithOrder(address);
                }))
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Failed to upload prescription. Please try again.", Toast.LENGTH_SHORT).show();
                    tvPrescriptionStatus.setText("Upload failed - tap to retry");
                });
    }

    private void proceedWithOrder(String address) {
        if (rbCard.isChecked()) {
            showLoading(false);
            startPayHerePayment(address);
        } else {
            saveOrderToFirebase(UUID.randomUUID().toString(), address, Constants.PAYMENT_CASH_ON_DELIVERY, "");
        }
    }

    private void startPayHerePayment(String address) {
        pendingOrderId = UUID.randomUUID().toString();
        pendingAddress = address;

        double subtotal = databaseHelper.getCartTotal();
        double deliveryFee = subtotal >= Constants.FREE_DELIVERY_THRESHOLD ? 0.0 : Constants.DELIVERY_FEE;
        double total = subtotal + deliveryFee;

        String fullName = sessionManager.getUserName() != null ? sessionManager.getUserName() : "Customer";
        String firstName = fullName, lastName = "";
        if (fullName.contains(" ")) {
            firstName = fullName.substring(0, fullName.indexOf(" "));
            lastName = fullName.substring(fullName.indexOf(" ") + 1);
        }

        String email = sessionManager.getUserEmail() != null ? sessionManager.getUserEmail() : "customer@medigo.lk";
        String phone = sessionManager.getUserPhone() != null ? sessionManager.getUserPhone() : "0700000000";

        PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL);
        InitRequest request = new InitRequest();
        request.setMerchantId(MERCHANT_ID);
        request.setMerchantSecret(MERCHANT_SECRET);
        request.setOrderId(pendingOrderId);
        request.setItemsDescription("MediGo Medicine Order");
        request.setAmount(total);
        request.setCurrency("LKR");

        request.getCustomer().setFirstName(firstName);
        request.getCustomer().setLastName(lastName);
        request.getCustomer().setEmail(email);
        request.getCustomer().setPhone(phone);
        request.getCustomer().getAddress().setAddress(address);
        request.getCustomer().getAddress().setCity("Colombo");
        request.getCustomer().getAddress().setCountry("Sri Lanka");

        List<Item> items = new ArrayList<>();
        for (CartItem cartItem : databaseHelper.getAllCartItems()) {
            Item item = new Item();
            item.setId(cartItem.getMedicineId());
            item.setName(cartItem.getMedicineName());
            item.setQuantity(cartItem.getQuantity());
            item.setAmount(cartItem.getPrice() * cartItem.getQuantity());
            items.add(item);
        }
        if (deliveryFee > 0) {
            Item delivery = new Item();
            delivery.setId("delivery");
            delivery.setName("Delivery Fee");
            delivery.setQuantity(1);
            delivery.setAmount(deliveryFee);
            items.add(delivery);
        }
        request.setItems(items);

        Intent intent = new Intent(this, PHMainActivity.class);
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, request);
        startActivityForResult(intent, PAYHERE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MAP_PICKER_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String address = data.getStringExtra(MapPickerActivity.RESULT_ADDRESS);
            deliveryLatitude = data.getDoubleExtra(MapPickerActivity.RESULT_LATITUDE, 0.0);
            deliveryLongitude = data.getDoubleExtra(MapPickerActivity.RESULT_LONGITUDE, 0.0);
            if (address != null) etDeliveryAddress.setText(address);
            return;
        }

        if (requestCode == PAYHERE_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                PHResponse<StatusResponse> response = (PHResponse<StatusResponse>) data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);
                if (response != null && response.isSuccess()) {
                    String transactionId = (response.getData() != null) ? String.valueOf(response.getData().getPaymentNo()) : "";
                    saveOrderToFirebase(pendingOrderId, pendingAddress, Constants.PAYMENT_CARD, transactionId);
                } else {
                    Serializable msg = (response != null) ? response.getData() : "Payment failed.";
                    new AlertDialog.Builder(this)
                            .setTitle("Payment Failed")
                            .setMessage(msg + "\n\nWould you like to use Cash on Delivery instead?")
                            .setPositiveButton("Use Cash on Delivery", (d, w) -> rbCashOnDelivery.setChecked(true))
                            .setNegativeButton("Try Again", null).show();
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Payment was cancelled.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveOrderToFirebase(String orderId, String address, String paymentMethod, String transactionId) {
        showLoading(true);

        List<CartItem> cartItems = databaseHelper.getAllCartItems();
        double subtotal = databaseHelper.getCartTotal();
        double deliveryFee = subtotal >= Constants.FREE_DELIVERY_THRESHOLD ? 0.0 : Constants.DELIVERY_FEE;
        double total = subtotal + deliveryFee;

        Order order = new Order(orderId, sessionManager.getUserId(), sessionManager.getSelectedOutletId(), cartItems, total, address, paymentMethod);
        order.setSubtotal(subtotal);
        order.setDeliveryFee(deliveryFee);
        order.setTransactionId(transactionId);
        order.setDeliveryLatitude(deliveryLatitude);
        order.setDeliveryLongitude(deliveryLongitude);
        order.setPrescriptionUrl(prescriptionUrl);

        databaseReference.child(Constants.NODE_ORDERS).child(orderId).setValue(order)
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        databaseHelper.clearCart();
                        Intent intent = new Intent(this, OrderTrackingActivity.class);
                        intent.putExtra(Constants.KEY_ORDER_ID, orderId);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, getString(R.string.error_order_failed), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnPlaceOrder.setEnabled(!show);
    }
}