package com.fileguardpro.scanner.ui.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.fileguardpro.scanner.R;
import com.fileguardpro.scanner.engine.ThreatDetectionEngine;
import com.fileguardpro.scanner.model.ScanResult;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ScanHistoryAdapter extends ListAdapter<ScanResult, ScanHistoryAdapter.ViewHolder> {

    private static final SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public ScanHistoryAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<ScanResult> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<ScanResult>() {
                @Override
                public boolean areItemsTheSame(@NonNull ScanResult oldItem, @NonNull ScanResult newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull ScanResult oldItem, @NonNull ScanResult newItem) {
                    return oldItem.getFileName().equals(newItem.getFileName()) &&
                            oldItem.getRiskScore() == newItem.getRiskScore();
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_scan_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScanResult result = getItem(position);
        holder.bind(result);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvFileName;
        private final TextView tvFileType;
        private final TextView tvDate;
        private final TextView tvRiskLevel;
        private final View riskColorIndicator;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFileName = itemView.findViewById(R.id.tv_item_file_name);
            tvFileType = itemView.findViewById(R.id.tv_item_file_type);
            tvDate = itemView.findViewById(R.id.tv_item_date);
            tvRiskLevel = itemView.findViewById(R.id.tv_item_risk_level);
            riskColorIndicator = itemView.findViewById(R.id.risk_color_indicator);
        }

        void bind(ScanResult result) {
            tvFileName.setText(result.getFileName());
            tvFileType.setText(result.getFileType());
            tvDate.setText(dateFormat.format(new Date(result.getScanTimestamp())));
            tvRiskLevel.setText(result.getRiskLevel() + " (" + result.getRiskScore() + ")");
            tvRiskLevel.setTextColor(ThreatDetectionEngine.getRiskColor(result.getRiskLevel()));
            riskColorIndicator.setBackgroundColor(ThreatDetectionEngine.getRiskColor(result.getRiskLevel()));
        }
    }
}
