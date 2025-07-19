package com.sh.hospitaldata.data;

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

    class PatientViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewName;
        private TextView textViewAge;
        private TextView textViewGender;
        private TextView textViewCondition;
        private TextView textViewId;

        public PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.text_view_name);
            textViewAge = itemView.findViewById(R.id.text_view_age);
            textViewGender = itemView.findViewById(R.id.text_view_gender);
            textViewCondition = itemView.findViewById(R.id.text_view_condition);
            textViewId = itemView.findViewById(R.id.text_view_id);
        }

        public void bind(PatientRecord record) {
            textViewName.setText("Name: " + record.getName());
            textViewAge.setText("Age: " + record.getAge());
            textViewGender.setText("Gender: " + record.getGender());
            textViewCondition.setText("Condition: " + record.getCondition());
            textViewId.setText("ID: " + record.getId());
        }
    }
}