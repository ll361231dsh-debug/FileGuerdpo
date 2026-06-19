package com.fileguardpro.scanner.ui.reports;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.fileguardpro.scanner.databinding.FragmentReportsBinding;

import java.util.ArrayList;

public class ReportsFragment extends Fragment {

    private FragmentReportsBinding binding;
    private ReportsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentReportsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.rvReports.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ReportsAdapter(new ArrayList<>());
        binding.rvReports.setAdapter(adapter);

        // placeholder demo data
        // TODO: wire to real report models
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
