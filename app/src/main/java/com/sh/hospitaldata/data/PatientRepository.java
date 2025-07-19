package com.sh.hospitaldata.data;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PatientRepository {
    private PatientRecordDao patientRecordDao;
    private LiveData<List<PatientRecord>> allRecords;
    private ExecutorService executor;

    public PatientRepository(Application application) {
        PatientDatabase database = PatientDatabase.getInstance(application);
        patientRecordDao = database.patientRecordDao();
        allRecords = patientRecordDao.getAllRecords();
        executor = Executors.newFixedThreadPool(2);
    }

    public void insert(PatientRecord patientRecord) {
        executor.execute(() -> patientRecordDao.insert(patientRecord));
    }

    public void update(PatientRecord patientRecord) {
        executor.execute(() -> patientRecordDao.update(patientRecord));
    }

    public void delete(PatientRecord patientRecord) {
        executor.execute(() -> patientRecordDao.delete(patientRecord));
    }

    public void deleteAllRecords() {
        executor.execute(() -> patientRecordDao.deleteAllRecords());
    }

    public LiveData<List<PatientRecord>> getAllRecords() {
        return allRecords;
    }

    public LiveData<PatientRecord> getRecordById(int id) {
        return patientRecordDao.getRecordById(id);
    }

    public LiveData<List<PatientRecord>> searchByName(String name) {
        return patientRecordDao.searchByName(name);
    }

    public LiveData<List<PatientRecord>> searchByCondition(String condition) {
        return patientRecordDao.searchByCondition(condition);
    }

    // New methods for duplicate checking
    public Future<Boolean> checkForDuplicates(String name, int age, String gender) {
        return executor.submit(() -> {
            // First check by name and age
            int countByNameAge = patientRecordDao.countDuplicatesByNameAndAge(name, age);
            if (countByNameAge > 0) {
                return true;
            }

            // If gender is provided and not empty, also check with gender included
            if (gender != null && !gender.trim().isEmpty() &&
                    !gender.trim().equalsIgnoreCase("Unknown") &&
                    !gender.trim().equalsIgnoreCase("Not specified")) {
                int countByNameAgeGender = patientRecordDao.countDuplicatesByNameAgeGender(name, age, gender);
                return countByNameAgeGender > 0;
            }

            return false;
        });
    }

    public Future<List<PatientRecord>> findDuplicateRecords(String name, int age, String gender) {
        return executor.submit(() -> {
            // Try to find duplicates with name, age, and gender first (more specific)
            if (gender != null && !gender.trim().isEmpty() &&
                    !gender.trim().equalsIgnoreCase("Unknown") &&
                    !gender.trim().equalsIgnoreCase("Not specified")) {
                List<PatientRecord> duplicates = patientRecordDao.findDuplicateByNameAgeGender(name, age, gender);
                if (!duplicates.isEmpty()) {
                    return duplicates;
                }
            }

            // Fall back to name and age only
            return patientRecordDao.findDuplicateByNameAndAge(name, age);
        });
    }
}