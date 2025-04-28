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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginFragment extends Fragment {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvSignUp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        btnLogin = view.findViewById(R.id.btn_login);
        tvSignUp = view.findViewById(R.id.tv_sign_up);

        btnLogin.setOnClickListener(v -> attemptLogin());
        tvSignUp.setOnClickListener(v -> goToSignUp());

        return view;
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getActivity(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            if (user.isEmailVerified()) {
                                // Email is verified → Proceed to MainActivity
                                ((AuthActivity) getActivity()).startMainActivity(user);
                            } else {
                                Toast.makeText(
                                        getActivity(),
                                        "Please verify your email first!",
                                        Toast.LENGTH_LONG
                                ).show();
                                auth.signOut(); // Force logout if email not verified
                            }
                        }
                    } else {
                        Toast.makeText(
                                getActivity(),
                                "Login failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void goToSignUp() {
        ((AuthActivity) getActivity()).loadFragment(new SignUpFragment());
    }
}