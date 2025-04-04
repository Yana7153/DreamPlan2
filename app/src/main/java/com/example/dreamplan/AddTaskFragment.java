package com.example.dreamplan;

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
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.dreamplan.database.DatabaseManager;
import com.example.dreamplan.database.Section;
import com.example.dreamplan.database.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.lang.ref.WeakReference;
import java.text.BreakIterator;
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

    public static AddTaskFragment newInstance(Section section) {
        AddTaskFragment fragment = new AddTaskFragment();
        Bundle args = new Bundle();
        args.putSerializable("section", section);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Just inflate the layout here
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

//    private void setupToggleGroup() {
//        MaterialButtonToggleGroup toggleGroup = requireView().findViewById(R.id.toggle_recurrence);
//
//        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
//            if (isChecked) {
//                boolean isOneTime = checkedId == R.id.btn_one_time;
//                oneTimeSection.setVisibility(isOneTime ? View.VISIBLE : View.GONE);
//                recurringOptions.setVisibility(isOneTime ? View.GONE : View.VISIBLE);
//
//                // Set default date when section becomes visible
//                if (isOneTime && btnDate.getText().toString().isEmpty()) {
//                    SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault());
//                    btnDate.setText(sdf.format(new Date()));
//                }
//
//
//            }
//        });
//
//        // Set initial state
//        toggleGroup.check(R.id.btn_one_time);
//    }

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
                    // Create date picker with current selection
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

            // FOR RECURRING TASKS ONLY
            if (!isOneTime) {
                String startDate = btnStartDate.getText().toString();
                String schedule = scheduleSpinner.getSelectedItem().toString();
                String timePreference = "";

                // Validate recurring fields
                if (TextUtils.isEmpty(startDate)) {
                    Toast.makeText(getContext(), "Please select start date", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get time preference if set
                if (timeSwitch.isChecked()) {
                    int selectedId = timeOptionsGroup.getCheckedRadioButtonId();
                    if (selectedId == R.id.radio_custom) {
                        timePreference = String.format("%02d:%02d",
                                timePicker.getHour(),
                                timePicker.getMinute());
                    } else if (selectedId != -1) {
                        RadioButton selected = requireView().findViewById(selectedId);
                        timePreference = selected.getText().toString();
                    }
                }

                // Create RECURRING task (ignore one-time fields completely)
// When creating a RECURRING task:
                Task task = new Task(
                        title.trim(),
                        description,
                        "", // Empty deadline for recurring
                        selectedColorResId,
                        selectedIconResId,
                        section.getId(),
                        true, // THIS MUST BE TRUE FOR RECURRING TASKS
                        startDate,
                        schedule,
                        timePreference
                );

                new DatabaseManager(requireContext()).saveTask(task);
            }
            // FOR ONE-TIME TASKS (original code)
            else {
                String dueDate = btnDate.getText().toString();
                if (TextUtils.isEmpty(dueDate)) {
                    Toast.makeText(getContext(), "Please select due date", Toast.LENGTH_SHORT).show();
                    return;
                }

                Task task = new Task(
                        title.trim(),
                        description,
                        dueDate,
                        selectedColorResId,
                        selectedIconResId,
                        section.getId(),
                        false, // Not recurring
                        "", // Empty start date
                        "", // Empty schedule
                        ""  // Empty time preference
                );

                new DatabaseManager(requireContext()).saveTask(task);
            }

            // Refresh UI
            requireActivity().runOnUiThread(() -> {
                if (getParentFragment() instanceof SectionDetailFragment) {
                    ((SectionDetailFragment) getParentFragment()).refreshTaskList();
                }
                getParentFragmentManager().popBackStack();
            });

        } catch (Exception e) {
            Log.e("TASK_SAVE", "Error saving task", e);
            Toast.makeText(getContext(), "Error saving task", Toast.LENGTH_SHORT).show();
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

    private void showDatePicker(Button targetButton) {
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
}