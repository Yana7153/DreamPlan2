package com.example.dreamplan;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dreamplan.adapters.SectionAdapter;
import com.example.dreamplan.database.DatabaseManager;
import com.example.dreamplan.database.Section;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import android.os.Handler;

public class HomeFragment extends Fragment {

    private RecyclerView rvSections;
    private SectionAdapter sectionAdapter;
    private List<Section> sectionList;
    private DatabaseManager dbManager;
    private TextView tvTasksTodayNumber;
    private TextView  tvTasksTomorrowNumber;
    private TextView  tvTasksWeekNumber;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);


        tvTasksTodayNumber = view.findViewById(R.id.tvTasksTodayNumber);
        tvTasksTomorrowNumber = view.findViewById(R.id.tvTasksTomorrowNumber);
        tvTasksWeekNumber = view.findViewById(R.id.tvTasksWeekNumber);


        // Initialize UI elements
        rvSections = view.findViewById(R.id.rvSections);
        dbManager = new DatabaseManager(getContext());


        // Insert predefined sections if not already inserted
        dbManager.insertMainSectionsIfNotExist();

        refreshTaskCounts();

        // Set up RecyclerView
        sectionList = dbManager.getAllSections();
        sectionAdapter = new SectionAdapter(sectionList, getContext(), this);  // 'this' refers to the HomeFragment
        rvSections.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSections.setAdapter(sectionAdapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        requireActivity().registerReceiver(updateReceiver,
                new IntentFilter("TASK_UPDATED"), Context.RECEIVER_NOT_EXPORTED);
    }

    @Override
    public void onStop() {
        super.onStop();
        requireActivity().unregisterReceiver(updateReceiver);
    }


    @Override
    public void onResume() {
        super.onResume();
        loadTaskCounts();
        // Show FAB again when returning to HomeFragment
        if (getActivity() != null && getActivity() instanceof MainActivity) {
            FloatingActionButton btnAddSection = getActivity().findViewById(R.id.btnAddSection);
            if (btnAddSection != null) {
                btnAddSection.setVisibility(View.VISIBLE);  // Make sure FAB is visible
                btnAddSection.setOnClickListener(v -> showAddSectionDialog()); // Set up click listener
            }
        }
        refreshTaskCounts();
    }

    private final BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Auto-refresh when tasks are modified anywhere in app
            refreshTaskCounts();
        }
    };

    // Method to show the Edit Section dialog
    public void showEditSectionDialog(Section section) {
        if (getContext() == null) return;

        // Inflate the dialog view
        Dialog sectionDialog = new Dialog(requireContext());
        sectionDialog.setContentView(R.layout.task_input_dialog);  // Ensure this layout has the correct views

        // Get references to the dialog elements
        EditText sectionName = sectionDialog.findViewById(R.id.et_section_name);
        EditText notes = sectionDialog.findViewById(R.id.et_notes);
        Button saveSectionButton = sectionDialog.findViewById(R.id.btn_save_section);
  //      Button colorButton = sectionDialog.findViewById(R.id.btn_select_color);  // Color selection button

        // Default color (can be modified later)
        final String[] selectedColor = new String[]{section.getColor()};  // Use the color of the current section

        // Set the initial values in the EditTexts based on the section being edited
        sectionName.setText(section.getName());
        notes.setText(section.getNotes());
      //  colorButton.setBackgroundColor(Color.parseColor(selectedColor[0]));

        // Show color selection dialog when button is clicked
//        colorButton.setOnClickListener(v -> {
//            final String[] colors = {"#FF6200EE", "#FF5722", "#8BC34A", "#03A9F4", "#9C27B0"};
//            new AlertDialog.Builder(getContext())
//                    .setTitle("Select Color")
//                    .setItems(new String[]{"Purple", "Red", "Green", "Blue", "Pink"}, (dialog, which) -> {
//                        selectedColor[0] = colors[which];
//                        colorButton.setBackgroundColor(Color.parseColor(selectedColor[0])); // Update button color
//                    })
//                    .show();
//        });

        // Save section button click listener
        saveSectionButton.setOnClickListener(v -> {
            String name = sectionName.getText().toString().trim();
            String notesText = notes.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Section Name is required!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update the section with the new data (edited section)
            section.setName(name);
            section.setNotes(notesText);
            section.setColor(selectedColor[0]);

            dbManager.updateSection(section);  // Update the section in the database

            sectionList = dbManager.getAllSections();  // Re-fetch data from DB
            sectionAdapter.notifyDataSetChanged();  // Notify the adapter about the updated section

            sectionDialog.dismiss();  // Dismiss the dialog after saving the section
        });

        // Show the dialog
        sectionDialog.show();
    }

    // Method to show the Add Section dialog
    // Method to show the Add Section dialog
    public void showAddSectionDialog() {
        if (getContext() == null) return;

        // Inflate dialog view
        Dialog sectionDialog = new Dialog(requireContext());
        sectionDialog.setContentView(R.layout.task_input_dialog);  // Ensure this layout has the correct views

        // Set rounded corners for the dialog
        if (sectionDialog.getWindow() != null) {
            sectionDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        }

        // Get references to the dialog elements
        EditText sectionName = sectionDialog.findViewById(R.id.et_section_name);
        EditText notes = sectionDialog.findViewById(R.id.et_notes);
        Button saveSectionButton = sectionDialog.findViewById(R.id.btn_save_section);

        // Get references to the color circles
        ImageView colorCircle1 = sectionDialog.findViewById(R.id.color_circle_1);
        ImageView colorCircle2 = sectionDialog.findViewById(R.id.color_circle_2);
        ImageView colorCircle3 = sectionDialog.findViewById(R.id.color_circle_3);
        ImageView colorCircle4 = sectionDialog.findViewById(R.id.color_circle_4);
        ImageView colorCircle5 = sectionDialog.findViewById(R.id.color_circle_5);
        ImageView colorCircle6 = sectionDialog.findViewById(R.id.color_circle_6);
        ImageView colorCircle7 = sectionDialog.findViewById(R.id.color_circle_7);

        // Default color (can be modified later)
        final String[] selectedColor = new String[]{"#D3D3D3"};  // Default color (light gray)
        ImageView[] colorCircles = {colorCircle1, colorCircle2, colorCircle3, colorCircle4, colorCircle5, colorCircle6, colorCircle7};
        String[] colors = {"#CCE1F2", "#C6F8E5", "#FBF7D5", "#F9DED7", "#F5CDDE", "#E2BEF1", "#D3D3D3"};

        // Function to update the selected color indicator
        Runnable updateSelectedColorIndicator = () -> {
            for (int i = 0; i < colorCircles.length; i++) {
                if (selectedColor[0].equals(colors[i])) {
                    // Add black border to the selected color circle
                    colorCircles[i].setBackground(getColorCircleDrawable(colors[i], true));
                } else {
                    // Remove black border from other circles
                    colorCircles[i].setBackground(getColorCircleDrawable(colors[i], false));
                }
            }
        };

        // Set click listeners for the color circles
        for (int i = 0; i < colorCircles.length; i++) {
            int finalI = i;
            colorCircles[i].setOnClickListener(v -> {
                selectedColor[0] = colors[finalI]; // Update the selected color
                updateSelectedColorIndicator.run(); // Update the UI
            });
        }

        // Initialize the default selected color (light gray)
        updateSelectedColorIndicator.run();

        // Save section button click listener
        saveSectionButton.setOnClickListener(v -> {
            String name = sectionName.getText().toString().trim();
            String notesText = notes.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Section Name is required!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create a new Section object with the selected color
            Section newSection = new Section(0, name, selectedColor[0], notesText);
            dbManager.insertSection(newSection);

            // Update the RecyclerView
            sectionList.clear(); // Clear the existing list
            sectionList.addAll(dbManager.getAllSections()); // Re-fetch all sections from the database
            sectionAdapter.notifyDataSetChanged(); // Notify the adapter about the data change

            sectionDialog.dismiss();  // Dismiss the dialog after saving the section
        });

        // Show the dialog
        sectionDialog.show();
    }

    // Helper method to get the drawable resource for a color circle
    private Drawable getColorCircleDrawable(String color, boolean isSelected) {
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(Color.parseColor(color)); // Set the circle color
        circle.setSize(40, 40); // Set the size

        if (isSelected) {
            // Add a black border for the selected color
            GradientDrawable border = new GradientDrawable();
            border.setShape(GradientDrawable.OVAL);
            border.setStroke(4, Color.BLACK); // Thicker black border
            border.setSize(40, 40);

            LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{circle, border});
            return layerDrawable;
        } else {
            // No border for unselected colors
            return circle;
        }
    }


    // Method to delete section from database
    public void deleteSection(Section section, int position) {
        dbManager.deleteSection(section.getId());
        sectionList.remove(position);  // Remove from list
        sectionAdapter.notifyItemRemoved(position);  // Update RecyclerView
        Toast.makeText(getContext(), "Section deleted", Toast.LENGTH_SHORT).show();
    }

    public void openSectionDetail(Section section) {
        // Open the section details page
        SectionDetailFragment fragment = SectionDetailFragment.newInstance(section);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);  // Allow going back to previous fragment
        transaction.commit();
    }

    private void loadTaskCounts() {
        try {
            if (dbManager != null && tvTasksTodayNumber != null
                    && tvTasksTomorrowNumber != null && tvTasksWeekNumber != null) {
                int todayCount = dbManager.getTasksDueTodayCount();
                int tomorrowCount = dbManager.getTasksDueTomorrowCount();
                int weekCount = dbManager.getTasksDueInWeekCount();

                tvTasksTodayNumber.setText(String.valueOf(todayCount));
                tvTasksTomorrowNumber.setText(String.valueOf(tomorrowCount));
                tvTasksWeekNumber.setText(String.valueOf(weekCount));
            }
        } catch (Exception e) {
            Log.e("HomeFragment", "Error loading task counts", e);
        }
    }

    private void loadSections() {
        // Your existing section loading code
        dbManager.insertMainSectionsIfNotExist();
        sectionList = dbManager.getAllSections();
        sectionAdapter = new SectionAdapter(sectionList, getContext(), this);
        rvSections.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSections.setAdapter(sectionAdapter);
    }

    private void refreshTaskCounts() {
        if (getActivity() == null || !isAdded()) return;

        new Thread(() -> {
            try {
                DatabaseManager db = new DatabaseManager(requireContext());
                final int todayCount = db.getTasksDueTodayCount();
                final int tomorrowCount = db.getTasksDueTomorrowCount();
                final int weekCount = db.getTasksDueInWeekCount();

                getActivity().runOnUiThread(() -> {
                    // Only update if values changed
                    if (!tvTasksTodayNumber.getText().equals(String.valueOf(todayCount))) {
                        tvTasksTodayNumber.setText(String.valueOf(todayCount));
                    }
                    if (!tvTasksTomorrowNumber.getText().equals(String.valueOf(tomorrowCount))) {
                        tvTasksTomorrowNumber.setText(String.valueOf(tomorrowCount));
                    }
                    if (!tvTasksWeekNumber.getText().equals(String.valueOf(weekCount))) {
                        tvTasksWeekNumber.setText(String.valueOf(weekCount));
                    }
                });
            } catch (Exception e) {
                Log.e("REFRESH", "Auto-refresh failed", e);
            }
        }).start();
    }
}
