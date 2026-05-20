package com.fileguardpro.scanner.di;

import android.content.Context;

import androidx.room.Room;

import com.fileguardpro.scanner.database.AppDatabase;
import com.fileguardpro.scanner.database.ScanResultDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Provides
    @Singleton
    public AppDatabase provideDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, "fileguard_db")
                .fallbackToDestructiveMigration()
                .build();
    }

    @Provides
    @Singleton
    public ScanResultDao provideScanResultDao(AppDatabase database) {
        return database.scanResultDao();
    }
}
