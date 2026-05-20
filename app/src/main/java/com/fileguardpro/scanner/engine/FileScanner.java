package com.fileguardpro.scanner.engine;

import android.content.Context;
import android.net.Uri;

import com.fileguardpro.scanner.model.ScanResult;

public interface FileScanner {
    ScanResult scan(Context context, Uri fileUri, String fileName, String mimeType);
    boolean canHandle(String mimeType, String extension);
}
