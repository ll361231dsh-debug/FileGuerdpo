package com.fileguardpro.scanner.ui.file;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.fileguardpro.scanner.databinding.FragmentFileScannerBinding;
import com.fileguardpro.scanner.model.UiThreat;
import com.fileguardpro.scanner.ui.scan.ThreatDetailsAdapter;

import java.util.ArrayList;
import java.util.List;

public class FileScannerFragment extends Fragment {

    private FragmentFileScannerBinding binding;
    private ThreatDetailsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFileScannerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.rvThreatDetails.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ThreatDetailsAdapter(new ArrayList<>());
        binding.rvThreatDetails.setAdapter(adapter);

        binding.btnSelectFile.setOnClickListener(v -> {
            // Placeholder: file selection to be wired to existing scanner logic
            Toast.makeText(requireContext(), "Select File (UI placeholder)", Toast.LENGTH_SHORT).show();
        });

        binding.btnExportReport.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Export Report (UI placeholder)", Toast.LENGTH_SHORT).show();
        });

        // placeholder data for UI preview
        List<UiThreat> demo = new ArrayList<>();
        demo.add(new UiThreat("Suspicious APK", "High", "/storage/emulated/0/Download/example.apk"));
        demo.add(new UiThreat("Macro in DOCX", "Medium", "/storage/emulated/0/Documents/test.docx"));
        adapter.setItems(demo);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
