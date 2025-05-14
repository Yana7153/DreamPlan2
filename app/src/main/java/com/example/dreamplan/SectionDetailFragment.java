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
import com.example.dreamplan.database.FirebaseDatabaseManager;
import com.example.dreamplan.database.Section;
import com.example.dreamplan.database.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SectionDetailFragment extends Fragment {

//    private static final String ARG_SECTION = "section";
//    private Section section;
//    private TextView sectionName, sectionNotes, currentDate;
//    private FloatingActionButton addTaskButton;
//    private DatabaseManager dbManager;
//  //  private List<Task> taskList;
//    private TaskAdapter taskAdapter;
//
//    private FirebaseFirestore db;
//    private String userId;
//    private List<Task> taskList = new ArrayList<>();
//
//    private FirebaseDatabaseManager dbManager;
//  //  private List<Task> taskList = new ArrayList<>();
//
private static final String ARG_SECTION = "section";
    private Section section;
    private TextView sectionName, sectionNotes, currentDate;
    private FloatingActionButton addTaskButton;
    private TaskAdapter taskAdapter;
    private List<Task> taskList = new ArrayList<>();
    private FirebaseDatabaseManager dbManager;


    public SectionDetailFragment() {}

    public static SectionDetailFragment newInstance(Section section) {
        SectionDetailFragment fragment = new SectionDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SECTION, section);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbManager = FirebaseDatabaseManager.getInstance();
        if (getArguments() != null) {
            section = (Section) getArguments().getSerializable(ARG_SECTION);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTasks();
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
        RecyclerView rvTasks = view.findViewById(R.id.rv_tasks);

        // Get section from arguments
        if (getArguments() != null) {
            section = (Section) getArguments().getSerializable(ARG_SECTION);
        }

        // Set section info
        sectionName.setText(section.getName());
        sectionNotes.setText(section.getNotes());
        currentDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));

        // Setup RecyclerView
        taskAdapter = new TaskAdapter(taskList, requireContext());
        rvTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTasks.setAdapter(taskAdapter);


        setupTaskClickListeners();

        // Set click listeners
        addTaskButton.setOnClickListener(v -> showAddTaskFragment());
        view.findViewById(R.id.btnBack).setOnClickListener(v -> getActivity().onBackPressed());


//        taskAdapter.setOnTaskClickListener(new TaskAdapter.OnTaskClickListener() {
//            @Override
//            public void onTaskClick(Task task) {
//                AddTaskFragment addTaskFragment = AddTaskFragment.newInstance(section, task);
//                getParentFragmentManager().beginTransaction()
//                        .replace(R.id.fragment_container, addTaskFragment)
//                        .addToBackStack("edit_task")
//                        .commit();
//            }
//
//            @Override
//            public void onTaskLongClick(Task task) {
//                // Delete task
//                int position = taskList.indexOf(task);
//                if (position != -1) {
//                    new AlertDialog.Builder(requireContext())
//                            .setTitle("Delete Task")
//                            .setMessage("Are you sure you want to delete this task?")
//                            .setPositiveButton("Delete", (dialog, which) -> {
//                                if (dbManager.deleteTask(task.getId())) {
//                                    taskList.remove(position);
//                                    taskAdapter.notifyItemRemoved(position); // Better than notifyDataSetChanged
//                                    Toast.makeText(getContext(), "Task deleted", Toast.LENGTH_SHORT).show();
//                                }
//                            })
//                            .setNegativeButton("Cancel", null)
//                            .show();
//                }
//            }
//        });


        // Back button
        ImageView backButton = view.findViewById(R.id.btnBack);
        if (backButton != null) {
            backButton.setOnClickListener(v -> getActivity().getSupportFragmentManager().popBackStack());
        }

        addTaskButton.setOnClickListener(v -> {
            AddTaskFragment addTaskFragment = AddTaskFragment.newInstance(section, null);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, addTaskFragment)
                    .addToBackStack("add_task")
                    .commit();
        });

        hideFabButton();

        loadTasks();
        return view;
    }

    private void loadTasks() {
        Log.d("TASK_DEBUG", "Loading tasks for section: " + section.getId());
        dbManager.getTasksForSection(section.getId(), new FirebaseDatabaseManager.DatabaseCallback<List<Task>>() {
            @Override
            public void onSuccess(List<Task> tasks) {
                Log.d("TASK_DEBUG", "Received " + tasks.size() + " tasks");
                for (Task t : tasks) {
                    Log.d("TASK_DEBUG", "Task: " + t.getTitle() +
                            " ID: " + t.getId() +
                            " Section: " + t.getSectionId());
                }

                taskList.clear();
                taskList.addAll(tasks);
                taskAdapter.notifyItemRangeChanged(0, tasks.size());
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("TASK_ERROR", "Failed to load tasks", e);
                Toast.makeText(getContext(), "Error loading tasks", Toast.LENGTH_SHORT).show();
            }
        });
    }





    private void deleteTask(Task task) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("users").document(userId)
                .collection("tasks").document(task.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    int position = taskList.indexOf(task);
                    if (position != -1) {
                        taskList.remove(position);
                        taskAdapter.notifyItemRemoved(position);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Delete failed", Toast.LENGTH_SHORT).show();
                });
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
        loadTasks();
    }

    public void openSectionDetail(Section section) {
        SectionDetailFragment fragment = SectionDetailFragment.newInstance(section);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack("section_detail")
                .commit();
    }

    private void showAddTaskFragment() {
        AddTaskFragment fragment = AddTaskFragment.newInstance(section, null);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack("add_task")
                .commit();
    }

    private void setupTaskClickListeners() {
        taskAdapter.setOnTaskClickListener(new TaskAdapter.OnTaskClickListener() {
            @Override
            public void onTaskClick(Task task) {
                AddTaskFragment fragment = AddTaskFragment.newInstance(section, task);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onTaskLongClick(Task task) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Delete Task")
                        .setMessage("Are you sure?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            dbManager.deleteTask(task.getId(), new FirebaseDatabaseManager.DatabaseCallback<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    int position = taskList.indexOf(task);
                                    if (position != -1) {
                                        taskList.remove(position);
                                        taskAdapter.notifyItemRemoved(position);
                                    }
                                }
                                @Override
                                public void onFailure(Exception e) {
                                    Toast.makeText(getContext(), "Delete failed", Toast.LENGTH_SHORT).show();
                                }
                            });
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
    }
}
