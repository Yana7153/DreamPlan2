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
    private FirebaseAuth mAuth;
    private ShapeableImageView profileImage;
    private StorageReference storageRef;
    private FirebaseUser currentUser;

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

        profileImage = view.findViewById(R.id.profile_image);
        switchDarkMode = view.findViewById(R.id.switch_dark_mode);
        tvUsername = view.findViewById(R.id.tv_username);
        tvEmail = view.findViewById(R.id.tv_email);
        tvVersion = view.findViewById(R.id.tv_version);

        loadUserProfile();
        setupDarkModeSwitch();
        setupVersionInfo();
        setupLogout(view);
        setupEditProfile(view);

        profileImage.setOnClickListener(v -> showImagePicker());

        return view;
    }

    private void loadUserProfile() {
        if (currentUser != null) {
            String displayName = currentUser.getDisplayName();
            String email = currentUser.getEmail();

            tvUsername.setText(displayName != null ? displayName : "User");
            tvEmail.setText(email != null ? email : "No email");

            if (currentUser.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(currentUser.getPhotoUrl())
                        .circleCrop()
                        .into(profileImage);
            }
        }
    }

    private void showImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void setupDarkModeSwitch() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);

        int currentMode = prefs.getInt("dark_mode_pref", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        switchDarkMode.setChecked(isDarkModeEnabled(currentMode));

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int newMode = isChecked ? AppCompatDelegate.MODE_NIGHT_YES
                    : AppCompatDelegate.MODE_NIGHT_NO;

            prefs.edit().putInt("dark_mode_pref", newMode).apply();
            AppCompatDelegate.setDefaultNightMode(newMode);

            requireActivity().recreate();
        });
    }

    private boolean isDarkModeEnabled(int mode) {
        if (mode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
            int currentNightMode = getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK;
            return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
        }
        return mode == AppCompatDelegate.MODE_NIGHT_YES;
    }

    private void setupVersionInfo() {
        try {
            PackageInfo pInfo = requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0);
            tvVersion.setText("Version " + pInfo.versionName);
        } catch (Exception e) {
            tvVersion.setText("Version 1.0.0");
        }
    }

    private void setupEditProfile(View view) {
        view.findViewById(R.id.btn_edit_profile).setOnClickListener(v -> {
            EditProfileFragment editProfileFragment = new EditProfileFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, editProfileFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void setupLogout(View view) {
        view.findViewById(R.id.tv_logout).setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                final InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
                if (inputStream != null) inputStream.close();

                uploadImageToFirebase(imageUri);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Error accessing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Uploading Profile Picture...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        try {
            // Create reference with full path
            StorageReference fileRef = FirebaseStorage.getInstance()
                    .getReference()
                    .child("profile_images")
                    .child(currentUser.getUid() + ".jpg");

            // Check if storage bucket exists (debug only)
            Log.d("StorageDebug", "Bucket: " + fileRef.getBucket());
            Log.d("StorageDebug", "Path: " + fileRef.getPath());

            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("image/jpeg")
                    .build();

            UploadTask uploadTask;

            // Handle HEIC/HEIF conversion if needed
            String mimeType = getContext().getContentResolver().getType(imageUri);
            if ("image/heic".equalsIgnoreCase(mimeType) || "image/heif".equalsIgnoreCase(mimeType)) {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageUri);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                uploadTask = fileRef.putBytes(baos.toByteArray(), metadata);
            } else {
                uploadTask = fileRef.putFile(imageUri, metadata);
            }

            uploadTask.addOnProgressListener(taskSnapshot -> {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                progressDialog.setMessage("Uploaded " + (int) progress + "%");
            }).addOnSuccessListener(taskSnapshot -> {
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setPhotoUri(uri)
                            .build();

                    currentUser.updateProfile(profileUpdates)
                            .addOnCompleteListener(task -> {
                                progressDialog.dismiss();
                                if (task.isSuccessful()) {
                                    Glide.with(SettingsFragment.this)
                                            .load(uri)
                                            .circleCrop()
                                            .into(profileImage);
                                    Toast.makeText(getContext(), "Profile updated!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(),
                                            "Profile update failed: " + task.getException().getMessage(),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                });
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Log.e("UploadError", "Full error: ", e);

                if (e instanceof StorageException) {
                    StorageException se = (StorageException) e;
                    Log.e("StorageError", "Error Code: " + se.getErrorCode());
                    Log.e("StorageError", "HTTP Result: " + se.getHttpResultCode());
                }

                Toast.makeText(getContext(),
                        "Upload failed. Please check your internet connection and try again.",
                        Toast.LENGTH_LONG).show();
            });

        } catch (IOException e) {
            progressDialog.dismiss();
            Toast.makeText(getContext(), "Error processing image", Toast.LENGTH_SHORT).show();
            Log.e("ImageError", "Error: ", e);
        }
    }

    private void handleUploadTask(UploadTask uploadTask, ProgressDialog progressDialog) {
        // Add progress listener
        uploadTask.addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            progressDialog.setMessage("Uploaded " + (int) progress + "%");
        });

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // Get download URL after successful upload
            taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                // Update user profile
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setPhotoUri(uri)
                        .build();

                currentUser.updateProfile(profileUpdates)
                        .addOnCompleteListener(task -> {
                            progressDialog.dismiss();
                            if (task.isSuccessful()) {
                                // Update UI
                                Glide.with(SettingsFragment.this)
                                        .load(uri)
                                        .circleCrop()
                                        .into(profileImage);
                                Toast.makeText(getContext(), "Profile updated!", Toast.LENGTH_SHORT).show();
                            } else {
                                showUploadError("Profile update failed", task.getException());
                            }
                        });
            });
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            showUploadError("Upload failed", e);
        });
    }

    private void showUploadError(String message, Exception e) {
        String errorMsg = message;
        if (e != null) {
            errorMsg += ": " + e.getMessage();
            Log.e("SettingsFragment", message, e);

            // Special handling for Firebase Storage errors
            if (e instanceof StorageException) {
                StorageException se = (StorageException) e;
                switch (se.getErrorCode()) {
                    case StorageException.ERROR_OBJECT_NOT_FOUND:
                        errorMsg = "Storage location not found";
                        break;
                    case StorageException.ERROR_QUOTA_EXCEEDED:
                        errorMsg = "Storage quota exceeded";
                        break;
                }
            }
        }
        Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
    }

    private String getErrorMessage(int errorCode) {
        switch (errorCode) {
            case StorageException.ERROR_OBJECT_NOT_FOUND: return "Storage path doesn't exist";
            case StorageException.ERROR_QUOTA_EXCEEDED: return "Storage quota exceeded";
            default: return "Unknown error";
        }
    }
}