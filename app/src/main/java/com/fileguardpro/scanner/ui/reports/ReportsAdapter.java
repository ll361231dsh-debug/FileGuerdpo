package com.fileguardpro.scanner.ui.reports;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.fileguardpro.scanner.R;

import java.util.List;

public class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.VH> {

    private List<String> items;

    public ReportsAdapter(List<String> items) {
        this.items = items;
    }

    public void setItems(List<String> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        String title = items.get(position);
        holder.title.setText(title);
        holder.summary.setText("Threat: Demo - High");
        holder.time.setText("2026-06-19 12:00");
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, summary, time;

        public VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_report_title);
            summary = itemView.findViewById(R.id.tv_report_summary);
            time = itemView.findViewById(R.id.tv_report_time);
        }
    }
}
