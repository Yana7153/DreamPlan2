package com.example.dreamplan;

import android.app.Dialog;
import android.os.Bundle;
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
import androidx.appcompat.widget.AppCompatImageButton; // Import AppCompatImageButton here
import androidx.fragment.app.Fragment;

import com.example.dreamplan.database.DatabaseManager;
import com.example.dreamplan.database.Section;
import com.example.dreamplan.database.Task;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SectionDetailFragment extends Fragment {

    private static final String ARG_SECTION = "section";
    private Section section;
    private TextView sectionName, sectionNotes, currentDate;
    private AppCompatImageButton addTaskButton; // Use AppCompatImageButton for FAB button
    private DatabaseManager dbManager;

    // Constructor to initialize Section
    public SectionDetailFragment() {}

    // Factory method to create an instance of this fragment
    public static SectionDetailFragment newInstance(Section section) {
        SectionDetailFragment fragment = new SectionDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SECTION, section);  // Pass the section to the fragment
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_section_detail, container, false);

        // Initialize UI components
        sectionName = view.findViewById(R.id.tvSectionName);
        sectionNotes = view.findViewById(R.id.tvSectionNotes);
        currentDate = view.findViewById(R.id.tvCurrentDate);
        addTaskButton = view.findViewById(R.id.fab_add_task); // AppCompatImageButton used for FAB
        dbManager = new DatabaseManager(getContext());

        // Get the Section object passed to the fragment
        if (getArguments() != null) {
            section = (Section) getArguments().getSerializable(ARG_SECTION);
        }

        // Set section details
        sectionName.setText(section.getName());
        sectionNotes.setText(section.getNotes());

        // Set today's date
        String todayDate = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
        currentDate.setText(todayDate);

        // Back button logic (use ImageView or ImageButton)
        ImageView backButton = view.findViewById(R.id.btnBack); // Corrected to ImageView
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                // Navigate back to the previous fragment/activity
                getActivity().getSupportFragmentManager().popBackStack();
            });
        }

        // Show add task dialog when the FAB button is clicked
        addTaskButton.setOnClickListener(v -> showAddTaskDialog());

        // Hide the FAB button when entering SectionDetailFragment
        hideFabButton();

        return view;
    }

    private void hideFabButton() {
        if (getActivity() != null) {
            // Get the FAB from MainActivity and hide it
            View fabAddSection = getActivity().findViewById(R.id.btnAddSection);
            if (fabAddSection != null) {
                fabAddSection.setVisibility(View.GONE); // Hide the "+" button
            }
        }
    }

    // Show dialog for adding a new task
    private void showAddTaskDialog() {
        Dialog taskDialog = new Dialog(getContext());
        taskDialog.setContentView(R.layout.dialog_add_task);

        EditText taskTitle = taskDialog.findViewById(R.id.etTaskTitle);
        EditText taskNotes = taskDialog.findViewById(R.id.etTaskNotes);
        Button saveTaskButton = taskDialog.findViewById(R.id.btnSaveTask);

        saveTaskButton.setOnClickListener(v -> {
            String title = taskTitle.getText().toString().trim();
            String notes = taskNotes.getText().toString().trim();

            if (title.isEmpty()) {
                Toast.makeText(getContext(), "Task title is required!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create and save the task (assuming you have a Task object and database manager)
            Task newTask = new Task(title, notes, section.getId()); // Example Task constructor
            dbManager.saveTask(newTask); // Method to save the task to the database

            Toast.makeText(getContext(), "Task added!", Toast.LENGTH_SHORT).show();

            taskDialog.dismiss(); // Dismiss the dialog after saving the task
        });

        taskDialog.show();
    }
}
