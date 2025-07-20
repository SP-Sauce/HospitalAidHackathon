package com.sh.hospitaldata.data;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.sh.hospitaldata.R;
import java.util.ArrayList;
import java.util.List;

public class PatientRecordAdapter extends RecyclerView.Adapter<PatientRecordAdapter.PatientViewHolder> {

    private List<PatientRecord> records = new ArrayList<>();

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_patient_record, parent, false);
        return new PatientViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        PatientRecord currentRecord = records.get(position);
        holder.bind(currentRecord);
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    public void setRecords(List<PatientRecord> records) {
        this.records = records;
        notifyDataSetChanged();
    }

    class PatientViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView textViewName;
        private TextView textViewAge;
        private TextView textViewGender;
        private TextView textViewCondition;
        private TextView textViewId;
        private TextView textViewStatus;
        private PatientRecord currentPatient;

        public PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.text_view_name);
            textViewAge = itemView.findViewById(R.id.text_view_age);
            textViewGender = itemView.findViewById(R.id.text_view_gender);
            textViewCondition = itemView.findViewById(R.id.text_view_condition);
            textViewId = itemView.findViewById(R.id.text_view_id);
            textViewStatus = itemView.findViewById(R.id.text_view_status);

            // Set click listener for the entire item
            itemView.setOnClickListener(this);

            // Add visual feedback for clicks
            itemView.setClickable(true);
            itemView.setFocusable(true);
        }

        public void bind(PatientRecord record) {
            currentPatient = record;
            textViewName.setText("Name: " + record.getName());
            textViewAge.setText("Age: " + record.getAge());
            textViewGender.setText("Gender: " + record.getGender());
            textViewCondition.setText("Condition: " + record.getCondition());
            textViewId.setText("ID: " + record.getId());

            // Display patient status (admitted or outpatient)
            if (record.isAdmitted()) {
                textViewStatus.setText("Status: Admitted - " + record.getWardName());
                textViewStatus.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
                textViewStatus.setVisibility(View.VISIBLE);
            } else {
                textViewStatus.setText("Status: Outpatient");
                textViewStatus.setTextColor(itemView.getContext().getResources().getColor(R.color.teal_700));
                textViewStatus.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onClick(View v) {
            if (currentPatient != null) {
                // Launch PatientDetailActivity with patient data
                Intent intent = new Intent(v.getContext(), PatientDetailActivity.class);
                intent.putExtra(PatientDetailActivity.EXTRA_PATIENT_ID, currentPatient.getId());
                intent.putExtra(PatientDetailActivity.EXTRA_PATIENT_NAME, currentPatient.getName());
                intent.putExtra(PatientDetailActivity.EXTRA_PATIENT_AGE, currentPatient.getAge());
                intent.putExtra(PatientDetailActivity.EXTRA_PATIENT_GENDER, currentPatient.getGender());
                intent.putExtra(PatientDetailActivity.EXTRA_PATIENT_CONDITION, currentPatient.getCondition());
                intent.putExtra(PatientDetailActivity.EXTRA_PATIENT_ADMITTED, currentPatient.isAdmitted());
                intent.putExtra(PatientDetailActivity.EXTRA_PATIENT_WARD, currentPatient.getWardName());
                intent.putExtra(PatientDetailActivity.EXTRA_PATIENT_ADMISSION_DATE, currentPatient.getAdmissionDate());
                intent.putExtra(PatientDetailActivity.EXTRA_PATIENT_PROGRESS_NOTES, currentPatient.getProgressNotes());
                v.getContext().startActivity(intent);
            }
        }
    }
}