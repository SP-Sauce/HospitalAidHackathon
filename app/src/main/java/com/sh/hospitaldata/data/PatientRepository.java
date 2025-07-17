package com.sh.hospitaldata.data;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
}