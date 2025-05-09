package com.example.dreamplan;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.example.dreamplan.database.DatabaseManager;
import com.example.dreamplan.database.Section;
import com.example.dreamplan.database.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.lang.ref.WeakReference;
import java.text.BreakIterator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddTaskFragment extends Fragment {
    private Section section;
    private int selectedColorResId = R.drawable.circle_background_1;
    private boolean isOneTime = true;
    private ImageView imgTaskIcon; // Store reference to avoid multiple findViewById calls
    private Spinner scheduleSpinner;
    private Switch timeSwitch;
    private RadioGroup timeOptionsGroup;
    private TimePicker timePicker;
    private Button btnStartDate;
    private LinearLayout recurringOptions;
    private int selectedIconResId = R.drawable.star;

    private LinearLayout oneTimeSection;
    private LinearLayout timeSuboptions;
    private Button btnDate;
    private EditText etTaskTitle;
    private EditText etDescription;

    private Task taskToEdit;
    private Button btnDelete;
    private int dbDate;

    private boolean isEditMode = false;
    private int taskIdToEdit = -1;

    public static AddTaskFragment newInstance(Section section, Task task) {
        AddTaskFragment fragment = new AddTaskFragment();
        Bundle args = new Bundle();
        args.putSerializable("section", section);
        if (task != null) {
            args.putParcelable("task", task);
        }
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            section = (Section) getArguments().getSerializable("section");
            taskToEdit = (Task) getArguments().getSerializable("task");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_task, container, false);

        btnDelete = view.findViewById(R.id.btnDeleteTask);
        btnDelete = view.findViewById(R.id.btnDeleteTask);
        btnDelete.setOnClickListener(v -> deleteTask());
//        btnDelete.setOnClickListener(v -> deleteTask());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize all views - only declare once as class fields
        etTaskTitle = view.findViewById(R.id.et_task_title);
        etDescription = view.findViewById(R.id.et_description);
        btnDate = view.findViewById(R.id.btn_date);
        imgTaskIcon = view.findViewById(R.id.img_task_icon);
        LinearLayout colorOptions = view.findViewById(R.id.color_options);
        MaterialButton btnOneTime = view.findViewById(R.id.btn_one_time);
        MaterialButton btnRegular = view.findViewById(R.id.btn_regular);

        // Initialize recurring options views
        recurringOptions = view.findViewById(R.id.recurring_options);
        btnStartDate = view.findViewById(R.id.btn_start_date);
        scheduleSpinner = view.findViewById(R.id.spinner_schedule);
        timeSwitch = view.findViewById(R.id.switch_time);
        timeOptionsGroup = view.findViewById(R.id.radio_time_options);
        timePicker = view.findViewById(R.id.time_picker);
        oneTimeSection = view.findViewById(R.id.one_time_section);
        timeSuboptions = view.findViewById(R.id.time_suboptions);

        // Set default icon
        imgTaskIcon.setImageResource(selectedIconResId);
        imgTaskIcon.setTag(selectedIconResId);
        imgTaskIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imgTaskIcon.setAdjustViewBounds(true);

//        btnDelete = view.findViewById(R.id.btnDeleteTask);
//        btnDelete.setOnClickListener(v -> deleteTask());



        // Get section from arguments
        if (getArguments() != null) {
            section = (Section) getArguments().getSerializable("section");
        }

        // Setup components
        setupImageSelection();
        setupColorSelection(imgTaskIcon, colorOptions);
        setupDatePicker(btnDate);
        setupRecurringOptions();
        setupTimeOptions();

        MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.toggle_recurrence);

        // Set initial visibility states
        oneTimeSection.setVisibility(View.VISIBLE);  // Show one-time by default
        recurringOptions.setVisibility(View.GONE);   // Hide recurring by default

        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                isOneTime = checkedId == R.id.btn_one_time;
                oneTimeSection.setVisibility(isOneTime ? View.VISIBLE : View.GONE);
                recurringOptions.setVisibility(isOneTime ? View.GONE : View.VISIBLE);

                // Debug log
                Log.d("TASK_TYPE", "Task type changed to: " + (isOneTime ? "One-time" : "Recurring"));
            }
        });
        toggleGroup.check(R.id.btn_one_time);

        // Set initial toggle state
        toggleGroup.check(isOneTime ? R.id.btn_one_time : R.id.btn_regular);


        if (getArguments() != null && getArguments().containsKey("task")) {
            Task taskToEdit = (Task) getArguments().getParcelable("task");
            if (taskToEdit != null) {
                populateTaskData(taskToEdit);
            }
        }


        // Set click listeners
        view.findViewById(R.id.btn_back).setOnClickListener(v -> getParentFragmentManager().popBackStack());
        view.findViewById(R.id.btn_save).setOnClickListener(v -> {
            try {
                if (TextUtils.isEmpty(etTaskTitle.getText())) {
                    Toast.makeText(getContext(), "Please enter a task title", Toast.LENGTH_SHORT).show();
                    return;
                }

                saveTask(
                        etTaskTitle.getText().toString(),
                        etDescription.getText().toString()
                );
            } catch (Exception e) {
                Log.e("AddTaskFragment", "Error saving task", e);
                Toast.makeText(getContext(), "Error saving task", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void setupTimeOptions() {
        timeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            timeSuboptions.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        timeOptionsGroup.setOnCheckedChangeListener((group, checkedId) -> {
            timePicker.setVisibility(checkedId == R.id.radio_custom ? View.VISIBLE : View.GONE);
        });
    }

    private void setupImageSelection() {
        imgTaskIcon.setOnClickListener(v -> {
            // Reset to default state
            imgTaskIcon.setImageResource(R.drawable.ic_default_task);
            imgTaskIcon.setBackgroundResource(R.drawable.circle_with_border);

            IconSelectionFragment fragment = new IconSelectionFragment();

            fragment.setIconSelectionListener(iconResId -> {
                selectedIconResId = iconResId;
                // Run on UI thread to ensure immediate update
                new Handler(Looper.getMainLooper()).post(() -> {
                    try {
                        // Clear any tint or color filters
                        imgTaskIcon.clearColorFilter();

                        // Set the new icon with proper scaling
                        imgTaskIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                        imgTaskIcon.setImageResource(iconResId);

                        // Store the selection
                        imgTaskIcon.setTag(iconResId);

                        Log.d("ICON_DEBUG", "Icon set to: " + getResources().getResourceName(iconResId));
                    } catch (Resources.NotFoundException e) {
                        Log.e("ICON_ERROR", "Icon not found", e);
                        imgTaskIcon.setImageResource(R.drawable.ic_default_task);
                    }
                });
            });

            // Execute transaction safely
            try {
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack("icon_selection")
                        .commit();
            } catch (Exception e) {
                Log.e("FRAGMENT", "Transaction failed", e);
            }
        });
    }

    private void setupColorSelection(ImageView colorPreview, LinearLayout colorOptions) {
        int[] colorDrawables = {
                R.drawable.circle_background_1,
                R.drawable.circle_background_2,
                R.drawable.circle_background_3,
                R.drawable.circle_background_4,
                R.drawable.circle_background_5,
                R.drawable.circle_background_6,
                R.drawable.circle_background_7
        };

        for (int drawableId : colorDrawables) {
            ImageView colorOption = new ImageView(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dpToPx(40),
                    dpToPx(40)
            );
            params.setMargins(dpToPx(8), 0, dpToPx(8), 0);

            colorOption.setLayoutParams(params);
            colorOption.setImageResource(drawableId);
            colorOption.setOnClickListener(v -> {
                selectedColorResId = drawableId;
                //      colorPreview.setImageResource(drawableId);
            });

            colorOptions.addView(colorOption);
        }
    }

    private void setupDatePicker(Button btnDate) {
        try {
            // Initialize date format
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault());

            // Set initial date only if the button has no text
            if (TextUtils.isEmpty(btnDate.getText().toString())) {
                btnDate.setText(sdf.format(new Date()));
            }

            btnDate.setOnClickListener(v -> {
                try {
                    // date picker with current selection
                    MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                            .setTitleText("Select Date")
                            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                            .build();

                    // Handle date selection
                    datePicker.addOnPositiveButtonClickListener(selection -> {
                        try {
                            if (selection != null) {
                                Date selectedDate = new Date(selection);
                                btnDate.setText(sdf.format(selectedDate));
                            }
                        } catch (Exception e) {
                            Log.e("DatePicker", "Error formatting selected date", e);
                            btnDate.setText(sdf.format(new Date())); // Fallback to current date
                        }
                    });

                    // Show the date picker safely
                    if (getParentFragmentManager() != null && !isRemoving()) {
                        datePicker.show(getParentFragmentManager(), "DATE_PICKER");
                    }
                } catch (Exception e) {
                    Log.e("DatePicker", "Error showing date picker", e);
                    Toast.makeText(getContext(), "Error showing date picker", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e("DatePicker", "Error initializing date picker", e);
        }
    }


    private void saveTask(String title, String description) {
        try {
            if (TextUtils.isEmpty(title)) {
                Toast.makeText(getContext(), "Title is required", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseManager dbManager = new DatabaseManager(requireContext());
            Task task;
            String displayDate = "";
            String dbFormattedDate = "";

            if (!isOneTime) {
                // Recurring task handling
                displayDate = btnStartDate.getText().toString();
                if (TextUtils.isEmpty(displayDate)) {
                    Toast.makeText(getContext(), "Please select start date", Toast.LENGTH_SHORT).show();
                    return;
                }

                SimpleDateFormat displayFormat = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault());
                SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                dbFormattedDate = dbFormat.format(displayFormat.parse(displayDate));

                String timePreference = "";
                if (timeSwitch.isChecked()) {
                    int selectedId = timeOptionsGroup.getCheckedRadioButtonId();
                    if (selectedId == R.id.radio_custom) {
                        timePreference = String.format(Locale.getDefault(), "%02d:%02d",
                                timePicker.getHour(),
                                timePicker.getMinute());
                    } else if (selectedId != -1) {
                        RadioButton selected = requireView().findViewById(selectedId);
                        timePreference = selected.getText().toString();
                    }
                }

                task = new Task(
                        isEditMode ? taskIdToEdit : 0, // Only use ID for edits
                        title.trim(),
                        description,
                        "", // Empty deadline for recurring
                        selectedColorResId,
                        selectedIconResId,
                        section.getId(),
                        true,
                        dbFormattedDate,
                        scheduleSpinner.getSelectedItem().toString(),
                        timePreference
                );
            } else {
                // One-time task handling
                displayDate = btnDate.getText().toString();
                if (TextUtils.isEmpty(displayDate)) {
                    Toast.makeText(getContext(), "Please select due date", Toast.LENGTH_SHORT).show();
                    return;
                }

                SimpleDateFormat displayFormat = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault());
                SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                dbFormattedDate = dbFormat.format(displayFormat.parse(displayDate));

                task = new Task(
                        isEditMode ? taskIdToEdit : 0, // Only use ID for edits
                        title.trim(),
                        description,
                        dbFormattedDate,
                        selectedColorResId,
                        selectedIconResId,
                        section.getId(),
                        false,
                        "",
                        "",
                        ""
                );
            }

            // Save or update based on mode
            if (isEditMode) {
                boolean updated = dbManager.updateTask(task);
                Toast.makeText(getContext(),
                        updated ? "Task updated!" : "Update failed",
                        Toast.LENGTH_SHORT).show();
            } else {
                dbManager.saveTask(task);
                Toast.makeText(getContext(), "Task created!", Toast.LENGTH_SHORT).show();
            }

            // Refresh UI
            if (getParentFragment() instanceof SectionDetailFragment) {
                ((SectionDetailFragment) getParentFragment()).refreshTaskList();
            }
            requireContext().sendBroadcast(new Intent("TASK_UPDATED"));
            getParentFragmentManager().popBackStack();

        } catch (ParseException e) {
            Toast.makeText(getContext(), "Invalid date format", Toast.LENGTH_SHORT).show();
            Log.e("TASK_SAVE", "Date error", e);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error saving task", Toast.LENGTH_SHORT).show();
            Log.e("TASK_SAVE", "Error", e);
        }
    }


    private String getSelectedTimePreference() {
        if (!timeSwitch.isChecked()) {
            return null;
        }

        int selectedId = timeOptionsGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.radio_custom) {
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();
            return String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
        } else {
            RadioButton selected = requireView().findViewById(selectedId);
            return selected.getText().toString();
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void setupRecurringOptions() {
        // 1. Initialize Start Date Picker with default if empty
        if (TextUtils.isEmpty(btnStartDate.getText())) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault());
            btnStartDate.setText(sdf.format(new Date()));
            btnStartDate.setTag(MaterialDatePicker.todayInUtcMilliseconds());
        }

        // 2. Set up schedule spinner with validation
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.schedule_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        scheduleSpinner.setAdapter(adapter);
        scheduleSpinner.setSelection(1); // Default to first real option (skip hint if exists)

        // 3. Time selection setup
        timeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            timeSuboptions.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked) timePicker.setVisibility(View.GONE);
        });

        timeOptionsGroup.setOnCheckedChangeListener((group, checkedId) -> {
            timePicker.setVisibility(checkedId == R.id.radio_custom ? View.VISIBLE : View.GONE);
        });

        // Set initial click listener
        btnStartDate.setOnClickListener(v -> showDatePicker(btnStartDate));
    }

    public void showDatePicker(Button targetButton) {
        SimpleDateFormat displayFormat = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault());

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            try {
                Date selectedDate = new Date(selection);
                targetButton.setText(displayFormat.format(selectedDate));
                targetButton.setTag(selection); // Store the timestamp for validation
            } catch (Exception e) {
                Log.e("DatePicker", "Error formatting date", e);
                targetButton.setText("Select Date");
                targetButton.setTag(null);
            }
        });

        // Show safely
        if (getParentFragmentManager() != null && !isRemoving()) {
            datePicker.show(getParentFragmentManager(), "DATE_PICKER");
        }
    }

    private void populateTaskData(Task task) {
        try {
            isEditMode = true;
            taskIdToEdit = task.getId();

            MaterialButtonToggleGroup toggleGroup = getView().findViewById(R.id.toggle_recurrence);
            Button btnOneTime = getView().findViewById(R.id.btn_one_time);
            Button btnRegular = getView().findViewById(R.id.btn_regular);
            Button btnDate = getView().findViewById(R.id.btn_date);
            Button btnStartDate = getView().findViewById(R.id.btn_start_date);

            etTaskTitle.setText(task.getTitle());
            etDescription.setText(task.getNotes());
            selectedIconResId = task.getIconResId();
            selectedColorResId = task.getColorResId();
            imgTaskIcon.setImageResource(selectedIconResId);

            if (task.isRecurring()) {
                toggleGroup.check(R.id.btn_regular);
                btnStartDate.setText(formatDate(task.getStartDate()));

                if (scheduleSpinner != null && task.getSchedule() != null) {
                    ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) scheduleSpinner.getAdapter();
                    for (int i = 0; i < adapter.getCount(); i++) {
                        if (adapter.getItem(i).equals(task.getSchedule())) {
                            scheduleSpinner.setSelection(i);
                            break;
                        }
                    }
                }
            } else {
                toggleGroup.check(R.id.btn_one_time);  // Check one-time button
                btnDate.setText(formatDate(task.getDeadline()));
            }

            // Show delete button
            btnDelete.setVisibility(View.VISIBLE);

            // Ensure proper visibility of options
            oneTimeSection.setVisibility(task.isRecurring() ? View.GONE : View.VISIBLE);
            recurringOptions.setVisibility(task.isRecurring() ? View.VISIBLE : View.GONE);

        } catch (Exception e) {
            Log.e("EditTask", "Error loading task data", e);
            Toast.makeText(getContext(), "Error loading task", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteTask() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    DatabaseManager dbManager = new DatabaseManager(requireContext());
                    boolean deleted = dbManager.deleteTask(taskIdToEdit);
                    if (deleted) {
                        Toast.makeText(getContext(), "Task deleted", Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().popBackStack();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Helper method for date formatting
    private String formatDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateString;
        }
    }

    private String formatDateForDb(String displayDate) throws ParseException {
        SimpleDateFormat displayFormat = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault());
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dbFormat.format(displayFormat.parse(displayDate));
    }

}