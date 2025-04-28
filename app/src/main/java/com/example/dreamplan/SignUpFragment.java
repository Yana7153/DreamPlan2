package com.example.dreamplan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.dreamplan.database.AuthManager;
import com.example.dreamplan.database.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpFragment extends Fragment {

    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnSignUp;
    private TextView tvLogin;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        etName = view.findViewById(R.id.et_name);
        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        etConfirmPassword = view.findViewById(R.id.et_confirm_password);
        btnSignUp = view.findViewById(R.id.btn_sign_up);
        tvLogin = view.findViewById(R.id.tv_login);

        btnSignUp.setOnClickListener(v -> attemptSignUp());
        tvLogin.setOnClickListener(v -> goToLogin());

        return view;
    }

    private void attemptSignUp() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validation checks
        if (email.isEmpty() || password.isEmpty() || name.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(getActivity(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(getActivity(), "Passwords don't match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase Sign-Up
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Send verification email
                        FirebaseUser user = auth.getCurrentUser();
                        user.sendEmailVerification()
                                .addOnCompleteListener(emailTask -> {
                                    if (emailTask.isSuccessful()) {
                                        Toast.makeText(
                                                getActivity(),
                                                "Verification email sent! Check your inbox.",
                                                Toast.LENGTH_LONG
                                        ).show();
                                        goToLogin(); // Go back to login screen
                                    } else {
                                        Toast.makeText(
                                                getActivity(),
                                                "Failed to send verification email: " + emailTask.getException(),
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }
                                });
                    } else {
                        Toast.makeText(
                                getActivity(),
                                "Sign-up failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void goToLogin() {
        ((AuthActivity) getActivity()).loadFragment(new LoginFragment());
    }
}