package com.sh.hospitaldata.camera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.sh.hospitaldata.R;
import com.sh.hospitaldata.data.PatientRecord;
import com.sh.hospitaldata.data.PatientViewModel;

import androidx.lifecycle.ViewModelProvider;

import java.io.File;
import java.io.IOException;

public class ScanResultActivity extends AppCompatActivity {

    private static final String TAG = "ScanResultActivity";

    private ImageView imageCaptured;
    private TextView textExtracted;
    private TextView textRawLabel;
    private ScrollView scrollRawText;
    private EditText editExtractedName;
    private EditText editExtractedAge;
    private EditText editExtractedGender;
    private EditText editExtractedCondition;
    private Button buttonSaveRecord;
    private Button buttonRetry;

    private String imagePath;
    private String extractedText = "";
    private PatientViewModel patientViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_result);

        initViews();
        patientViewModel = new ViewModelProvider(this).get(PatientViewModel.class);

        // Get image path from intent
        imagePath = getIntent().getStringExtra("image_path");

        if (imagePath != null) {
            loadAndDisplayImage();
            processImageWithMLKit();
        } else {
            Toast.makeText(this, "Error: No image found", Toast.LENGTH_SHORT).show();
            finish();
        }

        setupClickListeners();
    }

    private void initViews() {
        imageCaptured = findViewById(R.id.image_captured);
        textExtracted = findViewById(R.id.text_extracted);
        textRawLabel = findViewById(R.id.text_raw_label);
        scrollRawText = findViewById(R.id.scroll_raw_text);
        editExtractedName = findViewById(R.id.edit_extracted_name);
        editExtractedAge = findViewById(R.id.edit_extracted_age);
        editExtractedGender = findViewById(R.id.edit_extracted_gender);
        editExtractedCondition = findViewById(R.id.edit_extracted_condition);
        buttonSaveRecord = findViewById(R.id.button_save_record);
        buttonRetry = findViewById(R.id.button_retry);
    }

    private void setupClickListeners() {
        buttonSaveRecord.setOnClickListener(v -> saveManuallyEditedRecord());

        buttonRetry.setOnClickListener(v -> {
            // Delete the current image file
            if (imagePath != null) {
                File file = new File(imagePath);
                if (file.exists()) {
                    file.delete();
                }
            }

            // Go back to camera
            Intent intent = new Intent(this, FormScannerActivity.class);
            startActivity(intent);
            finish();
        });

        // Toggle raw text visibility
        textRawLabel.setOnClickListener(v -> {
            if (scrollRawText.getVisibility() == View.GONE) {
                scrollRawText.setVisibility(View.VISIBLE);
                textRawLabel.setText("▲ Raw Extracted Text (tap to collapse)");
            } else {
                scrollRawText.setVisibility(View.GONE);
                textRawLabel.setText("▼ Raw Extracted Text (tap to expand)");
            }
        });
    }

    private void loadAndDisplayImage() {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            if (bitmap != null) {
                imageCaptured.setImageBitmap(bitmap);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading image", e);
            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
        }
    }

    private void processImageWithMLKit() {
        textExtracted.setText("Processing image...");

        try {
            InputImage image = InputImage.fromFilePath(this, Uri.fromFile(new File(imagePath)));

            // Get the text recognizer with default options
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            recognizer.process(image)
                    .addOnSuccessListener(this::onTextRecognitionSuccess)
                    .addOnFailureListener(this::onTextRecognitionFailure);

        } catch (IOException e) {
            Log.e(TAG, "Error processing image with ML Kit", e);
            textExtracted.setText("Error processing image");
        }
    }

    private void onTextRecognitionSuccess(Text visionText) {
        extractedText = visionText.getText();

        if (extractedText.isEmpty()) {
            textExtracted.setText("No text found in the image. Please try again with better lighting or focus.");
            // Set empty values in edit fields
            editExtractedName.setText("");
            editExtractedAge.setText("");
            editExtractedGender.setText("");
            editExtractedCondition.setText("");
        } else {
            textExtracted.setText(extractedText);
            Log.d(TAG, "Extracted text: " + extractedText);

            // Parse the text and populate the edit fields
            PatientRecord parsedRecord = parseTextToPatientRecord(extractedText);
            populateEditFields(parsedRecord);
        }
    }

    private void populateEditFields(PatientRecord record) {
        if (record != null) {
            editExtractedName.setText(record.getName().equals("Scanned Patient") ? "" : record.getName());
            editExtractedAge.setText(record.getAge() == 0 ? "" : String.valueOf(record.getAge()));
            editExtractedGender.setText(record.getGender().equals("Unknown") ? "" : record.getGender());
            editExtractedCondition.setText(record.getCondition().equals("See scanned document") ? "" : record.getCondition());

            Log.d(TAG, "Populated edit fields with parsed data");
        }
    }

    private void saveManuallyEditedRecord() {
        // Get values from edit fields
        String name = editExtractedName.getText().toString().trim();
        String ageString = editExtractedAge.getText().toString().trim();
        String gender = editExtractedGender.getText().toString().trim();
        String condition = editExtractedCondition.getText().toString().trim();

        // Validate required fields
        if (name.isEmpty()) {
            editExtractedName.setError("Name is required");
            editExtractedName.requestFocus();
            return;
        }

        if (condition.isEmpty()) {
            editExtractedCondition.setError("Medical condition is required");
            editExtractedCondition.requestFocus();
            return;
        }

        // Parse age
        int age = 0;
        if (!ageString.isEmpty()) {
            try {
                age = Integer.parseInt(ageString);
                if (age < 0 || age > 150) {
                    editExtractedAge.setError("Please enter a valid age (0-150)");
                    editExtractedAge.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                editExtractedAge.setError("Please enter a valid number");
                editExtractedAge.requestFocus();
                return;
            }
        }

        // Set default values for empty optional fields
        if (gender.isEmpty()) {
            gender = "Not specified";
        }

        // Create and save the patient record
        PatientRecord patientRecord = new PatientRecord(name, age, gender, condition);
        patientViewModel.insert(patientRecord);

        Toast.makeText(this, "Patient record saved successfully!", Toast.LENGTH_LONG).show();

        // Clean up and return
        cleanupImageFile();
        finish();
    }

    private void onTextRecognitionFailure(@NonNull Exception e) {
        Log.e(TAG, "Text recognition failed", e);
        textExtracted.setText("Failed to extract text. Please try again.");
    }

    private PatientRecord parseTextToPatientRecord(String text) {
        // Enhanced parsing logic with OCR error handling
        String[] lines = text.split("\n");

        // Add debug logging
        Log.d(TAG, "Raw OCR text: " + text);
        Log.d(TAG, "Split into " + lines.length + " lines");
        for (int i = 0; i < lines.length; i++) {
            Log.d(TAG, "Line " + i + ": '" + lines[i] + "'");
        }

        String name = "";
        int age = 0;
        String gender = "";
        String condition = "";

        try {
            // First pass: Look for any recognizable patterns and values
            for (String line : lines) {
                String trimmedLine = line.trim();
                String cleanLine = trimmedLine.replaceAll("[^A-Za-z0-9\\s]", " ").trim();
                String lowerLine = trimmedLine.toLowerCase();

                // Look for name patterns (likely to be alphabetic)
                if (name.isEmpty() && trimmedLine.matches("^[A-Za-z][A-Za-z\\s]*$") &&
                        trimmedLine.length() >= 2 && trimmedLine.length() <= 30 &&
                        !isFormLabel(lowerLine)) {
                    name = trimmedLine;
                    Log.d(TAG, "Found potential name: " + name);
                }

                // Look for age patterns (1-3 digits, possibly with text)
                if (age == 0) {
                    String numbers = trimmedLine.replaceAll("[^0-9]", "");
                    if (numbers.length() >= 1 && numbers.length() <= 3) {
                        try {
                            int potentialAge = Integer.parseInt(numbers);
                            if (potentialAge > 0 && potentialAge < 150) {
                                age = potentialAge;
                                Log.d(TAG, "Found potential age: " + age);
                            }
                        } catch (NumberFormatException e) {
                            // Skip invalid numbers
                        }
                    }
                }

                // Look for gender patterns
                if (gender.isEmpty() && isGenderValue(lowerLine)) {
                    gender = trimmedLine;
                    Log.d(TAG, "Found potential gender: " + gender);
                }

                // Look for condition patterns (anything medical-sounding or remaining text)
                if (condition.isEmpty() && trimmedLine.length() >= 3 &&
                        !isFormLabel(lowerLine) && !trimmedLine.equals(name) &&
                        !trimmedLine.equals(String.valueOf(age)) && !trimmedLine.equals(gender)) {
                    // Check if it looks like a medical condition
                    if (lowerLine.contains("sick") || lowerLine.contains("pain") ||
                            lowerLine.contains("fever") || lowerLine.contains("cold") ||
                            lowerLine.contains("headache") || lowerLine.contains("cough") ||
                            trimmedLine.length() >= 4) {
                        condition = trimmedLine;
                        Log.d(TAG, "Found potential condition: " + condition);
                    }
                }
            }

            // Second pass: Try to extract from label-value pairs if still missing data
            for (String line : lines) {
                String trimmedLine = line.trim();
                String lowerLine = trimmedLine.toLowerCase();

                // Handle colon-separated values
                if (lowerLine.contains(":")) {
                    String[] parts = trimmedLine.split(":", 2);
                    if (parts.length == 2) {
                        String label = parts[0].trim().toLowerCase();
                        String value = parts[1].trim();

                        if (label.contains("name") && name.isEmpty() && value.length() > 0) {
                            name = value;
                            Log.d(TAG, "Found name from colon format: " + name);
                        } else if (label.contains("age") && age == 0) {
                            try {
                                String numbers = value.replaceAll("[^0-9]", "");
                                if (!numbers.isEmpty()) {
                                    int parsedAge = Integer.parseInt(numbers);
                                    if (parsedAge > 0 && parsedAge < 150) {
                                        age = parsedAge;
                                        Log.d(TAG, "Found age from colon format: " + age);
                                    }
                                }
                            } catch (NumberFormatException e) {
                                // Skip invalid age
                            }
                        } else if ((label.contains("gender") || label.contains("sex")) && gender.isEmpty()) {
                            gender = value;
                            Log.d(TAG, "Found gender from colon format: " + gender);
                        } else if ((label.contains("condition") || label.contains("diagnosis")) && condition.isEmpty()) {
                            condition = value;
                            Log.d(TAG, "Found condition from colon format: " + condition);
                        }
                    }
                }
            }

            // Third pass: Handle sequential label-value pairs (form style)
            for (int i = 0; i < lines.length - 1; i++) {
                String currentLine = lines[i].trim().toLowerCase();
                String nextLine = lines[i + 1].trim();

                if (nextLine.isEmpty()) continue;

                if (currentLine.contains("name") && name.isEmpty() && !isFormLabel(nextLine.toLowerCase())) {
                    name = nextLine;
                    Log.d(TAG, "Found name from sequential format: " + name);
                } else if (currentLine.contains("age") && age == 0) {
                    try {
                        String numbers = nextLine.replaceAll("[^0-9]", "");
                        if (!numbers.isEmpty()) {
                            int parsedAge = Integer.parseInt(numbers);
                            if (parsedAge > 0 && parsedAge < 150) {
                                age = parsedAge;
                                Log.d(TAG, "Found age from sequential format: " + age);
                            }
                        }
                    } catch (NumberFormatException e) {
                        // Skip invalid age
                    }
                } else if ((currentLine.contains("gender") || currentLine.contains("sex")) && gender.isEmpty()) {
                    gender = nextLine;
                    Log.d(TAG, "Found gender from sequential format: " + gender);
                } else if ((currentLine.contains("condition") || currentLine.contains("diagnosis")) && condition.isEmpty()) {
                    condition = nextLine;
                    Log.d(TAG, "Found condition from sequential format: " + condition);
                }
            }

            // Apply defaults for missing fields
            if (name.isEmpty()) {
                name = "Scanned Patient";
            }
            if (gender.isEmpty()) {
                gender = "Unknown";
            }
            if (condition.isEmpty()) {
                // Use any remaining unmatched text as condition
                StringBuilder remainingText = new StringBuilder();
                for (String line : lines) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty() && !isFormLabel(trimmed.toLowerCase()) &&
                            !trimmed.equals(name) && !trimmed.equals(String.valueOf(age)) &&
                            !trimmed.equals(gender)) {
                        if (remainingText.length() > 0) remainingText.append(" ");
                        remainingText.append(trimmed);
                    }
                }
                condition = remainingText.toString();
                if (condition.isEmpty()) {
                    condition = "See scanned document";
                }
            }

            Log.d(TAG, "Final parsed result - Name: " + name + ", Age: " + age + ", Gender: " + gender + ", Condition: " + condition);
            return new PatientRecord(name, age, gender, condition);

        } catch (Exception e) {
            Log.e(TAG, "Error parsing text", e);
            return new PatientRecord("Scanned Patient", 0, "Unknown", text);
        }
    }

    private boolean isFormLabel(String text) {
        text = text.toLowerCase().replaceAll("[^a-z]", "");
        return text.equals("name") || text.equals("age") || text.equals("gender") ||
                text.equals("sex") || text.equals("condition") || text.equals("diagnosis") ||
                text.equals("patient") || text.equals("patientname");
    }

    private boolean isGenderValue(String text) {
        text = text.toLowerCase().replaceAll("[^a-z]", "");
        return text.equals("male") || text.equals("female") || text.equals("m") ||
                text.equals("f") || text.equals("man") || text.equals("woman");
    }

    private String extractValueAfterKeyword(String line, String keyword) {
        String lowerLine = line.toLowerCase();
        String lowerKeyword = keyword.toLowerCase();

        int index = lowerLine.indexOf(lowerKeyword);
        if (index != -1) {
            String value = line.substring(index + keyword.length()).trim();
            // Remove common separators
            value = value.replaceFirst("^[:\\-\\s]+", "").trim();
            return value;
        }
        return "";
    }

    private void cleanupImageFile() {
        if (imagePath != null) {
            File file = new File(imagePath);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up image file when activity is destroyed
        cleanupImageFile();
    }
}