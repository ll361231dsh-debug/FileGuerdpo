package com.fileguardpro.scanner.viewmodel;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.fileguardpro.scanner.model.ScanResult;
import com.fileguardpro.scanner.repository.ScanRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ScanViewModel extends ViewModel {

    private final ScanRepository scanRepository;
    private final MutableLiveData<ScanResult> scanResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isScanning = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Integer> scanProgress = new MutableLiveData<>(0);

    @Inject
    public ScanViewModel(ScanRepository scanRepository) {
        this.scanRepository = scanRepository;
    }

    public void startScan(Context context, Uri fileUri, String fileName, String mimeType) {
        isScanning.postValue(true);
        scanProgress.postValue(0);

        scanRepository.scanFile(context, fileUri, fileName, mimeType, new ScanRepository.ScanCallback() {
            @Override
            public void onScanComplete(ScanResult result) {
                scanProgress.postValue(100);
                scanResult.postValue(result);
                isScanning.postValue(false);
            }

            @Override
            public void onScanError(String error) {
                errorMessage.postValue(error);
                isScanning.postValue(false);
            }
        });
    }

    public void quarantineFile(Context context, Uri fileUri, String fileName) {
        scanRepository.quarantineFile(context, fileUri, fileName);
    }

    public LiveData<ScanResult> getScanResult() {
        return scanResult;
    }

    public LiveData<Boolean> getIsScanning() {
        return isScanning;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Integer> getScanProgress() {
        return scanProgress;
    }
}
