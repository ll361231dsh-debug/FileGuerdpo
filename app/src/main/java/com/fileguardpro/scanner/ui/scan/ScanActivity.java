package com.fileguardpro.scanner.ui.scan;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.fileguardpro.scanner.R;
import com.fileguardpro.scanner.model.ScanResult;
import com.fileguardpro.scanner.ui.report.ThreatReportActivity;
import com.fileguardpro.scanner.viewmodel.ScanViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ScanActivity extends AppCompatActivity {

    private ScanViewModel viewModel;
    private ProgressBar progressBar;
    private TextView tvStatus;
    private TextView tvFileName;
    private View scanAnimationView;
    private Button btnCancel;

    private Uri fileUri;
    private String fileName;
    private String mimeType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        viewModel = new ViewModelProvider(this).get(ScanViewModel.class);

        initViews();
        handleIntent();
        observeViewModel();
    }

    private void initViews() {
        progressBar = findViewById(R.id.progress_scan);
        tvStatus = findViewById(R.id.tv_scan_status);
        tvFileName = findViewById(R.id.tv_file_name);
        scanAnimationView = findViewById(R.id.scan_animation_view);
        btnCancel = findViewById(R.id.btn_cancel);

        btnCancel.setOnClickListener(v -> finish());
    }

    private void handleIntent() {
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        String action = intent.getAction();
        mimeType = intent.getType();

        if (Intent.ACTION_VIEW.equals(action)) {
            fileUri = intent.getData();
        } else if (Intent.ACTION_SEND.equals(action)) {
            fileUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        }

        if (fileUri == null) {
            Toast.makeText(this, "No file received", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fileName = getFileName(fileUri);
        if (mimeType == null) {
            mimeType = getContentResolver().getType(fileUri);
        }

        tvFileName.setText(fileName);
        startScan();
    }

    private void startScan() {
        tvStatus.setText("Scanning...");
        progressBar.setVisibility(View.VISIBLE);
        scanAnimationView.setVisibility(View.VISIBLE);
        viewModel.startScan(this, fileUri, fileName, mimeType);
    }

    private void observeViewModel() {
        viewModel.getIsScanning().observe(this, scanning -> {
            if (scanning != null && !scanning) {
                progressBar.setVisibility(View.GONE);
                scanAnimationView.setVisibility(View.GONE);
            }
        });

        viewModel.getScanResult().observe(this, result -> {
            if (result != null) {
                tvStatus.setText("Scan Complete");
                navigateToReport(result);
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                tvStatus.setText("Error: " + error);
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void navigateToReport(ScanResult result) {
        Intent intent = new Intent(this, ThreatReportActivity.class);
        intent.putExtra("scan_result_id", result.getId());
        intent.putExtra("file_name", result.getFileName());
        intent.putExtra("file_type", result.getFileType());
        intent.putExtra("file_size", result.getFileSize());
        intent.putExtra("risk_score", result.getRiskScore());
        intent.putExtra("risk_level", result.getRiskLevel());
        intent.putExtra("has_macro", result.isHasMacro());
        intent.putExtra("has_external_links", result.isHasExternalLinks());
        intent.putExtra("has_suspicious_commands", result.isHasSuspiciousCommands());
        intent.putExtra("has_hidden_files", result.isHasHiddenFiles());
        intent.putExtra("has_javascript", result.isHasJavaScript());
        intent.putExtra("has_malicious_content", result.isHasMaliciousContent());
        intent.putExtra("file_uri", fileUri.toString());
        startActivity(intent);
        finish();
    }

    private String getFileName(Uri uri) {
        String name = "Unknown";
        try {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex >= 0 && cursor.moveToFirst()) {
                    name = cursor.getString(nameIndex);
                }
                cursor.close();
            }
        } catch (Exception e) {
            // Fallback to URI path
            String path = uri.getPath();
            if (path != null) {
                name = path.substring(path.lastIndexOf('/') + 1);
            }
        }
        return name;
    }
}
