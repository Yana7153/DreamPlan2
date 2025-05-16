package com.example.dreamplan;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import com.example.dreamplan.database.AuthManager;

@AndroidEntryPoint
public class LoginFragment extends Fragment {
    @Inject
    AuthManager authManager;

    @Inject
    FirebaseAuth mAuth;

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvSignUp, tvForgotPassword;
    private ProgressDialog progressDialog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        tvForgotPassword = view.findViewById(R.id.tv_forgot_password);
        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        btnLogin = view.findViewById(R.id.btn_login);
        tvSignUp = view.findViewById(R.id.tv_sign_up);

        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
        btnLogin.setOnClickListener(v -> attemptLogin());
        tvSignUp.setOnClickListener(v -> goToSignUp());

        return view;
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please fill all fields");
            return;
        }

        showProgressDialog("Logging in...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    dismissProgressDialog();

                    if (!isAdded() || requireActivity().isFinishing()) return;

                    if (task.isSuccessful()) {
                        handleSuccessfulLogin();
                    } else {
                        showError("Login failed: " + task.getException().getMessage());
                    }
                });
    }

    private void goToSignUp() {
        if (isAdded() && getActivity() != null) {
            ((AuthActivity) getActivity()).loadFragment(new SignUpFragment());
        }
    }

    private void showForgotPasswordDialog() {
        try {
            // Safely get context
            Context context = getContext();
            if (context == null) {
                context = requireContext(); // For fragments
            }

            // Create dialog with proper context
            AlertDialog.Builder builder = new AlertDialog.Builder(
                    context,
                    R.style.ForgotPasswordDialogTheme
            );

            // Inflate view safely
            View dialogView = LayoutInflater.from(context)
                    .inflate(R.layout.dialog_forgot_password, null);

            // Find views with null checks
            TextInputLayout emailLayout = dialogView.findViewById(R.id.email_input_layout);
            EditText emailInput = dialogView.findViewById(R.id.email_input);
            Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
            Button btnSubmit = dialogView.findViewById(R.id.btn_submit);

            if (emailLayout == null || emailInput == null ||
                    btnCancel == null || btnSubmit == null) {
                throw new IllegalStateException("Dialog layout missing required views");
            }

            builder.setView(dialogView);
            AlertDialog dialog = builder.create();

            // Set window attributes safely
            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawableResource(R.drawable.dialog_rounded_background);
            }

            btnCancel.setOnClickListener(v -> dialog.dismiss());

            btnSubmit.setOnClickListener(v -> {
                String email = emailInput.getText().toString().trim();
                if (TextUtils.isEmpty(email)) {
                    emailLayout.setError("Email cannot be empty");
                    return;
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailLayout.setError("Please enter a valid email");
                    return;
                }
                sendPasswordResetEmail(email);
                dialog.dismiss();
            });

            dialog.show();

        } catch (Exception e) {
            Log.e("ForgotPassword", "Dialog error: " + e.getMessage());
            Toast.makeText(getContext(), "Error showing password dialog", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendPasswordResetEmail(String email) {
        showProgressDialog("Sending reset link...");

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    dismissProgressDialog();

                    if (!isAdded() || requireActivity().isFinishing()) return;

                    if (task.isSuccessful()) {
                        showSuccess("Reset link sent to " + email);
                    } else {
                        showError("Failed to send reset link: " + task.getException().getMessage());
                    }
                });
    }


    private void handleSuccessfulLogin() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            if (user.isEmailVerified()) {
                authManager.initializeFirestoreUser(user.getUid(), user.getEmail());
                ((AuthActivity) requireActivity()).startMainActivity(user);
            } else {
                showError("Please verify your email first!");
                mAuth.signOut();
            }
        }
    }

    private void showError(String message) {
        if (isAdded() && getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    private void showSuccess(String message) {
        if (isAdded() && getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    private void showProgressDialog(String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(requireContext());
            progressDialog.setCancelable(false);
        }
        progressDialog.setMessage(message);
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}