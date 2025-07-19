package com.sh.hospitaldata.data;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.Future;

public class PatientViewModel extends AndroidViewModel {
    private PatientRepository repository;
    private LiveData<List<PatientRecord>> allRecords;

    public PatientViewModel(@NonNull Application application) {
        super(application);
        repository = new PatientRepository(application);
        allRecords = repository.getAllRecords();
    }

    public void insert(PatientRecord patientRecord) {
        repository.insert(patientRecord);
    }

    public void update(PatientRecord patientRecord) {
        repository.update(patientRecord);
    }

    public void delete(PatientRecord patientRecord) {
        repository.delete(patientRecord);
    }

    public void deleteAllRecords() {
        repository.deleteAllRecords();
    }

    public LiveData<List<PatientRecord>> getAllRecords() {
        return allRecords;
    }

    public LiveData<PatientRecord> getRecordById(int id) {
        return repository.getRecordById(id);
    }

    public LiveData<List<PatientRecord>> searchByName(String name) {
        return repository.searchByName(name);
    }

    public LiveData<List<PatientRecord>> searchByCondition(String condition) {
        return repository.searchByCondition(condition);
    }

    // New methods for duplicate checking
    public Future<Boolean> checkForDuplicates(String name, int age, String gender) {
        return repository.checkForDuplicates(name, age, gender);
    }

    public Future<List<PatientRecord>> findDuplicateRecords(String name, int age, String gender) {
        return repository.findDuplicateRecords(name, age, gender);
    }

    // Convenience method to insert after checking for duplicates
    public interface DuplicateCheckCallback {
        void onDuplicateFound(List<PatientRecord> duplicates);
        void onNoDuplicateFound();
        void onError(Exception e);
    }

    public void insertWithDuplicateCheck(PatientRecord patientRecord, DuplicateCheckCallback callback) {
        // Check for duplicates first
        Future<List<PatientRecord>> duplicatesFuture = findDuplicateRecords(
                patientRecord.getName(),
                patientRecord.getAge(),
                patientRecord.getGender()
        );

        // Handle the result in a background thread
        new Thread(() -> {
            try {
                List<PatientRecord> duplicates = duplicatesFuture.get();
                if (duplicates != null && !duplicates.isEmpty()) {
                    // Duplicates found - notify callback on UI thread
                    if (callback != null) {
                        // Post to main thread for UI updates
                        android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                        mainHandler.post(() -> callback.onDuplicateFound(duplicates));
                    }
                } else {
                    // No duplicates - proceed with insertion
                    insert(patientRecord);
                    if (callback != null) {
                        // Post to main thread for UI updates
                        android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                        mainHandler.post(() -> callback.onNoDuplicateFound());
                    }
                }
            } catch (Exception e) {
                if (callback != null) {
                    // Post to main thread for UI updates
                    android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                    mainHandler.post(() -> callback.onError(e));
                }
            }
        }).start();
    }
}