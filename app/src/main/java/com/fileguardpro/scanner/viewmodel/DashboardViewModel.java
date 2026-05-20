package com.fileguardpro.scanner.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.fileguardpro.scanner.model.ScanResult;
import com.fileguardpro.scanner.repository.ScanRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class DashboardViewModel extends ViewModel {

    private final ScanRepository scanRepository;

    @Inject
    public DashboardViewModel(ScanRepository scanRepository) {
        this.scanRepository = scanRepository;
    }

    public LiveData<List<ScanResult>> getRecentScans() {
        return scanRepository.getRecentScans(10);
    }

    public LiveData<Integer> getTotalScanCount() {
        return scanRepository.getTotalScanCount();
    }

    public LiveData<Integer> getThreatsDetectedCount() {
        return scanRepository.getThreatsDetectedCount();
    }

    public void clearHistory() {
        scanRepository.clearHistory();
    }
}
