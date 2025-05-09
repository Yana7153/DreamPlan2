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

    private MaterialButtonToggleGroup toggleGroup;



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

        toggleGroup = view.findViewById(R.id.toggle_recurrence);
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                isOneTime = (checkedId == R.id.btn_one_time);

                // Toggle visibility of the appropriate sections
                oneTimeSection.setVisibility(isOneTime ? View.VISIBLE : View.GONE);
                recurringOptions.setVisibility(isOneTime ? View.GONE : View.VISIBLE);

                Log.d("TASK_TYPE", "Task type changed to: " + (isOneTime ? "One-time" : "Recurring"));
            }
        });

        btnDate = view.findViewById(R.id.btn_date);
        btnStartDate = view.findViewById(R.id.btn_start_date);

        if (!isEditMode) {
            btnDate.setText("Select date");
            btnStartDate.setText("Select date");
        }

        setupDatePicker(btnDate, isEditMode && taskToEdit != null ? taskToEdit.getDeadline() : null);
        setupDatePicker(btnStartDate, isEditMode && taskToEdit != null ? taskToEdit.getStartDate() : null);


        // Get section from arguments
        if (getArguments() != null) {
            section = (Section) getArguments().getSerializable("section");
        }

        // Setup components
        setupImageSelection();
        setupColorSelection(imgTaskIcon, colorOptions);
        setupDatePicker(btnDate, isEditMode && taskToEdit != null ? taskToEdit.getDeadline() : null);
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
            taskToEdit = getArguments().getParcelable("task");
            if (taskToEdit != null) {
                isEditMode = true;
                populateTaskData(taskToEdit);
                btnDelete.setVisibility(View.VISIBLE); // Show delete button in edit mode
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
            IconSelectionFragment fragment = new IconSelectionFragment();

            // Pass current icon if editing
            if (isEditMode && taskToEdit != null) {
                fragment.setCurrentIcon(taskToEdit.getIconResId());
            }

            fragment.setIconSelectionListener(new IconSelectionFragment.IconSelectionListener() {
                @Override
                public void onIconSelected(int iconResId) {
                    // Update the big circle immediately
                    try {
                        imgTaskIcon.setImageResource(iconResId);
                        selectedIconResId = iconResId; // Save the selection
                    } catch (Resources.NotFoundException e) {
                        imgTaskIcon.setImageResource(R.drawable.ic_default_task);
                        selectedIconResId = R.drawable.ic_default_task;
                    }
                }
            });

            // Show icon selection
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void updateIconPreview(int iconResId) {
        try {
            // FIXED: Use requireActivity() for UI thread operations
            requireActivity().runOnUiThread(() -> {
                imgTaskIcon.setImageResource(iconResId);
                imgTaskIcon.setTag(iconResId);

                // Add visual feedback
                imgTaskIcon.animate()
                        .scaleX(1.1f)
                        .scaleY(1.1f)
                        .setDuration(100)
                        .withEndAction(() -> imgTaskIcon.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100))
                        .start();
            });
        } catch (Resources.NotFoundException e) {
            Log.e("ICON_ERROR", "Icon not found", e);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() ->
                        imgTaskIcon.setImageResource(R.drawable.ic_default_task));
            }
        }
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

    private void setupDatePicker(Button btnDate, String existingDate) {
        try {
            SimpleDateFormat displayFormat = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault());
            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            // Set initial text - show existing date in edit mode, placeholder for new tasks
            if (isEditMode && existingDate != null && !existingDate.isEmpty()) {
                try {
                    Date date = dbFormat.parse(existingDate);
                    btnDate.setText(displayFormat.format(date));
                } catch (ParseException e) {
                    btnDate.setText(existingDate); // Fallback to raw date if parsing fails
                }
            } else if (TextUtils.isEmpty(btnDate.getText().toString())) {
                btnDate.setText("Select date"); // Only set placeholder if empty
            }

            btnDate.setOnClickListener(v -> {
                try {
                    // Calculate initial selection - use existing date if available
                    long initialSelection = MaterialDatePicker.todayInUtcMilliseconds();
                    if (isEditMode && existingDate != null && !existingDate.isEmpty()) {
                        try {
                            Date date = dbFormat.parse(existingDate);
                            initialSelection = date.getTime();
                        } catch (ParseException e) {
                            Log.e("DatePicker", "Error parsing existing date", e);
                        }
                    }

                    MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                            .setTitleText("Select Date")
                            .setSelection(initialSelection)
                            .build();

                    datePicker.addOnPositiveButtonClickListener(selection -> {
                        try {
                            if (selection != null) {
                                Date selectedDate = new Date(selection);
                                btnDate.setText(displayFormat.format(selectedDate));
                            }
                        } catch (Exception e) {
                            Log.e("DatePicker", "Error formatting selected date", e);
                            btnDate.setText(displayFormat.format(new Date())); // Fallback to current date
                        }
                    });

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

            if (isEditMode && taskToEdit != null) {
                // EDIT MODE - Use existing dates if not changed
                task = new Task(
                        taskToEdit.getId(),
                        title.trim(),
                        description,
                        isOneTimeTask() ? getDateOrOriginal(btnDate, taskToEdit.getDeadline()) : taskToEdit.getDeadline(),
                        selectedColorResId,
                        selectedIconResId,
                        section.getId(),
                        !isOneTimeTask(),
                        isOneTimeTask() ? "" : getDateOrOriginal(btnStartDate, taskToEdit.getStartDate()),
                        isOneTimeTask() ? "" : scheduleSpinner.getSelectedItem().toString(),
                        isOneTimeTask() ? "" : getSelectedTimePreference()
                );
            } else {
                // CREATE MODE - Require date selection
                if (isOneTimeTask()) {
                    String displayDate = btnDate.getText().toString();
                    if (isDateEmpty(displayDate)) {
                        showDateRequiredToast("Please select due date");
                        return;
                    }
                    task = createOneTimeTask(title, description, displayDate);
                } else {
                    String displayDate = btnStartDate.getText().toString();
                    if (isDateEmpty(displayDate)) {
                        showDateRequiredToast("Please select start date");
                        return;
                    }
                    task = createRecurringTask(title, description, displayDate);
                }
            }

            saveOrUpdateTask(dbManager, task);

        } catch (Exception e) {
            Toast.makeText(getContext(), "Error saving task", Toast.LENGTH_SHORT).show();
            Log.e("TASK_SAVE", "Error", e);
        }
    }

    // Helper methods
    private boolean isOneTimeTask() {
        return toggleGroup.getCheckedButtonId() == R.id.btn_one_time;
    }

    private boolean isDateEmpty(String dateText) {
        return TextUtils.isEmpty(dateText) || dateText.equals("Select date");
    }

    private void showDateRequiredToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private String getDateOrOriginal(Button dateButton, String originalDate) {
        String displayDate = dateButton.getText().toString();
        if (isDateEmpty(displayDate)) return originalDate;
        try {
            return formatDateForDb(displayDate);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private Task createOneTimeTask(String title, String description, String displayDate) throws ParseException {
        return new Task(
                0, title.trim(), description, formatDateForDb(displayDate),
                selectedColorResId, selectedIconResId, section.getId(),
                false, "", "", ""
        );
    }

    private Task createRecurringTask(String title, String description, String displayDate) throws ParseException {
        return new Task(
                0, title.trim(), description, "",
                selectedColorResId, selectedIconResId, section.getId(),
                true, formatDateForDb(displayDate),
                scheduleSpinner.getSelectedItem().toString(),
                getSelectedTimePreference()
        );
    }

    private void saveOrUpdateTask(DatabaseManager dbManager, Task task) {
        if (isEditMode) {
            boolean updated = dbManager.updateTask(task);
            Toast.makeText(getContext(), updated ? "Task updated!" : "Update failed", Toast.LENGTH_SHORT).show();
        } else {
            dbManager.saveTask(task);
            Toast.makeText(getContext(), "Task created!", Toast.LENGTH_SHORT).show();
        }

        if (getParentFragment() instanceof SectionDetailFragment) {
            ((SectionDetailFragment) getParentFragment()).refreshTaskList();
        }
        getParentFragmentManager().popBackStack();
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
            taskToEdit = task;

            // Set basic fields
            etTaskTitle.setText(task.getTitle());
            etDescription.setText(task.getNotes());
            selectedIconResId = task.getIconResId();
            imgTaskIcon.setImageResource(selectedIconResId);
            selectedColorResId = task.getColorResId();

//            selectedIconResId = task.getIconResId();
//            try {
//                imgTaskIcon.setImageResource(selectedIconResId != 0 ?
//                        selectedIconResId : R.drawable.ic_default_task);
//            } catch (Resources.NotFoundException e) {
//                imgTaskIcon.setImageResource(R.drawable.ic_default_task);
//            }


            // Set the correct task type
            if (task.isRecurring()) {
                toggleGroup.check(R.id.btn_regular);
                oneTimeSection.setVisibility(View.GONE);
                recurringOptions.setVisibility(View.VISIBLE);

                // Set recurring task fields
                btnStartDate.setText(formatDateForDisplay(task.getStartDate()));
                setScheduleSelection(task.getSchedule());
            } else {
                toggleGroup.check(R.id.btn_one_time);
                oneTimeSection.setVisibility(View.VISIBLE);
                recurringOptions.setVisibility(View.GONE);

                // Set one-time task date
                btnDate.setText(formatDateForDisplay(task.getDeadline()));
            }

            // Show delete button
            btnDelete.setVisibility(View.VISIBLE);

        } catch (Exception e) {
            Log.e("EditTask", "Error loading task data", e);
            Toast.makeText(getContext(), "Error loading task", Toast.LENGTH_SHORT).show();
        }
    }

    private String formatDateForDisplay(String dbDate) {
        try {
            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault());
            Date date = dbFormat.parse(dbDate);
            return displayFormat.format(date);
        } catch (Exception e) {
            return dbDate; // fallback to raw date if formatting fails
        }
    }

    private void setScheduleSelection(String schedule) {
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) scheduleSpinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).equals(schedule)) {
                scheduleSpinner.setSelection(i);
                break;
            }
        }
    }
    private int getSchedulePosition(String schedule) {
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) scheduleSpinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).equals(schedule)) {
                return i;
            }
        }
        return 0;
    }
    private void deleteTask() {
        if (taskToEdit == null) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    DatabaseManager dbManager = new DatabaseManager(requireContext());
                    boolean deleted = dbManager.deleteTask(taskToEdit.getId());
                    if (deleted) {
                        Toast.makeText(getContext(), "Task deleted", Toast.LENGTH_SHORT).show();

                        // Refresh the task list in parent fragment
                        Fragment parent = getParentFragment();
                        if (parent instanceof SectionDetailFragment) {
                            ((SectionDetailFragment) parent).refreshTaskList();
                        }

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
            return dateString; // Return original if formatting fails
        }
    }

    private String formatDateForDb(String displayDate) throws ParseException {
        SimpleDateFormat displayFormat = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault());
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dbFormat.format(displayFormat.parse(displayDate));
    }

}