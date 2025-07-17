package com.sh.hospitaldata;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.sh.hospitaldata.data.PatientRecord;
import com.sh.hospitaldata.data.PatientViewModel;

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
            patientViewModel.insert(patientRecord);

            Toast.makeText(this, "Record saved successfully", Toast.LENGTH_SHORT).show();
            finish();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid age", Toast.LENGTH_SHORT).show();
        }
    }
}