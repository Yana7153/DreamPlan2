package com.example.dreamplan;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

//import com.example.dreamplan.calendar.CalendarFragment;
//import com.example.dreamplan.settings.SettingsFragment;
import com.example.dreamplan.calendar.CalendarFragment;
import com.example.dreamplan.settings.NotificationHelper;
import com.example.dreamplan.settings.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class MainActivity extends AppCompatActivity {
    private FloatingActionButton btnAddSection;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NotificationHelper.createNotificationChannel(this);

        btnAddSection = findViewById(R.id.btnAddSection);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        setupFloatingActionButton();
        setupBottomNavigation();

        FirebaseApp.initializeApp(this);
        FirebaseFirestore.getInstance().setFirestoreSettings(
                new FirebaseFirestoreSettings.Builder()
                        .setPersistenceEnabled(true)
                        .build()
        );

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment(), "home_fragment");
        }
    }

    private void setupFloatingActionButton() {
        btnAddSection.setOnClickListener(v -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

            if (currentFragment instanceof HomeFragment) {
                ((HomeFragment) currentFragment).showAddSectionDialog();
            }
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String tag = null;

            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
                tag = "home_fragment";
            }
//            else if (itemId == R.id.nav_statistics) {
//                selectedFragment = new StatisticsFragment();
//            }
            else if (itemId == R.id.nav_calendar) {
                selectedFragment = new CalendarFragment();
            }
            else if (itemId == R.id.nav_settings) {
                selectedFragment = new SettingsFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment, tag);
            }
            return true;
        });
    }

    private void loadFragment(@NonNull Fragment fragment, String tag) {
        try {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            if (fragment instanceof CalendarFragment) {
                transaction.setCustomAnimations(
                        R.anim.slide_in_right,  // Enter animation
                        R.anim.fade_out,        // Exit animation
                        R.anim.fade_in,         // Pop enter animation
                        R.anim.slide_out_left    // Pop exit animation
                );
            }
            else {
                transaction.setCustomAnimations(
                        R.anim.fade_in,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.fade_out
                );
            }

            transaction.replace(R.id.fragment_container, fragment, tag);

            if (!(fragment instanceof HomeFragment)) {
                transaction.addToBackStack(null);
            }

            transaction.commit();

            btnAddSection.setVisibility(fragment instanceof HomeFragment ? View.VISIBLE : View.GONE);
        } catch (Exception e) {
            Log.e("MainActivity", "Fragment load failed", e);
            Toast.makeText(this, "Error loading screen", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof HomeFragment) {
            btnAddSection.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}