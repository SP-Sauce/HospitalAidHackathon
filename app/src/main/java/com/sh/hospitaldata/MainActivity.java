package com.sh.hospitaldata;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Button uploadOldRecordBtn, addNewRecordBtn, sendRecordBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uploadOldRecordBtn = findViewById(R.id.button_upload_old_record);
        addNewRecordBtn = findViewById(R.id.button_add_new_record);
        sendRecordBtn = findViewById(R.id.button_send_record);

        uploadOldRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Launch camera or OCR activity
            }
        });

        addNewRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Launch form to manually add record
            }
        });

        sendRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Launch Bluetooth send activity
            }
        });
    }
}
