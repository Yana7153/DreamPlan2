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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class SectionDetailFragment extends Fragment {

    private static final String ARG_SECTION = "section";
    private Section section;
    private TextView sectionName, sectionNotes, currentDate;
    private FloatingActionButton addTaskButton;
    private DatabaseManager dbManager;
    private List<Task> taskList;
    private TaskAdapter taskAdapter;

    public SectionDetailFragment() {}

    public static SectionDetailFragment newInstance(Section section) {
        SectionDetailFragment fragment = new SectionDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SECTION, section);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_section_detail, container, false);

        // Initialize views
        sectionName = view.findViewById(R.id.tvSectionName);
        sectionNotes = view.findViewById(R.id.tvSectionNotes);
        currentDate = view.findViewById(R.id.tvCurrentDate);
        addTaskButton = view.findViewById(R.id.fab_add_task);
        dbManager = new DatabaseManager(getContext());

        // Get section from arguments
        if (getArguments() != null) {
            section = (Section) getArguments().getSerializable(ARG_SECTION);
        }

        // Set section info
        sectionName.setText(section.getName());
        sectionNotes.setText(section.getNotes());

        // Set current date
        String todayDate = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
        currentDate.setText(todayDate);

        // Initialize RecyclerView
        RecyclerView rvTasks = view.findViewById(R.id.rv_tasks);
        rvTasks.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize task list and adapter
        taskList = dbManager.getAllTasksForSection(section.getId());
        taskAdapter = new TaskAdapter(taskList, requireContext()); // Using the field now

        taskAdapter.setOnTaskClickListener(new TaskAdapter.OnTaskClickListener() {
            @Override
            public void onTaskClick(Task task) {
                // Edit task
                AddTaskFragment addTaskFragment = AddTaskFragment.newInstance(section, task);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, addTaskFragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onTaskLongClick(Task task) {
                // Delete task
                int position = taskList.indexOf(task);
                if (position != -1) {
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Delete Task")
                            .setMessage("Are you sure you want to delete this task?")
                            .setPositiveButton("Delete", (dialog, which) -> {
                                if (dbManager.deleteTask(task.getId())) {
                                    taskList.remove(position);
                                    taskAdapter.notifyItemRemoved(position); // Better than notifyDataSetChanged
                                    Toast.makeText(getContext(), "Task deleted", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }
            }
        });

        rvTasks.setAdapter(taskAdapter);

        // Back button
        ImageView backButton = view.findViewById(R.id.btnBack);
        if (backButton != null) {
            backButton.setOnClickListener(v -> getActivity().getSupportFragmentManager().popBackStack());
        }

        // Add task button
        addTaskButton.setOnClickListener(v -> {
            AddTaskFragment addTaskFragment = AddTaskFragment.newInstance(section, null);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, addTaskFragment)
                    .addToBackStack(null)
                    .commit();
        });

        hideFabButton();

        return view;
    }

    private void hideFabButton() {
        if (getActivity() != null) {
            View fabAddSection = getActivity().findViewById(R.id.btnAddSection);
            if (fabAddSection != null) {
                fabAddSection.setVisibility(View.GONE);
            }
        }
    }

    public void refreshTaskList() {
        if (taskAdapter != null) {
            taskList.clear();
            taskList.addAll(dbManager.getAllTasksForSection(section.getId()));
            taskAdapter.notifyDataSetChanged();
        }
    }
}
