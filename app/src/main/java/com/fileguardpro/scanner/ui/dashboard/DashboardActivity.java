package com.fileguardpro.scanner.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fileguardpro.scanner.R;
import com.fileguardpro.scanner.ui.history.ScanHistoryActivity;
import com.fileguardpro.scanner.ui.history.ScanHistoryAdapter;
import com.fileguardpro.scanner.viewmodel.DashboardViewModel;
import com.google.android.material.card.MaterialCardView;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DashboardActivity extends AppCompatActivity {

    private DashboardViewModel viewModel;
    private TextView tvTotalScans;
    private TextView tvThreatsDetected;
    private TextView tvProtectionStatus;
    private RecyclerView rvRecentScans;
    private ScanHistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        initViews();
        setupRecyclerView();
        observeData();
    }

    private void initViews() {
        tvTotalScans = findViewById(R.id.tv_total_scans);
        tvThreatsDetected = findViewById(R.id.tv_threats_detected);
        tvProtectionStatus = findViewById(R.id.tv_protection_status);
        rvRecentScans = findViewById(R.id.rv_recent_scans);

        MaterialCardView cardHistory = findViewById(R.id.card_history);
        cardHistory.setOnClickListener(v -> {
            startActivity(new Intent(this, ScanHistoryActivity.class));
        });
    }

    private void setupRecyclerView() {
        adapter = new ScanHistoryAdapter();
        rvRecentScans.setLayoutManager(new LinearLayoutManager(this));
        rvRecentScans.setAdapter(adapter);
    }

    private void observeData() {
        viewModel.getTotalScanCount().observe(this, count -> {
            tvTotalScans.setText(String.valueOf(count != null ? count : 0));
        });

        viewModel.getThreatsDetectedCount().observe(this, count -> {
            tvThreatsDetected.setText(String.valueOf(count != null ? count : 0));
            if (count != null && count > 0) {
                tvProtectionStatus.setText("Threats Detected");
                tvProtectionStatus.setTextColor(getColor(R.color.risk_dangerous));
            } else {
                tvProtectionStatus.setText("Protected");
                tvProtectionStatus.setTextColor(getColor(R.color.risk_safe));
            }
        });

        viewModel.getRecentScans().observe(this, scans -> {
            if (scans != null) {
                adapter.submitList(scans);
            }
        });
    }
}
