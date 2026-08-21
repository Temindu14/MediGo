package com.styro.medigo.activity;

import android.graphics.Color;
import android.os.AsyncTask;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.styro.medigo.R;
import com.styro.medigo.model.Order;
import com.styro.medigo.util.Constants;

public class OrderTrackingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "OrderTracking";

    // MediHealth Colombo outlet location
    private static final LatLng OUTLET_LOCATION = new LatLng(6.9271, 79.8612);

    // Your Google Maps API key (same one in AndroidManifest.xml)
    private static final String MAPS_API_KEY = "AIzaSyD5_tn-4AezSPK491p2sSQCvobUmU3B95Y";

    private TextView tvOrderId;
    private TextView tvOrderStatus;
    private TextView tvDeliveryAddress;
    private TextView tvPaymentMethod;
    private TextView tvTotalAmount;
    private TextView tvStatusDetail;
    private Button   btnCallOutlet;
    private View     progressBar;

    private GoogleMap         googleMap;
    private DatabaseReference databaseReference;

    private String             orderId;
    private ValueEventListener orderListener;
    private Order              currentOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_tracking);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.orderTrackingRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        orderId = getIntent().getStringExtra(Constants.KEY_ORDER_ID);

        initViews();
        setupToolbar();
        initFirebase();
        setupMap();
        listenToOrderUpdates();
    }

    private void initViews() {
        tvOrderId         = findViewById(R.id.tvOrderId);
        tvOrderStatus     = findViewById(R.id.tvOrderStatus);
        tvDeliveryAddress = findViewById(R.id.tvDeliveryAddress);
        tvPaymentMethod   = findViewById(R.id.tvPaymentMethod);
        tvTotalAmount     = findViewById(R.id.tvTotalAmount);
        tvStatusDetail    = findViewById(R.id.tvStatusDetail);
        btnCallOutlet     = findViewById(R.id.btnCallOutlet);
        progressBar       = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.title_order_tracking));
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initFirebase() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void listenToOrderUpdates() {
        progressBar.setVisibility(View.VISIBLE);

        orderListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);
                if (snapshot.exists()) {
                    Order order = snapshot.getValue(Order.class);
                    if (order != null) {
                        currentOrder = order;
                        updateUI(order);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(OrderTrackingActivity.this,
                        getString(R.string.error_generic), Toast.LENGTH_SHORT).show();
            }
        };

        databaseReference.child(Constants.NODE_ORDERS)
                .child(orderId)
                .addValueEventListener(orderListener);
    }

    private void updateUI(Order order) {
        String shortOrderId = order.getOrderId().substring(0, 8).toUpperCase();
        tvOrderId.setText(getString(R.string.label_order_id, shortOrderId));
        tvDeliveryAddress.setText(order.getDeliveryAddress());
        tvTotalAmount.setText(getString(R.string.price_format, order.getTotalAmount()));
        tvPaymentMethod.setText(formatPaymentMethod(order.getPaymentMethod()));
        updateStatusDisplay(order.getStatus());

        // Draw map once map is ready and we have coordinates
        if (googleMap != null) {
            drawMapMarkers(order);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MAP — draw outlet pin, delivery pin, and route between them
    // ─────────────────────────────────────────────────────────────────────────
    private void drawMapMarkers(Order order) {
        googleMap.clear();

        // Always show outlet pin
        googleMap.addMarker(new MarkerOptions()
                .position(OUTLET_LOCATION)
                .title("MediHealth Colombo Outlet")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        // If customer picked a location on map, show their pin and draw route
        if (order.getDeliveryLatitude() != 0 && order.getDeliveryLongitude() != 0) {
            LatLng deliveryLocation = new LatLng(
                    order.getDeliveryLatitude(),
                    order.getDeliveryLongitude());

            googleMap.addMarker(new MarkerOptions()
                    .position(deliveryLocation)
                    .title("Your Delivery Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

            // Zoom to show both pins
            LatLngBounds bounds = new LatLngBounds.Builder()
                    .include(OUTLET_LOCATION)
                    .include(deliveryLocation)
                    .build();
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));

            // Fetch and draw route
            fetchRoute(OUTLET_LOCATION, deliveryLocation);

        } else {
            // No delivery location — just show outlet
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(OUTLET_LOCATION, 14f));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FETCH ROUTE from Google Directions API
    // ─────────────────────────────────────────────────────────────────────────
    private void fetchRoute(LatLng origin, LatLng destination) {
        String url = "https://maps.googleapis.com/maps/api/directions/json?"
                + "origin=" + origin.latitude + "," + origin.longitude
                + "&destination=" + destination.latitude + "," + destination.longitude
                + "&mode=driving"
                + "&key=" + MAPS_API_KEY;

        new FetchRouteTask().execute(url);
    }

    private class FetchRouteTask extends AsyncTask<String, Void, List<LatLng>> {
        @Override
        protected List<LatLng> doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                return parseDirectionsResponse(sb.toString());
            } catch (Exception e) {
                Log.e(TAG, "Error fetching route", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<LatLng> points) {
            if (points != null && !points.isEmpty() && googleMap != null) {
                googleMap.addPolyline(new PolylineOptions()
                        .addAll(points)
                        .width(10f)
                        .color(Color.parseColor("#C0392B")) // MediGo red
                        .geodesic(true));
            }
        }
    }

    // Parse the encoded polyline from the Directions API response
    private List<LatLng> parseDirectionsResponse(String json) {
        List<LatLng> points = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray routes = jsonObject.getJSONArray("routes");
            if (routes.length() == 0) return points;

            JSONObject route    = routes.getJSONObject(0);
            JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
            String encodedPolyline = overviewPolyline.getString("points");

            points = decodePolyline(encodedPolyline);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing directions", e);
        }
        return points;
    }

    // Google's polyline encoding algorithm decoder
    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0; result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            poly.add(new LatLng(lat / 1E5, lng / 1E5));
        }
        return poly;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STATUS DISPLAY
    // ─────────────────────────────────────────────────────────────────────────
    private void updateStatusDisplay(String status) {
        String displayStatus;
        String detailMessage;
        int statusColor;

        switch (status) {
            case Order.STATUS_CONFIRMED:
                displayStatus = getString(R.string.status_confirmed);
                detailMessage = getString(R.string.status_detail_confirmed);
                statusColor   = getColor(R.color.blue_info);
                break;
            case Order.STATUS_PROCESSING:
                displayStatus = getString(R.string.status_processing);
                detailMessage = getString(R.string.status_detail_processing);
                statusColor   = getColor(R.color.orange_warning);
                break;
            case Order.STATUS_OUT_FOR_DELIVERY:
                displayStatus = getString(R.string.status_out_for_delivery);
                detailMessage = getString(R.string.status_detail_out_for_delivery);
                statusColor   = getColor(R.color.orange_warning);
                break;
            case Order.STATUS_DELIVERED:
                displayStatus = getString(R.string.status_delivered);
                detailMessage = getString(R.string.status_detail_delivered);
                statusColor   = getColor(R.color.green_success);
                break;
            case Order.STATUS_CANCELLED:
                displayStatus = getString(R.string.status_cancelled);
                detailMessage = getString(R.string.status_detail_cancelled);
                statusColor   = getColor(R.color.red_error);
                break;
            default:
                displayStatus = getString(R.string.status_pending);
                detailMessage = getString(R.string.status_detail_pending);
                statusColor   = getColor(R.color.medigo_primary);
                break;
        }

        tvOrderStatus.setText(displayStatus);
        tvOrderStatus.setTextColor(statusColor);
        tvStatusDetail.setText(detailMessage);
    }

    private String formatPaymentMethod(String method) {
        if (Constants.PAYMENT_CASH_ON_DELIVERY.equals(method)) {
            return getString(R.string.label_cash_on_delivery);
        }
        return getString(R.string.label_card_payment);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(false);

        // Show outlet by default
        googleMap.addMarker(new MarkerOptions()
                .position(OUTLET_LOCATION)
                .title("MediHealth Colombo Outlet"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(OUTLET_LOCATION, 14f));

        // If order data already loaded, draw markers now
        if (currentOrder != null) {
            drawMapMarkers(currentOrder);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (orderListener != null) {
            databaseReference.child(Constants.NODE_ORDERS)
                    .child(orderId)
                    .removeEventListener(orderListener);
        }
    }
}