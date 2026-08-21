package com.styro.medigo.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import com.styro.medigo.R;
import com.styro.medigo.util.Constants;
import com.styro.medigo.util.SessionManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            }
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        setContentView(R.layout.activity_splash);

        ImageView logoImageView = findViewById(R.id.splashLogo);
        Glide.with(this)
                .asBitmap()
                .load(R.drawable.app_logo)
                .override(300)
                .into(logoImageView);

        new Handler(Looper.getMainLooper()).postDelayed(() -> 
                findViewById(R.id.splashProgressBar).setVisibility(View.VISIBLE), 1000);

        new Handler(Looper.getMainLooper()).postDelayed(this::navigateToNextScreen, 3000);
    }

    private void navigateToNextScreen() {
        SessionManager sessionManager = new SessionManager(this);
        Intent intent;

        if (sessionManager.isLoggedIn()) {
            String role = sessionManager.getUserRole();
            switch (role) {
                case Constants.ROLE_MAIN_ADMIN:
                    intent = new Intent(this, AdminMainActivity.class);
                    break;
                case Constants.ROLE_OUTLET_MANAGER:
                    intent = new Intent(this, AdminManagerActivity.class);
                    break;
                case Constants.ROLE_OUTLET_STAFF:
                    intent = new Intent(this, AdminStaffActivity.class);
                    break;
                default:
                    intent = new Intent(this, MainActivity.class);
                    break;
            }
        } else {
            intent = new Intent(this, LoginActivity.class);
        }

        startActivity(intent);
        finish();
    }
}