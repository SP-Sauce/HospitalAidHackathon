package com.sh.hospitaldata;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000; // 3 seconds

    private ImageView logoImage;
    private TextView appTitle;
    private TextView hospitalSubtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initViews();
        setupLogo();
        startAnimations();

        // Navigate to MainActivity after delay
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close splash activity
        }, SPLASH_DURATION);
    }

    private void initViews() {
        logoImage = findViewById(R.id.logo_image);
        appTitle = findViewById(R.id.app_title);
        hospitalSubtitle = findViewById(R.id.hospital_subtitle);
    }

    private void setupLogo() {
        // Try to use custom hospital logo, fallback to launcher icon if needed
        try {
            logoImage.setImageResource(R.drawable.hospital_logo);
        } catch (Exception e) {
            // Fallback to launcher icon if custom logo doesn't exist
            logoImage.setImageResource(R.mipmap.ic_launcher);
        }
    }

    private void startAnimations() {
        // Logo fade in animation (programmatic)
        AlphaAnimation logoFadeIn = new AlphaAnimation(0.0f, 1.0f);
        logoFadeIn.setDuration(1000);
        logoFadeIn.setStartOffset(200);
        logoImage.startAnimation(logoFadeIn);

        // Title slide up and fade in
        TranslateAnimation titleSlideUp = new TranslateAnimation(0, 0, 100, 0);
        titleSlideUp.setDuration(800);
        titleSlideUp.setStartOffset(700);

        AlphaAnimation titleFadeIn = new AlphaAnimation(0.0f, 1.0f);
        titleFadeIn.setDuration(800);
        titleFadeIn.setStartOffset(700);

        appTitle.startAnimation(titleSlideUp);
        appTitle.startAnimation(titleFadeIn);

        // Subtitle fade in
        AlphaAnimation subtitleFadeIn = new AlphaAnimation(0.0f, 1.0f);
        subtitleFadeIn.setDuration(800);
        subtitleFadeIn.setStartOffset(1200);
        hospitalSubtitle.startAnimation(subtitleFadeIn);

        // Set final alpha values after animations
        logoImage.postDelayed(() -> logoImage.setAlpha(1.0f), 1200);
        appTitle.postDelayed(() -> appTitle.setAlpha(1.0f), 1500);
        hospitalSubtitle.postDelayed(() -> hospitalSubtitle.setAlpha(1.0f), 2000);
    }

    @Override
    public void onBackPressed() {
        // Disable back button during splash
        // Do nothing
    }
}