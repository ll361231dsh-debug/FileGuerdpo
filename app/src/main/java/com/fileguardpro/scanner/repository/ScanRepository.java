package com.fileguardpro.scanner.repository;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.LiveData;

import com.fileguardpro.scanner.database.ScanResultDao;
import com.fileguardpro.scanner.engine.FileScanner;
import com.fileguardpro.scanner.engine.ScannerFactory;
import com.fileguardpro.scanner.engine.ThreatDetectionEngine;
import com.fileguardpro.scanner.model.ScanResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ScanRepository {

    private final ScanResultDao scanResultDao;
    private final ScannerFactory scannerFactory;
    private final ThreatDetectionEngine threatDetectionEngine;
    private final ExecutorService executorService;

    @Inject
    public ScanRepository(ScanResultDao scanResultDao,
                          ScannerFactory scannerFactory,
                          ThreatDetectionEngine threatDetectionEngine) {
        this.scanResultDao = scanResultDao;
        this.scannerFactory = scannerFactory;
        this.threatDetectionEngine = threatDetectionEngine;
        this.executorService = Executors.newFixedThreadPool(2);
    }

    public interface ScanCallback {
        void onScanComplete(ScanResult result);
        void onScanError(String error);
    }

    public void scanFile(Context context, Uri fileUri, String fileName, String mimeType, ScanCallback callback) {
        executorService.execute(() -> {
            try {
                String extension = getExtension(fileName);
                FileScanner scanner = scannerFactory.getScanner(mimeType, extension);

                if (scanner == null) {
                    callback.onScanError("Unsupported file type: " + mimeType);
                    return;
                }

                ScanResult result = scanner.scan(context, fileUri, fileName, mimeType);
                result.setFilePath(fileUri.toString());

                // Apply threat detection engine for final analysis
                result = threatDetectionEngine.analyzeFinal(result);

                // Save to database
                long id = scanResultDao.insert(result);
                result.setId(id);

                callback.onScanComplete(result);

            } catch (Exception e) {
                callback.onScanError("Scan failed: " + e.getMessage());
            }
        });
    }

    public void quarantineFile(Context context, Uri fileUri, String fileName) {
        executorService.execute(() -> {
            try {
                File quarantineDir = new File(context.getFilesDir(), "quarantine");
                if (!quarantineDir.exists()) quarantineDir.mkdirs();

                File quarantinedFile = new File(quarantineDir, System.currentTimeMillis() + "_" + fileName);
                InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
                if (inputStream != null) {
                    FileOutputStream outputStream = new FileOutputStream(quarantinedFile);
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.close();
                    inputStream.close();
                }
            } catch (Exception e) {
                // Handle error silently
            }
        });
    }

    public LiveData<List<ScanResult>> getAllScanResults() {
        return scanResultDao.getAllScanResults();
    }

    public LiveData<List<ScanResult>> getRecentScans(int limit) {
        return scanResultDao.getRecentScans(limit);
    }

    public LiveData<Integer> getTotalScanCount() {
        return scanResultDao.getTotalScanCount();
    }

    public LiveData<Integer> getThreatsDetectedCount() {
        return scanResultDao.getThreatsDetectedCount();
    }

    public void deleteScanResult(ScanResult result) {
        executorService.execute(() -> scanResultDao.delete(result));
    }

    public void clearHistory() {
        executorService.execute(scanResultDao::deleteAll);
    }

    private String getExtension(String fileName) {
        if (fileName == null) return "";
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 ? fileName.substring(dot + 1) : "";
    }
}
