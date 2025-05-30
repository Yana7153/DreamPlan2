package com.example.dreamplan;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
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
import com.example.dreamplan.database.Section;
import com.example.dreamplan.database.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SectionDetailFragment extends Fragment {

private static final String ARG_SECTION = "section";
    private Section section;
    private TextView sectionName, sectionNotes, currentDate;
    private FloatingActionButton addTaskButton;
    private TaskAdapter taskAdapter;
    private List<Task> taskList = new ArrayList<>();
    private FirebaseDatabaseManager dbManager;
    private long lastOperationTime = 0;
    private static final long MIN_OPERATION_DELAY = 300;
    private final Object taskListLock = new Object();


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
    public void onStart() {
        super.onStart();
        FirebaseDatabaseManager.getInstance().addTaskChangeListener(taskChangeCallback);
    }


    private final FirebaseDatabaseManager.DatabaseCallback<Void> taskChangeCallback =
            new FirebaseDatabaseManager.DatabaseCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    if (isAdded() && !isDetached()) {
                        loadTasks();
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("Fragment", "Task update failed", e);
                }
            };

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
        ImageView btnBack = view.findViewById(R.id.btnBack);

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

        // Click listeners
        addTaskButton.setOnClickListener(v -> showAddTaskFragment());

        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new HomeFragment())
                        .commit();
            }
        });

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
        dbManager.getTasksForSection(section.getId(), new FirebaseDatabaseManager.DatabaseCallback<List<Task>>() {
            @Override
            public void onSuccess(List<Task> tasks) {
                List<Task> filteredTasks = new ArrayList<>();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String currentDate = sdf.format(new Date());

                for (Task task : tasks) {
                    if (task.isRecurring() && task.getEndDate() != null && !task.getEndDate().isEmpty()) {
                        try {
                            Date endDate = sdf.parse(task.getEndDate());
                            Date today = sdf.parse(currentDate);
                            if (today.after(endDate)) {
                                continue;
                            }
                        } catch (ParseException e) {
                            Log.e("SectionDetail", "Error parsing dates", e);
                        }
                    }
                    filteredTasks.add(task);
                }

                requireActivity().runOnUiThread(() -> {
                    taskList.clear();
                    taskList.addAll(filteredTasks);
                    taskAdapter.notifyDataSetChanged();
                });
            }
            @Override
            public void onFailure(Exception e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Failed to load tasks", Toast.LENGTH_SHORT).show()
                );
            }
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

    private void showAddTaskFragment() {
        AddTaskFragment fragment = AddTaskFragment.newInstance(section, null);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack("add_task")
                .commit();
    }

    private void setupTaskClickListeners() {
        taskAdapter.setOnTaskClickListener(task -> {
            AddTaskFragment fragment = AddTaskFragment.newInstance(section, task);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        taskAdapter.setOnTaskDeleteListener(task -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Delete Task")
                    .setMessage("Are you sure you want to delete this task?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        dbManager.deleteTask(task.getId(), new FirebaseDatabaseManager.DatabaseCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                requireActivity().runOnUiThread(() -> {
                                    int position = taskList.indexOf(task);
                                    if (position != -1) {
                                        taskList.remove(position);
                                        taskAdapter.notifyItemRemoved(position);
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                requireActivity().runOnUiThread(() ->
                                        Toast.makeText(requireContext(), "Failed to delete task", Toast.LENGTH_SHORT).show()
                                );
                            }
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

//    private void deleteTask(Task task) {
//        getView().setEnabled(false);
//
//        long currentTime = System.currentTimeMillis();
//        if (currentTime - lastOperationTime < MIN_OPERATION_DELAY) {
//            return;
//        }
//        lastOperationTime = currentTime;
//
//        dbManager.deleteTask(task.getId(), new FirebaseDatabaseManager.DatabaseCallback<Void>() {
//            @Override
//            public void onSuccess(Void result) {
//                requireActivity().runOnUiThread(() -> {
//                    taskAdapter.removeTaskById(task.getId());
//                    getView().setEnabled(true);
//                    Toast.makeText(requireContext(), "Task deleted", Toast.LENGTH_SHORT).show();
//                });
//            }
//
//            @Override
//            public void onFailure(Exception e) {
//                requireActivity().runOnUiThread(() -> {
//                    getView().setEnabled(true);
//                    Toast.makeText(requireContext(), "Failed to delete task", Toast.LENGTH_SHORT).show();
//                });
//            }
//        });
//    }
}
