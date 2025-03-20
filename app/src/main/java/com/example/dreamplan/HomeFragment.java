package com.example.dreamplan;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

import java.util.List;public class HomeFragment extends Fragment {

    private RecyclerView rvSections;
    private SectionAdapter sectionAdapter;
    private List<Section> sectionList;
    private DatabaseManager dbManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize UI elements
        rvSections = view.findViewById(R.id.rvSections);
        dbManager = new DatabaseManager(getContext());

        // Insert predefined sections if not already inserted
        dbManager.insertMainSectionsIfNotExist();

        // Set up RecyclerView
        sectionList = dbManager.getAllSections();
        sectionAdapter = new SectionAdapter(sectionList, getContext(), this);  // 'this' refers to the HomeFragment
        rvSections.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSections.setAdapter(sectionAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Show FAB again when returning to HomeFragment
        if (getActivity() != null && getActivity() instanceof MainActivity) {
            FloatingActionButton btnAddSection = getActivity().findViewById(R.id.btnAddSection);
            if (btnAddSection != null) {
                btnAddSection.setVisibility(View.VISIBLE);  // Make sure FAB is visible
                btnAddSection.setOnClickListener(v -> showAddSectionDialog()); // Set up click listener
            }
        }
    }

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
        Button colorButton = sectionDialog.findViewById(R.id.btn_select_color);  // Color selection button

        // Default color (can be modified later)
        final String[] selectedColor = new String[]{section.getColor()};  // Use the color of the current section

        // Set the initial values in the EditTexts based on the section being edited
        sectionName.setText(section.getName());
        notes.setText(section.getNotes());
        colorButton.setBackgroundColor(Color.parseColor(selectedColor[0]));

        // Show color selection dialog when button is clicked
        colorButton.setOnClickListener(v -> {
            final String[] colors = {"#FF6200EE", "#FF5722", "#8BC34A", "#03A9F4", "#9C27B0"};
            new AlertDialog.Builder(getContext())
                    .setTitle("Select Color")
                    .setItems(new String[]{"Purple", "Red", "Green", "Blue", "Pink"}, (dialog, which) -> {
                        selectedColor[0] = colors[which];
                        colorButton.setBackgroundColor(Color.parseColor(selectedColor[0])); // Update button color
                    })
                    .show();
        });

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

        // Get references to the dialog elements
        EditText sectionName = sectionDialog.findViewById(R.id.et_section_name);
        EditText notes = sectionDialog.findViewById(R.id.et_notes);
        Button saveSectionButton = sectionDialog.findViewById(R.id.btn_save_section);
        Button colorButton = sectionDialog.findViewById(R.id.btn_select_color);  // Color selection button

        // Check if saveSectionButton is null
        if (saveSectionButton == null) {
            Log.e("HomeFragment", "Save button not found in the dialog layout.");
            return;
        }

        // Default color (can be modified later)
        final String[] selectedColor = new String[]{"#D3D3D3"};  // Default color (light gray)

        // Show color selection dialog when button is clicked
        colorButton.setOnClickListener(v -> {
            final String[] colors = {"#FF6200EE", "#FF5722", "#8BC34A", "#03A9F4", "#9C27B0"};
            new AlertDialog.Builder(getContext())
                    .setTitle("Select Color")
                    .setItems(new String[]{"Purple", "Red", "Green", "Blue", "Pink"}, (dialog, which) -> {
                        selectedColor[0] = colors[which];
                        colorButton.setBackgroundColor(Color.parseColor(selectedColor[0])); // Update button color
                    })
                    .show();
        });

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

            sectionList = dbManager.getAllSections();  // Re-fetch data from DB
            sectionAdapter.notifyDataSetChanged();  // Notify the adapter about the new section

            sectionDialog.dismiss();  // Dismiss the dialog after saving the section
        });

        // Show the dialog
        sectionDialog.show();
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
}
