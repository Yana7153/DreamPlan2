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
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
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
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
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
//    private static final int PICK_IMAGE_REQUEST = 1;
    private SwitchMaterial switchDarkMode;
    private TextView tvUsername, tvEmail, tvVersion;
    private ShapeableImageView profileImage;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private StorageReference storageRef;
    private static final int PICK_IMAGE_REQUEST = 101;
    private static final int MAX_IMAGE_SIZE_KB = 512;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
       // storageRef = FirebaseStorage.getInstance().getReference();
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
        loadLocalProfileImage();

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
                handleImageSelection(imageUri);
            }
        }
    }

//    private void uploadProfilePicture(Uri imageUri) {
//        if (currentUser == null) return;
//
//        ProgressDialog progressDialog = new ProgressDialog(getContext());
//        progressDialog.setTitle("Uploading...");
//        progressDialog.setCancelable(false);
//        progressDialog.show();
//
//        // Create storage reference
//        StorageReference fileRef = storageRef
//                .child("profile_pictures/" + currentUser.getUid() + ".jpg");
//
//        // Upload the file
//        fileRef.putFile(imageUri)
//                .addOnProgressListener(taskSnapshot -> {
//                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) /
//                            taskSnapshot.getTotalByteCount();
//                    progressDialog.setMessage("Uploaded " + (int) progress + "%");
//                })
//                .addOnSuccessListener(taskSnapshot -> {
//                    // Get download URL
//                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
//                        // Update user profile
//                        UserProfileChangeRequest profileUpdates =
//                                new UserProfileChangeRequest.Builder()
//                                        .setPhotoUri(uri)
//                                        .build();
//
//                        currentUser.updateProfile(profileUpdates)
//                                .addOnCompleteListener(task -> {
//                                    progressDialog.dismiss();
//                                    if (task.isSuccessful()) {
//                                        Glide.with(this)
//                                                .load(uri)
//                                                .circleCrop()
//                                                .into(profileImage);
//                                        Toast.makeText(getContext(),
//                                                "Profile updated!",
//                                                Toast.LENGTH_SHORT).show();
//                                    } else {
//                                        showError("Update failed", task.getException());
//                                    }
//                                });
//                    });
//                })
//                .addOnFailureListener(e -> {
//                    progressDialog.dismiss();
//                    showError("Upload failed", e);
//                });
//    }

//    private void showError(String message, Exception e) {
//        String errorMessage = message;
//        if (e != null) {
//            errorMessage += ": " + e.getMessage();
//            Log.e("SettingsFragment", message, e);
//        }
//        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
//    }
//
//    private void handleProfilePicture(Uri imageUri) {
//        try {
//            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
//            String encoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
//
//            SharedPreferences prefs = requireContext().getSharedPreferences("profile", Context.MODE_PRIVATE);
//            prefs.edit().putString("profile_image", encoded).apply();
//
//            profileImage.setImageBitmap(bitmap);
//
//        } catch (IOException e) {
//            Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private void loadLocalProfile() {
//        SharedPreferences prefs = requireContext().getSharedPreferences("profile", Context.MODE_PRIVATE);
//        String encoded = prefs.getString("profile_image", null);
//        if (encoded != null) {
//            byte[] decoded = Base64.decode(encoded, Base64.DEFAULT);
//            Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
//            profileImage.setImageBitmap(bitmap);
//        }
//    }
//
//
//    private void handleProfileImageSelection(Uri imageUri) {
//        // Option 1: Try Firebase Auth profile (works for Google/GitHub accounts)
//        updateFirebaseAuthProfile(imageUri);
//
//        // Option 2: Store locally as fallback
//        saveImageLocally(imageUri);
//    }

    private void updateFirebaseAuthProfile(Uri imageUri) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(imageUri)
                .build();

        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("Profile", "Auth profile updated");
                    }
                });
    }

    private void saveImageLocally(Bitmap bitmap) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);

            String encoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
            SharedPreferences prefs = requireContext()
                    .getSharedPreferences("user_profile", Context.MODE_PRIVATE);
            prefs.edit().putString("profile_image", encoded).apply();

        } catch (Exception e) {
            Log.e("LocalSave", "Failed to save image", e);
        }
    }

    private void loadLocalProfileImage() {
        SharedPreferences prefs = requireContext()
                .getSharedPreferences("user_profile", Context.MODE_PRIVATE);
        String encoded = prefs.getString("profile_image", null);

        if (encoded != null) {
            byte[] decoded = Base64.decode(encoded, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
            profileImage.setImageBitmap(bitmap);
        }
    }

    private void handleImageSelection(Uri imageUri) {
        try {
            Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(
                    requireActivity().getContentResolver(),
                    imageUri
            );

            Bitmap scaledBitmap = scaleBitmap(originalBitmap, 500);

            saveImageLocally(scaledBitmap);

            Glide.with(this)
                    .load(scaledBitmap)
                    .circleCrop()
                    .transition(DrawableTransitionOptions.withCrossFade(200))
                    .into(profileImage);

            updateFirebaseAuthProfile(imageUri);

        } catch (IOException e) {
            Toast.makeText(getContext(),
                    "Failed to process image",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap scaleBitmap(Bitmap bitmap, int maxWidth) {
        try {
            float ratio = (float) maxWidth / bitmap.getWidth();
            int scaledHeight = (int) (bitmap.getHeight() * ratio);

            return Bitmap.createScaledBitmap(
                    bitmap,
                    maxWidth,
                    scaledHeight,
                    true
            );
        } catch (Exception e) {
            Log.e("BitmapScaling", "Error scaling bitmap", e);
            return bitmap;
        }
    }
}