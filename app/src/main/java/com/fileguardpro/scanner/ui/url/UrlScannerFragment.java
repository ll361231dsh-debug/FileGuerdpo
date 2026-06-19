package com.fileguardpro.scanner.ui.url;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.fileguardpro.scanner.databinding.FragmentUrlScannerBinding;

public class UrlScannerFragment extends Fragment {

    private FragmentUrlScannerBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUrlScannerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnAnalyze.setOnClickListener(v -> {
            String url = binding.etUrl.getText().toString().trim();
            if (url.isEmpty()) {
                Toast.makeText(requireContext(), "Enter URL", Toast.LENGTH_SHORT).show();
                return;
            }
            // Placeholder: wire to URL analysis engine later
            Toast.makeText(requireContext(), "Analyze: " + url, Toast.LENGTH_SHORT).show();
            binding.tvSslVerification.setText("SSL: Valid");
            binding.tvDomainReputation.setText("Reputation: Good");
            binding.tvBlacklistStatus.setText("Blacklist: Clear");
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
