package com.fileguardpro.scanner.engine;

import android.content.Context;
import android.net.Uri;

import com.fileguardpro.scanner.model.ScanResult;
import com.fileguardpro.scanner.model.Threat;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class OfficeScannerEngine implements FileScanner {

    private static final Set<String> OFFICE_MIME_TYPES = new HashSet<>(Arrays.asList(
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-word.document.macroEnabled.12",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-excel.sheet.macroEnabled.12",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/vnd.ms-powerpoint.presentation.macroEnabled.12",
            "application/rtf",
            "application/vnd.oasis.opendocument.text"
    ));

    private static final Set<String> OFFICE_EXTENSIONS = new HashSet<>(Arrays.asList(
            "doc", "docx", "docm", "xls", "xlsx", "xlsm",
            "ppt", "pptx", "pptm", "rtf", "odt"
    ));

    private static final Pattern URL_PATTERN = Pattern.compile(
            "(https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern POWERSHELL_PATTERN = Pattern.compile(
            "(powershell|pwsh|invoke-expression|invoke-webrequest|iex|downloadstring|start-process)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern CMD_PATTERN = Pattern.compile(
            "(cmd\\.exe|cmd /c|command\\.com|wscript|cscript|mshta|regsvr32)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern BASE64_PATTERN = Pattern.compile(
            "[A-Za-z0-9+/]{50,}={0,2}");
    private static final Pattern DDE_PATTERN = Pattern.compile(
            "(DDEAUTO|DDE\\s)", Pattern.CASE_INSENSITIVE);
    private static final Pattern TEMPLATE_INJECTION_PATTERN = Pattern.compile(
            "(attachedTemplate|Target\\s*=\\s*\"https?://)", Pattern.CASE_INSENSITIVE);

    @Inject
    public OfficeScannerEngine() {}

    @Override
    public boolean canHandle(String mimeType, String extension) {
        if (mimeType != null && OFFICE_MIME_TYPES.contains(mimeType.toLowerCase())) return true;
        if (extension != null && OFFICE_EXTENSIONS.contains(extension.toLowerCase())) return true;
        return false;
    }

    @Override
    public ScanResult scan(Context context, Uri fileUri, String fileName, String mimeType) {
        ScanResult result = new ScanResult();
        result.setFileName(fileName);
        result.setFileType(getFileTypeLabel(mimeType, fileName));

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
            if (inputStream == null) return result;

            result.setFileSize(inputStream.available());

            if (isZipBasedFormat(fileName)) {
                scanZipBasedOffice(inputStream, result);
            } else {
                scanLegacyOffice(inputStream, result);
            }

            inputStream.close();
        } catch (Exception e) {
            result.addThreat(new Threat(
                    "Analysis Error",
                    "Could not fully analyze file: " + e.getMessage(),
                    Threat.Severity.LOW,
                    Threat.Category.SUSPICIOUS_COMMAND
            ));
        }

        calculateRiskScore(result);
        return result;
    }

    private boolean isZipBasedFormat(String fileName) {
        if (fileName == null) return false;
        String lower = fileName.toLowerCase();
        return lower.endsWith(".docx") || lower.endsWith(".docm") ||
                lower.endsWith(".xlsx") || lower.endsWith(".xlsm") ||
                lower.endsWith(".pptx") || lower.endsWith(".pptm") ||
                lower.endsWith(".odt");
    }

    private void scanZipBasedOffice(InputStream inputStream, ScanResult result) {
        try {
            ZipInputStream zipStream = new ZipInputStream(inputStream);
            ZipEntry entry;
            boolean hasVbaProject = false;

            while ((entry = zipStream.getNextEntry()) != null) {
                String entryName = entry.getName().toLowerCase();

                // Check for VBA Macros
                if (entryName.contains("vbaproject.bin") || entryName.contains("vba")) {
                    hasVbaProject = true;
                    result.setHasMacro(true);
                    result.addThreat(new Threat(
                            "VBA Macro Detected",
                            "File contains VBA macros which can execute malicious code",
                            Threat.Severity.HIGH,
                            Threat.Category.MACRO
                    ));
                }

                // Check for embedded executables
                if (entryName.endsWith(".exe") || entryName.endsWith(".dll") ||
                        entryName.endsWith(".bat") || entryName.endsWith(".cmd") ||
                        entryName.endsWith(".ps1") || entryName.endsWith(".vbs")) {
                    result.setHasHiddenFiles(true);
                    result.addThreat(new Threat(
                            "Embedded Executable",
                            "File contains embedded executable: " + entry.getName(),
                            Threat.Severity.CRITICAL,
                            Threat.Category.EMBEDDED_EXECUTABLE
                    ));
                }

                // Check for OLE objects
                if (entryName.contains("oleobject") || entryName.contains("embeddings")) {
                    result.addThreat(new Threat(
                            "OLE Object Found",
                            "File contains embedded OLE objects that may contain malicious content",
                            Threat.Severity.MEDIUM,
                            Threat.Category.HIDDEN_FILE
                    ));
                }

                // Analyze XML content
                if (entryName.endsWith(".xml") || entryName.endsWith(".rels")) {
                    analyzeXmlContent(zipStream, entry, result);
                }

                zipStream.closeEntry();
            }
            zipStream.close();

        } catch (Exception e) {
            // Silently handle zip parsing errors
        }
    }

    private void analyzeXmlContent(ZipInputStream zipStream, ZipEntry entry, ScanResult result) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(zipStream));
            StringBuilder content = new StringBuilder();
            String line;
            int lineCount = 0;

            while ((line = reader.readLine()) != null && lineCount < 1000) {
                content.append(line).append("\n");
                lineCount++;
            }

            String xmlContent = content.toString();

            // Check for external links
            Matcher urlMatcher = URL_PATTERN.matcher(xmlContent);
            if (urlMatcher.find()) {
                result.setHasExternalLinks(true);
                result.addThreat(new Threat(
                        "External Link Detected",
                        "File contains external URLs that may be used for data exfiltration",
                        Threat.Severity.MEDIUM,
                        Threat.Category.EXTERNAL_LINK
                ));
            }

            // Check for DDE exploits
            Matcher ddeMatcher = DDE_PATTERN.matcher(xmlContent);
            if (ddeMatcher.find()) {
                result.setHasSuspiciousCommands(true);
                result.addThreat(new Threat(
                        "DDE Exploit Detected",
                        "File contains DDE fields that can execute arbitrary commands",
                        Threat.Severity.CRITICAL,
                        Threat.Category.DDE_EXPLOIT
                ));
            }

            // Check for Remote Template Injection
            Matcher templateMatcher = TEMPLATE_INJECTION_PATTERN.matcher(xmlContent);
            if (templateMatcher.find()) {
                result.addThreat(new Threat(
                        "Remote Template Injection",
                        "File references a remote template which may download malicious content",
                        Threat.Severity.CRITICAL,
                        Threat.Category.TEMPLATE_INJECTION
                ));
            }

            // Check for PowerShell commands
            Matcher psMatcher = POWERSHELL_PATTERN.matcher(xmlContent);
            if (psMatcher.find()) {
                result.setHasSuspiciousCommands(true);
                result.addThreat(new Threat(
                        "PowerShell Command Detected",
                        "File contains PowerShell commands that may execute malicious scripts",
                        Threat.Severity.CRITICAL,
                        Threat.Category.POWERSHELL
                ));
            }

            // Check for CMD commands
            Matcher cmdMatcher = CMD_PATTERN.matcher(xmlContent);
            if (cmdMatcher.find()) {
                result.setHasSuspiciousCommands(true);
                result.addThreat(new Threat(
                        "CMD Command Detected",
                        "File contains command-line commands that may be used for exploitation",
                        Threat.Severity.HIGH,
                        Threat.Category.SUSPICIOUS_COMMAND
                ));
            }

            // Check for Base64 encoded content
            Matcher base64Matcher = BASE64_PATTERN.matcher(xmlContent);
            if (base64Matcher.find()) {
                result.addThreat(new Threat(
                        "Base64 Encoded Content",
                        "File contains Base64 encoded data which may hide malicious payloads",
                        Threat.Severity.MEDIUM,
                        Threat.Category.BASE64_ENCODED
                ));
            }

        } catch (Exception e) {
            // Silently handle
        }
    }

    private void scanLegacyOffice(InputStream inputStream, ScanResult result) {
        try {
            byte[] buffer = new byte[8192];
            StringBuilder content = new StringBuilder();
            int bytesRead;
            int totalRead = 0;

            while ((bytesRead = inputStream.read(buffer)) != -1 && totalRead < 1024 * 1024) {
                content.append(new String(buffer, 0, bytesRead));
                totalRead += bytesRead;
            }

            String fileContent = content.toString();

            // Check for OLE compound file signature
            if (fileContent.length() >= 8) {
                byte[] header = fileContent.substring(0, 8).getBytes();
                if (header[0] == (byte) 0xD0 && header[1] == (byte) 0xCF) {
                    // OLE Compound File detected
                    checkOleContent(fileContent, result);
                }
            }

            // Check for macro indicators in legacy format
            if (fileContent.contains("_VBA_PROJECT") || fileContent.contains("VBAProject") ||
                    fileContent.contains("Attribute VB_")) {
                result.setHasMacro(true);
                result.addThreat(new Threat(
                        "VBA Macro Detected (Legacy)",
                        "Legacy Office file contains VBA macros",
                        Threat.Severity.HIGH,
                        Threat.Category.MACRO
                ));
            }

            // Check for suspicious patterns
            Matcher psMatcher = POWERSHELL_PATTERN.matcher(fileContent);
            if (psMatcher.find()) {
                result.setHasSuspiciousCommands(true);
                result.addThreat(new Threat(
                        "PowerShell Reference",
                        "File references PowerShell which may indicate malicious intent",
                        Threat.Severity.HIGH,
                        Threat.Category.POWERSHELL
                ));
            }

            Matcher urlMatcher = URL_PATTERN.matcher(fileContent);
            if (urlMatcher.find()) {
                result.setHasExternalLinks(true);
            }

        } catch (Exception e) {
            // Silently handle
        }
    }

    private void checkOleContent(String content, ScanResult result) {
        if (content.contains("\\x00O\\x00b\\x00j\\x00e\\x00c\\x00t")) {
            result.addThreat(new Threat(
                    "OLE Object Stream",
                    "File contains OLE object streams that may embed malicious content",
                    Threat.Severity.MEDIUM,
                    Threat.Category.HIDDEN_FILE
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

    private String getFileTypeLabel(String mimeType, String fileName) {
        if (fileName != null) {
            String lower = fileName.toLowerCase();
            if (lower.endsWith(".docx") || lower.endsWith(".doc") || lower.endsWith(".docm"))
                return "Microsoft Word";
            if (lower.endsWith(".xlsx") || lower.endsWith(".xls") || lower.endsWith(".xlsm"))
                return "Microsoft Excel";
            if (lower.endsWith(".pptx") || lower.endsWith(".ppt") || lower.endsWith(".pptm"))
                return "Microsoft PowerPoint";
            if (lower.endsWith(".rtf")) return "Rich Text Format";
            if (lower.endsWith(".odt")) return "OpenDocument Text";
        }
        return "Office Document";
    }
}
