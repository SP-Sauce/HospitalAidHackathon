package com.sh.hospitaldata;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.sh.hospitaldata.data.PatientViewModel;

public class MainActivity extends AppCompatActivity {

    Button uploadOldRecordBtn, addNewRecordBtn, sendRecordBtn;
    private PatientViewModel patientViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uploadOldRecordBtn = findViewById(R.id.button_upload_old_record);
        addNewRecordBtn = findViewById(R.id.button_add_new_record);
        sendRecordBtn = findViewById(R.id.button_send_record);

        // Initialize ViewModel
        patientViewModel = new ViewModelProvider(this).get(PatientViewModel.class);

        uploadOldRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Launch camera or OCR activity
                Toast.makeText(MainActivity.this, "Upload feature coming soon", Toast.LENGTH_SHORT).show();
            }
        });

        addNewRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Launch AddRecordActivity
                Intent intent = new Intent(MainActivity.this, AddRecordActivity.class);
                startActivity(intent);
            }
        });

        sendRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Launch Bluetooth send activity
                Toast.makeText(MainActivity.this, "Send feature coming soon", Toast.LENGTH_SHORT).show();
            }
        });

        // Optional: Observe all records for debugging
        patientViewModel.getAllRecords().observe(this, patientRecords -> {
            // You can add logging here to see when records are added
            if (patientRecords != null) {
                // Log.d("MainActivity", "Total records: " + patientRecords.size());
            }
        });
    }
}