package com.fileguardpro.scanner.engine;

import com.fileguardpro.scanner.model.ScanResult;
import com.fileguardpro.scanner.model.Threat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ThreatDetectionEngine {

    public static final String RISK_SAFE = "Safe";
    public static final String RISK_LOW = "Low Risk";
    public static final String RISK_SUSPICIOUS = "Suspicious";
    public static final String RISK_DANGEROUS = "Dangerous";
    public static final String RISK_CRITICAL = "Critical";

    // Known malicious signatures (hash patterns)
    private static final Set<String> KNOWN_SIGNATURES = new HashSet<>(Arrays.asList(
            "4d5a90000300000004000000ffff0000",  // PE executable header
            "d0cf11e0a1b11ae1",                    // OLE compound file
            "504b030414000600",                    // OOXML with macros indicator
            "7b5c727466315c616e7369",              // RTF with potential exploit
            "255044462d312e"                        // PDF header
    ));

    // Suspicious keywords for heuristic detection
    private static final List<String> HEURISTIC_KEYWORDS = Arrays.asList(
            "AutoOpen", "Auto_Open", "Document_Open", "Workbook_Open",
            "Shell", "WScript", "CreateObject", "GetObject",
            "ADODB.Stream", "Scripting.FileSystemObject",
            "Microsoft.XMLHTTP", "Msxml2.XMLHTTP",
            "powershell", "cmd /c", "bitsadmin",
            "certutil", "mshta", "regsvr32",
            "rundll32", "wmic", "cmstp"
    );

    @Inject
    public ThreatDetectionEngine() {}

    public ScanResult analyzeFinal(ScanResult result) {
        if (result == null) return new ScanResult();

        // Apply signature-based detection
        applySignatureDetection(result);

        // Apply heuristic detection
        applyHeuristicDetection(result);

        // Apply behavioral analysis
        applyBehavioralAnalysis(result);

        // Recalculate final risk score
        recalculateRiskScore(result);

        return result;
    }

    private void applySignatureDetection(ScanResult result) {
        // Check if file characteristics match known malware patterns
        if (result.isHasMacro() && result.isHasSuspiciousCommands()) {
            boolean alreadyHasThreat = false;
            for (Threat t : result.getThreats()) {
                if ("Macro + Command Execution".equals(t.getName())) {
                    alreadyHasThreat = true;
                    break;
                }
            }
            if (!alreadyHasThreat) {
                result.addThreat(new Threat(
                        "Macro + Command Execution",
                        "File contains macros combined with command execution - high probability of malware",
                        Threat.Severity.CRITICAL,
                        Threat.Category.MALICIOUS_CONTENT
                ));
            }
        }
    }

    private void applyHeuristicDetection(ScanResult result) {
        int suspiciousIndicators = 0;

        if (result.isHasMacro()) suspiciousIndicators++;
        if (result.isHasExternalLinks()) suspiciousIndicators++;
        if (result.isHasSuspiciousCommands()) suspiciousIndicators += 2;
        if (result.isHasHiddenFiles()) suspiciousIndicators += 2;
        if (result.isHasJavaScript()) suspiciousIndicators++;
        if (result.isHasMaliciousContent()) suspiciousIndicators += 3;

        if (suspiciousIndicators >= 4) {
            boolean alreadyHasThreat = false;
            for (Threat t : result.getThreats()) {
                if ("Multiple Threat Indicators".equals(t.getName())) {
                    alreadyHasThreat = true;
                    break;
                }
            }
            if (!alreadyHasThreat) {
                result.addThreat(new Threat(
                        "Multiple Threat Indicators",
                        "File exhibits " + suspiciousIndicators + " suspicious characteristics - likely malicious",
                        Threat.Severity.CRITICAL,
                        Threat.Category.MALICIOUS_CONTENT
                ));
            }
        }
    }

    private void applyBehavioralAnalysis(ScanResult result) {
        // Analyze threat combination patterns
        boolean hasDownloader = false;
        boolean hasExecution = false;
        boolean hasEvasion = false;

        for (Threat threat : result.getThreats()) {
            switch (threat.getCategory()) {
                case EXTERNAL_LINK:
                case TEMPLATE_INJECTION:
                    hasDownloader = true;
                    break;
                case POWERSHELL:
                case SUSPICIOUS_COMMAND:
                case DDE_EXPLOIT:
                    hasExecution = true;
                    break;
                case OBFUSCATION:
                case BASE64_ENCODED:
                case HIDDEN_FILE:
                    hasEvasion = true;
                    break;
            }
        }

        if (hasDownloader && hasExecution) {
            result.addThreat(new Threat(
                    "Dropper Behavior Detected",
                    "File shows dropper/downloader behavior pattern: downloads and executes payload",
                    Threat.Severity.CRITICAL,
                    Threat.Category.MALICIOUS_CONTENT
            ));
        }

        if (hasExecution && hasEvasion) {
            result.addThreat(new Threat(
                    "Evasion Technique Detected",
                    "File uses evasion techniques combined with code execution capabilities",
                    Threat.Severity.HIGH,
                    Threat.Category.OBFUSCATION
            ));
        }
    }

    private void recalculateRiskScore(ScanResult result) {
        int score = 0;
        Set<String> countedThreats = new HashSet<>();

        for (Threat threat : result.getThreats()) {
            String key = threat.getName();
            if (!countedThreats.contains(key)) {
                score += threat.getImpactScore();
                countedThreats.add(key);
            }
        }

        score = Math.min(score, 100);
        result.setRiskScore(score);
        result.setRiskLevel(getRiskLevelFromScore(score));
    }

    public static String getRiskLevelFromScore(int score) {
        if (score == 0) return RISK_SAFE;
        if (score <= 20) return RISK_LOW;
        if (score <= 50) return RISK_SUSPICIOUS;
        if (score <= 75) return RISK_DANGEROUS;
        return RISK_CRITICAL;
    }

    public static int getRiskColor(String riskLevel) {
        switch (riskLevel) {
            case RISK_SAFE: return 0xFF4CAF50;       // Green
            case RISK_LOW: return 0xFF8BC34A;        // Light Green
            case RISK_SUSPICIOUS: return 0xFFFF9800; // Orange
            case RISK_DANGEROUS: return 0xFFFF5722;  // Deep Orange
            case RISK_CRITICAL: return 0xFFF44336;   // Red
            default: return 0xFF9E9E9E;              // Grey
        }
    }
}
