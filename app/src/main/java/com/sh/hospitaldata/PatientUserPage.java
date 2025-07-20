package com.sh.hospitaldata;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.sh.hospitaldata.data.PatientRecord;
import com.sh.hospitaldata.data.PatientViewModel;

public class PatientUserPage extends AppCompatActivity {

    // Login views
    private EditText editPatientName, editPatientId;
    private Button buttonLogin;
    private LinearLayout layoutLogin;

    // Patient info views
    private LinearLayout layoutPatientInfo;
    private TextView textWelcomeMessage;
    private TextView textPatientId, textPatientName, textPatientAge, textPatientGender;
    private TextView textPatientCondition, textPatientStatus;
    private LinearLayout cardAdmissionInfo;
    private TextView textWardName, textAdmissionDate, textProgressNotes;
    private Button buttonLogout;

    private PatientViewModel patientViewModel;
    private PatientRecord currentPatient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_user_page);

        // Initialize ViewModel first
        patientViewModel = new ViewModelProvider(this).get(PatientViewModel.class);

        // Safe view initialization with null checks
        if (initViews()) {
            setupClickListeners();
            showLoginScreen();
        } else {
            // Fallback: show simple dialog-based interface
            showSimplePatientLogin();
        }
    }

    private boolean initViews() {
        try {
            // Login views
            editPatientName = findViewById(R.id.edit_patient_name);
            editPatientId = findViewById(R.id.edit_patient_id);
            buttonLogin = findViewById(R.id.button_login);
            layoutLogin = findViewById(R.id.layout_login);

            // Patient info views
            layoutPatientInfo = findViewById(R.id.layout_patient_info);
            textWelcomeMessage = findViewById(R.id.text_welcome_message);
            textPatientId = findViewById(R.id.text_patient_id);
            textPatientName = findViewById(R.id.text_patient_name);
            textPatientAge = findViewById(R.id.text_patient_age);
            textPatientGender = findViewById(R.id.text_patient_gender);
            textPatientCondition = findViewById(R.id.text_patient_condition);
            textPatientStatus = findViewById(R.id.text_patient_status);
            cardAdmissionInfo = findViewById(R.id.card_admission_info);
            textWardName = findViewById(R.id.text_ward_name);
            textAdmissionDate = findViewById(R.id.text_admission_date);
            textProgressNotes = findViewById(R.id.text_progress_notes);
            buttonLogout = findViewById(R.id.button_logout);

            // Check if critical views exist
            return (editPatientName != null && editPatientId != null && buttonLogin != null &&
                    layoutLogin != null && layoutPatientInfo != null);

        } catch (Exception e) {
            return false;
        }
    }

    private void setupClickListeners() {
        if (buttonLogin != null) {
            buttonLogin.setOnClickListener(v -> authenticatePatient());
        }
        if (buttonLogout != null) {
            buttonLogout.setOnClickListener(v -> logout());
        }
    }

    private void showLoginScreen() {
        if (layoutLogin != null && layoutPatientInfo != null) {
            layoutLogin.setVisibility(View.VISIBLE);
            layoutPatientInfo.setVisibility(View.GONE);
            currentPatient = null;

            // Clear previous input
            if (editPatientName != null) editPatientName.setText("");
            if (editPatientId != null) editPatientId.setText("");
        }
    }

    private void showSimplePatientLogin() {
        // Fallback dialog-based interface if layout views don't exist
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Patient Portal Login");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText nameInput = new EditText(this);
        nameInput.setHint("Enter your full name");
        layout.addView(nameInput);

        final EditText idInput = new EditText(this);
        idInput.setHint("Enter your patient ID");
        idInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(idInput);

        builder.setView(layout);

        builder.setPositiveButton("Login", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String idStr = idInput.getText().toString().trim();

            if (name.isEmpty() || idStr.isEmpty()) {
                Toast.makeText(this, "Please enter both name and ID", Toast.LENGTH_SHORT).show();
                showSimplePatientLogin(); // Show again
                return;
            }

            try {
                int id = Integer.parseInt(idStr);
                searchForPatientSimple(name, id);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid ID number", Toast.LENGTH_SHORT).show();
                showSimplePatientLogin(); // Show again
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> finish());
        builder.setCancelable(false);
        builder.show();
    }

    private void authenticatePatient() {
        if (editPatientName == null || editPatientId == null) return;

        String enteredName = editPatientName.getText().toString().trim();
        String enteredIdStr = editPatientId.getText().toString().trim();

        // Validate input
        if (enteredName.isEmpty()) {
            editPatientName.setError("Please enter your name");
            editPatientName.requestFocus();
            return;
        }

        if (enteredIdStr.isEmpty()) {
            editPatientId.setError("Please enter your patient ID");
            editPatientId.requestFocus();
            return;
        }

        int enteredId;
        try {
            enteredId = Integer.parseInt(enteredIdStr);
        } catch (NumberFormatException e) {
            editPatientId.setError("Please enter a valid ID number");
            editPatientId.requestFocus();
            return;
        }

        // Search for patient in database
        if (buttonLogin != null) {
            buttonLogin.setEnabled(false);
            buttonLogin.setText("Searching...");
        }

        searchForPatient(enteredName, enteredId);
    }

    private void searchForPatient(String name, int id) {
        patientViewModel.getAllRecords().observe(this, patientRecords -> {
            boolean patientFound = false;

            if (patientRecords != null) {
                for (PatientRecord record : patientRecords) {
                    if (record.getId() == id && record.getName().equalsIgnoreCase(name)) {
                        currentPatient = record;
                        patientFound = true;
                        break;
                    }
                }
            }

            if (buttonLogin != null) {
                buttonLogin.setEnabled(true);
                buttonLogin.setText("🔐 Access My Records");
            }

            if (patientFound) {
                showPatientInfo();
            } else {
                showLoginError();
            }

            patientViewModel.getAllRecords().removeObservers(this);
        });
    }

    private void searchForPatientSimple(String name, int id) {
        patientViewModel.getAllRecords().observe(this, patientRecords -> {
            PatientRecord foundPatient = null;

            if (patientRecords != null) {
                for (PatientRecord record : patientRecords) {
                    if (record.getId() == id && record.getName().equalsIgnoreCase(name)) {
                        foundPatient = record;
                        break;
                    }
                }
            }

            if (foundPatient != null) {
                showPatientInfoSimple(foundPatient);
            } else {
                Toast.makeText(this, "Patient not found. Please check your name and ID.", Toast.LENGTH_LONG).show();
                showSimplePatientLogin();
            }

            patientViewModel.getAllRecords().removeObservers(this);
        });
    }

    private void showLoginError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Patient Not Found");
        builder.setMessage("We couldn't find a patient with the name and ID you entered.\n\n" +
                "Please check:\n" +
                "• Your name is spelled correctly\n" +
                "• Your patient ID number is correct\n" +
                "• You have an existing record in our system");
        builder.setPositiveButton("Try Again", (dialog, which) -> {
            dialog.dismiss();
            if (editPatientName != null) editPatientName.requestFocus();
        });
        builder.show();
    }

    private void showPatientInfo() {
        if (currentPatient == null || layoutLogin == null || layoutPatientInfo == null) return;

        // Switch to patient info view
        layoutLogin.setVisibility(View.GONE);
        layoutPatientInfo.setVisibility(View.VISIBLE);

        // Display welcome message
        if (textWelcomeMessage != null) {
            textWelcomeMessage.setText("Welcome, " + currentPatient.getName() + "!");
        }

        // Display basic patient information (just the data, no "Name:" labels)
        if (textPatientId != null) textPatientId.setText(String.valueOf(currentPatient.getId()));
        if (textPatientName != null) textPatientName.setText(currentPatient.getName());
        if (textPatientAge != null) textPatientAge.setText(String.valueOf(currentPatient.getAge()));
        if (textPatientGender != null) textPatientGender.setText(currentPatient.getGender());
        if (textPatientCondition != null) textPatientCondition.setText(currentPatient.getCondition());

        // Display admission status
        if (textPatientStatus != null) {
            if (currentPatient.isAdmitted()) {
                textPatientStatus.setText("Currently Admitted");
                textPatientStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));

                // Show admission details if card exists
                if (cardAdmissionInfo != null) {
                    cardAdmissionInfo.setVisibility(View.VISIBLE);
                    if (textWardName != null) textWardName.setText("Ward: " + currentPatient.getWardName());
                    if (textAdmissionDate != null) textAdmissionDate.setText("Admitted: " + currentPatient.getAdmissionDate());

                    if (textProgressNotes != null) {
                        String progressNotes = currentPatient.getProgressNotes();
                        if (progressNotes.isEmpty()) {
                            textProgressNotes.setText("No progress notes available yet.");
                        } else {
                            textProgressNotes.setText(progressNotes);
                        }
                    }
                }
            } else {
                textPatientStatus.setText("Outpatient");
                textPatientStatus.setTextColor(getResources().getColor(R.color.teal_700));
                if (cardAdmissionInfo != null) {
                    cardAdmissionInfo.setVisibility(View.GONE);
                }
            }
        }
    }

    private void showPatientInfoSimple(PatientRecord patient) {
        StringBuilder info = new StringBuilder();
        info.append("📋 Hospital Data\n\n");
        info.append("ID: ").append(patient.getId()).append("\n");
        info.append("Name: ").append(patient.getName()).append("\n");
        info.append("Age: ").append(patient.getAge()).append("\n");
        info.append("Gender: ").append(patient.getGender()).append("\n");
        info.append("Condition: ").append(patient.getCondition()).append("\n\n");

        if (patient.isAdmitted()) {
            info.append("Status: Currently Admitted\n");
            info.append("Ward: ").append(patient.getWardName()).append("\n");
            info.append("Admission Date: ").append(patient.getAdmissionDate()).append("\n");

            if (!patient.getProgressNotes().isEmpty()) {
                info.append("\nProgress Notes:\n").append(patient.getProgressNotes());
            }
        } else {
            info.append("Status: Outpatient");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Your Medical Record");
        builder.setMessage(info.toString());

        builder.setPositiveButton("Logout", (dialog, which) -> showSimplePatientLogin());
        builder.show();
    }

    private void logout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");

        builder.setPositiveButton("Logout", (dialog, which) -> {
            if (layoutLogin != null && layoutPatientInfo != null) {
                showLoginScreen();
            } else {
                showSimplePatientLogin();
            }
            Toast.makeText(this, "You have been logged out", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @Override
    public void onBackPressed() {
        if (layoutPatientInfo != null && layoutPatientInfo.getVisibility() == View.VISIBLE) {
            showLoginScreen();
        } else {
            super.onBackPressed();
        }
    }
}