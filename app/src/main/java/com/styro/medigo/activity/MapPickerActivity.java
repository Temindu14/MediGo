package com.styro.medigo.activity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.styro.medigo.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String RESULT_ADDRESS = "result_address";
    public static final String RESULT_LATITUDE = "result_latitude";
    public static final String RESULT_LONGITUDE = "result_longitude";

    private GoogleMap mMap;
    private TextView tvSelectedAddress;
    private Button btnConfirmLocation;
    private View pinOverlay;

    private LatLng selectedLatLng;
    private String selectedAddress = "";

    private static final LatLng DEFAULT_LOCATION = new LatLng(6.9271, 80.0000);
    private static final float DEFAULT_ZOOM = 15f;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_picker);

        tvSelectedAddress = findViewById(R.id.tvSelectedAddress);
        btnConfirmLocation = findViewById(R.id.btnConfirmLocation);
        pinOverlay = findViewById(R.id.ivMapPin);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapPicker);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        btnConfirmLocation.setOnClickListener(v -> confirmLocation());
        findViewById(R.id.btnMapBack).setOnClickListener(v -> finish());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, DEFAULT_ZOOM));
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        mMap.setOnCameraIdleListener(() -> {
            selectedLatLng = mMap.getCameraPosition().target;
            reverseGeocode(selectedLatLng);
        });

        mMap.setOnCameraMoveStartedListener(reason -> tvSelectedAddress.setText("Locating..."));

        selectedLatLng = DEFAULT_LOCATION;
        reverseGeocode(DEFAULT_LOCATION);
    }

    private void reverseGeocode(LatLng latLng) {
        executor.execute(() -> {
            String address = getAddressFromLatLng(latLng);
            handler.post(() -> {
                selectedAddress = address;
                tvSelectedAddress.setText(address.isEmpty() ? "Drag map to select your location" : address);
                btnConfirmLocation.setEnabled(!address.isEmpty());
            });
        });
    }

    private String getAddressFromLatLng(LatLng latLng) {
        if (!Geocoder.isPresent()) return "";
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address addr = addresses.get(0);
                StringBuilder sb = new StringBuilder();
                if (addr.getSubThoroughfare() != null) sb.append(addr.getSubThoroughfare()).append(" ");
                if (addr.getThoroughfare() != null) sb.append(addr.getThoroughfare()).append(", ");
                if (addr.getSubLocality() != null) sb.append(addr.getSubLocality()).append(", ");
                if (addr.getLocality() != null) sb.append(addr.getLocality());
                return sb.toString().trim().replaceAll(", $", "");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void confirmLocation() {
        if (selectedLatLng == null || selectedAddress.isEmpty()) {
            Toast.makeText(this, "Please drag the map to select your location", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent result = new Intent();
        result.putExtra(RESULT_ADDRESS, selectedAddress);
        result.putExtra(RESULT_LATITUDE, selectedLatLng.latitude);
        result.putExtra(RESULT_LONGITUDE, selectedLatLng.longitude);
        setResult(RESULT_OK, result);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}