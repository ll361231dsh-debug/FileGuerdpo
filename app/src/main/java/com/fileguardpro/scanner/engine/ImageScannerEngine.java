package com.fileguardpro.scanner.engine;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.fileguardpro.scanner.model.ScanResult;
import com.fileguardpro.scanner.model.Threat;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ImageScannerEngine implements FileScanner {

    private static final Set<String> IMAGE_MIME_TYPES = new HashSet<>(Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/webp", "image/bmp"
    ));

    private static final Set<String> IMAGE_EXTENSIONS = new HashSet<>(Arrays.asList(
            "jpg", "jpeg", "png", "webp", "bmp"
    ));

    // Magic bytes for image formats
    private static final byte[] JPEG_MAGIC = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_MAGIC = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] BMP_MAGIC = {0x42, 0x4D};
    private static final byte[] WEBP_MAGIC_RIFF = {0x52, 0x49, 0x46, 0x46};

    // Patterns for detecting hidden content
    private static final Pattern SCRIPT_PATTERN = Pattern.compile(
            "(<script|<iframe|<object|<embed|<applet|javascript:|vbscript:)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern EXECUTABLE_PATTERN = Pattern.compile(
            "(MZ|PE\\x00\\x00|\\x7fELF|#!/bin|cmd\\.exe|powershell)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern PHP_PATTERN = Pattern.compile(
            "(<\\?php|<\\?=|eval\\s*\\(|base64_decode|system\\s*\\()",
            Pattern.CASE_INSENSITIVE);

    @Inject
    public ImageScannerEngine() {}

    @Override
    public boolean canHandle(String mimeType, String extension) {
        if (mimeType != null && IMAGE_MIME_TYPES.contains(mimeType.toLowerCase())) return true;
        if (extension != null && IMAGE_EXTENSIONS.contains(extension.toLowerCase())) return true;
        return false;
    }

    @Override
    public ScanResult scan(Context context, Uri fileUri, String fileName, String mimeType) {
        ScanResult result = new ScanResult();
        result.setFileName(fileName);
        result.setFileType("Image (" + getImageFormat(fileName) + ")");

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
            if (inputStream == null) return result;

            // Read entire file into byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            inputStream.close();

            byte[] fileBytes = baos.toByteArray();
            result.setFileSize(fileBytes.length);

            // Verify magic bytes
            verifyMagicBytes(fileBytes, fileName, result);

            // Check for hidden data after image end
            checkTrailingData(fileBytes, fileName, result);

            // Check for steganography indicators
            checkSteganography(fileBytes, result);

            // Check EXIF data
            checkExifData(context, fileUri, fileBytes, result);

            // Check for embedded scripts/executables
            checkEmbeddedContent(fileBytes, result);

            // Verify image can be decoded
            verifyImageIntegrity(fileBytes, result);

            // Check file size anomalies
            checkSizeAnomalies(fileBytes, fileName, result);

        } catch (Exception e) {
            result.addThreat(new Threat(
                    "Analysis Error",
                    "Could not fully analyze image: " + e.getMessage(),
                    Threat.Severity.LOW,
                    Threat.Category.SUSPICIOUS_COMMAND
            ));
        }

        calculateRiskScore(result);
        return result;
    }

    private void verifyMagicBytes(byte[] fileBytes, String fileName, ScanResult result) {
        if (fileBytes.length < 8) {
            result.addThreat(new Threat(
                    "Corrupted File",
                    "File is too small to be a valid image",
                    Threat.Severity.HIGH,
                    Threat.Category.FAKE_EXTENSION
            ));
            return;
        }

        String extension = getExtension(fileName).toLowerCase();
        boolean validMagic = false;

        switch (extension) {
            case "jpg":
            case "jpeg":
                validMagic = matchBytes(fileBytes, JPEG_MAGIC);
                break;
            case "png":
                validMagic = matchBytes(fileBytes, PNG_MAGIC);
                break;
            case "bmp":
                validMagic = matchBytes(fileBytes, BMP_MAGIC);
                break;
            case "webp":
                validMagic = matchBytes(fileBytes, WEBP_MAGIC_RIFF);
                break;
            default:
                validMagic = true;
        }

        if (!validMagic) {
            result.addThreat(new Threat(
                    "Fake Extension Detected",
                    "File extension does not match actual file format - possible disguised malware",
                    Threat.Severity.CRITICAL,
                    Threat.Category.FAKE_EXTENSION
            ));
        }
    }

    private void checkTrailingData(byte[] fileBytes, String fileName, ScanResult result) {
        String extension = getExtension(fileName).toLowerCase();

        if ("jpg".equals(extension) || "jpeg".equals(extension)) {
            // JPEG should end with FF D9
            int endMarker = findJpegEnd(fileBytes);
            if (endMarker > 0 && endMarker < fileBytes.length - 2) {
                int trailingSize = fileBytes.length - endMarker - 2;
                if (trailingSize > 100) {
                    result.setHasHiddenFiles(true);
                    result.addThreat(new Threat(
                            "Hidden Data After Image",
                            "File contains " + trailingSize + " bytes of data appended after the image end marker",
                            Threat.Severity.HIGH,
                            Threat.Category.PAYLOAD_DETECTED
                    ));
                }
            }
        }

        if ("png".equals(extension)) {
            // PNG should end with IEND chunk
            int iendPos = findPngEnd(fileBytes);
            if (iendPos > 0 && iendPos + 12 < fileBytes.length) {
                int trailingSize = fileBytes.length - iendPos - 12;
                if (trailingSize > 100) {
                    result.setHasHiddenFiles(true);
                    result.addThreat(new Threat(
                            "Hidden Data After PNG",
                            "File contains " + trailingSize + " bytes appended after the PNG IEND chunk",
                            Threat.Severity.HIGH,
                            Threat.Category.PAYLOAD_DETECTED
                    ));
                }
            }
        }
    }

    private void checkSteganography(byte[] fileBytes, ScanResult result) {
        // Check for unusual patterns in LSB (Least Significant Bits)
        if (fileBytes.length > 1024) {
            int uniformLsbCount = 0;
            int sampleSize = Math.min(fileBytes.length, 10000);

            for (int i = 100; i < sampleSize; i++) {
                if ((fileBytes[i] & 0x01) == (fileBytes[i - 1] & 0x01)) {
                    uniformLsbCount++;
                }
            }

            double lsbUniformity = (double) uniformLsbCount / (sampleSize - 100);
            if (lsbUniformity > 0.85 || lsbUniformity < 0.15) {
                result.addThreat(new Threat(
                        "Possible Steganography",
                        "Image LSB pattern suggests possible hidden data (steganography)",
                        Threat.Severity.MEDIUM,
                        Threat.Category.STEGANOGRAPHY
                ));
            }
        }

        // Check for known steganography tool signatures
        String contentSample = new String(fileBytes, Math.max(0, fileBytes.length - 500),
                Math.min(500, fileBytes.length), StandardCharsets.ISO_8859_1);
        if (contentSample.contains("OpenStego") || contentSample.contains("steghide") ||
                contentSample.contains("SilentEye")) {
            result.addThreat(new Threat(
                    "Steganography Tool Signature",
                    "File contains signatures of known steganography tools",
                    Threat.Severity.HIGH,
                    Threat.Category.STEGANOGRAPHY
            ));
        }
    }

    private void checkExifData(Context context, Uri fileUri, byte[] fileBytes, ScanResult result) {
        // Check for suspicious EXIF data
        String content = new String(fileBytes, StandardCharsets.ISO_8859_1);

        // Check for script injection in EXIF comments
        if (content.contains("<?php") || content.contains("<script") ||
                content.contains("javascript:")) {
            result.setHasMaliciousContent(true);
            result.addThreat(new Threat(
                    "Malicious EXIF Data",
                    "Image EXIF/metadata contains injected code",
                    Threat.Severity.CRITICAL,
                    Threat.Category.MALICIOUS_CONTENT
            ));
        }

        // Check for unusually large EXIF data
        int exifStart = content.indexOf("Exif");
        if (exifStart > 0) {
            // Look for next image data marker
            int exifSize = 0;
            for (int i = exifStart; i < Math.min(exifStart + 100000, fileBytes.length); i++) {
                exifSize++;
                if (fileBytes[i] == (byte) 0xFF && i + 1 < fileBytes.length &&
                        fileBytes[i + 1] == (byte) 0xDA) break;
            }
            if (exifSize > 50000) {
                result.addThreat(new Threat(
                        "Oversized EXIF Data",
                        "Image contains unusually large metadata (" + exifSize + " bytes) which may hide payloads",
                        Threat.Severity.MEDIUM,
                        Threat.Category.EXIF_ANOMALY
                ));
            }
        }
    }

    private void checkEmbeddedContent(byte[] fileBytes, ScanResult result) {
        String content = new String(fileBytes, StandardCharsets.ISO_8859_1);

        // Check for embedded scripts
        Matcher scriptMatcher = SCRIPT_PATTERN.matcher(content);
        if (scriptMatcher.find()) {
            result.setHasJavaScript(true);
            result.setHasMaliciousContent(true);
            result.addThreat(new Threat(
                    "Embedded Script Detected",
                    "Image contains embedded script tags or code",
                    Threat.Severity.CRITICAL,
                    Threat.Category.MALICIOUS_CONTENT
            ));
        }

        // Check for embedded executables
        Matcher exeMatcher = EXECUTABLE_PATTERN.matcher(content);
        if (exeMatcher.find()) {
            result.setHasHiddenFiles(true);
            result.addThreat(new Threat(
                    "Embedded Executable",
                    "Image contains embedded executable code signatures",
                    Threat.Severity.CRITICAL,
                    Threat.Category.EMBEDDED_EXECUTABLE
            ));
        }

        // Check for PHP code
        Matcher phpMatcher = PHP_PATTERN.matcher(content);
        if (phpMatcher.find()) {
            result.setHasMaliciousContent(true);
            result.addThreat(new Threat(
                    "PHP Code Detected",
                    "Image contains embedded PHP code - possible web shell",
                    Threat.Severity.CRITICAL,
                    Threat.Category.MALICIOUS_CONTENT
            ));
        }
    }

    private void verifyImageIntegrity(byte[] fileBytes, ScanResult result) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(fileBytes, 0, fileBytes.length, options);

            if (options.outWidth <= 0 || options.outHeight <= 0) {
                result.addThreat(new Threat(
                        "Corrupted Image",
                        "Image cannot be decoded - file may be intentionally corrupted or disguised",
                        Threat.Severity.HIGH,
                        Threat.Category.FAKE_EXTENSION
                ));
            }
        } catch (Exception e) {
            result.addThreat(new Threat(
                    "Image Decode Error",
                    "Failed to decode image structure",
                    Threat.Severity.MEDIUM,
                    Threat.Category.FAKE_EXTENSION
            ));
        }
    }

    private void checkSizeAnomalies(byte[] fileBytes, String fileName, ScanResult result) {
        // A very large "image" file might be suspicious
        if (fileBytes.length > 50 * 1024 * 1024) { // > 50MB
            result.addThreat(new Threat(
                    "Unusually Large Image",
                    "Image file is unusually large (" + (fileBytes.length / (1024 * 1024)) + " MB) which may indicate hidden content",
                    Threat.Severity.LOW,
                    Threat.Category.SUSPICIOUS_COMMAND
            ));
        }
    }

    private int findJpegEnd(byte[] data) {
        for (int i = data.length - 2; i >= 0; i--) {
            if (data[i] == (byte) 0xFF && data[i + 1] == (byte) 0xD9) {
                return i;
            }
        }
        return -1;
    }

    private int findPngEnd(byte[] data) {
        byte[] iend = {0x49, 0x45, 0x4E, 0x44};
        for (int i = data.length - 8; i >= 0; i--) {
            if (data[i] == iend[0] && data[i + 1] == iend[1] &&
                    data[i + 2] == iend[2] && data[i + 3] == iend[3]) {
                return i;
            }
        }
        return -1;
    }

    private boolean matchBytes(byte[] data, byte[] magic) {
        if (data.length < magic.length) return false;
        for (int i = 0; i < magic.length; i++) {
            if (data[i] != magic[i]) return false;
        }
        return true;
    }

    private String getExtension(String fileName) {
        if (fileName == null) return "";
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 ? fileName.substring(dot + 1) : "";
    }

    private String getImageFormat(String fileName) {
        String ext = getExtension(fileName).toUpperCase();
        return ext.isEmpty() ? "Unknown" : ext;
    }

    private void calculateRiskScore(ScanResult result) {
        int score = 0;
        for (Threat threat : result.getThreats()) {
            score += threat.getImpactScore();
        }
        score = Math.min(score, 100);
        result.setRiskScore(score);
        result.setRiskLevel(ThreatDetectionEngine.getRiskLevelFromScore(score));
    }
}
