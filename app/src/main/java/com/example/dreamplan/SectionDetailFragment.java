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
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class SectionDetailFragment extends Fragment {

    private static final String ARG_SECTION = "section";
    private Section section;
    private TextView sectionName, sectionNotes, currentDate;
    private FloatingActionButton addTaskButton;
    private DatabaseManager dbManager;

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

        sectionName = view.findViewById(R.id.tvSectionName);
        sectionNotes = view.findViewById(R.id.tvSectionNotes);
        currentDate = view.findViewById(R.id.tvCurrentDate);
        addTaskButton = view.findViewById(R.id.fab_add_task);
        dbManager = new DatabaseManager(getContext());

        if (getArguments() != null) {
            section = (Section) getArguments().getSerializable(ARG_SECTION);
        }

        sectionName.setText(section.getName());
        sectionNotes.setText(section.getNotes());

        String todayDate = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
        currentDate.setText(todayDate);

        RecyclerView rvTasks = view.findViewById(R.id.rv_tasks);
        rvTasks.setLayoutManager(new LinearLayoutManager(getContext()));

        List<Task> tasks = dbManager.getAllTasksForSection(section.getId());


        TaskAdapter taskAdapter = new TaskAdapter(tasks, requireContext());
        taskAdapter.setOnTaskClickListener(new TaskAdapter.OnTaskClickListener() {
            @Override
            public void onTaskClick(Task task) {
                AddTaskFragment addTaskFragment = AddTaskFragment.newInstance(section, task);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, addTaskFragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onTaskLongClick(Task task) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Delete Task")
                        .setMessage("Delete this task?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            dbManager.deleteTask(task.getId());
                            refreshTaskList();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        rvTasks.setAdapter(taskAdapter);

        ImageView backButton = view.findViewById(R.id.btnBack);
        if (backButton != null) {
            backButton.setOnClickListener(v -> getActivity().getSupportFragmentManager().popBackStack());
        }

        addTaskButton.setOnClickListener(v -> {
            AddTaskFragment addTaskFragment = AddTaskFragment.newInstance(section, null); // Pass null for task
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
        List<Task> updatedTasks = dbManager.getAllTasksForSection(section.getId());
        RecyclerView rvTasks = getView().findViewById(R.id.rv_tasks);
        TaskAdapter adapter = (TaskAdapter) rvTasks.getAdapter();
        if (adapter != null) {
            adapter.updateTasks(updatedTasks);
        }
    }
}
