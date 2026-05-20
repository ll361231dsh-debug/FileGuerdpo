package com.fileguardpro.scanner.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.fileguardpro.scanner.model.ScanResult;
import com.fileguardpro.scanner.repository.ScanRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class HistoryViewModel extends ViewModel {

    private final ScanRepository scanRepository;

    @Inject
    public HistoryViewModel(ScanRepository scanRepository) {
        this.scanRepository = scanRepository;
    }

    public LiveData<List<ScanResult>> getAllScans() {
        return scanRepository.getAllScanResults();
    }

    public void deleteScan(ScanResult result) {
        scanRepository.deleteScanResult(result);
    }

    public void clearAll() {
        scanRepository.clearHistory();
    }
}
