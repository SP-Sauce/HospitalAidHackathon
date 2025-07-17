package com.sh.hospitaldata.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "patient_records")
public class PatientRecord {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;
    private int age;
    private String gender;
    private String condition;

    // Constructor
    public PatientRecord(String name, int age, String gender, String condition) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.condition = condition;
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
}