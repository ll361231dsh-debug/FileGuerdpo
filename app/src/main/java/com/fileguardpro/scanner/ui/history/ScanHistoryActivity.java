package com.fileguardpro.scanner.ui.history;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fileguardpro.scanner.R;
import com.fileguardpro.scanner.viewmodel.HistoryViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ScanHistoryActivity extends AppCompatActivity {

    private HistoryViewModel viewModel;
    private RecyclerView recyclerView;
    private ScanHistoryAdapter adapter;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_history);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Scan History");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        viewModel = new ViewModelProvider(this).get(HistoryViewModel.class);

        initViews();
        observeData();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.rv_scan_history);
        tvEmpty = findViewById(R.id.tv_empty);

        adapter = new ScanHistoryAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void observeData() {
        viewModel.getAllScans().observe(this, scans -> {
            if (scans != null && !scans.isEmpty()) {
                adapter.submitList(scans);
                recyclerView.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.GONE);
            } else {
                recyclerView.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_clear) {
            new AlertDialog.Builder(this)
                    .setTitle("Clear History")
                    .setMessage("Delete all scan history?")
                    .setPositiveButton("Clear", (d, w) -> viewModel.clearAll())
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
