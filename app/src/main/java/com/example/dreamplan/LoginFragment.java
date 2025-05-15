package com.example.dreamplan;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(
                requireContext(),
                R.style.RoundedDialogTheme
        );

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_forgot_password, null);

        TextInputLayout emailLayout = dialogView.findViewById(R.id.email_input_layout);
        EditText emailInput = dialogView.findViewById(R.id.email_input);

        builder.setView(dialogView)
                .setTitle("Reset Password")
                .setMessage("Enter your email to receive a reset link")
                .setPositiveButton("Send Link", (dialog, which) -> {
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
                })
                .setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_color));

        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        negativeButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));
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