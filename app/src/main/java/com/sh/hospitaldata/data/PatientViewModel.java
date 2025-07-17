package com.sh.hospitaldata.data;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;

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
}