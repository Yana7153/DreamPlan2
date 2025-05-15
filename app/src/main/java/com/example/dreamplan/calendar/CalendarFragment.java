package com.example.dreamplan.calendar;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dreamplan.R;
import com.example.dreamplan.TaskAdapter;
import com.example.dreamplan.database.FirebaseDatabaseManager;
import com.example.dreamplan.database.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment {
    private GridView calendarGrid;
    private TextView monthYearText;
    private TextView selectedDateText;
    private TextView emptyStateText;
    private RecyclerView tasksRecyclerView;
    private Calendar currentCalendar;
    private CalendarGridAdapter calendarAdapter;
    private TaskAdapter tasksAdapter;
    private FirebaseDatabaseManager dbManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentCalendar = Calendar.getInstance();
        dbManager = FirebaseDatabaseManager.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_calendar, container, false);

        try {
            calendarGrid = rootView.findViewById(R.id.calendarGrid);
            monthYearText = rootView.findViewById(R.id.monthYearText);
            selectedDateText = rootView.findViewById(R.id.selectedDateText);
            tasksRecyclerView = rootView.findViewById(R.id.tasksRecyclerView);
            emptyStateText = rootView.findViewById(R.id.emptyStateText);

            if (calendarGrid == null || monthYearText == null ||
                    selectedDateText == null || tasksRecyclerView == null) {
                throw new IllegalStateException("Critical views not found in layout");
            }

            setupCalendarGrid();
            setupTasksList();
            setupNavigation(rootView);
            loadTasksForDate(new Date());

        } catch (Exception e) {
            Log.e("CalendarFragment", "Initialization error", e);
            // Show a simple error message if in production
            Toast.makeText(getContext(), "Calendar loading failed", Toast.LENGTH_SHORT).show();
        }

        return rootView;
    }

    private void setupCalendarGrid() {
        try {
            List<Date> dates = getDatesForMonth(currentCalendar);
            calendarAdapter = new CalendarGridAdapter(requireContext(), dates);
            calendarGrid.setAdapter(calendarAdapter);

            calendarGrid.setOnItemClickListener((parent, view, position, id) -> {
                Date selectedDate = calendarAdapter.getItem(position);
                calendarAdapter.setSelectedDate(selectedDate);
                loadTasksForDate(selectedDate);
            });

            updateMonthHeader();
        } catch (Exception e) {
            Log.e("CalendarFragment", "Error setting up calendar grid", e);
            showErrorView("Couldn't load calendar");
        }
    }

    private List<Date> getDatesForMonth(Calendar calendar) {
        List<Date> dates = new ArrayList<>();
        Calendar tempCalendar = (Calendar) calendar.clone();

        // Set to first day of month
        tempCalendar.set(Calendar.DAY_OF_MONTH, 1);

        // Add previous month's days if needed
        int firstDayOfWeek = tempCalendar.get(Calendar.DAY_OF_WEEK);
        int daysFromPrevMonth = firstDayOfWeek - Calendar.SUNDAY;
        if (daysFromPrevMonth > 0) {
            tempCalendar.add(Calendar.DAY_OF_MONTH, -daysFromPrevMonth);
            for (int i = 0; i < daysFromPrevMonth; i++) {
                dates.add(tempCalendar.getTime());
                tempCalendar.add(Calendar.DAY_OF_MONTH, 1);
            }
        }

        // Add current month's days
        tempCalendar.set(Calendar.DAY_OF_MONTH, 1);
        int daysInMonth = tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 0; i < daysInMonth; i++) {
            dates.add(tempCalendar.getTime());
            tempCalendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Add next month's days if needed
        int remainingCells = 42 - dates.size();
        for (int i = 0; i < remainingCells; i++) {
            dates.add(tempCalendar.getTime());
            tempCalendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return dates;
    }

    private void setupTasksList() {
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        tasksAdapter = new TaskAdapter(new ArrayList<>(), requireContext());
        tasksRecyclerView.setAdapter(tasksAdapter);
    }

    private void loadTasksForDate(Date date) {
        if (getActivity() == null) return;

        try {
            // Update selected date text
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
            selectedDateText.setText(sdf.format(date));

            // Show loading state
            emptyStateText.setVisibility(View.GONE);
            tasksRecyclerView.setVisibility(View.VISIBLE);
            tasksAdapter.updateTasks(new ArrayList<>());

            // Format date for Firebase
            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String dateString = dbFormat.format(date);

            // Load tasks from Firebase
            FirebaseDatabaseManager.getInstance().getTasksForDate(dateString,
                    new FirebaseDatabaseManager.DatabaseCallback<List<Task>>() {
                        @Override
                        public void onSuccess(List<Task> tasks) {
                            Activity activity = getActivity();
                            if (activity != null) {
                                activity.runOnUiThread(() -> updateTaskList(tasks));
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Activity activity = getActivity();
                            if (activity != null) {
                                activity.runOnUiThread(() -> {
                                    showErrorView("Couldn't load tasks");
                                    Log.e("CalendarFragment", "Error loading tasks", e);
                                });
                            }
                        }
                    });
        } catch (Exception e) {
            showErrorView("Error loading date");
            Log.e("CalendarFragment", "Date formatting error", e);
        }
    }

    private void setupNavigation(View rootView) {
        rootView.findViewById(R.id.btnPrevMonth).setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            updateCalendar();
        });

        rootView.findViewById(R.id.btnNextMonth).setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            updateCalendar();
        });
    }

    private void updateCalendar() {
        calendarAdapter = new CalendarGridAdapter(requireContext(), getDatesForMonth(currentCalendar));
        calendarGrid.setAdapter(calendarAdapter);
        updateMonthHeader();
    }

    private void updateMonthHeader() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        monthYearText.setText(sdf.format(currentCalendar.getTime()));
    }

    private void updateTaskList(List<Task> tasks) {
        if (tasks.isEmpty()) {
            tasksRecyclerView.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
            emptyStateText.setText("No tasks for this day");
        } else {
            emptyStateText.setVisibility(View.GONE);
            tasksRecyclerView.setVisibility(View.VISIBLE);
            tasksAdapter.updateTasks(tasks);
        }
    }

    private void showErrorView(String message) {
        if (emptyStateText != null) {
            emptyStateText.setVisibility(View.VISIBLE);
            emptyStateText.setText(message);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}