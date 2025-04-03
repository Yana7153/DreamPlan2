package com.example.dreamplan;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

    public static AddTaskFragment newInstance(Section section) {
        AddTaskFragment fragment = new AddTaskFragment();
        Bundle args = new Bundle();
        args.putSerializable("section", section);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_task, container, false);

        // Initialize all views
        imgTaskIcon = rootView.findViewById(R.id.img_task_icon);
        LinearLayout colorOptions = rootView.findViewById(R.id.color_options);
        Button btnDate = rootView.findViewById(R.id.btn_date);
        MaterialButton btnOneTime = rootView.findViewById(R.id.btn_one_time);
        MaterialButton btnRegular = rootView.findViewById(R.id.btn_regular);
        EditText etTaskTitle = rootView.findViewById(R.id.et_task_title);
        EditText etDescription = rootView.findViewById(R.id.et_description);

        // Initialize recurring options views
        recurringOptions = rootView.findViewById(R.id.recurring_options);
        btnStartDate = rootView.findViewById(R.id.btn_start_date);
        scheduleSpinner = rootView.findViewById(R.id.spinner_schedule);
        timeSwitch = rootView.findViewById(R.id.switch_time);
        timeOptionsGroup = rootView.findViewById(R.id.radio_time_options);
        timePicker = rootView.findViewById(R.id.time_picker);

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
        setupRecurringOptions(); // Initialize recurring options

        // Setup toggle group
        MaterialButtonToggleGroup toggleGroup = rootView.findViewById(R.id.toggle_recurrence);
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                isOneTime = checkedId == R.id.btn_one_time;
                recurringOptions.setVisibility(isOneTime ? View.GONE : View.VISIBLE);
            }
        });

        // Set initial toggle state
        toggleGroup.check(isOneTime ? R.id.btn_one_time : R.id.btn_regular);

        // Set click listeners
        rootView.findViewById(R.id.btn_back).setOnClickListener(v -> getParentFragmentManager().popBackStack());
        rootView.findViewById(R.id.btn_save).setOnClickListener(v -> saveTask(
                etTaskTitle.getText().toString(),
                etDescription.getText().toString(),
                btnDate.getText().toString()
        ));

        return rootView;
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
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault());
        btnDate.setText(sdf.format(new Date()));

        btnDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                Date selectedDate = new Date(selection);
                btnDate.setText(sdf.format(selectedDate));
            });

            datePicker.show(getParentFragmentManager(), "DATE_PICKER");
        });
    }

//    private void setupToggleButtons(Button oneTime, Button regular) {
//        if (oneTime == null || regular == null) {
//            Log.e("AddTaskFragment", "Toggle buttons not found");
//            return;
//        }
//
//        MaterialButtonToggleGroup toggleGroup = requireView().findViewById(R.id.toggle_recurrence);
//        toggleGroup.check(isOneTime ? R.id.btn_one_time : R.id.btn_regular);
//        recurringOptions.setVisibility(isOneTime ? View.GONE : View.VISIBLE);
//
//        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
//            if (isChecked) {
//                isOneTime = checkedId == R.id.btn_one_time;
//                recurringOptions.setVisibility(isOneTime ? View.GONE : View.VISIBLE);
//            }
//        });

//        updateToggleButtonStates(oneTime, regular);
//
//        oneTime.setOnClickListener(v -> {
//            isOneTime = true;
//            updateToggleButtonStates(oneTime, regular);
//            recurringOptions.setVisibility(View.GONE); // Hide recurring options
//        });
//
//        regular.setOnClickListener(v -> {
//            isOneTime = false;
//            updateToggleButtonStates(oneTime, regular);
//            recurringOptions.setVisibility(View.VISIBLE); // Show recurring options
//        });
 //   }

//    private void updateToggleButtonStates(Button oneTime, Button regular) {
//        if (oneTime == null || regular == null || recurringOptions == null) {
//            Log.e("AddTaskFragment", "Views not properly initialized");
//            return;
//        }
//
//        oneTime.setBackgroundResource(isOneTime ? R.drawable.btn_toggle_selected : R.drawable.btn_toggle_unselected);
//        regular.setBackgroundResource(!isOneTime ? R.drawable.btn_toggle_selected : R.drawable.btn_toggle_unselected);
//
//        int selectedTextColor = ContextCompat.getColor(requireContext(), android.R.color.white);
//        int unselectedTextColor = ContextCompat.getColor(requireContext(), android.R.color.black);
//
//        oneTime.setTextColor(getResources().getColor(isOneTime ? android.R.color.white : android.R.color.black));
//        regular.setTextColor(getResources().getColor(!isOneTime ? android.R.color.white : android.R.color.black));
//
//        // Show/hide recurring options
//        if (recurringOptions != null) {
//            recurringOptions.setVisibility(isOneTime ? View.GONE : View.VISIBLE);
//        }
//    }

    private void saveTask(String title, String description, String deadline) {
        try {
            // Validate required fields
            if (title == null || title.trim().isEmpty()) {
                Toast.makeText(getContext(), "Title required", Toast.LENGTH_SHORT).show();
                return;
            }

            // Safely handle null values
            String safeDescription = description != null ? description : "";
            String safeDeadline = deadline != null ? deadline : "";

            // Initialize recurring task fields
            String startDate = null;
            String schedule = null;
            String timePreference = null;

            if (!isOneTime) {
                // Safely get start date
                startDate = btnStartDate.getText() != null ?
                        btnStartDate.getText().toString() : "";

                // Safely get schedule
                schedule = scheduleSpinner.getSelectedItem() != null ?
                        scheduleSpinner.getSelectedItem().toString() : "";

                // Get time preference if enabled
                if (timeSwitch.isChecked() && timeOptionsGroup != null) {
                    int selectedId = timeOptionsGroup.getCheckedRadioButtonId();
                    if (selectedId == R.id.radio_custom && timePicker != null) {
                        timePreference = String.format(Locale.getDefault(),
                                "%02d:%02d",
                                timePicker.getHour(),
                                timePicker.getMinute());
                    } else if (selectedId != -1) {
                        RadioButton selected = requireView().findViewById(selectedId);
                        if (selected != null && selected.getText() != null) {
                            timePreference = selected.getText().toString();
                        }
                    }
                }
            }

            // Create and save task
            Task task = new Task(
                    title.trim(),
                    safeDescription,
                    safeDeadline,
                    selectedColorResId,
                    selectedIconResId,
                    section.getId(),
                    !isOneTime,
                    startDate,
                    schedule,
                    timePreference
            );

            new DatabaseManager(requireContext()).saveTask(task);

            // Refresh parent fragment
            if (getParentFragment() instanceof SectionDetailFragment) {
                ((SectionDetailFragment) getParentFragment()).refreshTaskList();
            }

            getParentFragmentManager().popBackStack();

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
        // 1. Start Date Picker
        btnStartDate.setOnClickListener(v -> showDatePicker(btnStartDate));

        // 2. Schedule Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.schedule_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        scheduleSpinner.setAdapter(adapter);

        // 3. Time Switch
        timeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            timeOptionsGroup.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        // Time Radio Options
        timeOptionsGroup.setOnCheckedChangeListener((group, checkedId) -> {
            timePicker.setVisibility(checkedId == R.id.radio_custom ? View.VISIBLE : View.GONE);
        });
    }

    private void showDatePicker(Button targetButton) {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            String date = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                    .format(new Date(selection));
            targetButton.setText(date);
        });

        datePicker.show(getParentFragmentManager(), "DATE_PICKER");
    }
}