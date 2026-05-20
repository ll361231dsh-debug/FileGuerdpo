package com.fileguardpro.scanner.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.fileguardpro.scanner.model.ScanResult;

@Database(entities = {ScanResult.class}, version = 1, exportSchema = false)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ScanResultDao scanResultDao();
}
