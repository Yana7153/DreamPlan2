package com.example.dreamplan;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.dreamplan.database.DatabaseManager;
import com.example.dreamplan.database.Section;
import com.example.dreamplan.database.Task;
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

        new Handler().postDelayed(() -> {
            imgTaskIcon.setImageResource(R.drawable.ic_work);
            Log.d("ICON_TEST", "Test icon should be visible now");
        }, 2000);

        // Initialize all views first
        imgTaskIcon = rootView.findViewById(R.id.img_task_icon);
        LinearLayout colorOptions = rootView.findViewById(R.id.color_options);
        Button btnDate = rootView.findViewById(R.id.btn_date);
        Button btnOneTime = rootView.findViewById(R.id.btn_one_time);
        Button btnRegular = rootView.findViewById(R.id.btn_regular);
        EditText etTaskTitle = rootView.findViewById(R.id.et_task_title);
        EditText etDescription = rootView.findViewById(R.id.et_description);

        // Get section from arguments
        if (getArguments() != null) {
            section = (Section) getArguments().getSerializable("section");
        }

        // Setup all components
        setupImageSelection();
        setupColorSelection(imgTaskIcon, colorOptions);
        setupDatePicker(btnDate);
        setupToggleButtons(btnOneTime, btnRegular);

        // Set click listeners
        rootView.findViewById(R.id.btn_back).setOnClickListener(v -> getParentFragmentManager().popBackStack());
        rootView.findViewById(R.id.btn_save).setOnClickListener(v -> saveTask(
                etTaskTitle.getText().toString(),
                etDescription.getText().toString(),
                btnDate.getText().toString()
        ));

        // Verify the ImageView is properly initialized
        Log.d("VIEW_CHECK", "imgTaskIcon is " + (imgTaskIcon == null ? "NULL" : "VALID"));

// Test resource availability
        try {
            getResources().getDrawable(R.drawable.ic_work);
            Log.d("RES_CHECK", "ic_work exists");
        } catch (Resources.NotFoundException e) {
            Log.e("RES_CHECK", "ic_work missing!", e);
        }

        return rootView;


    }

    private void setupImageSelection() {
        imgTaskIcon.setOnClickListener(v -> {
            try {
                // Clear any previous state
                imgTaskIcon.setImageResource(0);
                imgTaskIcon.setBackgroundResource(R.drawable.circle_with_border);

                IconSelectionFragment fragment = new IconSelectionFragment();
                fragment.setIconSelectionListener(iconResId -> {
                    imgTaskIcon.setImageResource(iconResId);
                    imgTaskIcon.setTag(iconResId);
                });

                // Use this transaction approach instead:
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack("icon_selection")
                        .commit();

            } catch (Exception e) {
                Log.e("ICON_SELECT", "Error in icon selection", e);
                Toast.makeText(getContext(), "Error opening icon selection", Toast.LENGTH_SHORT).show();
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

    private void setupToggleButtons(Button oneTime, Button regular) {
        updateToggleButtonStates(oneTime, regular);

        oneTime.setOnClickListener(v -> {
            isOneTime = true;
            updateToggleButtonStates(oneTime, regular);
            requireView().findViewById(R.id.time_selection).setVisibility(View.GONE);
        });

        regular.setOnClickListener(v -> {
            isOneTime = false;
            updateToggleButtonStates(oneTime, regular);
            requireView().findViewById(R.id.time_selection).setVisibility(View.VISIBLE);
        });
    }

    private void updateToggleButtonStates(Button oneTime, Button regular) {
        oneTime.setBackgroundResource(isOneTime ? R.drawable.btn_toggle_selected : R.drawable.btn_toggle_unselected);
        regular.setBackgroundResource(!isOneTime ? R.drawable.btn_toggle_selected : R.drawable.btn_toggle_unselected);
        oneTime.setTextColor(getResources().getColor(isOneTime ? android.R.color.white : android.R.color.black));
        regular.setTextColor(getResources().getColor(!isOneTime ? android.R.color.white : android.R.color.black));
    }

    private void saveTask(String title, String description, String deadline) {
        if (title.isEmpty()) {
            Toast.makeText(getContext(), "Title required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the selected icon (default if none selected)
        int selectedIconResId = imgTaskIcon.getTag() != null ?
                (int) imgTaskIcon.getTag() :
                R.drawable.ic_default_task;

        Task task = new Task(
                title,
                description,
                deadline,
                selectedColorResId,
                selectedIconResId,  // Use the selected icon
                section.getId()
        );

        try {
            new DatabaseManager(requireContext()).saveTask(task);

            // Refresh the task list in parent fragment
            if (getParentFragment() instanceof SectionDetailFragment) {
                ((SectionDetailFragment) getParentFragment()).refreshTaskList();
            }

            getParentFragmentManager().popBackStack();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error saving task", Toast.LENGTH_SHORT).show();
            Log.e("TASK_SAVE", "Error saving task", e);
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}