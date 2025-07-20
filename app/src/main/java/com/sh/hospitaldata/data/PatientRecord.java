package com.sh.hospitaldata.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "patient_records")
public class PatientRecord {
    @PrimaryKey(autoGenerate = true)
    private int id;

    // Basic patient information
    private String name;
    private int age;
    private String gender;
    private String condition;

    // Hospital admission fields (optional)
    private boolean isAdmitted;
    private String wardName;
    private String admissionDate;
    private String progressNotes;

    // Primary constructor for Room (all fields)
    public PatientRecord(String name, int age, String gender, String condition,
                         boolean isAdmitted, String wardName, String admissionDate, String progressNotes) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.condition = condition;
        this.isAdmitted = isAdmitted;
        this.wardName = wardName;
        this.admissionDate = admissionDate;
        this.progressNotes = progressNotes != null ? progressNotes : "";
    }

    // Convenience constructor for basic patient (not admitted) - ignored by Room
    @androidx.room.Ignore
    public PatientRecord(String name, int age, String gender, String condition) {
        this(name, age, gender, condition, false, null, null, "");
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public boolean isAdmitted() {
        return isAdmitted;
    }

    public void setAdmitted(boolean admitted) {
        isAdmitted = admitted;
    }

    public String getWardName() {
        return wardName;
    }

    public void setWardName(String wardName) {
        this.wardName = wardName;
    }

    public String getAdmissionDate() {
        return admissionDate;
    }

    public void setAdmissionDate(String admissionDate) {
        this.admissionDate = admissionDate;
    }

    public String getProgressNotes() {
        return progressNotes != null ? progressNotes : "";
    }

    public void setProgressNotes(String progressNotes) {
        this.progressNotes = progressNotes != null ? progressNotes : "";
    }

    // Utility methods
    public String getPatientStatus() {
        return isAdmitted ? "Admitted - " + wardName : "Outpatient";
    }

    public boolean hasProgressNotes() {
        return progressNotes != null && !progressNotes.trim().isEmpty();
    }
}