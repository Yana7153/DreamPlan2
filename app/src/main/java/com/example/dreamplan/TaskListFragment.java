package com.example.dreamplan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dreamplan.adapters.TaskAdapter;
import com.example.dreamplan.database.FirebaseDatabaseManager;
import com.example.dreamplan.database.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskListFragment extends Fragment {
    private static final String ARG_FILTER_TYPE = "filter_type";
    private RecyclerView rvTasks;
    private TaskAdapter taskAdapter;
    private TextView tvTitle;
    private ImageView btnBack;

    public static TaskListFragment newInstance(String filterType) {
        TaskListFragment fragment = new TaskListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FILTER_TYPE, filterType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_list, container, false);

        tvTitle = view.findViewById(R.id.tvTitle);
        btnBack = view.findViewById(R.id.btnBack);
        rvTasks = view.findViewById(R.id.rvTasks);

        taskAdapter = new TaskAdapter(new ArrayList<>(), requireContext());
        rvTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTasks.setAdapter(taskAdapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        taskAdapter = new TaskAdapter(new ArrayList<>(), requireContext());

        taskAdapter.setOnTaskClickListener(task -> {

        });

        taskAdapter.setOnTaskDeleteListener(null);

        rvTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTasks.setAdapter(taskAdapter);

        if (getArguments() != null) {
            String filterType = getArguments().getString(ARG_FILTER_TYPE);
            if (filterType != null) {
                updateTitle(filterType);
                loadTasks(filterType);
            }
        }

        taskAdapter.setOnTaskDeleteListener(task -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Delete Task")
                    .setMessage("Delete this task?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        FirebaseDatabaseManager.getInstance().deleteTask(task.getId(),
                                new FirebaseDatabaseManager.DatabaseCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void result) {
                                        loadTasks(getArguments().getString(ARG_FILTER_TYPE));
                                    }
                                    @Override
                                    public void onFailure(Exception e) {
                                        Toast.makeText(getContext(), "Delete failed", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void updateTitle(String filterType) {
        String title = "";
        switch (filterType) {
            case "today":
                title = "Today's Tasks";
                break;
            case "tomorrow":
                title = "Tomorrow's Tasks";
                break;
            case "week":
                title = "This Week's Tasks";
                break;
        }
        tvTitle.setText(title);
    }

    private void loadTasks(String filterType) {
        FirebaseDatabaseManager dbManager = FirebaseDatabaseManager.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(new Date());

        FirebaseDatabaseManager.DatabaseCallback<List<Task>> callback = new FirebaseDatabaseManager.DatabaseCallback<List<Task>>() {
            @Override
            public void onSuccess(List<Task> tasks) {
                if (taskAdapter != null) {
                    taskAdapter.updateTasks(tasks);
                }
            }

            @Override
            public void onFailure(Exception e) {
                showError("Error loading tasks");
            }
        };

        switch (filterType) {
            case "today":
                dbManager.getTasksForDate(today, callback);
                break;
            case "tomorrow":
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DATE, 1);
                String tomorrow = sdf.format(cal.getTime());
                dbManager.getTasksForDate(tomorrow, callback);
                break;
            case "week":
                cal = Calendar.getInstance();
                String startDate = sdf.format(cal.getTime());
                cal.add(Calendar.DATE, 6);
                String endDate = sdf.format(cal.getTime());
                dbManager.getTasksForDateRange(startDate, endDate, callback);
                break;
        }
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}