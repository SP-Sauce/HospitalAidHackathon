package com.sh.hospitaldata;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.sh.hospitaldata.data.PatientRecord;
import com.sh.hospitaldata.data.PatientViewModel;
import java.util.List;

public class AddRecordActivity extends AppCompatActivity {

    private EditText editTextName, editTextAge, editTextGender, editTextCondition;
    private Button buttonSave, buttonCancel;
    private PatientViewModel patientViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_record);

        initViews();

        patientViewModel = new ViewModelProvider(this).get(PatientViewModel.class);

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveRecord();
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initViews() {
        editTextName = findViewById(R.id.edit_text_name);
        editTextAge = findViewById(R.id.edit_text_age);
        editTextGender = findViewById(R.id.edit_text_gender);
        editTextCondition = findViewById(R.id.edit_text_condition);
        buttonSave = findViewById(R.id.button_save);
        buttonCancel = findViewById(R.id.button_cancel);
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
                    // Optionally, you could still save the record here
                    // patientViewModel.insert(patientRecord);
                }
            });

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid age", Toast.LENGTH_SHORT).show();
        }
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