package com.example.dreamplan.settings;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.dreamplan.AuthActivity;
import com.example.dreamplan.MainActivity;
import com.example.dreamplan.R;
import com.example.dreamplan.database.AuthManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class SettingsFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private SwitchMaterial switchDarkMode;
    private TextView tvUsername, tvEmail, tvVersion;
    private ShapeableImageView profileImage;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private StorageReference storageRef;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        storageRef = FirebaseStorage.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize views
        profileImage = view.findViewById(R.id.profile_image);
        switchDarkMode = view.findViewById(R.id.switch_dark_mode);
        tvUsername = view.findViewById(R.id.tv_username);
        tvEmail = view.findViewById(R.id.tv_email);
        tvVersion = view.findViewById(R.id.tv_version);

        // Setup components
        loadUserProfile();
        setupDarkModeSwitch();
        setupVersionInfo();
        setupLogout(view);
        setupEditProfile(view);

        // Profile image click listener
        profileImage.setOnClickListener(v -> openImagePicker());

        return view;
    }

    private void loadUserProfile() {
        if (currentUser != null) {
            tvUsername.setText(currentUser.getDisplayName() != null ?
                    currentUser.getDisplayName() : "User");
            tvEmail.setText(currentUser.getEmail() != null ?
                    currentUser.getEmail() : "No email");

            if (currentUser.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(currentUser.getPhotoUrl())
                        .circleCrop()
                        .into(profileImage);
            }
        }
    }

    private void setupDarkModeSwitch() {
        int currentNightMode = getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        boolean isDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES;
        switchDarkMode.setChecked(isDarkMode);

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int newMode = isChecked ?
                    AppCompatDelegate.MODE_NIGHT_YES :
                    AppCompatDelegate.MODE_NIGHT_NO;
            AppCompatDelegate.setDefaultNightMode(newMode);
            requireActivity().recreate();
        });
    }

    private void setupVersionInfo() {
        try {
            String versionName = requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0).versionName;
            tvVersion.setText("Version " + versionName);
        } catch (Exception e) {
            tvVersion.setText("Version 1.0.0");
        }
    }

    private void setupLogout(View view) {
        view.findViewById(R.id.tv_logout).setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Log Out")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Log Out", (dialog, which) -> {
                        mAuth.signOut();
                        startActivity(new Intent(requireActivity(), AuthActivity.class));
                        requireActivity().finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void setupEditProfile(View view) {
        view.findViewById(R.id.btn_edit_profile).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new EditProfileFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                uploadProfilePicture(imageUri);
            }
        }
    }

    private void uploadProfilePicture(Uri imageUri) {
        if (currentUser == null) return;

        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Uploading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Create storage reference
        StorageReference fileRef = storageRef
                .child("profile_pictures/" + currentUser.getUid() + ".jpg");

        // Upload the file
        fileRef.putFile(imageUri)
                .addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) /
                            taskSnapshot.getTotalByteCount();
                    progressDialog.setMessage("Uploaded " + (int) progress + "%");
                })
                .addOnSuccessListener(taskSnapshot -> {
                    // Get download URL
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Update user profile
                        UserProfileChangeRequest profileUpdates =
                                new UserProfileChangeRequest.Builder()
                                        .setPhotoUri(uri)
                                        .build();

                        currentUser.updateProfile(profileUpdates)
                                .addOnCompleteListener(task -> {
                                    progressDialog.dismiss();
                                    if (task.isSuccessful()) {
                                        Glide.with(this)
                                                .load(uri)
                                                .circleCrop()
                                                .into(profileImage);
                                        Toast.makeText(getContext(),
                                                "Profile updated!",
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        showError("Update failed", task.getException());
                                    }
                                });
                    });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    showError("Upload failed", e);
                });
    }

    private void showError(String message, Exception e) {
        String errorMessage = message;
        if (e != null) {
            errorMessage += ": " + e.getMessage();
            Log.e("SettingsFragment", message, e);
        }
        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
    }
}