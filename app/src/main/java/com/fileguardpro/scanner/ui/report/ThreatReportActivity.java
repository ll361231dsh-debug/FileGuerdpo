package com.fileguardpro.scanner.ui.report;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import com.fileguardpro.scanner.R;
import com.fileguardpro.scanner.engine.ThreatDetectionEngine;
import com.fileguardpro.scanner.ui.scan.RiskIndicatorView;
import com.fileguardpro.scanner.viewmodel.ScanViewModel;
import com.google.android.material.chip.Chip;

import java.io.File;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ThreatReportActivity extends AppCompatActivity {

    private ScanViewModel viewModel;

    private RiskIndicatorView riskIndicator;
    private TextView tvRiskLevel;
    private TextView tvRiskScore;
    private TextView tvFileName;
    private TextView tvFileType;
    private TextView tvFileSize;
    private Chip chipMacro;
    private Chip chipLinks;
    private Chip chipCommands;
    private Chip chipHidden;
    private Chip chipJavascript;
    private Chip chipMalicious;
    private Button btnOpen;
    private Button btnCancel;
    private Button btnQuarantine;
    private Button btnDelete;

    private String fileUriString;
    private String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_threat_report);

        viewModel = new ViewModelProvider(this).get(ScanViewModel.class);

        initViews();
        loadReportData();
        setupButtons();
    }

    private void initViews() {
        riskIndicator = findViewById(R.id.risk_indicator);
        tvRiskLevel = findViewById(R.id.tv_risk_level);
        tvRiskScore = findViewById(R.id.tv_risk_score);
        tvFileName = findViewById(R.id.tv_file_name);
        tvFileType = findViewById(R.id.tv_file_type);
        tvFileSize = findViewById(R.id.tv_file_size);
        chipMacro = findViewById(R.id.chip_macro);
        chipLinks = findViewById(R.id.chip_links);
        chipCommands = findViewById(R.id.chip_commands);
        chipHidden = findViewById(R.id.chip_hidden);
        chipJavascript = findViewById(R.id.chip_javascript);
        chipMalicious = findViewById(R.id.chip_malicious);
        btnOpen = findViewById(R.id.btn_open_file);
        btnCancel = findViewById(R.id.btn_cancel);
        btnQuarantine = findViewById(R.id.btn_quarantine);
        btnDelete = findViewById(R.id.btn_delete);
    }

    private void loadReportData() {
        Intent intent = getIntent();
        fileName = intent.getStringExtra("file_name");
        String fileType = intent.getStringExtra("file_type");
        long fileSize = intent.getLongExtra("file_size", 0);
        int riskScore = intent.getIntExtra("risk_score", 0);
        String riskLevel = intent.getStringExtra("risk_level");
        boolean hasMacro = intent.getBooleanExtra("has_macro", false);
        boolean hasLinks = intent.getBooleanExtra("has_external_links", false);
        boolean hasCommands = intent.getBooleanExtra("has_suspicious_commands", false);
        boolean hasHidden = intent.getBooleanExtra("has_hidden_files", false);
        boolean hasJs = intent.getBooleanExtra("has_javascript", false);
        boolean hasMalicious = intent.getBooleanExtra("has_malicious_content", false);
        fileUriString = intent.getStringExtra("file_uri");

        tvFileName.setText(fileName);
        tvFileType.setText(fileType);
        tvFileSize.setText(formatFileSize(fileSize));
        tvRiskScore.setText(riskScore + "/100");
        tvRiskLevel.setText(riskLevel);
        tvRiskLevel.setTextColor(ThreatDetectionEngine.getRiskColor(riskLevel));

        riskIndicator.setRiskScore(riskScore);
        riskIndicator.setRiskColor(ThreatDetectionEngine.getRiskColor(riskLevel));

        // Set chip visibility
        chipMacro.setVisibility(hasMacro ? View.VISIBLE : View.GONE);
        chipLinks.setVisibility(hasLinks ? View.VISIBLE : View.GONE);
        chipCommands.setVisibility(hasCommands ? View.VISIBLE : View.GONE);
        chipHidden.setVisibility(hasHidden ? View.VISIBLE : View.GONE);
        chipJavascript.setVisibility(hasJs ? View.VISIBLE : View.GONE);
        chipMalicious.setVisibility(hasMalicious ? View.VISIBLE : View.GONE);
    }

    private void setupButtons() {
        btnOpen.setOnClickListener(v -> openFile());
        btnCancel.setOnClickListener(v -> finish());
        btnQuarantine.setOnClickListener(v -> quarantineFile());
        btnDelete.setOnClickListener(v -> deleteFile());
    }

    private void openFile() {
        try {
            Uri uri = Uri.parse(fileUriString);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, getIntent().getStringExtra("file_type"));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Open with"));
        } catch (Exception e) {
            Toast.makeText(this, "Cannot open file", Toast.LENGTH_SHORT).show();
        }
    }

    private void quarantineFile() {
        new AlertDialog.Builder(this)
                .setTitle("Quarantine File")
                .setMessage("Move this file to quarantine? It will be isolated from the system.")
                .setPositiveButton("Quarantine", (d, w) -> {
                    Uri uri = Uri.parse(fileUriString);
                    viewModel.quarantineFile(this, uri, fileName);
                    Toast.makeText(this, "File quarantined successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteFile() {
        new AlertDialog.Builder(this)
                .setTitle("Delete File")
                .setMessage("Are you sure you want to delete this file? This action cannot be undone.")
                .setPositiveButton("Delete", (d, w) -> {
                    try {
                        Uri uri = Uri.parse(fileUriString);
                        getContentResolver().delete(uri, null, null);
                        Toast.makeText(this, "File deleted", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "Cannot delete file", Toast.LENGTH_SHORT).show();
                    }
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        else if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        else return String.format("%.1f MB", size / (1024.0 * 1024.0));
    }
}
