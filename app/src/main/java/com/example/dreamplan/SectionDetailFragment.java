package com.example.dreamplan;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dreamplan.database.DatabaseManager;
import com.example.dreamplan.database.Section;
import com.example.dreamplan.database.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class SectionDetailFragment extends Fragment {

    private static final String ARG_SECTION = "section";
    private Section section;
    private TextView sectionName, sectionNotes, currentDate;
    private FloatingActionButton addTaskButton; // Use AppCompatImageButton for FAB button
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
        addTaskButton = view.findViewById(R.id.fab_add_task);
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

        // Initialize RecyclerView for tasks
        RecyclerView rvTasks = view.findViewById(R.id.rv_tasks);
        rvTasks.setLayoutManager(new LinearLayoutManager(getContext()));

        // Fetch tasks for this section
        List<Task> tasks = dbManager.getAllTasksForSection(section.getId());

        // Set up the adapter
        TaskAdapter taskAdapter = new TaskAdapter(tasks);
        rvTasks.setAdapter(taskAdapter);

        // Back button logic
        ImageView backButton = view.findViewById(R.id.btnBack);
        if (backButton != null) {
            backButton.setOnClickListener(v -> getActivity().getSupportFragmentManager().popBackStack());
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

        // Initialize views
        EditText etTaskTitle = taskDialog.findViewById(R.id.et_task_title);
        EditText etTaskNotes = taskDialog.findViewById(R.id.et_task_notes);
        TextView tvDeadline = taskDialog.findViewById(R.id.tv_deadline);
        ImageView ivCalendarArrow = taskDialog.findViewById(R.id.iv_calendar_arrow);
        CalendarView calendarView = taskDialog.findViewById(R.id.calendar_view);
        Button btnSaveTask = taskDialog.findViewById(R.id.btn_save_task);

        // Handle calendar visibility
        ivCalendarArrow.setOnClickListener(v -> {
            if (calendarView.getVisibility() == View.GONE) {
                calendarView.setVisibility(View.VISIBLE);
            } else {
                calendarView.setVisibility(View.GONE);
            }
        });

        // Handle calendar date selection
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String deadline = dayOfMonth + "/" + (month + 1) + "/" + year;
            tvDeadline.setText(deadline);
            calendarView.setVisibility(View.GONE);
        });

        // Handle color selection
        ImageView[] colorCircles = {
                taskDialog.findViewById(R.id.color_circle_1),
                taskDialog.findViewById(R.id.color_circle_2),
                taskDialog.findViewById(R.id.color_circle_3),
                taskDialog.findViewById(R.id.color_circle_4),
                taskDialog.findViewById(R.id.color_circle_5),
                taskDialog.findViewById(R.id.color_circle_6),
                taskDialog.findViewById(R.id.color_circle_7)
        };

        int[] colors = {
                Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.CYAN, Color.MAGENTA, Color.GRAY
        };

        final int[] selectedColor = {colors[0]}; // Default color

        for (int i = 0; i < colorCircles.length; i++) {
            int index = i;
            colorCircles[i].setOnClickListener(v -> {
                // Highlight selected color
                for (ImageView circle : colorCircles) {
                    circle.setBackgroundResource(0); // Remove highlight
                }
                colorCircles[index].setBackgroundResource(R.drawable.circle_background_selected); // Highlight selected
                selectedColor[0] = colors[index]; // Update selected color
            });
        }

        // Save task
        btnSaveTask.setOnClickListener(v -> {
            String title = etTaskTitle.getText().toString().trim();
            String notes = etTaskNotes.getText().toString().trim();
            String deadline = tvDeadline.getText().toString();

            if (title.isEmpty()) {
                Toast.makeText(getContext(), "Task title is required!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create and save the task
            Task newTask = new Task(title, notes, deadline, selectedColor[0], section.getId());
            dbManager.saveTask(newTask);

            Toast.makeText(getContext(), "Task added!", Toast.LENGTH_SHORT).show();
            taskDialog.dismiss();
        });

        taskDialog.show();
    }
}
