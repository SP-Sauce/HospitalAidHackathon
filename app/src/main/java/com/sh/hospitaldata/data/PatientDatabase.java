package com.sh.hospitaldata.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {PatientRecord.class}, version = 2, exportSchema = false)
public abstract class PatientDatabase extends RoomDatabase {

    private static PatientDatabase instance;

    public abstract PatientRecordDao patientRecordDao();

    // Migration from version 1 to 2 to add admission fields
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Add new columns for hospital admission
            database.execSQL("ALTER TABLE patient_records ADD COLUMN isAdmitted INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE patient_records ADD COLUMN wardName TEXT");
            database.execSQL("ALTER TABLE patient_records ADD COLUMN admissionDate TEXT");
            database.execSQL("ALTER TABLE patient_records ADD COLUMN progressNotes TEXT DEFAULT ''");
        }
    };

    public static synchronized PatientDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            PatientDatabase.class, "patient_database")
                    .addMigrations(MIGRATION_1_2)
                    .build();
        }
        return instance;
    }
}