package com.sh.hospitaldata.bluetooth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.sh.hospitaldata.R;
import com.sh.hospitaldata.data.PatientRecord;
import com.sh.hospitaldata.data.PatientViewModel;
import com.sh.hospitaldata.utils.EncryptionUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecordExportActivity extends AppCompatActivity {

    private PatientViewModel patientViewModel;
    private TextView textSelectedRecords;
    private EditText editPassword;
    private CheckBox checkGeneratePassword;
    private Button buttonSelectFile;
    private Button buttonExport;
    private Button buttonCancel;
    private ProgressBar progressBar;

    private List<PatientRecord> selectedRecords;
    private Uri exportFileUri;

    private ActivityResultLauncher<Intent> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_export);

        initViews();
        setupFilePickerLauncher();
        setupViewModel();
        setupClickListeners();
        loadSelectedRecords();

        // Set default: generate password automatically
        checkGeneratePassword.setChecked(true);
        generateNewPassword();
    }

    private void initViews() {
        textSelectedRecords = findViewById(R.id.text_selected_records);
        editPassword = findViewById(R.id.edit_password);
        checkGeneratePassword = findViewById(R.id.check_generate_password);
        buttonSelectFile = findViewById(R.id.button_select_file);
        buttonExport = findViewById(R.id.button_export);
        buttonCancel = findViewById(R.id.button_cancel);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupFilePickerLauncher() {
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        exportFileUri = result.getData().getData();
                        if (exportFileUri != null) {
                            buttonSelectFile.setText("File Selected ✓");
                            buttonSelectFile.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                            updateExportButtonState();
                        }
                    }
                }
        );
    }

    private void setupViewModel() {
        patientViewModel = new ViewModelProvider(this).get(PatientViewModel.class);
    }

    private void setupClickListeners() {
        checkGeneratePassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                generateNewPassword();
                editPassword.setEnabled(false);
            } else {
                editPassword.setEnabled(true);
                editPassword.setText("");
            }
        });

        buttonSelectFile.setOnClickListener(v -> selectExportFile());
        buttonExport.setOnClickListener(v -> exportRecords());
        buttonCancel.setOnClickListener(v -> finish());
    }

    private void generateNewPassword() {
        String randomPassword = EncryptionUtils.generateRandomPassword(12);
        editPassword.setText(randomPassword);
    }

    private void loadSelectedRecords() {
        patientViewModel.getAllRecords().observe(this, records -> {
            selectedRecords = records;
            if (records != null) {
                textSelectedRecords.setText("Selected Records: " + records.size());
                updateExportButtonState();
            } else {
                textSelectedRecords.setText("Selected Records: 0");
            }
        });
    }

    private void updateExportButtonState() {
        boolean hasRecords = selectedRecords != null && !selectedRecords.isEmpty();
        boolean hasFile = exportFileUri != null;
        boolean hasPassword = !editPassword.getText().toString().trim().isEmpty();

        buttonExport.setEnabled(hasRecords && hasFile && hasPassword);
    }

    private void selectExportFile() {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        String fileName = "patient_records_" + timestamp + ".hdata";

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/octet-stream");
        intent.putExtra(Intent.EXTRA_TITLE, fileName);

        try {
            filePickerLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, "File picker not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void exportRecords() {
        String password = editPassword.getText().toString().trim();

        if (password.isEmpty()) {
            editPassword.setError("Password is required");
            return;
        }

        if (password.length() < 6) {
            editPassword.setError("Password must be at least 6 characters");
            return;
        }

        if (selectedRecords == null || selectedRecords.isEmpty()) {
            Toast.makeText(this, "No records to export", Toast.LENGTH_SHORT).show();
            return;
        }

        if (exportFileUri == null) {
            Toast.makeText(this, "Please select an export file", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress and disable UI
        progressBar.setVisibility(View.VISIBLE);
        buttonExport.setEnabled(false);
        buttonSelectFile.setEnabled(false);

        // Perform export in background thread
        new Thread(() -> {
            try {
                boolean success = performExport(password);

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    buttonExport.setEnabled(true);
                    buttonSelectFile.setEnabled(true);

                    if (success) {
                        showExportSuccess(password);
                    } else {
                        Toast.makeText(this, "Export failed. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    buttonExport.setEnabled(true);
                    buttonSelectFile.setEnabled(true);
                    Toast.makeText(this, "Export error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private boolean performExport(String password) {
        try {
            // Create export metadata
            JSONObject exportData = new JSONObject();
            exportData.put("version", "1.0");
            exportData.put("app", "HospitalData");
            exportData.put("exported_at", System.currentTimeMillis());
            exportData.put("exported_by", "Doctor");
            exportData.put("record_count", selectedRecords.size());

            // Convert records to JSON array
            JSONArray recordsArray = new JSONArray();
            for (PatientRecord record : selectedRecords) {
                JSONObject recordJson = new JSONObject();
                recordJson.put("id", record.getId());
                recordJson.put("name", record.getName());
                recordJson.put("age", record.getAge());
                recordJson.put("gender", record.getGender());
                recordJson.put("condition", record.getCondition());
                recordsArray.put(recordJson);
            }
            exportData.put("records", recordsArray);

            // Convert to JSON string
            String jsonString = exportData.toString(2); // Pretty print with 2 spaces

            // Encrypt the data
            String encryptedData = EncryptionUtils.encryptWithPassword(jsonString, password);

            if (encryptedData == null) {
                return false;
            }

            // Create final export format
            JSONObject finalExport = new JSONObject();
            finalExport.put("format", "HospitalData_Encrypted");
            finalExport.put("version", "1.0");
            finalExport.put("created_at", System.currentTimeMillis());
            finalExport.put("data", encryptedData);

            // Write to file
            try (OutputStream outputStream = getContentResolver().openOutputStream(exportFileUri)) {
                if (outputStream != null) {
                    outputStream.write(finalExport.toString().getBytes());
                    outputStream.flush();
                    return true;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private void showExportSuccess(String password) {
        String message = "✅ Records exported successfully!\n\n" +
                "📁 " + selectedRecords.size() + " patient records encrypted\n" +
                "🔑 Password: " + password + "\n\n" +
                "⚠️ IMPORTANT: Share this password separately from the file for maximum security.";

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Export Complete")
                .setMessage(message)
                .setPositiveButton("Share File", (dialog, which) -> shareExportedFile())
                .setNegativeButton("Done", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void shareExportedFile() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/octet-stream");
        shareIntent.putExtra(Intent.EXTRA_STREAM, exportFileUri);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Encrypted Patient Records");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                "📋 Encrypted patient records file from HospitalData app.\n\n" +
                        "🔐 This file is password protected. The password will be shared separately for security.\n\n" +
                        "To open: Use a compatible medical records app with the provided password.");

        try {
            Intent chooser = Intent.createChooser(shareIntent, "Share encrypted records file");
            startActivity(chooser);
        } catch (Exception e) {
            Toast.makeText(this, "No apps available to share file", Toast.LENGTH_SHORT).show();
        }
    }
}