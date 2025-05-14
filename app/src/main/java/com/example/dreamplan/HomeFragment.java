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
import com.example.dreamplan.database.FirebaseDatabaseManager;
import com.example.dreamplan.database.Section;
import com.google.android.gms.tasks.Task;
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

public class HomeFragment extends Fragment {

    private RecyclerView rvSections;
    private SectionAdapter sectionAdapter;
 //   private List<Section> sectionList;
  //  private DatabaseManager dbManager;
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
    //    dbManager = new DatabaseManager(getContext());



        // Insert predefined sections if not already inserted
    //    dbManager.insertMainSectionsIfNotExist();

    //    refreshTaskCounts();

        // Set up RecyclerView
    //    sectionList = dbManager.getAllSections();
        sectionAdapter = new SectionAdapter(sectionList, getContext(), this);
//        sectionAdapter.setOnSectionActionListener(new SectionAdapter.OnSectionActionListener() {
//            @Override
//            public void onEditSection(Section section) {
//                showEditSectionDialog(section);
//            }
//
//            @Override
//            public void onDeleteSection(Section section) {
//                showDeleteConfirmationDialog(section);
//            }
//        });

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
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTaskCounts();
        // Show FAB again when returning to HomeFragment
        if (getActivity() != null && getActivity() instanceof MainActivity) {
            FloatingActionButton btnAddSection = getActivity().findViewById(R.id.btnAddSection);
            if (btnAddSection != null) {
                btnAddSection.setVisibility(View.VISIBLE);
                btnAddSection.setOnClickListener(v -> showAddSectionDialog());
            }
        }
        refreshTaskCounts();
    }

    private final BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshTaskCounts();
        }
    };

    // Method to show the Edit Section dialog
//    public void showEditSectionDialog(Section section) {
//        Dialog sectionDialog = new Dialog(requireContext());
//        sectionDialog.setContentView(R.layout.task_input_dialog);
//
//        // Get views
//        EditText sectionName = sectionDialog.findViewById(R.id.et_section_name);
//        EditText notes = sectionDialog.findViewById(R.id.et_notes);
//        Button saveButton = sectionDialog.findViewById(R.id.btn_save_section);
//        ImageView[] colorCircles = {
//                sectionDialog.findViewById(R.id.color_circle_1),
//                sectionDialog.findViewById(R.id.color_circle_2),
//                sectionDialog.findViewById(R.id.color_circle_3),
//                sectionDialog.findViewById(R.id.color_circle_4),
//                sectionDialog.findViewById(R.id.color_circle_5),
//                sectionDialog.findViewById(R.id.color_circle_6),
//                sectionDialog.findViewById(R.id.color_circle_7)
//        };
//
//        String[] colors = {"#CCE1F2", "#C6F8E5", "#FBF7D5", "#F9DED7", "#F5CDDE", "#E2BEF1", "#D3D3D3"};
//        final String[] selectedColor = {section.getColor()};
//
//        // Set initial values
//        sectionName.setText(section.getName());
//        notes.setText(section.getNotes());
//
//        // Highlight current color
//        for (int i = 0; i < colors.length; i++) {
//            if (colors[i].equalsIgnoreCase(section.getColor())) {
//                colorCircles[i].setBackground(getColorCircleDrawable(colors[i], true));
//            } else {
//                colorCircles[i].setBackground(getColorCircleDrawable(colors[i], false));
//            }
//
//            final int index = i;
//            colorCircles[i].setOnClickListener(v -> {
//                selectedColor[0] = colors[index];
//                // Update UI
//                for (int j = 0; j < colors.length; j++) {
//                    colorCircles[j].setBackground(getColorCircleDrawable(
//                            colors[j],
//                            j == index
//                    ));
//                }
//            });
//        }
//
//        saveButton.setOnClickListener(v -> {
//                    section.setName(sectionName.getText().toString());
//                    section.setColor(selectedColor[0]);
//                    section.setNotes(notes.getText().toString());
//
//                    Map<String, Object> updates = new HashMap<>();
//                    updates.put("name", section.getName());
//                    updates.put("color", section.getColor());
//                    updates.put("notes", section.getNotes());
//
//                    dbManager.updateSection(section.getId(), updates, new FirebaseDatabaseManager.DatabaseCallback<Void>() {
//                        @Override
//                        public void onSuccess(Void result) {
//                            int position = sectionList.indexOf(section);
//                            if (position != -1) {
//                                sectionAdapter.notifyItemChanged(position);
//                            }
//                            dialog.dismiss();
//                        }
//
//                        @Override
//                        public void onFailure(Exception e) {
//                            Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                });
//        sectionDialog.show();
//    }

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

        // Set up color selection
        for (int i = 0; i < colorCircles.length; i++) {
            final int index = i;
            colorCircles[i].setOnClickListener(v -> {
                selectedColor[0] = String.valueOf(index + 1);
                updateColorSelectionUI(colorCircles, index);
            });

            // Set initial selection state
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
                    sectionList.add(newSection);
                    sectionAdapter.notifyItemInserted(sectionList.size() - 1);
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

        // Proceed with normal deletion
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
        // Open the section details page
        SectionDetailFragment fragment = SectionDetailFragment.newInstance(section);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack("section_detail");
        transaction.commit();
    }

    private void loadTaskCounts() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Today's tasks
        dbManager.getTaskCountForDate(today, new FirebaseDatabaseManager.DatabaseCallback<Integer>() {
            @Override
            public void onSuccess(Integer count) {
                tvTasksTodayNumber.setText(String.valueOf(count));
            }
            @Override
            public void onFailure(Exception e) {
                tvTasksTodayNumber.setText("0");
            }
        });

        // Tomorrow's tasks
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 1);
        String tomorrow = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());

        dbManager.getTaskCountForDate(tomorrow, new FirebaseDatabaseManager.DatabaseCallback<Integer>() {
            @Override
            public void onSuccess(Integer count) {
                tvTasksTomorrowNumber.setText(String.valueOf(count));
            }
            @Override
            public void onFailure(Exception e) {
                tvTasksTomorrowNumber.setText("0");
            }
        });

        cal.add(Calendar.DATE, 6);
        String weekLater = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());

        dbManager.getTaskCountForDate(today, new FirebaseDatabaseManager.DatabaseCallback<Integer>() {
            @Override
            public void onSuccess(Integer count) {
                tvTasksWeekNumber.setText(String.valueOf(count));
            }
            @Override
            public void onFailure(Exception e) {
                tvTasksWeekNumber.setText("0");
            }
        });
    }


    private void loadSections() {
        dbManager.getSections(new FirebaseDatabaseManager.DatabaseCallback<List<Section>>() {
            @Override
            public void onSuccess(List<Section> sections) {
                sectionList.clear();
                sectionList.addAll(sections);
                sectionAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Error loading sections", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createDefaultSections() {
        // Manually create default sections if they don't exist
        String[] defaultSections = {"Work", "Study", "Personal"};
        String[] colors = {"#FFB74D", "#81C784", "#64B5F6"};

        for (int i = 0; i < defaultSections.length; i++) {
            Section section = new Section(null, defaultSections[i], colors[i], "", true);
            dbManager.addSection(section, new FirebaseDatabaseManager.DatabaseCallback<String>() {
                @Override
                public void onSuccess(String sectionId) {
                    section.setId(sectionId);
                    sectionList.add(section);
                    sectionAdapter.notifyItemInserted(sectionList.size() - 1);
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("HomeFragment", "Error creating default section", e);
                }
            });
        }
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
}
