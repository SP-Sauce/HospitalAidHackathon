package com.sh.hospitaldata.data;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.sh.hospitaldata.R;

public class ViewRecordsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PatientRecordAdapter adapter;
    private PatientViewModel patientViewModel;
    private TextView emptyStateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_records);

        // Initialize views
        recyclerView = findViewById(R.id.recycler_view_records);
        emptyStateText = findViewById(R.id.text_empty_state);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PatientRecordAdapter();
        recyclerView.setAdapter(adapter);

        // Initialize ViewModel
        patientViewModel = new ViewModelProvider(this).get(PatientViewModel.class);

        // Observe data changes
        patientViewModel.getAllRecords().observe(this, patientRecords -> {
            adapter.setRecords(patientRecords);

            // Show/hide empty state
            if (patientRecords == null || patientRecords.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyStateText.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyStateText.setVisibility(View.GONE);
            }
        });
    }
}