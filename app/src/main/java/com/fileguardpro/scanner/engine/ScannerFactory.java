package com.fileguardpro.scanner.engine;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ScannerFactory {

    private final OfficeScannerEngine officeScannerEngine;
    private final PdfScannerEngine pdfScannerEngine;
    private final ImageScannerEngine imageScannerEngine;

    @Inject
    public ScannerFactory(OfficeScannerEngine officeScannerEngine,
                          PdfScannerEngine pdfScannerEngine,
                          ImageScannerEngine imageScannerEngine) {
        this.officeScannerEngine = officeScannerEngine;
        this.pdfScannerEngine = pdfScannerEngine;
        this.imageScannerEngine = imageScannerEngine;
    }

    public FileScanner getScanner(String mimeType, String extension) {
        if (officeScannerEngine.canHandle(mimeType, extension)) {
            return officeScannerEngine;
        }
        if (pdfScannerEngine.canHandle(mimeType, extension)) {
            return pdfScannerEngine;
        }
        if (imageScannerEngine.canHandle(mimeType, extension)) {
            return imageScannerEngine;
        }
        return null;
    }
}
