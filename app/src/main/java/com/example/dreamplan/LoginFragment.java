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

public class LoginFragment extends Fragment {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvSignUp, tvForgotPassword;

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
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgressDialog();

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (getActivity() == null || getActivity().isFinishing()) return;

                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            if (user.isEmailVerified()) {
                                ((AuthActivity) requireActivity()).startMainActivity(user);
                            } else {
                                Toast.makeText(requireContext(),
                                        "Please verify your email first!",
                                        Toast.LENGTH_LONG).show();
                                FirebaseAuth.getInstance().signOut();
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(),
                                "Login failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
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
            if (!isAdded() || getActivity() == null || getActivity().isFinishing()) return;

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(
                    requireContext(),
                    R.style.CustomDialogTheme
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
            if (!getActivity().isFinishing()) {
                dialog.show();

                Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_color));

                Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                negativeButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));
            }

        } catch (Exception e) {
            Log.e("LoginFragment", "Dialog error", e);
            if (getActivity() != null && !getActivity().isFinishing()) {
                Toast.makeText(getActivity(), "Error showing dialog", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showProgressDialog() {
        if (getActivity() == null || getActivity().isFinishing()) return;

        ProgressDialog progress = new ProgressDialog(getActivity());
        progress.setMessage("Processing...");
        progress.setCancelable(false);
        progress.show();
    }

    private void sendPasswordResetEmail(String email) {
        showProgressDialog();

        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (getActivity() == null || getActivity().isFinishing()) return;

                    if (task.isSuccessful()) {
                        Toast.makeText(requireContext(),
                                "Reset link sent to " + email,
                                Toast.LENGTH_LONG).show();
                    } else {
                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Error")
                                .setMessage(task.getException().getMessage())
                                .setPositiveButton("OK", null)
                                .show();
                    }
                });
    }
}