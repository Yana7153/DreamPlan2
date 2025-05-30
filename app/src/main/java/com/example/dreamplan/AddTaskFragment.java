package com.example.dreamplan;

import static com.example.dreamplan.database.FirebaseDatabaseManager.*;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
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
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.dreamplan.database.FirebaseDatabaseManager;
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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import java.util.HashMap;
import java.util.Map;

public class AddTaskFragment extends Fragment {
    private Section section;
    private int selectedColorResId = R.drawable.circle_background_1;
    private boolean isOneTime = true;
    private ImageView imgTaskIcon;
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

    private boolean isSaving = false;

    private FirebaseFirestore db;
    private String userId;
    private FirebaseDatabaseManager dbManager;

    private String iconResName = "star";
    private String selectedIconName = "star";
    private boolean iconSelectionLock = false;

    private static final String SAVED_ICON_ID = "selected_icon_id";
    private static final String SAVED_ICON_NAME = "selected_icon_name";

    private Button btnEndDate;
    private String selectedEndDate = "";

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
        dbManager = FirebaseDatabaseManager.getInstance();
        if (getArguments() != null) {
            section = (Section) getArguments().getSerializable("section");
            taskToEdit = getArguments().getParcelable("task");
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_task, container, false);
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
        ImageView btnBack = view.findViewById(R.id.btn_back);

        // Initialize recurring options views
        recurringOptions = view.findViewById(R.id.recurring_options);
        btnStartDate = view.findViewById(R.id.btn_start_date);
        scheduleSpinner = view.findViewById(R.id.spinner_schedule);
        timeSwitch = view.findViewById(R.id.switch_time);
        timeOptionsGroup = view.findViewById(R.id.radio_time_options);
        timePicker = view.findViewById(R.id.time_picker);
        oneTimeSection = view.findViewById(R.id.one_time_section);
        timeSuboptions = view.findViewById(R.id.time_suboptions);

        btnEndDate = view.findViewById(R.id.btn_end_date); // Changed from btnEndTime
        setupDatePicker(btnEndDate, isEditMode && taskToEdit != null ? taskToEdit.getEndDate() : null);
        // Set default icon
        imgTaskIcon = view.findViewById(R.id.img_task_icon);
        imgTaskIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imgTaskIcon.setAdjustViewBounds(false);

        btnDelete = view.findViewById(R.id.btnDeleteTask);
        if (taskToEdit != null) {
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            btnDelete.setVisibility(View.GONE);
        }

        btnDelete.setOnClickListener(v -> showDeleteConfirmation());

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

        if (savedInstanceState != null) {
            selectedIconResId = savedInstanceState.getInt("selected_icon", R.drawable.star);
            iconSelectionLock = savedInstanceState.getBoolean("icon_lock", false);
            updateIconPreview(selectedIconResId);
        }

        if (getArguments() != null && getArguments().containsKey("task")) {
            taskToEdit = getArguments().getParcelable("task");
            if (taskToEdit != null) {
                populateTaskData(taskToEdit);
            }
        }

        if (!isEditMode || taskToEdit == null) {
            imgTaskIcon.setImageResource(selectedIconResId);
        }

        btnDate = view.findViewById(R.id.btn_date);
        btnStartDate = view.findViewById(R.id.btn_start_date);

        if (!isEditMode) {
            btnDate.setText("Select date");
            btnStartDate.setText("Select date");
        }

        setupDatePicker(btnDate, isEditMode && taskToEdit != null ? taskToEdit.getDeadline() : null);
        setupDatePicker(btnStartDate, isEditMode && taskToEdit != null ? taskToEdit.getStartDate() : null);
        setupImageSelection();

        if (getArguments() != null) {
            section = (Section) getArguments().getSerializable("section");
        }

        // Setup components
        setupColorSelection(imgTaskIcon, colorOptions);
        setupDatePicker(btnDate, isEditMode && taskToEdit != null ? taskToEdit.getDeadline() : null);
        setupRecurringOptions();
        setupTimeOptions();

        MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.toggle_recurrence);

        // Set initial visibility states
        oneTimeSection.setVisibility(View.VISIBLE);
        recurringOptions.setVisibility(View.GONE);

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
                if (btnDelete != null) {
                    btnDelete.setVisibility(View.VISIBLE);
                }
            }
        }


        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new HomeFragment())
                        .commit();
            }
        });

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

            if (checkedId == R.id.radio_morning) {
                timePicker.setHour(9);  // 9 AM
                timePicker.setMinute(0);
            } else if (checkedId == R.id.radio_noon) {
                timePicker.setHour(13); // 1 PM
                timePicker.setMinute(0);
            } else if (checkedId == R.id.radio_evening) {
                timePicker.setHour(18);  // 6 PM
                timePicker.setMinute(0);
            }
        });
    }

    private IconSelectionFragment.IconSelectionListener iconSelectionListener =
            new IconSelectionFragment.IconSelectionListener() {
                @Override
                public void onIconSelected(int iconResId, String iconName) {
                    if (!isAdded()) return;

                    selectedIconResId = iconResId;
                    selectedIconName = iconName;

                    imgTaskIcon.setImageResource(iconResId);

                    if (isEditMode && taskToEdit != null) {
                        taskToEdit.setIconResId(iconResId);
                        taskToEdit.setIconResName(iconName);
                    }

                    Log.d("ICON_DEBUG", "Icon selected - ID: " + iconResId + ", Name: " + iconName);
                }
            };


    private void persistIconSelection(int resId, String name) {
        // Store in SharedPreferences as backup
        requireContext().getSharedPreferences("icon_prefs", Context.MODE_PRIVATE)
                .edit()
                .putInt("selected_icon_res", resId)
                .putString("selected_icon_name", name)
                .apply();
    }

    private void resetIconSelection() {
        selectedIconResId = R.drawable.star;
        selectedIconName = "star";
        updateIconPreview(selectedIconResId);
        persistIconSelection(selectedIconResId, selectedIconName);
    }

    private void setupImageSelection() {
        imgTaskIcon.setOnClickListener(v -> {
            IconSelectionFragment fragment = IconSelectionFragment.newInstance(selectedIconResId);

            fragment.setIconSelectionListener((iconResId, iconName) -> {
                selectedIconResId = iconResId;
                selectedIconName = iconName;
                updateIconPreview(iconResId);

                if (isEditMode && taskToEdit != null) {
                    taskToEdit.setIconResId(iconResId);
                    taskToEdit.setIconResName(iconName);
                }
            });

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void updateIconPreview(int resId) {
        try {
            if (imgTaskIcon != null) {
                imgTaskIcon.setImageResource(resId);
                imgTaskIcon.invalidate(); // Force redraw
            }
        } catch (Resources.NotFoundException e) {
            Log.e("ICON_ERROR", "Icon resource not found", e);
            imgTaskIcon.setImageResource(R.drawable.star);
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

        colorOptions.removeAllViews();

        int initialSelection = isEditMode && taskToEdit != null ?
                taskToEdit.getColorResId() : R.drawable.circle_background_1;

        for (int i = 0; i < colorDrawables.length; i++) {
            int drawableId = colorDrawables[i];

            // Create container FrameLayout
            FrameLayout container = new FrameLayout(requireContext());
            FrameLayout.LayoutParams containerParams = new FrameLayout.LayoutParams(
                    dpToPx(44),
                    dpToPx(44)
            );
            containerParams.setMargins(dpToPx(8), 0, dpToPx(8), 0);
            container.setLayoutParams(containerParams);

            // Create the color circle
            ImageView colorOption = new ImageView(requireContext());
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    dpToPx(36),
                    dpToPx(36),
                    Gravity.CENTER
            );
            colorOption.setLayoutParams(params);
            colorOption.setImageResource(drawableId);
            colorOption.setTag(drawableId);

            // Set selection state
            if (drawableId == initialSelection) {
                container.setBackgroundResource(R.drawable.circle_selected_border);
                selectedColorResId = drawableId;
            }

            colorOption.setOnClickListener(v -> {
                for (int j = 0; j < colorOptions.getChildCount(); j++) {
                    View child = colorOptions.getChildAt(j);
                    if (child instanceof FrameLayout) {
                        child.setBackground(null);
                    }
                }

                ((ViewGroup)v.getParent()).setBackgroundResource(R.drawable.circle_selected_border);

                selectedColorResId = (int) v.getTag();
            });

            container.addView(colorOption);
            colorOptions.addView(container);
        }
    }

    private int getColorForDrawable(int drawableId) {
        if (drawableId == R.drawable.circle_background_1) {
            return R.color.task_bg_1;
        } else if (drawableId == R.drawable.circle_background_2) {
            return R.color.task_bg_2;
        } else if (drawableId == R.drawable.circle_background_3) {
            return R.color.task_bg_3;
        } else if (drawableId == R.drawable.circle_background_4) {
            return R.color.task_bg_4;
        } else if (drawableId == R.drawable.circle_background_5) {
            return R.color.task_bg_5;
        } else if (drawableId == R.drawable.circle_background_6) {
            return R.color.task_bg_6;
        } else if (drawableId == R.drawable.circle_background_7) {
            return R.color.task_bg_7;
        } else {
            return R.color.task_bg_1;
        }
    }

    private void setupDatePicker(Button btnDate, String existingDate) {
        final SimpleDateFormat displayFormat = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault());
        final SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        btnDate.setTag("");

        if (existingDate != null && !existingDate.isEmpty()) {
            try {
                Date date = dbFormat.parse(existingDate);
                btnDate.setText(displayFormat.format(date));
                btnDate.setTag(existingDate);
            } catch (ParseException e1) {
                try {
                    Date date = displayFormat.parse(existingDate);
                    String dbDate = dbFormat.format(date);
                    btnDate.setText(existingDate);
                    btnDate.setTag(dbDate);
                } catch (ParseException e2) {
                    btnDate.setText(existingDate);
                    btnDate.setTag(existingDate);
                }
            }
        } else {
            btnDate.setText("Select date");
        }

        btnDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                try {
                    Date selectedDate = new Date(selection);
                    String displayDate = displayFormat.format(selectedDate);
                    String dbDate = dbFormat.format(selectedDate);

                    btnDate.setText(displayDate);
                    btnDate.setTag(dbDate);
                    Log.d("DATE_DEBUG", "Date selected - Display: " + displayDate + " | DB: " + dbDate);
                } catch (Exception e) {
                    Log.e("DATE_ERROR", "Error formatting date", e);
                    btnDate.setTag("");
                }
            });

            datePicker.show(getParentFragmentManager(), "DATE_PICKER");
        });
    }

    private String getSafeDateString(Button dateButton) {
        try {
            Object tag = dateButton.getTag();
            if (tag == null) return "";

            if (tag instanceof String) {
                return (String) tag;
            } else if (tag instanceof Long) {
                SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                return dbFormat.format(new Date((Long) tag));
            }
            return "";
        } catch (Exception e) {
            Log.e("DATE_ERROR", "Error getting date string", e);
            return "";
        }
    }

    private void saveTask(String title, String description) {
        if (isSaving) return;
        isSaving = true;

        Log.d("TASK_DEBUG", "Attempting to save task for section: " + section.getId());

        try {
            if (TextUtils.isEmpty(title)) {
                Toast.makeText(getContext(), "Please enter a task title", Toast.LENGTH_SHORT).show();
                isSaving = false;
                return;
            }

            String dueDate = isOneTime ? getSafeDateString(btnDate) : "";
            String startDate = !isOneTime ? getSafeDateString(btnStartDate) : "";
            String iconResName = this.selectedIconName;
            int iconResId = this.selectedIconResId;

            Log.d("ICON_DEBUG", "Final icon before save - ID: " + iconResId + ", Name: " + iconResName);
            Log.d("ICON_DEBUG", "Saving with icon - ID: " + selectedIconResId + ", Name: " + iconResName);

            if (!validateDates()) {
                isSaving = false;
                return;
            }

            if (selectedIconResId == 0) {
                selectedIconResId = R.drawable.star;
                selectedIconName = "star";
            }
            Log.d("ICON_SAVE", "Saving with icon - ID: " + selectedIconResId + ", Name: " + selectedIconName);

            if (!isOneTime) {
                if (TextUtils.isEmpty(startDate)) {
                    Toast.makeText(getContext(), "Please select a start date", Toast.LENGTH_SHORT).show();
                    isSaving = false;
                    return;
                }
                if (scheduleSpinner.getSelectedItem() == null) {
                    Toast.makeText(getContext(), "Please select a schedule", Toast.LENGTH_SHORT).show();
                    isSaving = false;
                    return;
                }
            }

            if (!isOneTime) {
                String schedule = scheduleSpinner.getSelectedItem().toString();
                if (!schedule.equals("Every day") &&
                        !schedule.equals("Weekdays only") &&
                        !schedule.equals("Weekends only")) {
                    Toast.makeText(getContext(), "Please select a valid schedule", Toast.LENGTH_SHORT).show();
                    isSaving = false;
                    return;
                }
            }

            // Debug logging
            Log.d("TASK_SAVE", "Creating task with: " +
                    "\nTitle: " + title +
                    "\nOneTime: " + isOneTime +
                    "\nDueDate: " + dueDate +
                    "\nStartDate: " + startDate +
                    "\nRecurring: " + !isOneTime +
                    "\nSchedule: " + (!isOneTime ? scheduleSpinner.getSelectedItem().toString() : "N/A"));


            Task task;
            if (isEditMode && taskToEdit != null) {
                task = taskToEdit;
                task.setTitle(title);
                task.setNotes(description);
                task.setDeadline(dueDate);
                task.setColorResId(selectedColorResId);
                task.setIconResId(selectedIconResId);
                task.setIconResName(selectedIconName);
                task.setRecurring(!isOneTime);
                task.setStartDate(startDate);
                task.setSchedule(!isOneTime ? scheduleSpinner.getSelectedItem().toString() : "");
                task.setTimePreference(!isOneTime ? getSelectedTimePreference() : "");

                if (taskToEdit.getCreatedAt() != null) {
                    task.setCreatedAt(taskToEdit.getCreatedAt());
                }
            } else {
                // Create new task
                task = new Task(
                        null,
                        title,
                        description,
                        dueDate,
                        selectedColorResId,
                        selectedIconResId,
                        selectedIconName,
                        section.getId(),
                        !isOneTime,
                        startDate,
                        !isOneTime ? scheduleSpinner.getSelectedItem().toString() : "",
                        !isOneTime ? getSelectedTimePreference() : "",
                        !isOneTime ? getSafeDateString(btnEndDate) : ""
                );
            }

            // Save the task
            dbManager.saveTask(task, new FirebaseDatabaseManager.DatabaseCallback<String>() {
                @Override
                public void onSuccess(String taskId) {
                    isSaving = false;
                    Log.d("TASK_DEBUG", "Successfully saved task with ID: " + taskId);
                    getParentFragmentManager().popBackStack();

                    Fragment parent = getParentFragment();
                    if (parent instanceof SectionDetailFragment) {
                        ((SectionDetailFragment) parent).refreshTaskList();
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    isSaving = false;
                    Log.e("TASK_DEBUG", "Failed to save task", e);
                    Toast.makeText(getContext(), "Failed to save: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

            Log.d("TASK_SAVE", "Saving task with deadline: " + task.getDeadline());
            Log.d("TASK_SAVE", "Is recurring: " + task.isRecurring());

        } catch (Exception e) {
            isSaving = false;
            Log.e("SaveTask", "Unexpected error", e);
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        //  Initialize Start Date Picker with default if empty
        if (TextUtils.isEmpty(btnStartDate.getText())) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault());
            btnStartDate.setText(sdf.format(new Date()));
            btnStartDate.setTag(MaterialDatePicker.todayInUtcMilliseconds());
        }

        //  Set up schedule spinner with validation
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.schedule_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        scheduleSpinner.setAdapter(adapter);
        scheduleSpinner.setSelection(1);

        //  Time selection setup
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
                targetButton.setTag(selection);
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

            etTaskTitle.setText(task.getTitle());
            etDescription.setText(task.getNotes());

            if (task.hasValidIcon()) {
                selectedIconResId = task.getIconResId(requireContext());
                selectedIconName = task.getIconResName();
                imgTaskIcon.setImageResource(selectedIconResId);
                Log.d("ICON_DEBUG", "Loaded task icon - ID: " + selectedIconResId + ", Name: " + selectedIconName);
            } else {
                selectedIconResId = R.drawable.star;
                selectedIconName = "star";
                imgTaskIcon.setImageResource(selectedIconResId);
            }

            if (task.isRecurring()) {
                toggleGroup.check(R.id.btn_regular);
                if (!TextUtils.isEmpty(task.getStartDate())) {
                    btnStartDate.setText(formatDateForDisplay(task.getStartDate()));
                    btnStartDate.setTag(task.getStartDate());
                }
                if (!TextUtils.isEmpty(task.getSchedule())) {
                    setScheduleSelection(task.getSchedule());
                }
            } else {
                toggleGroup.check(R.id.btn_one_time);
                if (!TextUtils.isEmpty(task.getDeadline())) {
                    btnDate.setText(formatDateForDisplay(task.getDeadline()));
                    btnDate.setTag(task.getDeadline());
                }
            }

            if (!TextUtils.isEmpty(task.getTimePreference())) {
                timeSwitch.setChecked(true);
            }

            if (!TextUtils.isEmpty(task.getEndDate())) {
                selectedEndDate = task.getEndDate();
                btnEndDate.setText(formatDateForDisplay(task.getEndDate()));
                btnEndDate.setTag(task.getEndDate());
            }

            Log.d("TASK_EDIT", "Loaded task data for editing");
        } catch (Exception e) {
            Log.e("EditTask", "Error loading task data", e);
         //   Toast.makeText(getContext(), "Error loading task", Toast.LENGTH_SHORT).show();
        }
    }

    private String formatDateForDisplay(String dbDate) {
        try {
            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault());
            Date date = dbFormat.parse(dbDate);
            return displayFormat.format(date);
        } catch (Exception e) {
            Log.e("DATE_FORMAT", "Error formatting date", e);
            return dbDate; // Return original if formatting fails
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

    private String formatDateForDb(String displayDate) throws ParseException {
        SimpleDateFormat displayFormat = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault());
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dbFormat.format(displayFormat.parse(displayDate));
    }

    private void navigateBackToSectionDetail() {
        if (getActivity() != null) {
            FragmentManager fm = getActivity().getSupportFragmentManager();
            Fragment sectionDetail = fm.findFragmentByTag("section_detail");

            if (sectionDetail == null) {
                sectionDetail = SectionDetailFragment.newInstance(section);
            }

            fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fm.beginTransaction()
                    .replace(R.id.fragment_container, sectionDetail, "section_detail")
                    .commit();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVED_ICON_ID, selectedIconResId);
        outState.putString(SAVED_ICON_NAME, selectedIconName);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            selectedIconResId = savedInstanceState.getInt(SAVED_ICON_ID, R.drawable.star);
            selectedIconName = savedInstanceState.getString(SAVED_ICON_NAME, "star");
            updateIconPreview(selectedIconResId);
        }
    }


    private void showDeleteConfirmation() {
        if (taskToEdit == null) return;

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Get reference to parent before async operations
                    Fragment parentFragment = getParentFragment();

                    dbManager.deleteTask(taskToEdit.getId(), new FirebaseDatabaseManager.DatabaseCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            requireActivity().runOnUiThread(() -> {
                                // Notify parent fragment to refresh
                                if (parentFragment instanceof SectionDetailFragment) {
                                    ((SectionDetailFragment) parentFragment).refreshTaskList();
                                }
                                // Close the edit screen
                                getParentFragmentManager().popBackStack();
                                Toast.makeText(getContext(), "Task deleted", Toast.LENGTH_SHORT).show();
                            });
                        }

                        @Override
                        public void onFailure(Exception e) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), "Delete failed", Toast.LENGTH_SHORT).show()
                            );
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private boolean validateDates() {
        if (!isOneTime) {
            String startDateStr = getSafeDateString(btnStartDate);
            String endDateStr = getSafeDateString(btnEndDate);

            if (!TextUtils.isEmpty(startDateStr) && !TextUtils.isEmpty(endDateStr)) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Date startDate = sdf.parse(startDateStr);
                    Date endDate = sdf.parse(endDateStr);

                    if (endDate.before(startDate)) {
                        Toast.makeText(getContext(),
                                "End date must be after start date",
                                Toast.LENGTH_SHORT).show();
                        return false;
                    }
                } catch (ParseException e) {
                    Log.e("DateValidation", "Error parsing dates", e);
                    return true; 
                }
            }
        }
        return true;
    }
}