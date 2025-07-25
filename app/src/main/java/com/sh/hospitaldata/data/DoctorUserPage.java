package com.sh.hospitaldata.data;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.sh.hospitaldata.R;
import com.sh.hospitaldata.bluetooth.RecordExportActivity;
import com.sh.hospitaldata.camera.FormScannerActivity;

public class DoctorUserPage extends AppCompatActivity {

    Button uploadOldRecordBtn, addNewRecordBtn, viewAllRecordsBtn, sendRecordBtn, importRecordBtn;
    private PatientViewModel patientViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_user_page);

        uploadOldRecordBtn = findViewById(R.id.button_upload_old_record);
        addNewRecordBtn = findViewById(R.id.button_add_new_record);
        viewAllRecordsBtn = findViewById(R.id.button_view_all_records);
        sendRecordBtn = findViewById(R.id.button_send_record);
        importRecordBtn = findViewById(R.id.button_import_record);

        // Initialize ViewModel
        patientViewModel = new ViewModelProvider(this).get(PatientViewModel.class);

        uploadOldRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Launch camera scanner activity
                Intent intent = new Intent(DoctorUserPage.this, FormScannerActivity.class);
                startActivity(intent);
            }
        });

        addNewRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Launch AddRecordActivity
                Intent intent = new Intent(DoctorUserPage.this, com.sh.hospitaldata.data.AddRecordActivity.class);
                startActivity(intent);
            }
        });

        viewAllRecordsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Launch ViewRecordsActivity
                Intent intent = new Intent(DoctorUserPage.this, ViewRecordsActivity.class);
                startActivity(intent);
            }
        });

        sendRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Launch Record Export Activity
                Intent intent = new Intent(DoctorUserPage.this, RecordExportActivity.class);
                startActivity(intent);
            }
        });

        // Optional: Observe all records for debugging
        patientViewModel.getAllRecords().observe(this, patientRecords -> {
            // You can add logging here to see when records are added
            if (patientRecords != null) {
                // Log.d("DoctorUserPage", "Total records: " + patientRecords.size());
            }
        });

        importRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Launch Record Import Activity
                Intent intent = new Intent(DoctorUserPage.this, com.sh.hospitaldata.bluetooth.RecordImportActivity.class);
                startActivity(intent);
            }
        });
    }
}