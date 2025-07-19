package com.sh.hospitaldata.data;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.sh.hospitaldata.R;
import com.sh.hospitaldata.data.PatientDetailActivity;
import com.sh.hospitaldata.data.PatientRecord;
import com.sh.hospitaldata.data.PatientViewModel;
import java.util.List;

public class AddRecordActivity extends AppCompatActivity {

    private TextView titleText;
    private EditText editTextName, editTextAge, editTextGender, editTextCondition;
    private Button buttonSave, buttonCancel;
    private PatientViewModel patientViewModel;

    // Edit mode variables
    private boolean isEditMode = false;
    private int editPatientId = -1;
    private PatientRecord originalPatient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_record);

        initViews();
        patientViewModel = new ViewModelProvider(this).get(PatientViewModel.class);

        // Check if we're in edit mode
        checkEditMode();

        setupClickListeners();
    }

    private void initViews() {
        editTextName = findViewById(R.id.edit_text_name);
        editTextAge = findViewById(R.id.edit_text_age);
        editTextGender = findViewById(R.id.edit_text_gender);
        editTextCondition = findViewById(R.id.edit_text_condition);
        buttonSave = findViewById(R.id.button_save);
        buttonCancel = findViewById(R.id.button_cancel);
    }

    private void checkEditMode() {
        Intent intent = getIntent();
        isEditMode = intent.getBooleanExtra("edit_mode", false);

        if (isEditMode) {
            // Update title and button text for edit mode
            setTitle("Edit Patient Record");
            buttonSave.setText("Update Record");

            // Get patient data for editing
            editPatientId = intent.getIntExtra(PatientDetailActivity.EXTRA_PATIENT_ID, -1);
            String patientName = intent.getStringExtra(PatientDetailActivity.EXTRA_PATIENT_NAME);
            int patientAge = intent.getIntExtra(PatientDetailActivity.EXTRA_PATIENT_AGE, 0);
            String patientGender = intent.getStringExtra(PatientDetailActivity.EXTRA_PATIENT_GENDER);
            String patientCondition = intent.getStringExtra(PatientDetailActivity.EXTRA_PATIENT_CONDITION);

            // Create original patient record for comparison
            originalPatient = new PatientRecord(patientName, patientAge, patientGender, patientCondition);
            originalPatient.setId(editPatientId);

            // Populate fields with existing data
            populateFieldsForEdit(patientName, patientAge, patientGender, patientCondition);
        } else {
            setTitle("Add Patient Record");
            buttonSave.setText("Save Record");
        }
    }

    private void populateFieldsForEdit(String name, int age, String gender, String condition) {
        editTextName.setText(name);
        editTextAge.setText(String.valueOf(age));
        editTextGender.setText(gender);
        editTextCondition.setText(condition);
    }

    private void setupClickListeners() {
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEditMode) {
                    updateRecord();
                } else {
                    saveRecord();
                }
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void saveRecord() {
        String name = editTextName.getText().toString().trim();
        String ageString = editTextAge.getText().toString().trim();
        String gender = editTextGender.getText().toString().trim();
        String condition = editTextCondition.getText().toString().trim();

        if (name.isEmpty() || ageString.isEmpty() || gender.isEmpty() || condition.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int age = Integer.parseInt(ageString);

            PatientRecord patientRecord = new PatientRecord(name, age, gender, condition);

            // Check for duplicates before saving
            patientViewModel.insertWithDuplicateCheck(patientRecord, new PatientViewModel.DuplicateCheckCallback() {
                @Override
                public void onDuplicateFound(List<PatientRecord> duplicates) {
                    showDuplicateDialog(duplicates, patientRecord);
                }

                @Override
                public void onNoDuplicateFound() {
                    Toast.makeText(AddRecordActivity.this, "Record saved successfully", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(AddRecordActivity.this, "Error checking for duplicates: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid age", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateRecord() {
        String name = editTextName.getText().toString().trim();
        String ageString = editTextAge.getText().toString().trim();
        String gender = editTextGender.getText().toString().trim();
        String condition = editTextCondition.getText().toString().trim();

        if (name.isEmpty() || ageString.isEmpty() || gender.isEmpty() || condition.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int age = Integer.parseInt(ageString);

            // Create updated patient record
            PatientRecord updatedPatient = new PatientRecord(name, age, gender, condition);
            updatedPatient.setId(editPatientId);

            // Check if data actually changed
            if (hasDataChanged(updatedPatient)) {
                // Update the record
                patientViewModel.update(updatedPatient);

                // Return result to PatientDetailActivity
                Intent resultIntent = new Intent();
                resultIntent.putExtra(PatientDetailActivity.EXTRA_PATIENT_NAME, name);
                resultIntent.putExtra(PatientDetailActivity.EXTRA_PATIENT_AGE, age);
                resultIntent.putExtra(PatientDetailActivity.EXTRA_PATIENT_GENDER, gender);
                resultIntent.putExtra(PatientDetailActivity.EXTRA_PATIENT_CONDITION, condition);
                setResult(RESULT_OK, resultIntent);

                Toast.makeText(this, "Record updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "No changes detected", Toast.LENGTH_SHORT).show();
                finish();
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid age", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean hasDataChanged(PatientRecord newPatient) {
        if (originalPatient == null) return true;

        return !originalPatient.getName().equals(newPatient.getName()) ||
                originalPatient.getAge() != newPatient.getAge() ||
                !originalPatient.getGender().equals(newPatient.getGender()) ||
                !originalPatient.getCondition().equals(newPatient.getCondition());
    }

    private void showDuplicateDialog(List<PatientRecord> duplicates, PatientRecord newRecord) {
        StringBuilder message = new StringBuilder();
        message.append("A patient with similar details already exists:\n\n");

        for (PatientRecord duplicate : duplicates) {
            message.append("• ").append(duplicate.getName())
                    .append(", Age: ").append(duplicate.getAge())
                    .append(", Gender: ").append(duplicate.getGender())
                    .append("\n  Condition: ").append(duplicate.getCondition())
                    .append("\n\n");
        }

        message.append("Do you still want to add this new record?");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Duplicate Patient Found");
        builder.setMessage(message.toString());

        builder.setPositiveButton("Add Anyway", (dialog, which) -> {
            // User chooses to add the record despite duplicates
            patientViewModel.insert(newRecord);
            Toast.makeText(AddRecordActivity.this, "Record saved successfully", Toast.LENGTH_SHORT).show();
            finish();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            // User chooses not to add the record
            dialog.dismiss();
        });

        builder.setNeutralButton("Edit Record", (dialog, which) -> {
            // User wants to modify the current record
            dialog.dismiss();
            // Focus could be set to the first field for editing
            editTextName.requestFocus();
        });

        builder.setCancelable(false); // Prevent dismissing by touching outside
        builder.show();
    }
}