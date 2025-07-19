package com.sh.hospitaldata;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

        //Doctor login: Requires biometric authentication
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(MainActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(), "Doctor authenticated ", Toast.LENGTH_SHORT).show();
                    // Launch AddRecordActivity
                    Intent intent = new Intent(MainActivity.this, com.sh.hospitaldata.data.DoctorUserPage.class);
                    startActivity(intent);
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
                    Toast.makeText(getApplicationContext(), "Face not recognized ", Toast.LENGTH_SHORT).show()
                );
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Doctor Login")
                .setSubtitle("Use Face ID to continue")
                .setNegativeButtonText("Cancel")
                .build();

        doctorLoginBtn.setOnClickListener(v -> {
            BiometricManager biometricManager = BiometricManager.from(this);
            if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                    == BiometricManager.BIOMETRIC_SUCCESS) {
                biometricPrompt.authenticate(promptInfo);

            } else {
                Toast.makeText(this, "Biometric login not available on this device", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
