package com.sh.hospitaldata.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PatientRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PatientRecord patientRecord);

    @Update
    void update(PatientRecord patientRecord);

    @Delete
    void delete(PatientRecord patientRecord);

    @Query("DELETE FROM patient_records")
    void deleteAllRecords();

    @Query("SELECT * FROM patient_records ORDER BY id DESC")
    LiveData<List<PatientRecord>> getAllRecords();

    @Query("SELECT * FROM patient_records WHERE id = :id")
    LiveData<PatientRecord> getRecordById(int id);

    @Query("SELECT * FROM patient_records WHERE name LIKE '%' || :name || '%'")
    LiveData<List<PatientRecord>> searchByName(String name);

    @Query("SELECT * FROM patient_records WHERE condition LIKE '%' || :condition || '%'")
    LiveData<List<PatientRecord>> searchByCondition(String condition);

    // New methods for duplicate checking
    @Query("SELECT * FROM patient_records WHERE LOWER(TRIM(name)) = LOWER(TRIM(:name)) AND age = :age")
    List<PatientRecord> findDuplicateByNameAndAge(String name, int age);

    @Query("SELECT * FROM patient_records WHERE LOWER(TRIM(name)) = LOWER(TRIM(:name)) AND age = :age AND LOWER(TRIM(gender)) = LOWER(TRIM(:gender))")
    List<PatientRecord> findDuplicateByNameAgeGender(String name, int age, String gender);

    @Query("SELECT COUNT(*) FROM patient_records WHERE LOWER(TRIM(name)) = LOWER(TRIM(:name)) AND age = :age")
    int countDuplicatesByNameAndAge(String name, int age);

    @Query("SELECT COUNT(*) FROM patient_records WHERE LOWER(TRIM(name)) = LOWER(TRIM(:name)) AND age = :age AND LOWER(TRIM(gender)) = LOWER(TRIM(:gender))")
    int countDuplicatesByNameAgeGender(String name, int age, String gender);
}