package com.styro.medigo.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import com.styro.medigo.R;
import com.styro.medigo.model.DiagnosticBooking;
import com.styro.medigo.util.Constants;
import com.styro.medigo.util.SessionManager;

public class DiagnosticBookingActivity extends AppCompatActivity {

    private TextView tvTestName, tvTestPrice;
    private EditText etAddress, etSpecialInstructions;
    private TextView tvSelectedDate, tvSelectedTime;
    private RadioGroup rgBookingMode;
    private Button btnSelectDate, btnSelectTime, btnConfirmBooking, btnPickOnMap;
    private View progressBar;

    private DatabaseReference databaseReference;
    private SessionManager sessionManager;

    private String selectedDate = "";
    private String selectedTime = "";
    private double selectedLat  = 0.0;
    private double selectedLng  = 0.0;



    // Launches MapPickerActivity and handles the result
    private final ActivityResultLauncher<Intent> mapPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    String address = data.getStringExtra(MapPickerActivity.RESULT_ADDRESS);
                    selectedLat    = data.getDoubleExtra(MapPickerActivity.RESULT_LATITUDE,  0.0);
                    selectedLng    = data.getDoubleExtra(MapPickerActivity.RESULT_LONGITUDE, 0.0);
                    if (address != null && !address.isEmpty()) {
                        etAddress.setText(address);
                        etAddress.setError(null);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnostic_booking);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.diagnosticRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseReference = FirebaseDatabase.getInstance().getReference();
        sessionManager    = new SessionManager(this);

        bindViews();
        setupToolbar();
        setClickListeners();
    }

    private void bindViews() {
        tvTestName            = findViewById(R.id.tvTestName);
        tvTestPrice           = findViewById(R.id.tvTestPrice);
        etAddress             = findViewById(R.id.etAddress);
        etSpecialInstructions = findViewById(R.id.etSpecialInstructions);
        tvSelectedDate        = findViewById(R.id.tvSelectedDate);
        tvSelectedTime        = findViewById(R.id.tvSelectedTime);
        rgBookingMode         = findViewById(R.id.rgBookingMode);
        btnSelectDate         = findViewById(R.id.btnSelectDate);
        btnSelectTime         = findViewById(R.id.btnSelectTime);
        btnConfirmBooking     = findViewById(R.id.btnConfirmBooking);
        btnPickOnMap          = findViewById(R.id.btnPickOnMap);
        progressBar           = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.title_book_diagnostic));
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setClickListeners() {
        btnPickOnMap.setOnClickListener(v ->
                mapPickerLauncher.launch(new Intent(this, MapPickerActivity.class)));

        btnSelectDate.setOnClickListener(v -> showDatePicker());
        btnSelectTime.setOnClickListener(v -> showTimePicker());
        btnConfirmBooking.setOnClickListener(v -> confirmBooking());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this,
                (view, year, month, day) -> {
                    selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
                    tvSelectedDate.setText(selectedDate);
                    tvSelectedDate.setVisibility(View.VISIBLE);
                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        {{
            getDatePicker().setMinDate(System.currentTimeMillis() + 86400000L);
        }}.show();
    }

    private void showTimePicker() {
        Calendar cal = Calendar.getInstance();
        new TimePickerDialog(this,
                (view, hour, minute) -> {
                    selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
                    tvSelectedTime.setText(selectedTime);
                    tvSelectedTime.setVisibility(View.VISIBLE);
                },
                cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true
        ).show();
    }

    private void confirmBooking() {
        String address             = etAddress.getText().toString().trim();
        String specialInstructions = etSpecialInstructions.getText().toString().trim();

        if (address.isEmpty()) {
            etAddress.setError(getString(R.string.error_address_required));
            etAddress.requestFocus();
            return;
        }
        if (selectedDate.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_date_required), Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedTime.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_time_required), Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        String mode      = rgBookingMode.getCheckedRadioButtonId() == R.id.rbHomeVisit
                ? DiagnosticBooking.MODE_HOME_VISIT : DiagnosticBooking.MODE_SELF_TEST_KIT;
        String bookingId = UUID.randomUUID().toString();
        String outletId  = sessionManager.getSelectedOutletId();

        DiagnosticBooking booking = new DiagnosticBooking(
                bookingId,
                sessionManager.getUserId(),
                outletId,
                getString(R.string.label_general_diagnostic),
                0.0,
                mode,
                address,
                selectedDate,
                selectedTime
        );
        booking.setSpecialInstructions(specialInstructions);

        // Save lat/lng if user picked from map
        if (selectedLat != 0.0 && selectedLng != 0.0) {
            booking.setLatitude(selectedLat);
            booking.setLongitude(selectedLng);
        }

        databaseReference.child(Constants.NODE_BOOKINGS)
                .child(bookingId)
                .setValue(booking)
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, getString(R.string.msg_booking_confirmed), Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(this, getString(R.string.error_booking_failed), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnConfirmBooking.setEnabled(!show);
    }
}