package com.example.dreamplan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.dreamplan.database.DatabaseManager;
import com.example.dreamplan.database.Section;
import com.example.dreamplan.database.Task;
import com.google.android.material.datepicker.MaterialDatePicker;

public class AddTaskFragment extends Fragment {
    private Section section;
    private int selectedColorResId = R.drawable.circle_background_1; // Default

    public static AddTaskFragment newInstance(Section section) {
        AddTaskFragment fragment = new AddTaskFragment();
        Bundle args = new Bundle();
        args.putSerializable("section", section);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_task, container, false);

        if (getArguments() != null) {
            section = (Section) getArguments().getSerializable("section");
        }

        // Initialize views
        ImageView colorPreview = view.findViewById(R.id.selected_color_preview);
        LinearLayout colorOptions = view.findViewById(R.id.color_options);

        // Setup color selection
        setupColorSelection(colorPreview, colorOptions);

        // Setup back button
        view.findViewById(R.id.btn_back).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        // Setup save button
        view.findViewById(R.id.btn_save).setOnClickListener(v -> saveTask(view));

        // Setup date picker
        setupDatePicker(view);

        return view;
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
            ImageView colorOption = new ImageView(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dpToPx(40),
                    dpToPx(40)
            );
            params.setMargins(dpToPx(8), 0, dpToPx(8), 0);

            colorOption.setLayoutParams(params);
            colorOption.setImageResource(drawableId);
            colorOption.setOnClickListener(v -> {
                selectedColorResId = drawableId;
                colorPreview.setImageResource(drawableId);
            });

            colorOptions.addView(colorOption);
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void saveTask(View view) {
        String title = ((EditText) view.findViewById(R.id.et_task_title)).getText().toString();
        String description = ((EditText) view.findViewById(R.id.et_description)).getText().toString();
        String deadline = ((Button) view.findViewById(R.id.btn_date)).getText().toString();

        if (title.isEmpty()) {
            Toast.makeText(getContext(), "Title required", Toast.LENGTH_SHORT).show();
            return;
        }

        Task task = new Task(
                title,
                description,
                deadline,
                selectedColorResId, // Store the drawable resource ID
                section.getId()
        );

        new DatabaseManager(getContext()).saveTask(task);
        getParentFragmentManager().popBackStack();
    }

    private void setupDatePicker(View view) {
        Button btnDate = view.findViewById(R.id.btn_date);
        btnDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Date")
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                // Format the selected date
                String formattedDate = formatDate(selection);
                btnDate.setText(formattedDate);
            });

            datePicker.show(getParentFragmentManager(), "DATE_PICKER");
        });
    }

    private String formatDate(long timestamp) {
        // Implement your date formatting logic here
        return "Selected Date"; // Replace with actual formatted date
    }
}