package com.sh.hospitaldata.data;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;

import com.sh.hospitaldata.R;

import java.util.ArrayList;
import java.util.Locale;

public class PatientDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PATIENT_ID = "patient_id";
    public static final String EXTRA_PATIENT_NAME = "patient_name";
    public static final String EXTRA_PATIENT_AGE = "patient_age";
    public static final String EXTRA_PATIENT_GENDER = "patient_gender";
    public static final String EXTRA_PATIENT_CONDITION = "patient_condition";
    public static final String EXTRA_PATIENT_ADMITTED = "patient_admitted";
    public static final String EXTRA_PATIENT_WARD = "patient_ward";
    public static final String EXTRA_PATIENT_ADMISSION_DATE = "patient_admission_date";
    public static final String EXTRA_PATIENT_PROGRESS_NOTES = "patient_progress_notes";

    private TextView textPatientId, textPatientName, textPatientAge, textPatientGender, textPatientCondition;
    private TextView textPatientStatus, textPatientWard, textAdmissionDate, textProgressNotes;
    private CardView cardAdmissionInfo;
    private Button buttonEditPatient, buttonDeletePatient, buttonBack, buttonAdmissionToggle, buttonUpdateNotes;
    private PatientViewModel patientViewModel;
    private PatientRecord currentPatient;

    // Dialog components for speech-to-text
    private EditText currentEditText;
    private boolean isAppendMode = true;
    private AlertDialog currentNotesDialog;

    // Activity result launchers
    private ActivityResultLauncher<Intent> speechRecognitionLauncher;
    private ActivityResultLauncher<Intent> editPatientLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_detail);

        initViews();
        initActivityResultLaunchers();
        patientViewModel = new ViewModelProvider(this).get(PatientViewModel.class);

        // Get patient data from intent
        getPatientDataFromIntent();
        setupClickListeners();
    }

    private void initActivityResultLaunchers() {
        // Speech recognition launcher
        speechRecognitionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        ArrayList<String> speechResult = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        if (speechResult != null && !speechResult.isEmpty()) {
                            String spokenText = speechResult.get(0);
                            handleSpeechResult(spokenText);
                        }
                    }
                }
        );

        // Edit patient launcher
        editPatientLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        handleEditPatientResult(result.getData());
                    }
                }
        );
    }

    private void initViews() {
        textPatientId = findViewById(R.id.text_patient_id);
        textPatientName = findViewById(R.id.text_patient_name);
        textPatientAge = findViewById(R.id.text_patient_age);
        textPatientGender = findViewById(R.id.text_patient_gender);
        textPatientCondition = findViewById(R.id.text_patient_condition);
        textPatientStatus = findViewById(R.id.text_patient_status);
        textPatientWard = findViewById(R.id.text_patient_ward);
        textAdmissionDate = findViewById(R.id.text_admission_date);
        textProgressNotes = findViewById(R.id.text_progress_notes);
        cardAdmissionInfo = findViewById(R.id.card_admission_info);
        buttonEditPatient = findViewById(R.id.button_edit_patient);
        buttonDeletePatient = findViewById(R.id.button_delete_patient);
        buttonBack = findViewById(R.id.button_back);
        buttonAdmissionToggle = findViewById(R.id.button_admission_toggle);
        buttonUpdateNotes = findViewById(R.id.button_update_notes);
    }

    private void getPatientDataFromIntent() {
        Intent intent = getIntent();

        int patientId = intent.getIntExtra(EXTRA_PATIENT_ID, -1);
        String patientName = intent.getStringExtra(EXTRA_PATIENT_NAME);
        int patientAge = intent.getIntExtra(EXTRA_PATIENT_AGE, 0);
        String patientGender = intent.getStringExtra(EXTRA_PATIENT_GENDER);
        String patientCondition = intent.getStringExtra(EXTRA_PATIENT_CONDITION);
        boolean isAdmitted = intent.getBooleanExtra(EXTRA_PATIENT_ADMITTED, false);
        String wardName = intent.getStringExtra(EXTRA_PATIENT_WARD);
        String admissionDate = intent.getStringExtra(EXTRA_PATIENT_ADMISSION_DATE);
        String progressNotes = intent.getStringExtra(EXTRA_PATIENT_PROGRESS_NOTES);

        if (patientId == -1 || patientName == null) {
            Toast.makeText(this, "Error loading patient data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Create patient record object for operations
        currentPatient = new PatientRecord(patientName, patientAge, patientGender, patientCondition,
                isAdmitted, wardName, admissionDate, progressNotes);
        currentPatient.setId(patientId);

        // Display patient data
        displayPatientData();
    }

    private void displayPatientData() {
        if (currentPatient != null) {
            // Basic information
            textPatientId.setText(String.valueOf(currentPatient.getId()));
            textPatientName.setText(currentPatient.getName());
            textPatientAge.setText(currentPatient.getAge() + " years");
            textPatientGender.setText(currentPatient.getGender());
            textPatientCondition.setText(currentPatient.getCondition());

            // Status and admission information
            updateStatusAndAdmissionDisplay();
        }
    }

    private void updateStatusAndAdmissionDisplay() {
        if (currentPatient.isAdmitted()) {
            // Show admitted status
            textPatientStatus.setText("Admitted - " + currentPatient.getWardName());
            textPatientStatus.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));

            // Show admission details
            cardAdmissionInfo.setVisibility(View.VISIBLE);
            textPatientWard.setText(currentPatient.getWardName());
            textAdmissionDate.setText(currentPatient.getAdmissionDate());
            textProgressNotes.setText(currentPatient.getProgressNotes().isEmpty() ?
                    "No progress notes yet." : currentPatient.getProgressNotes());

            buttonAdmissionToggle.setText("Discharge Patient");
            buttonAdmissionToggle.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light));
        } else {
            // Show outpatient status
            textPatientStatus.setText("Outpatient");
            textPatientStatus.setBackgroundColor(getResources().getColor(R.color.teal_200));

            // Hide admission details
            cardAdmissionInfo.setVisibility(View.GONE);

            buttonAdmissionToggle.setText("Admit to Hospital");
            buttonAdmissionToggle.setBackgroundColor(getResources().getColor(R.color.teal_200));
        }
    }

    private void setupClickListeners() {
        buttonEditPatient.setOnClickListener(v -> editPatient());
        buttonDeletePatient.setOnClickListener(v -> showDeleteConfirmationDialog());
        buttonBack.setOnClickListener(v -> finish());
        buttonAdmissionToggle.setOnClickListener(v -> toggleAdmissionStatus());
        buttonUpdateNotes.setOnClickListener(v -> showSpeechToTextDialog());
    }

    private void showSpeechToTextDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Progress Notes");

        // Create custom layout with speech-to-text options
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_speech_notes, null);
        EditText editNotes = dialogView.findViewById(R.id.edit_notes);
        ImageButton buttonMicrophone = dialogView.findViewById(R.id.button_microphone);
        Button buttonAppendMode = dialogView.findViewById(R.id.button_append_mode);
        Button buttonReplaceMode = dialogView.findViewById(R.id.button_replace_mode);

        // Store reference for speech result handling
        currentEditText = editNotes;

        // Set current notes
        editNotes.setText(currentPatient.getProgressNotes());

        // Mode selection
        buttonAppendMode.setOnClickListener(v -> {
            isAppendMode = true;
            buttonAppendMode.setBackgroundColor(getResources().getColor(R.color.purple_500));
            buttonAppendMode.setTextColor(getResources().getColor(android.R.color.white));
            buttonReplaceMode.setBackgroundColor(getResources().getColor(android.R.color.background_light));
            buttonReplaceMode.setTextColor(getResources().getColor(android.R.color.black));
        });

        buttonReplaceMode.setOnClickListener(v -> {
            isAppendMode = false;
            buttonReplaceMode.setBackgroundColor(getResources().getColor(R.color.purple_500));
            buttonReplaceMode.setTextColor(getResources().getColor(android.R.color.white));
            buttonAppendMode.setBackgroundColor(getResources().getColor(android.R.color.background_light));
            buttonAppendMode.setTextColor(getResources().getColor(android.R.color.black));
        });

        // Initialize append mode as selected
        buttonAppendMode.performClick();

        // Microphone button click
        buttonMicrophone.setOnClickListener(v -> startSpeechToText());

        builder.setView(dialogView);

        builder.setPositiveButton("Save Notes", (dialog, which) -> {
            String updatedNotes = editNotes.getText().toString().trim();
            saveProgressNotes(updatedNotes);
            currentNotesDialog = null;
            currentEditText = null;
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
            currentNotesDialog = null;
            currentEditText = null;
        });

        currentNotesDialog = builder.create();
        currentNotesDialog.show();
    }

    private void startSpeechToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your progress notes...");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

        try {
            speechRecognitionLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Speech recognition not available on this device", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleSpeechResult(String spokenText) {
        if (currentEditText != null) {
            // Add timestamp to speech input
            String timestamp = getCurrentDate() + " " + getCurrentTime() + ": ";
            String formattedNote = timestamp + spokenText;

            if (isAppendMode) {
                // Append mode: add to existing text
                String currentText = currentEditText.getText().toString();
                String updatedText;
                if (currentText.isEmpty()) {
                    updatedText = formattedNote;
                } else {
                    updatedText = currentText + "\n\n" + formattedNote;
                }
                currentEditText.setText(updatedText);
            } else {
                // Replace mode: replace all text
                currentEditText.setText(formattedNote);
            }

            // Show feedback
            Toast.makeText(this, "Speech added: " + spokenText, Toast.LENGTH_SHORT).show();
        }
    }

    private void saveProgressNotes(String notes) {
        currentPatient.setProgressNotes(notes);
        patientViewModel.update(currentPatient);
        textProgressNotes.setText(notes.isEmpty() ? "No progress notes yet." : notes);
        Toast.makeText(this, "Progress notes updated", Toast.LENGTH_SHORT).show();
    }

    private void editPatient() {
        // Launch AddRecordActivity in edit mode
        Intent intent = new Intent(this, AddRecordActivity.class);
        intent.putExtra("edit_mode", true);
        intent.putExtra(EXTRA_PATIENT_ID, currentPatient.getId());
        intent.putExtra(EXTRA_PATIENT_NAME, currentPatient.getName());
        intent.putExtra(EXTRA_PATIENT_AGE, currentPatient.getAge());
        intent.putExtra(EXTRA_PATIENT_GENDER, currentPatient.getGender());
        intent.putExtra(EXTRA_PATIENT_CONDITION, currentPatient.getCondition());
        intent.putExtra(EXTRA_PATIENT_ADMITTED, currentPatient.isAdmitted());
        intent.putExtra(EXTRA_PATIENT_WARD, currentPatient.getWardName());
        intent.putExtra(EXTRA_PATIENT_ADMISSION_DATE, currentPatient.getAdmissionDate());
        intent.putExtra(EXTRA_PATIENT_PROGRESS_NOTES, currentPatient.getProgressNotes());
        editPatientLauncher.launch(intent);
    }

    private void handleEditPatientResult(Intent data) {
        // Update current patient data from the result
        currentPatient.setName(data.getStringExtra(EXTRA_PATIENT_NAME));
        currentPatient.setAge(data.getIntExtra(EXTRA_PATIENT_AGE, 0));
        currentPatient.setGender(data.getStringExtra(EXTRA_PATIENT_GENDER));
        currentPatient.setCondition(data.getStringExtra(EXTRA_PATIENT_CONDITION));
        currentPatient.setAdmitted(data.getBooleanExtra(EXTRA_PATIENT_ADMITTED, false));
        currentPatient.setWardName(data.getStringExtra(EXTRA_PATIENT_WARD));
        currentPatient.setAdmissionDate(data.getStringExtra(EXTRA_PATIENT_ADMISSION_DATE));
        currentPatient.setProgressNotes(data.getStringExtra(EXTRA_PATIENT_PROGRESS_NOTES));

        // Refresh the display
        displayPatientData();
        Toast.makeText(this, "Patient record updated successfully", Toast.LENGTH_SHORT).show();
    }

    private void toggleAdmissionStatus() {
        if (currentPatient.isAdmitted()) {
            showDischargeConfirmationDialog();
        } else {
            showAdmitPatientDialog();
        }
    }

    private void showAdmitPatientDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Admit Patient to Hospital");

        // Create custom layout for admission
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_admit_patient, null);
        EditText editWard = dialogView.findViewById(R.id.edit_ward_name);
        EditText editAdmissionDate = dialogView.findViewById(R.id.edit_admission_date);

        // Set current date as default
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        String currentDate = String.format("%02d/%02d/%04d",
                calendar.get(java.util.Calendar.DAY_OF_MONTH),
                calendar.get(java.util.Calendar.MONTH) + 1,
                calendar.get(java.util.Calendar.YEAR));
        editAdmissionDate.setText(currentDate);

        builder.setView(dialogView);

        builder.setPositiveButton("Admit", (dialog, which) -> {
            String wardName = editWard.getText().toString().trim();
            String admissionDate = editAdmissionDate.getText().toString().trim();

            if (wardName.isEmpty()) {
                Toast.makeText(this, "Ward name is required", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update patient record
            currentPatient.setAdmitted(true);
            currentPatient.setWardName(wardName);
            currentPatient.setAdmissionDate(admissionDate);
            currentPatient.setProgressNotes("Patient admitted to " + wardName + " on " + admissionDate);

            // Save to database
            patientViewModel.update(currentPatient);

            // Update display
            updateStatusAndAdmissionDisplay();

            Toast.makeText(this, "Patient admitted successfully", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showDischargeConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Discharge Patient");
        builder.setMessage("Are you sure you want to discharge " + currentPatient.getName() +
                " from " + currentPatient.getWardName() + "?");

        builder.setPositiveButton("Discharge", (dialog, which) -> {
            // Update patient record
            currentPatient.setAdmitted(false);
            currentPatient.setWardName(null);
            currentPatient.setAdmissionDate(null);

            // Keep progress notes for historical record
            String dischargeNote = "\n\nPatient discharged on " + getCurrentDate();
            currentPatient.setProgressNotes(currentPatient.getProgressNotes() + dischargeNote);

            // Save to database
            patientViewModel.update(currentPatient);

            // Update display
            updateStatusAndAdmissionDisplay();

            Toast.makeText(this, "Patient discharged successfully", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Patient Record");

        String message = "Are you sure you want to permanently delete this patient record?\n\nPatient: " +
                currentPatient.getName();

        if (currentPatient.isAdmitted()) {
            message += "\n\nWARNING: This patient is currently admitted to " +
                    currentPatient.getWardName() + ". Please discharge first or ensure proper handover.";
        }

        message += "\n\nThis action cannot be undone.";

        builder.setMessage(message);

        builder.setPositiveButton("Delete", (dialog, which) -> deletePatient());
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.show();
    }

    private void deletePatient() {
        if (currentPatient != null) {
            patientViewModel.delete(currentPatient);
            Toast.makeText(this, "Patient record deleted successfully", Toast.LENGTH_SHORT).show();

            // Return to ViewRecordsActivity
            Intent intent = new Intent(this, ViewRecordsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }

    private String getCurrentDate() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        return String.format("%02d/%02d/%04d",
                calendar.get(java.util.Calendar.DAY_OF_MONTH),
                calendar.get(java.util.Calendar.MONTH) + 1,
                calendar.get(java.util.Calendar.YEAR));
    }

    private String getCurrentTime() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        return String.format("%02d:%02d",
                calendar.get(java.util.Calendar.HOUR_OF_DAY),
                calendar.get(java.util.Calendar.MINUTE));
    }
}