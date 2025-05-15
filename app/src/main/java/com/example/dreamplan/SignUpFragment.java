package com.example.dreamplan;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import com.example.dreamplan.database.AuthManager;
import com.example.dreamplan.database.FirebaseDatabaseManager;
import com.example.dreamplan.database.Section;
import com.example.dreamplan.database.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SignUpFragment extends Fragment {

    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnSignUp;
    private TextView tvLogin;
    private ProgressDialog progressDialog;

    @Inject
    AuthManager authManager;

    @Inject
    FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup, container, false);
        initializeViews(view);
        setupClickListeners();
        return view;
    }

    private void initializeViews(View view) {
        etName = view.findViewById(R.id.et_name);
        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        etConfirmPassword = view.findViewById(R.id.et_confirm_password);
        btnSignUp = view.findViewById(R.id.btn_sign_up);
        tvLogin = view.findViewById(R.id.tv_login);
        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Creating account...");
        progressDialog.setCancelable(false);
    }

    private void setupClickListeners() {
        btnSignUp.setOnClickListener(v -> attemptSignUp());
        tvLogin.setOnClickListener(v -> navigateToLogin());
    }

    private void attemptSignUp() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (!validateInputs(name, email, password, confirmPassword)) {
            return;
        }

        showProgressDialog();
        createFirebaseUser(email, password);
    }

    private boolean validateInputs(String name, String email, String password, String confirmPassword) {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Please fill all fields");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Please enter a valid email");
            return false;
        }

        if (password.length() < 6) {
            showError("Password must be at least 6 characters");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords don't match");
            return false;
        }

        return true;
    }

    private void createFirebaseUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!isAdded() || requireActivity().isFinishing()) return;

                    if (task.isSuccessful()) {
                        sendVerificationEmail();
                    } else {
                        dismissProgressDialog();
                        showError("Sign up failed: " + task.getException().getMessage());
                    }
                });
    }

    private void sendVerificationEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(emailTask -> {
                        dismissProgressDialog();

                        if (!isAdded() || requireActivity().isFinishing()) return;

                        if (emailTask.isSuccessful()) {
                            handleSuccessfulSignUp(user);
                        } else {
                            showError("Failed to send verification email: " +
                                    emailTask.getException().getMessage());
                        }
                    });
        }
    }

    private void handleSuccessfulSignUp(FirebaseUser user) {
        authManager.initializeFirestoreUser(user.getUid(), user.getEmail(), new AuthManager.InitCallback() {
            @Override
            public void onSuccess() {
                createDefaultSections(user.getUid());
                showSuccess("Verification email sent. Please check your email.");
                navigateToLogin();
            }

            @Override
            public void onFailure(Exception e) {
                dismissProgressDialog();
                showError("Error setting up account: " + e.getMessage());
            }
        });
    }

    private void showProgressDialog() {
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }

    private void showSuccess(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }

    private void navigateToLogin() {
        if (isAdded() && getActivity() != null) {
            ((AuthActivity) requireActivity()).loadFragment(new LoginFragment());
        }
    }

    @Override
    public void onDestroyView() {
        dismissProgressDialog();
        super.onDestroyView();
    }

    private void createDefaultSections(String userId) {
        FirebaseDatabaseManager dbManager = FirebaseDatabaseManager.getInstance();

        dbManager.getSections(new FirebaseDatabaseManager.DatabaseCallback<List<Section>>() {
            @Override
            public void onSuccess(List<Section> existingSections) {
                if (existingSections == null || existingSections.isEmpty()) {
                    String[] sectionNames = {"Work", "Personal", "Study"};
                    String[] sectionColors = {"1", "2", "3"};
                    String[] sectionNotes = {
                            "Your professional tasks",
                            "Personal life matters",
                            "Learning and education"
                    };

                    for (int i = 0; i < sectionNames.length; i++) {
                        Section section = new Section("", sectionNames[i], sectionColors[i], sectionNotes[i], true);
                        dbManager.addSection(section, new FirebaseDatabaseManager.DatabaseCallback<String>() {
                            @Override
                            public void onSuccess(String result) {
                                Log.d("SignUp", "Created default section: " + section.getName());
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.e("SignUp", "Error creating default section", e);
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("SignUp", "Error checking sections", e);
            }
        });
    }
}