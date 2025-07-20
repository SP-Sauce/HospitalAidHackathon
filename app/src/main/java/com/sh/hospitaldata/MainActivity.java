package com.sh.hospitaldata;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    private Button patientLoginBtn, doctorLoginBtn;

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    // Fallback credentials
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        patientLoginBtn = findViewById(R.id.button_patient_login);
        doctorLoginBtn = findViewById(R.id.button_doctor_login);

        //Patient login: No authentication
        patientLoginBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PatientUserPage.class);
            startActivity(intent);
        });

        //Doctor login: Requires biometric authentication or fallback
        setupBiometricAuthentication();

        doctorLoginBtn.setOnClickListener(v -> {
            BiometricManager biometricManager = BiometricManager.from(this);
            if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                    == BiometricManager.BIOMETRIC_SUCCESS) {
                // Biometric available - use it
                biometricPrompt.authenticate(promptInfo);
            } else {
                // Biometric not available - show username/password dialog
                showFallbackLoginDialog();
            }
        });
    }

    private void setupBiometricAuthentication() {
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(MainActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(), "Doctor authenticated", Toast.LENGTH_SHORT).show();
                    launchDoctorDashboard();
                });
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                runOnUiThread(() ->
                        Toast.makeText(getApplicationContext(), "Authentication error: " + errString, Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                runOnUiThread(() ->
                        Toast.makeText(getApplicationContext(), "Face not recognized", Toast.LENGTH_SHORT).show()
                );
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Doctor Login")
                .setSubtitle("Use Face ID to continue")
                .setNegativeButtonText("Cancel")
                .build();
    }
    private void showFallbackLoginDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Doctor Login");
        builder.setMessage("Biometric authentication not available. Please enter credentials:");
        // Create layout for username and password
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // Username field
        final EditText usernameInput = new EditText(this);
        usernameInput.setHint("Username");
        usernameInput.setInputType(InputType.TYPE_CLASS_TEXT);
        layout.addView(usernameInput);

        // Password field
        final EditText passwordInput = new EditText(this);
        passwordInput.setHint("Password");
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(passwordInput);

        builder.setView(layout);

        // Set up the buttons
        builder.setPositiveButton("Login", (dialog, which) -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (validateCredentials(username, password)) {
                Toast.makeText(MainActivity.this, "Doctor authenticated", Toast.LENGTH_SHORT).show();
                launchDoctorDashboard();
            } else {
                Toast.makeText(MainActivity.this, "Invalid credentials. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private boolean validateCredentials(String username, String password) {
        return ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password);
    }

    private void launchDoctorDashboard() {
        Intent intent = new Intent(MainActivity.this, com.sh.hospitaldata.data.DoctorUserPage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}