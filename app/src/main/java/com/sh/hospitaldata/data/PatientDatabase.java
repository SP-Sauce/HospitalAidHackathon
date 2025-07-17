package com.sh.hospitaldata.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {PatientRecord.class}, version = 1, exportSchema = false)
public abstract class PatientDatabase extends RoomDatabase {

    private static PatientDatabase instance;

    public abstract PatientRecordDao patientRecordDao();

    public static synchronized PatientDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            PatientDatabase.class, "patient_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}