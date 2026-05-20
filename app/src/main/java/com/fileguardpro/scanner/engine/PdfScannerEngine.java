package com.fileguardpro.scanner.engine;

import android.content.Context;
import android.net.Uri;

import com.fileguardpro.scanner.model.ScanResult;
import com.fileguardpro.scanner.model.Threat;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PdfScannerEngine implements FileScanner {

    private static final Pattern JS_PATTERN = Pattern.compile(
            "(/JavaScript|/JS\\s|/S\\s*/JavaScript)", Pattern.CASE_INSENSITIVE);
    private static final Pattern URL_PATTERN = Pattern.compile(
            "(https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern AUTO_ACTION_PATTERN = Pattern.compile(
            "(/OpenAction|/AA\\s|/Names\\s)", Pattern.CASE_INSENSITIVE);
    private static final Pattern LAUNCH_PATTERN = Pattern.compile(
            "(/Launch|/Win|/F\\s*\\()", Pattern.CASE_INSENSITIVE);
    private static final Pattern EMBEDDED_FILE_PATTERN = Pattern.compile(
            "(/EmbeddedFile|/FileAttachment|/Filespec)", Pattern.CASE_INSENSITIVE);
    private static final Pattern ENCRYPT_PATTERN = Pattern.compile(
            "(/Encrypt\\s)", Pattern.CASE_INSENSITIVE);
    private static final Pattern SUSPICIOUS_PATTERN = Pattern.compile(
            "(eval\\s*\\(|unescape|fromCharCode|String\\.raw|ActiveXObject|WScript\\.Shell|cmd\\.exe|powershell)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern STREAM_PATTERN = Pattern.compile(
            "(stream\\s*\\n|endstream)", Pattern.CASE_INSENSITIVE);
    private static final Pattern OBJECT_PATTERN = Pattern.compile(
            "(\\d+\\s+\\d+\\s+obj)", Pattern.CASE_INSENSITIVE);

    @Inject
    public PdfScannerEngine() {}

    @Override
    public boolean canHandle(String mimeType, String extension) {
        if ("application/pdf".equalsIgnoreCase(mimeType)) return true;
        if ("pdf".equalsIgnoreCase(extension)) return true;
        return false;
    }

    @Override
    public ScanResult scan(Context context, Uri fileUri, String fileName, String mimeType) {
        ScanResult result = new ScanResult();
        result.setFileName(fileName);
        result.setFileType("PDF Document");

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
            if (inputStream == null) return result;

            result.setFileSize(inputStream.available());

            // Read PDF content as raw text for pattern analysis
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder content = new StringBuilder();
            String line;
            int lineCount = 0;

            while ((line = reader.readLine()) != null && lineCount < 10000) {
                content.append(line).append("\n");
                lineCount++;
            }

            String pdfContent = content.toString();
            reader.close();

            // Analyze PDF structure
            analyzePdfStructure(pdfContent, result);

        } catch (Exception e) {
            result.addThreat(new Threat(
                    "Analysis Error",
                    "Could not fully analyze PDF: " + e.getMessage(),
                    Threat.Severity.LOW,
                    Threat.Category.SUSPICIOUS_COMMAND
            ));
        }

        calculateRiskScore(result);
        return result;
    }

    private void analyzePdfStructure(String content, ScanResult result) {
        // Check for JavaScript
        Matcher jsMatcher = JS_PATTERN.matcher(content);
        if (jsMatcher.find()) {
            result.setHasJavaScript(true);
            result.addThreat(new Threat(
                    "JavaScript Detected",
                    "PDF contains JavaScript code which can be used for exploitation",
                    Threat.Severity.HIGH,
                    Threat.Category.JAVASCRIPT
            ));
        }

        // Check for external URLs
        Matcher urlMatcher = URL_PATTERN.matcher(content);
        int urlCount = 0;
        while (urlMatcher.find() && urlCount < 50) {
            urlCount++;
        }
        if (urlCount > 0) {
            result.setHasExternalLinks(true);
            Threat.Severity severity = urlCount > 10 ? Threat.Severity.HIGH : Threat.Severity.MEDIUM;
            result.addThreat(new Threat(
                    "External Links Found (" + urlCount + ")",
                    "PDF contains " + urlCount + " external URLs that may redirect to malicious sites",
                    severity,
                    Threat.Category.EXTERNAL_LINK
            ));
        }

        // Check for Auto Actions
        Matcher autoMatcher = AUTO_ACTION_PATTERN.matcher(content);
        if (autoMatcher.find()) {
            result.setHasSuspiciousCommands(true);
            result.addThreat(new Threat(
                    "Auto Action Detected",
                    "PDF contains automatic actions that execute when the document is opened",
                    Threat.Severity.HIGH,
                    Threat.Category.AUTO_ACTION
            ));
        }

        // Check for Launch Actions
        Matcher launchMatcher = LAUNCH_PATTERN.matcher(content);
        if (launchMatcher.find()) {
            result.setHasSuspiciousCommands(true);
            result.addThreat(new Threat(
                    "Launch Action Detected",
                    "PDF contains launch actions that can execute external programs",
                    Threat.Severity.CRITICAL,
                    Threat.Category.AUTO_ACTION
            ));
        }

        // Check for Embedded Files
        Matcher embedMatcher = EMBEDDED_FILE_PATTERN.matcher(content);
        if (embedMatcher.find()) {
            result.setHasHiddenFiles(true);
            result.addThreat(new Threat(
                    "Embedded File Detected",
                    "PDF contains embedded files that may contain malicious payloads",
                    Threat.Severity.MEDIUM,
                    Threat.Category.HIDDEN_FILE
            ));
        }

        // Check for Encryption
        Matcher encryptMatcher = ENCRYPT_PATTERN.matcher(content);
        if (encryptMatcher.find()) {
            result.addThreat(new Threat(
                    "Encrypted Content",
                    "PDF contains encrypted sections which may hide malicious content",
                    Threat.Severity.MEDIUM,
                    Threat.Category.ENCRYPTED_CONTENT
            ));
        }

        // Check for Suspicious Code Patterns
        Matcher suspiciousMatcher = SUSPICIOUS_PATTERN.matcher(content);
        if (suspiciousMatcher.find()) {
            result.setHasMaliciousContent(true);
            result.addThreat(new Threat(
                    "Suspicious Code Pattern",
                    "PDF contains code patterns commonly used in exploits",
                    Threat.Severity.CRITICAL,
                    Threat.Category.MALICIOUS_CONTENT
            ));
        }

        // Analyze object count for anomalies
        Matcher objMatcher = OBJECT_PATTERN.matcher(content);
        int objectCount = 0;
        while (objMatcher.find()) objectCount++;

        Matcher streamMatcher = STREAM_PATTERN.matcher(content);
        int streamCount = 0;
        while (streamMatcher.find()) streamCount++;

        // High stream-to-object ratio may indicate obfuscation
        if (objectCount > 0 && streamCount > objectCount * 2) {
            result.addThreat(new Threat(
                    "Unusual Structure",
                    "PDF has an unusual object/stream ratio which may indicate obfuscation",
                    Threat.Severity.LOW,
                    Threat.Category.OBFUSCATION
            ));
        }

        // Check PDF header
        if (!content.startsWith("%PDF-")) {
            result.addThreat(new Threat(
                    "Invalid PDF Header",
                    "File does not start with a valid PDF header - may be a disguised file",
                    Threat.Severity.HIGH,
                    Threat.Category.FAKE_EXTENSION
            ));
        }
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
