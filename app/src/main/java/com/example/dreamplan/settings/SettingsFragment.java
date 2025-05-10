package com.example.dreamplan.settings;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

//import com.bumptech.glide.Glide;
import com.example.dreamplan.AuthActivity;
import com.example.dreamplan.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsFragment extends Fragment {
    private SwitchMaterial switchDarkMode;
    private TextView tvUsername, tvEmail, tvVersion;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize views
        switchDarkMode = view.findViewById(R.id.switch_dark_mode);
        tvUsername = view.findViewById(R.id.tv_username);
        tvEmail = view.findViewById(R.id.tv_email);
        tvVersion = view.findViewById(R.id.tv_version);

        // Load settings
        loadUserProfile();
        setupDarkModeSwitch();
        setupVersionInfo();
        setupLogout(view);

        return view;
    }

    private void loadUserProfile() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        tvUsername.setText(prefs.getString("username", "User Name"));
        tvEmail.setText(prefs.getString("email", "user@example.com"));
    }

    private void setupDarkModeSwitch() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        switchDarkMode.setChecked(isDarkMode);

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("dark_mode", isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES
                            : AppCompatDelegate.MODE_NIGHT_NO
            );
            requireActivity().recreate();
        });
    }

    private void setupVersionInfo() {
        try {
            PackageInfo pInfo = requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0);
            tvVersion.setText(getString(R.string.version_format, pInfo.versionName));
        } catch (PackageManager.NameNotFoundException e) {
            tvVersion.setVisibility(View.GONE);
        }
    }

    private void setupLogout(View view) {
        view.findViewById(R.id.tv_logout).setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Log Out")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Log Out", (dialog, which) -> performLogout())
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void performLogout() {
        // Clear user preferences
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();

        // Navigate to login screen
        startActivity(new Intent(requireActivity(), AuthActivity.class));
        requireActivity().finish();
    }
}