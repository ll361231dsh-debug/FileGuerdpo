package com.fileguardpro.scanner.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.fileguardpro.scanner.database.Converters;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "scan_results")
@TypeConverters(Converters.class)
public class ScanResult {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String fileName;
    private String fileType;
    private long fileSize;
    private String filePath;
    private long scanTimestamp;
    private int riskScore;
    private String riskLevel;
    private boolean hasMacro;
    private boolean hasExternalLinks;
    private boolean hasSuspiciousCommands;
    private boolean hasHiddenFiles;
    private boolean hasJavaScript;
    private boolean hasMaliciousContent;
    private List<Threat> threats;

    public ScanResult() {
        this.threats = new ArrayList<>();
        this.scanTimestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public long getScanTimestamp() { return scanTimestamp; }
    public void setScanTimestamp(long scanTimestamp) { this.scanTimestamp = scanTimestamp; }

    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public boolean isHasMacro() { return hasMacro; }
    public void setHasMacro(boolean hasMacro) { this.hasMacro = hasMacro; }

    public boolean isHasExternalLinks() { return hasExternalLinks; }
    public void setHasExternalLinks(boolean hasExternalLinks) { this.hasExternalLinks = hasExternalLinks; }

    public boolean isHasSuspiciousCommands() { return hasSuspiciousCommands; }
    public void setHasSuspiciousCommands(boolean hasSuspiciousCommands) { this.hasSuspiciousCommands = hasSuspiciousCommands; }

    public boolean isHasHiddenFiles() { return hasHiddenFiles; }
    public void setHasHiddenFiles(boolean hasHiddenFiles) { this.hasHiddenFiles = hasHiddenFiles; }

    public boolean isHasJavaScript() { return hasJavaScript; }
    public void setHasJavaScript(boolean hasJavaScript) { this.hasJavaScript = hasJavaScript; }

    public boolean isHasMaliciousContent() { return hasMaliciousContent; }
    public void setHasMaliciousContent(boolean hasMaliciousContent) { this.hasMaliciousContent = hasMaliciousContent; }

    public List<Threat> getThreats() { return threats; }
    public void setThreats(List<Threat> threats) { this.threats = threats; }

    public void addThreat(Threat threat) {
        if (this.threats == null) this.threats = new ArrayList<>();
        this.threats.add(threat);
    }

    public String getFormattedFileSize() {
        if (fileSize < 1024) return fileSize + " B";
        else if (fileSize < 1024 * 1024) return String.format("%.1f KB", fileSize / 1024.0);
        else return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
    }
}
