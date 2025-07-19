package com.sh.hospitaldata.sharing;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sh.hospitaldata.R;
import com.sh.hospitaldata.data.PatientRecord;
import com.sh.hospitaldata.data.PatientRecordAdapter;
import com.sh.hospitaldata.data.PatientViewModel;
import com.sh.hospitaldata.utils.EncryptionUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecordImportActivity extends AppCompatActivity {

    private EditText editPassword;
    private Button buttonSelectFile;
    private Button buttonImport;
    private Button buttonSaveRecords;
    private Button buttonCancel;
    private TextView textFileInfo;
    private TextView textImportStatus;
    private ProgressBar progressBar;
    private RecyclerView recyclerViewPreview;

    private PatientRecordAdapter previewAdapter;
    private PatientViewModel patientViewModel;
    private Uri selectedFileUri;
    private List<PatientRecord> importedRecords;

    private ActivityResultLauncher<Intent> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_import);

        initViews();
        setupFilePickerLauncher();
        setupRecyclerView();
        setupViewModel();
        setupClickListeners();

        // Check if we were launched with a file intent
        handleIncomingFile();
    }

    private void initViews() {
        editPassword = findViewById(R.id.edit_password);
        buttonSelectFile = findViewById(R.id.button_select_file);
        buttonImport = findViewById(R.id.button_import);
        buttonSaveRecords = findViewById(R.id.button_save_records);
        buttonCancel = findViewById(R.id.button_cancel);
        textFileInfo = findViewById(R.id.text_file_info);
        textImportStatus = findViewById(R.id.text_import_status);
        progressBar = findViewById(R.id.progress_bar);
        recyclerViewPreview = findViewById(R.id.recycler_view_preview);
    }

    private void setupFilePickerLauncher() {
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedFileUri = result.getData().getData();
                        if (selectedFileUri != null) {
                            updateFileInfo();
                            buttonImport.setEnabled(!editPassword.getText().toString().trim().isEmpty());
                        }
                    }
                }
        );
    }

    private void setupRecyclerView() {
        recyclerViewPreview.setLayoutManager(new LinearLayoutManager(this));
        previewAdapter = new PatientRecordAdapter();
        recyclerViewPreview.setAdapter(previewAdapter);
    }

    private void setupViewModel() {
        patientViewModel = new ViewModelProvider(this).get(PatientViewModel.class);
    }

    private void setupClickListeners() {
        buttonSelectFile.setOnClickListener(v -> selectImportFile());
        buttonImport.setOnClickListener(v -> importRecords());
        buttonSaveRecords.setOnClickListener(v -> saveImportedRecords());
        buttonCancel.setOnClickListener(v -> finish());

        // Enable import button when password is entered
        editPassword.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean hasFile = selectedFileUri != null;
                boolean hasPassword = s.toString().trim().length() > 0;
                buttonImport.setEnabled(hasFile && hasPassword);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void handleIncomingFile() {
        Intent intent = getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null) {
            selectedFileUri = intent.getData();
            updateFileInfo();
        }
    }

    private void selectImportFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"application/octet-stream", "*/*"});

        try {
            filePickerLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, "File picker not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateFileInfo() {
        if (selectedFileUri != null) {
            String fileName = getFileName(selectedFileUri);
            textFileInfo.setText("📁 Selected: " + fileName);
            textFileInfo.setVisibility(View.VISIBLE);
            buttonSelectFile.setText("File Selected ✓");
            buttonSelectFile.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }
    }

    private String getFileName(Uri uri) {
        String result = uri.getLastPathSegment();
        if (result == null) {
            result = "Unknown file";
        }
        return result;
    }

    private void importRecords() {
        String password = editPassword.getText().toString().trim();

        if (password.isEmpty()) {
            editPassword.setError("Password is required");
            return;
        }

        if (selectedFileUri == null) {
            Toast.makeText(this, "Please select a file to import", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        buttonImport.setEnabled(false);
        textImportStatus.setText("🔓 Decrypting file...");
        textImportStatus.setVisibility(View.VISIBLE);

        // Perform import in background thread
        new Thread(() -> {
            try {
                List<PatientRecord> records = performImport(password);

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    buttonImport.setEnabled(true);

                    if (records != null && !records.isEmpty()) {
                        showImportSuccess(records);
                    } else {
                        showImportError("Failed to decrypt file. Please check your password.");
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    buttonImport.setEnabled(true);
                    showImportError("Import error: " + e.getMessage());
                });
            }
        }).start();
    }

    private List<PatientRecord> performImport(String password) {
        try {
            // Read file content
            String fileContent = readFileContent(selectedFileUri);
            if (fileContent == null) {
                return null;
            }

            // Parse the outer JSON structure
            JSONObject fileJson = new JSONObject(fileContent);

            // Check if it's our format
            if (!fileJson.has("format") || !fileJson.getString("format").equals("HospitalData_Encrypted")) {
                throw new Exception("Invalid file format");
            }

            // Get encrypted data
            String encryptedData = fileJson.getString("data");

            // Decrypt the data
            String decryptedJson = EncryptionUtils.decryptWithPassword(encryptedData, password);
            if (decryptedJson == null) {
                return null; // Wrong password
            }

            // Parse decrypted data
            JSONObject exportData = new JSONObject(decryptedJson);
            JSONArray recordsArray = exportData.getJSONArray("records");

            // Convert to PatientRecord objects
            List<PatientRecord> records = new ArrayList<>();
            for (int i = 0; i < recordsArray.length(); i++) {
                JSONObject recordJson = recordsArray.getJSONObject(i);

                String name = recordJson.getString("name");
                int age = recordJson.getInt("age");
                String gender = recordJson.getString("gender");
                String condition = recordJson.getString("condition");

                PatientRecord record = new PatientRecord(name, age, gender, condition);
                records.add(record);
            }

            return records;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String readFileContent(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder content = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                content.append(line);
            }

            reader.close();
            inputStream.close();

            return content.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showImportSuccess(List<PatientRecord> records) {
        importedRecords = records;

        // Show preview
        previewAdapter.setRecords(records);
        recyclerViewPreview.setVisibility(View.VISIBLE);

        // Update status
        textImportStatus.setText("✅ Successfully imported " + records.size() + " patient records!");
        textImportStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));

        // Show save button
        buttonSaveRecords.setVisibility(View.VISIBLE);
        buttonSaveRecords.setText("💾 Save " + records.size() + " Records to Database");
    }

    private void showImportError(String error) {
        textImportStatus.setText("❌ " + error);
        textImportStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        textImportStatus.setVisibility(View.VISIBLE);

        // Hide preview and save button
        recyclerViewPreview.setVisibility(View.GONE);
        buttonSaveRecords.setVisibility(View.GONE);
    }

    private void saveImportedRecords() {
        if (importedRecords == null || importedRecords.isEmpty()) {
            Toast.makeText(this, "No records to save", Toast.LENGTH_SHORT).show();
            return;
        }

        buttonSaveRecords.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        // Save records in background
        new Thread(() -> {
            int savedCount = 0;
            for (PatientRecord record : importedRecords) {
                try {
                    patientViewModel.insert(record);
                    savedCount++;

                    // Small delay to allow database operations
                    Thread.sleep(50);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            final int finalSavedCount = savedCount;
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);

                if (finalSavedCount > 0) {
                    showSaveSuccess(finalSavedCount);
                } else {
                    Toast.makeText(this, "Failed to save records", Toast.LENGTH_SHORT).show();
                    buttonSaveRecords.setEnabled(true);
                }
            });
        }).start();
    }

    private void showSaveSuccess(int savedCount) {
        String message = "🎉 Successfully saved " + savedCount + " patient records to your database!\n\n" +
                "You can now view them in 'View All Records'.";

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Import Complete")
                .setMessage(message)
                .setPositiveButton("View Records", (dialog, which) -> {
                    // Go to view records activity
                    Intent intent = new Intent(this, com.sh.hospitaldata.data.ViewRecordsActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Done", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }
}