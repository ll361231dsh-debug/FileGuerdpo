package com.fileguardpro.scanner.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.fileguardpro.scanner.model.ScanResult;

import java.util.List;

@Dao
public interface ScanResultDao {

    @Insert
    long insert(ScanResult scanResult);

    @Delete
    void delete(ScanResult scanResult);

    @Query("SELECT * FROM scan_results ORDER BY scanTimestamp DESC")
    LiveData<List<ScanResult>> getAllScanResults();

    @Query("SELECT * FROM scan_results ORDER BY scanTimestamp DESC LIMIT :limit")
    LiveData<List<ScanResult>> getRecentScans(int limit);

    @Query("SELECT * FROM scan_results WHERE id = :id")
    LiveData<ScanResult> getScanResultById(long id);

    @Query("SELECT COUNT(*) FROM scan_results")
    LiveData<Integer> getTotalScanCount();

    @Query("SELECT COUNT(*) FROM scan_results WHERE riskScore > 50")
    LiveData<Integer> getThreatsDetectedCount();

    @Query("DELETE FROM scan_results")
    void deleteAll();
}
