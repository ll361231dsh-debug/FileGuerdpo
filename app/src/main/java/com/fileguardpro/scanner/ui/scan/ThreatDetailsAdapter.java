package com.fileguardpro.scanner.ui.scan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.fileguardpro.scanner.R;
import com.fileguardpro.scanner.model.UiThreat;

import java.util.List;

public class ThreatDetailsAdapter extends RecyclerView.Adapter<ThreatDetailsAdapter.VH> {

    private List<UiThreat> items;

    public ThreatDetailsAdapter(List<UiThreat> items) {
        this.items = items;
    }

    public void setItems(List<UiThreat> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_threat_detail, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        UiThreat t = items.get(position);
        holder.name.setText(t.getName());
        holder.level.setText(t.getLevel());
        holder.path.setText(t.getPath());
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView name, level, path;

        public VH(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_threat_name);
            level = itemView.findViewById(R.id.tv_threat_level);
            path = itemView.findViewById(R.id.tv_threat_path);
        }
    }
}
