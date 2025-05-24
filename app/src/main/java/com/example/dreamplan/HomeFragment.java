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
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dreamplan.adapters.SectionAdapter;
import com.example.dreamplan.database.FirebaseDatabaseManager;
import com.example.dreamplan.database.Section;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.os.Handler;

public class HomeFragment extends Fragment implements SectionAdapter.OnSectionActionListener {

    private RecyclerView rvSections;
    private SectionAdapter sectionAdapter;
    private TextView tvTasksTodayNumber;
    private TextView  tvTasksTomorrowNumber;
    private TextView  tvTasksWeekNumber;

    private FirebaseFirestore db;
    private String userId;
    private List<Section> sectionList = new ArrayList<>();
    private FirebaseDatabaseManager dbManager;




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvTasksTodayNumber = view.findViewById(R.id.tvTasksTodayNumber);
        tvTasksTomorrowNumber = view.findViewById(R.id.tvTasksTomorrowNumber);
        tvTasksWeekNumber = view.findViewById(R.id.tvTasksWeekNumber);
        rvSections = view.findViewById(R.id.rvSections);

        // Initialize adapter with action listener
        sectionAdapter = new SectionAdapter(sectionList, requireContext());
        sectionAdapter.setOnSectionActionListener(this);

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbManager = FirebaseDatabaseManager.getInstance();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbManager = FirebaseDatabaseManager.getInstance();
        loadSections();
        loadTaskCounts();

        view.findViewById(R.id.todayCard).setOnClickListener(v -> {
            navigateToTaskList("today");
        });

        view.findViewById(R.id.tomorrowCard).setOnClickListener(v -> {
            navigateToTaskList("tomorrow");
        });

        view.findViewById(R.id.weekCard).setOnClickListener(v -> {
            navigateToTaskList("week");
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTaskCounts();
        loadSections();

        if (getActivity() != null && getActivity() instanceof MainActivity) {
            FloatingActionButton btnAddSection = getActivity().findViewById(R.id.btnAddSection);
            if (btnAddSection != null) {
                btnAddSection.setVisibility(View.VISIBLE);
                btnAddSection.setOnClickListener(v -> showAddSectionDialog());
            }
        }

//        @Override
//        public void onEditSection(Section section) {
//            showEditSectionDialog(section);
//        }
//
//        @Override
//        public void onDeleteSection(Section section) {
//            showDeleteConfirmationDialog(section);
//        }
//
//        @Override
//        public void onOpenSection(Section section) {
//            openSectionDetail(section);
//        }
    }

    private final BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadTaskCounts();
        }
    };


    public void showAddSectionDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.task_input_dialog);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);

        EditText sectionName = dialog.findViewById(R.id.et_section_name);
        EditText notes = dialog.findViewById(R.id.et_notes);
        Button saveButton = dialog.findViewById(R.id.btn_save_section);

        ImageView[] colorCircles = {
                dialog.findViewById(R.id.color_circle_1),
                dialog.findViewById(R.id.color_circle_2),
                dialog.findViewById(R.id.color_circle_3),
                dialog.findViewById(R.id.color_circle_4),
                dialog.findViewById(R.id.color_circle_5),
                dialog.findViewById(R.id.color_circle_6),
                dialog.findViewById(R.id.color_circle_7)
        };

        final String[] selectedColor = {"1"};

        for (int i = 0; i < colorCircles.length; i++) {
            final int index = i;
            colorCircles[i].setOnClickListener(v -> {
                selectedColor[0] = String.valueOf(index + 1);
                updateColorSelectionUI(colorCircles, index);
            });

            GradientDrawable drawable = (GradientDrawable) colorCircles[i].getBackground();
            drawable.setStroke(i == 0 ? 4 : 0, Color.BLACK); // Only first selected initially
        }

        saveButton.setOnClickListener(v -> {
            String name = sectionName.getText().toString().trim();
            String notesText = notes.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Section name required", Toast.LENGTH_SHORT).show();
                return;
            }

            Section newSection = new Section(
                    null,
                    name,
                    selectedColor[0],
                    notesText,
                    false
            );

            dbManager.addSection(newSection, new FirebaseDatabaseManager.DatabaseCallback<String>() {
                @Override
                public void onSuccess(String documentId) {
                    newSection.setId(documentId);
                    sectionList.add(0, newSection);
                    sectionAdapter.notifyItemInserted(0);
                    dialog.dismiss();
                    Toast.makeText(getContext(), "Section created", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });

        dialog.show();
    }

    private void updateColorSelectionUI(ImageView[] colorCircles, int selectedIndex) {
        for (int i = 0; i < colorCircles.length; i++) {
            GradientDrawable drawable = (GradientDrawable) colorCircles[i].getBackground();
            drawable.setStroke(i == selectedIndex ? 4 : 0, Color.BLACK);
        }
    }

    private Drawable getColorCircleDrawable(String color, boolean isSelected) {
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(Color.parseColor(color));
        circle.setSize(40, 40);

        if (isSelected) {
            GradientDrawable border = new GradientDrawable();
            border.setShape(GradientDrawable.OVAL);
            border.setStroke(4, Color.BLACK);
            border.setSize(40, 40);

            LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{circle, border});
            return layerDrawable;
        } else {
            return circle;
        }
    }


    public void deleteSection(Section section) {
        if (section.isDefault()) {
            Toast.makeText(getContext(),
                    "Default sections cannot be deleted",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        dbManager.deleteSection(section.getId(), new FirebaseDatabaseManager.DatabaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                int position = sectionList.indexOf(section);
                if (position != -1) {
                    sectionList.remove(position);
                    sectionAdapter.notifyItemRemoved(position);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Delete failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void openSectionDetail(Section section) {
        SectionDetailFragment fragment = SectionDetailFragment.newInstance(section);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack("section_detail");
        transaction.commit();
    }

    private void loadTaskCounts() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(new Date());

        // Today's tasks
        dbManager.getTaskCountForDate(today, new FirebaseDatabaseManager.DatabaseCallback<Integer>() {
            @Override
            public void onSuccess(Integer count) {
                tvTasksTodayNumber.setText(String.valueOf(count));
                Log.d("TASK_COUNT", "Today's tasks: " + count);
            }
            @Override
            public void onFailure(Exception e) {
                tvTasksTodayNumber.setText("0");
                Log.e("TASK_COUNT", "Error getting today's tasks", e);
            }
        });

        // Tomorrow's tasks
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 1);
        String tomorrow = sdf.format(cal.getTime());

        dbManager.getTaskCountForDate(tomorrow, new FirebaseDatabaseManager.DatabaseCallback<Integer>() {
            @Override
            public void onSuccess(Integer count) {
                tvTasksTomorrowNumber.setText(String.valueOf(count));
                Log.d("TASK_COUNT", "Tomorrow's tasks: " + count);
            }
            @Override
            public void onFailure(Exception e) {
                tvTasksTomorrowNumber.setText("0");
                Log.e("TASK_COUNT", "Error getting tomorrow's tasks", e);
            }
        });

        // Week's tasks
        cal.add(Calendar.DATE, 6);
        String weekLater = sdf.format(cal.getTime());

        dbManager.getTaskCountForDateRange(today, weekLater, new FirebaseDatabaseManager.DatabaseCallback<Integer>() {
            @Override
            public void onSuccess(Integer count) {
                tvTasksWeekNumber.setText(String.valueOf(count));
                Log.d("TASK_COUNT", "Week's tasks: " + count);
            }
            @Override
            public void onFailure(Exception e) {
                tvTasksWeekNumber.setText("0");
                Log.e("TASK_COUNT", "Error getting week's tasks", e);
            }
        });
    }


    private void loadSections() {
        Log.d("FIREBASE", "Attempting to load sections...");
        dbManager.getSections(new FirebaseDatabaseManager.DatabaseCallback<List<Section>>() {
            @Override
            public void onSuccess(List<Section> sections) {
                Log.d("FIREBASE", "Successfully loaded " + sections.size() + " sections");
                sectionList.clear();
                sectionList.addAll(sections);
                sectionAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("FIREBASE", "Error loading sections", e);
                Toast.makeText(getContext(), "Error loading sections: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


//    public void onEditSection(Section section) {
//        showEditSectionDialog(section);
//    }
//
//
//    public void onDeleteSection(Section section) {
//        showDeleteConfirmationDialog(section);
//    }
//
//
//    public void onOpenSection(Section section) {
//        openSectionDetail(section);
//    }

//    private void createDefaultSections() {
//        String[] defaultSections = {"Work", "Study", "Personal"};
//        String[] colors = {"#FFB74D", "#81C784", "#64B5F6"};
//
//        for (int i = 0; i < defaultSections.length; i++) {
//            Section section = new Section(null, defaultSections[i], colors[i], "", true);
//            dbManager.addSection(section, new FirebaseDatabaseManager.DatabaseCallback<String>() {
//                @Override
//                public void onSuccess(String sectionId) {
//                    section.setId(sectionId);
//                    sectionList.add(section);
//                    sectionAdapter.notifyItemInserted(sectionList.size() - 1);
//                }
//
//                @Override
//                public void onFailure(Exception e) {
//                    Log.e("HomeFragment", "Error creating default section", e);
//                }
//            });
//        }
//    }

//    private void refreshTaskCounts() {
//        if (getActivity() == null || !isAdded()) return;
//
//        new Thread(() -> {
//            try {
//                DatabaseManager db = new DatabaseManager(requireContext());
//                final int todayCount = db.getTasksDueTodayCount();
//                final int tomorrowCount = db.getTasksDueTomorrowCount();
//                final int weekCount = db.getTasksDueInWeekCount();
//
//                getActivity().runOnUiThread(() -> {
//                    if (!tvTasksTodayNumber.getText().equals(String.valueOf(todayCount))) {
//                        tvTasksTodayNumber.setText(String.valueOf(todayCount));
//                    }
//                    if (!tvTasksTomorrowNumber.getText().equals(String.valueOf(tomorrowCount))) {
//                        tvTasksTomorrowNumber.setText(String.valueOf(tomorrowCount));
//                    }
//                    if (!tvTasksWeekNumber.getText().equals(String.valueOf(weekCount))) {
//                        tvTasksWeekNumber.setText(String.valueOf(weekCount));
//                    }
//                });
//            } catch (Exception e) {
//                Log.e("REFRESH", "Auto-refresh failed", e);
//            }
//        }).start();
//    }

//    private void showDeleteConfirmationDialog(Section section) {
//        new AlertDialog.Builder(requireContext())
//                .setTitle("Delete Section")
//                .setMessage("Are you sure you want to delete '" + section.getName() + "'?")
//                .setPositiveButton("Delete", (dialog, which) -> {
//                    int position = sectionList.indexOf(section);
//                    if (position != -1) {
//                        dbManager.deleteSection(section.getId());
//                        sectionList.remove(position);
//                        sectionAdapter.notifyItemRemoved(position);
//                        Toast.makeText(getContext(), "Section deleted", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .setNegativeButton("Cancel", null)
//                .show();
//    }

    private void addSection(Section section) {
        Map<String, Object> sectionData = new HashMap<>();
        sectionData.put("name", section.getName());
        sectionData.put("color", section.getColor());
        sectionData.put("notes", section.getNotes());
        sectionData.put("isDefault", false);

        db.collection("users").document(userId)
                .collection("sections")
                .add(sectionData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Section added", Toast.LENGTH_SHORT).show();
                });
    }

//    private void setupSectionAdapter() {
//        sectionAdapter = new SectionAdapter(sectionList, requireContext(), this);
//        sectionAdapter.setOnSectionActionListener(new SectionAdapter.OnSectionActionListener() {
//            @Override
//            public void onEditSection(Section section) {
//                updateSectionInFirebase(section);
//            }
//
//            @Override
//            public void onDeleteSection(Section section) {
//                deleteSectionFromFirebase(section);
//            }
//        });
//        sectionsRecyclerView.setAdapter(sectionAdapter);
//    }

    private void updateSectionInFirebase(Section section) {
        FirebaseDatabaseManager.getInstance().updateSection(section, new FirebaseDatabaseManager.DatabaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(requireContext(), "Section updated", Toast.LENGTH_SHORT).show();
                loadSections();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(requireContext(), "Failed to update section", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteSectionFromFirebase(Section section) {
        FirebaseDatabaseManager.getInstance().deleteSection(section.getId(), new FirebaseDatabaseManager.DatabaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(requireContext(), "Section deleted", Toast.LENGTH_SHORT).show();
                loadSections();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(requireContext(), "Failed to delete section", Toast.LENGTH_SHORT).show();
            }
        });
    }

//    private void showDeleteConfirmationDialog(Section section) {
//        new AlertDialog.Builder(context)
//                .setTitle("Delete Section")
//                .setMessage("Are you sure you want to delete this section? All tasks in it will be deleted.")
//                .setPositiveButton("Delete", (dialog, which) -> {
//                    if (actionListener != null) {
//                        actionListener.onDeleteSection(section);
//                    }
//                })
//                .setNegativeButton("Cancel", null)
//                .show();
//    }

    private void showSectionDialog(@Nullable Section section, boolean isEditMode) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.task_input_dialog);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext(), R.style.RoundedDialogTheme);


        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.task_input_dialog, null);
        builder.setView(dialogView);
        // Setup views
        TextView title = dialog.findViewById(R.id.dialog_title);
        EditText etName = dialog.findViewById(R.id.et_section_name);
        EditText etNotes = dialog.findViewById(R.id.et_notes);
        Button btnSave = dialog.findViewById(R.id.btn_save_section);
        Button btnDelete = dialog.findViewById(R.id.btnDeleteTask);
        LinearLayout colorPicker = dialog.findViewById(R.id.color_picker);

        // Configure based on mode
        title.setText(isEditMode ? "Edit Section" : "Add Section");
        btnDelete.setVisibility(isEditMode ? View.VISIBLE : View.GONE);



        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        }

        // Pre-fill for edit mode
        if (isEditMode && section != null) {
            etName.setText(section.getName());
            etNotes.setText(section.getNotes());
            int colorPos = Integer.parseInt(section.getColor());
            updateColorSelectionUI(colorPicker, colorPos);
        }

        // Color selection logic
        final int[] selectedColor = {isEditMode ? Integer.parseInt(section.getColor()) : 1};
        for (int i = 0; i < colorPicker.getChildCount(); i++) {
            final int position = i + 1;
            ImageView circle = (ImageView) colorPicker.getChildAt(i);
            circle.setOnClickListener(v -> {
                selectedColor[0] = position;
                updateColorSelectionUI(colorPicker, position);
            });
        }

        // Save button handler
        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Name required", Toast.LENGTH_SHORT).show();
                return;
            }

            Section targetSection = isEditMode ? section : new Section();
            targetSection.setName(name);
            targetSection.setNotes(etNotes.getText().toString().trim());
            targetSection.setColor(String.valueOf(selectedColor[0]));

            if (isEditMode) {
                updateSectionInFirebase(targetSection);
            } else {
                dbManager.addSection(targetSection, new FirebaseDatabaseManager.DatabaseCallback<String>() {
                    @Override
                    public void onSuccess(String documentId) {
                        targetSection.setId(documentId);
                        sectionList.add(targetSection);
                        sectionAdapter.notifyItemInserted(sectionList.size() - 1);
                        dialog.dismiss();
                        Toast.makeText(getContext(), "Section created", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), "Failed to create section: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
            dialog.dismiss();
        });

        // Delete button handler (only in edit mode)
        btnDelete.setOnClickListener(v -> {
            dialog.dismiss();
            showDeleteConfirmationDialog(section);
        });

        dialog.show();
    }


    public void onEditSection(Section section) {
        showSectionDialog(section, true);
    }


    public void onDeleteSection(Section section) {
        showDeleteConfirmationDialog(section);
    }


    public void onOpenSection(Section section) {
        openSectionDetail(section);
    }

    private void showEditSectionDialog(Section section) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.task_input_dialog);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);

        TextView dialogTitle = dialog.findViewById(R.id.dialog_title);
        dialogTitle.setText("Edit Section");

        EditText etName = dialog.findViewById(R.id.et_section_name);
        EditText etNotes = dialog.findViewById(R.id.et_notes);
        Button btnDelete = dialog.findViewById(R.id.btnDeleteTask);
        Button btnSave = dialog.findViewById(R.id.btn_save_section);
        LinearLayout colorPicker = dialog.findViewById(R.id.color_picker);

        // Initialize with current section color
        final int[] selectedColorHolder = {Integer.parseInt(section.getColor())};

        // Set current values
        etName.setText(section.getName());
        etNotes.setText(section.getNotes());
        updateColorSelectionUI(colorPicker, selectedColorHolder[0]);

        btnDelete.setVisibility(View.VISIBLE);
        // Color selection
        for (int i = 0; i < colorPicker.getChildCount(); i++) {
            final int position = i + 1;
            ImageView colorCircle = (ImageView) colorPicker.getChildAt(i);
            colorCircle.setOnClickListener(v -> {
                selectedColorHolder[0] = position; // Now this works
                updateColorSelectionUI(colorPicker, position);
            });
        }

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String notes = etNotes.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Section name required", Toast.LENGTH_SHORT).show();
                return;
            }

            section.setName(name);
            section.setNotes(notes);
            section.setColor(String.valueOf(selectedColorHolder[0])); // Use the holder here

            updateSectionInFirebase(section);
            dialog.dismiss();
        });

        btnDelete.setOnClickListener(v -> {
            dialog.dismiss();
            showDeleteConfirmationDialog(section);
        });

        dialog.show();
    }

    private void updateColorSelectionUI(LinearLayout colorPicker, int selectedPosition) {
        for (int i = 0; i < colorPicker.getChildCount(); i++) {
            ImageView colorCircle = (ImageView) colorPicker.getChildAt(i);
            GradientDrawable background = (GradientDrawable) colorCircle.getBackground();
            background.setStroke(i + 1 == selectedPosition ? 4 : 0, Color.BLACK);
        }
    }

    private void showDeleteConfirmationDialog(Section section) {
        new MaterialAlertDialogBuilder(requireContext(), R.style.RoundedDialogTheme)
                .setTitle("Delete Section")
                .setMessage("Are you sure you want to delete this section?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteSectionFromFirebase(section);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void navigateToTaskList(String filterType) {
        TaskListFragment fragment = TaskListFragment.newInstance(filterType);
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack("task_list");
        transaction.commit();
    }
}
