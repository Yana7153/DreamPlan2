package com.example.dreamplan;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            startMainActivity(currentUser);
        } else {
            loadFragment(new LoginFragment());
        }
    }

    public void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.auth_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void startMainActivity(FirebaseUser user) {
        Intent intent = new Intent(this, MainActivity.class);

        intent.putExtra("user_email", user.getEmail());
        intent.putExtra("user_name", user.getDisplayName());

        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}