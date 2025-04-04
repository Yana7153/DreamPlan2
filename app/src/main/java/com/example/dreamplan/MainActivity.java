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

import com.example.dreamplan.calendar.CalendarFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
public class MainActivity extends AppCompatActivity {

    private FloatingActionButton btnAddSection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize FAB (Floating Action Button)
        btnAddSection = findViewById(R.id.btnAddSection);

        // Handle FAB click - Add Section logic
        btnAddSection.setOnClickListener(v -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

            // Check if HomeFragment is currently loaded
            if (currentFragment instanceof HomeFragment) {
                // Open the add section dialog or start an activity to add a section (implement your logic)
                ((HomeFragment) currentFragment).showAddSectionDialog();  // Fixed line here
            }
        });

        // Initialize BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_statistics) {
                selectedFragment = new StatisticsFragment();
            } else if (itemId == R.id.nav_calendar) {
                selectedFragment = new CalendarFragment();
            } else if (itemId == R.id.nav_settings) {
                selectedFragment = new SettingsFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }

            return true;
        });

        // Load HomeFragment by default if no saved state exists
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    // Helper method to load fragments into the container
    private void loadFragment(@NonNull Fragment fragment) {
        try {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            // Set custom animations
            transaction.setCustomAnimations(
                    R.anim.fade_in,  // enter
                    R.anim.fade_out, // exit
                    R.anim.fade_in,  // popEnter
                    R.anim.fade_out  // popExit
            );

            // Replace the fragment
            transaction.replace(R.id.fragment_container, fragment);

            // Only add to back stack if not the home fragment
            if (!(fragment instanceof HomeFragment)) {
                transaction.addToBackStack(null);
            }

            transaction.commit();

            // Handle FAB visibility
            btnAddSection.setVisibility(fragment instanceof HomeFragment ? View.VISIBLE : View.GONE);

        } catch (Exception e) {
            Log.e("MainActivity", "Fragment load failed", e);
            if (!isFinishing()) {
                Toast.makeText(this, "Loading failed, returning home", Toast.LENGTH_SHORT).show();
                loadFragment(new HomeFragment());
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Check if HomeFragment is showing after returning
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof HomeFragment) {
            btnAddSection.setVisibility(View.VISIBLE);  // Show FAB when HomeFragment is resumed
        }
    }


}
