package com.fileguardpro.scanner.model;

public class Threat {

    public enum Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum Category {
        MACRO, EXTERNAL_LINK, SUSPICIOUS_COMMAND, HIDDEN_FILE,
        JAVASCRIPT, MALICIOUS_CONTENT, STEGANOGRAPHY, FAKE_EXTENSION,
        EMBEDDED_EXECUTABLE, DDE_EXPLOIT, TEMPLATE_INJECTION,
        POWERSHELL, BASE64_ENCODED, OBFUSCATION, AUTO_ACTION,
        ENCRYPTED_CONTENT, EXIF_ANOMALY, PAYLOAD_DETECTED
    }

    private String name;
    private String description;
    private Severity severity;
    private Category category;
    private String details;
    private int impactScore;

    public Threat() {}

    public Threat(String name, String description, Severity severity, Category category) {
        this.name = name;
        this.description = description;
        this.severity = severity;
        this.category = category;
        this.impactScore = calculateImpactScore(severity);
    }

    private int calculateImpactScore(Severity severity) {
        switch (severity) {
            case CRITICAL: return 25;
            case HIGH: return 20;
            case MEDIUM: return 15;
            case LOW: return 10;
            default: return 5;
        }
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public int getImpactScore() { return impactScore; }
    public void setImpactScore(int impactScore) { this.impactScore = impactScore; }
}
