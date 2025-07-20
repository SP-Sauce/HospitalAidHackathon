package com.sh.hospitaldata.data;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.sh.hospitaldata.R;
import com.sh.hospitaldata.data.PatientDetailActivity;
import com.sh.hospitaldata.data.PatientRecord;
import com.sh.hospitaldata.data.PatientViewModel;
import java.util.Calendar;
import java.util.List;

public class AddRecordActivity extends AppCompatActivity {

    private TextView titleText;
    private EditText editTextName, editTextAge, editTextGender, editTextCondition;
    private CheckBox checkboxAdmitted;
    private LinearLayout layoutAdmissionDetails;
    private EditText editTextWard, editTextAdmissionDate, editTextProgressNotes;
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
        setupAdmissionToggle();
        setupDatePicker();
    }

    private void initViews() {
        titleText = findViewById(R.id.text_title);
        editTextName = findViewById(R.id.edit_text_name);
        editTextAge = findViewById(R.id.edit_text_age);
        editTextGender = findViewById(R.id.edit_text_gender);
        editTextCondition = findViewById(R.id.edit_text_condition);
        checkboxAdmitted = findViewById(R.id.checkbox_admitted);
        layoutAdmissionDetails = findViewById(R.id.layout_admission_details);
        editTextWard = findViewById(R.id.edit_text_ward);
        editTextAdmissionDate = findViewById(R.id.edit_text_admission_date);
        editTextProgressNotes = findViewById(R.id.edit_text_progress_notes);
        buttonSave = findViewById(R.id.button_save);
        buttonCancel = findViewById(R.id.button_cancel);
    }

    private void checkEditMode() {
        Intent intent = getIntent();
        isEditMode = intent.getBooleanExtra("edit_mode", false);

        if (isEditMode) {
            // Update title and button text for edit mode
            titleText.setText("Edit Patient Record");
            buttonSave.setText("Update Record");

            // Get patient data for editing
            editPatientId = intent.getIntExtra(PatientDetailActivity.EXTRA_PATIENT_ID, -1);
            String patientName = intent.getStringExtra(PatientDetailActivity.EXTRA_PATIENT_NAME);
            int patientAge = intent.getIntExtra(PatientDetailActivity.EXTRA_PATIENT_AGE, 0);
            String patientGender = intent.getStringExtra(PatientDetailActivity.EXTRA_PATIENT_GENDER);
            String patientCondition = intent.getStringExtra(PatientDetailActivity.EXTRA_PATIENT_CONDITION);

            // Get admission data
            boolean isAdmitted = intent.getBooleanExtra(PatientDetailActivity.EXTRA_PATIENT_ADMITTED, false);
            String wardName = intent.getStringExtra(PatientDetailActivity.EXTRA_PATIENT_WARD);
            String admissionDate = intent.getStringExtra(PatientDetailActivity.EXTRA_PATIENT_ADMISSION_DATE);
            String progressNotes = intent.getStringExtra(PatientDetailActivity.EXTRA_PATIENT_PROGRESS_NOTES);

            // Create original patient record for comparison
            originalPatient = new PatientRecord(patientName, patientAge, patientGender, patientCondition,
                    isAdmitted, wardName, admissionDate, progressNotes);
            originalPatient.setId(editPatientId);

            // Populate fields with existing data
            populateFieldsForEdit(patientName, patientAge, patientGender, patientCondition,
                    isAdmitted, wardName, admissionDate, progressNotes);
        } else {
            titleText.setText("Add Patient Record");
            buttonSave.setText("Save Record");
        }
    }

    private void populateFieldsForEdit(String name, int age, String gender, String condition,
                                       boolean isAdmitted, String wardName, String admissionDate, String progressNotes) {
        editTextName.setText(name);
        editTextAge.setText(String.valueOf(age));
        editTextGender.setText(gender);
        editTextCondition.setText(condition);

        checkboxAdmitted.setChecked(isAdmitted);
        if (isAdmitted) {
            layoutAdmissionDetails.setVisibility(View.VISIBLE);
            editTextWard.setText(wardName != null ? wardName : "");
            editTextAdmissionDate.setText(admissionDate != null ? admissionDate : "");
            editTextProgressNotes.setText(progressNotes != null ? progressNotes : "");
        }
    }

    private void setupAdmissionToggle() {
        checkboxAdmitted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                layoutAdmissionDetails.setVisibility(View.VISIBLE);
                // Set current date as default if admission date is empty
                if (editTextAdmissionDate.getText().toString().trim().isEmpty()) {
                    Calendar calendar = Calendar.getInstance();
                    String currentDate = String.format("%02d/%02d/%04d",
                            calendar.get(Calendar.DAY_OF_MONTH),
                            calendar.get(Calendar.MONTH) + 1,
                            calendar.get(Calendar.YEAR));
                    editTextAdmissionDate.setText(currentDate);
                }
            } else {
                layoutAdmissionDetails.setVisibility(View.GONE);
                // Clear admission fields when unchecked
                editTextWard.setText("");
                editTextAdmissionDate.setText("");
                editTextProgressNotes.setText("");
            }
        });
    }

    private void setupDatePicker() {
        editTextAdmissionDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            // Parse existing date if present
            String existingDate = editTextAdmissionDate.getText().toString();
            if (!existingDate.isEmpty()) {
                try {
                    String[] parts = existingDate.split("/");
                    if (parts.length == 3) {
                        calendar.set(Integer.parseInt(parts[2]),
                                Integer.parseInt(parts[1]) - 1,
                                Integer.parseInt(parts[0]));
                    }
                } catch (Exception e) {
                    // Use current date if parsing fails
                }
            }

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        String selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
                        editTextAdmissionDate.setText(selectedDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));

            datePickerDialog.show();
        });
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
        if (!validateBasicFields()) return;

        String name = editTextName.getText().toString().trim();
        int age = Integer.parseInt(editTextAge.getText().toString().trim());
        String gender = editTextGender.getText().toString().trim();
        String condition = editTextCondition.getText().toString().trim();

        PatientRecord patientRecord;

        if (checkboxAdmitted.isChecked()) {
            if (!validateAdmissionFields()) return;

            String wardName = editTextWard.getText().toString().trim();
            String admissionDate = editTextAdmissionDate.getText().toString().trim();
            String progressNotes = editTextProgressNotes.getText().toString().trim();

            patientRecord = new PatientRecord(name, age, gender, condition,
                    true, wardName, admissionDate, progressNotes);
        } else {
            patientRecord = new PatientRecord(name, age, gender, condition);
        }

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
    }

    private void updateRecord() {
        if (!validateBasicFields()) return;

        String name = editTextName.getText().toString().trim();
        int age = Integer.parseInt(editTextAge.getText().toString().trim());
        String gender = editTextGender.getText().toString().trim();
        String condition = editTextCondition.getText().toString().trim();

        PatientRecord updatedPatient;

        if (checkboxAdmitted.isChecked()) {
            if (!validateAdmissionFields()) return;

            String wardName = editTextWard.getText().toString().trim();
            String admissionDate = editTextAdmissionDate.getText().toString().trim();
            String progressNotes = editTextProgressNotes.getText().toString().trim();

            updatedPatient = new PatientRecord(name, age, gender, condition,
                    true, wardName, admissionDate, progressNotes);
        } else {
            updatedPatient = new PatientRecord(name, age, gender, condition);
        }

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
            resultIntent.putExtra(PatientDetailActivity.EXTRA_PATIENT_ADMITTED, updatedPatient.isAdmitted());
            resultIntent.putExtra(PatientDetailActivity.EXTRA_PATIENT_WARD, updatedPatient.getWardName());
            resultIntent.putExtra(PatientDetailActivity.EXTRA_PATIENT_ADMISSION_DATE, updatedPatient.getAdmissionDate());
            resultIntent.putExtra(PatientDetailActivity.EXTRA_PATIENT_PROGRESS_NOTES, updatedPatient.getProgressNotes());
            setResult(RESULT_OK, resultIntent);

            Toast.makeText(this, "Record updated successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "No changes detected", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private boolean validateBasicFields() {
        String name = editTextName.getText().toString().trim();
        String ageString = editTextAge.getText().toString().trim();
        String gender = editTextGender.getText().toString().trim();
        String condition = editTextCondition.getText().toString().trim();

        if (name.isEmpty() || ageString.isEmpty() || gender.isEmpty() || condition.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            int age = Integer.parseInt(ageString);
            if (age < 0 || age > 150) {
                Toast.makeText(this, "Please enter a valid age (0-150)", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid age", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean validateAdmissionFields() {
        String wardName = editTextWard.getText().toString().trim();
        String admissionDate = editTextAdmissionDate.getText().toString().trim();

        if (wardName.isEmpty()) {
            editTextWard.setError("Ward name is required for admitted patients");
            editTextWard.requestFocus();
            return false;
        }

        if (admissionDate.isEmpty()) {
            editTextAdmissionDate.setError("Admission date is required");
            editTextAdmissionDate.requestFocus();
            return false;
        }

        return true;
    }

    private boolean hasDataChanged(PatientRecord newPatient) {
        if (originalPatient == null) return true;

        return !originalPatient.getName().equals(newPatient.getName()) ||
                originalPatient.getAge() != newPatient.getAge() ||
                !originalPatient.getGender().equals(newPatient.getGender()) ||
                !originalPatient.getCondition().equals(newPatient.getCondition()) ||
                originalPatient.isAdmitted() != newPatient.isAdmitted() ||
                !safeEquals(originalPatient.getWardName(), newPatient.getWardName()) ||
                !safeEquals(originalPatient.getAdmissionDate(), newPatient.getAdmissionDate()) ||
                !safeEquals(originalPatient.getProgressNotes(), newPatient.getProgressNotes());
    }

    private boolean safeEquals(String str1, String str2) {
        if (str1 == null && str2 == null) return true;
        if (str1 == null || str2 == null) return false;
        return str1.equals(str2);
    }

    private void showDuplicateDialog(List<PatientRecord> duplicates, PatientRecord newRecord) {
        StringBuilder message = new StringBuilder();
        message.append("A patient with similar details already exists:\n\n");

        for (PatientRecord duplicate : duplicates) {
            message.append("• ").append(duplicate.getName())
                    .append(", Age: ").append(duplicate.getAge())
                    .append(", Gender: ").append(duplicate.getGender())
                    .append("\n  Status: ").append(duplicate.getPatientStatus())
                    .append("\n  Condition: ").append(duplicate.getCondition())
                    .append("\n\n");
        }

        message.append("Do you still want to add this new record?");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Duplicate Patient Found");
        builder.setMessage(message.toString());

        builder.setPositiveButton("Add Anyway", (dialog, which) -> {
            patientViewModel.insert(newRecord);
            Toast.makeText(AddRecordActivity.this, "Record saved successfully", Toast.LENGTH_SHORT).show();
            finish();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.setNeutralButton("Edit Record", (dialog, which) -> {
            dialog.dismiss();
            editTextName.requestFocus();
        });

        builder.setCancelable(false);
        builder.show();
    }
}