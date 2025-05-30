package com.example.dreamplan.calendar;

import static java.security.AccessController.getContext;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.dreamplan.R;
import com.example.dreamplan.database.FirebaseDatabaseManager;
//import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.os.Handler;
import com.example.dreamplan.database.Task;


public class CalendarGridAdapter extends BaseAdapter {
    private Context context;
    private List<Date> dates;
    private Calendar currentCalendar;
    private Date selectedDate;
    private Date previouslySelectedDate = null;

    public CalendarGridAdapter(Context context, List<Date> dates) {
        this.context = context;
        this.dates = dates;
        this.currentCalendar = Calendar.getInstance();
        this.selectedDate = new Date();
    }

    public Date getSelectedDate() {
        return selectedDate;
    }

    @Override
    public int getCount() {
        return dates.size();
    }

    @Override
    public Date getItem(int position) {
        return dates.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_calendar_day, parent, false);
        }

        TextView dayText = view.findViewById(R.id.dayText);
        View dayIndicator = view.findViewById(R.id.dayIndicator);

        Date date = getItem(position);
        Calendar dateCalendar = Calendar.getInstance();
        dateCalendar.setTime(date);

        // Set day number
        dayText.setText(String.valueOf(dateCalendar.get(Calendar.DAY_OF_MONTH)));

        // Handle selection and coloring
        updateDayAppearance(dayText, dateCalendar, date);

        // Check for tasks asynchronously
        checkTasksForDate(date, dayIndicator);

        return view;
    }

    private void updateDayAppearance(TextView dayText, Calendar dateCalendar, Date date) {
        // Reset appearance first
        dayText.setBackgroundResource(0);

        // Get colors safely
        int currentMonthColor = getColorSafe(R.color.black);
        int otherMonthColor = getColorSafe(R.color.gray);
        int selectedTextColor = getColorSafe(R.color.white);

        if (isToday(date)) {
            dayText.setBackgroundResource(R.drawable.circle_today);
            dayText.setTextColor(selectedTextColor);
        }
        else if (isSameDay(date, selectedDate)) {
            dayText.setBackgroundResource(R.drawable.circle_selected);
            dayText.setTextColor(selectedTextColor);
            Log.d("CalendarDebug", "Setting selected style for: " + date);
        }
        else {
            dayText.setTextColor(
                    dateCalendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH)
                            ? currentMonthColor
                            : otherMonthColor
            );
        }
    }

    private int getColorSafe(int colorResId) {
        try {
            return ContextCompat.getColor(context, colorResId);
        } catch (Resources.NotFoundException e) {
            // Fallback colors
            if (colorResId == R.color.white) return Color.WHITE;
            if (colorResId == R.color.black) return Color.BLACK;
            return Color.GRAY;
        }
    }


    private boolean isToday(Date date) {
        return isSameDay(date, new Date());
    }

    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }

    public void setSelectedDate(Date date) {
        this.selectedDate = date;
        notifyDataSetChanged();
        this.previouslySelectedDate = this.selectedDate;
    }

    private void checkTasksForDate(Date date, View indicator) {
        String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);

        FirebaseDatabaseManager.getInstance().getTasksForDate(dateStr,
                new FirebaseDatabaseManager.DatabaseCallback<List<Task>>() {
                    @Override
                    public void onSuccess(List<Task> tasks) {
                        if (context instanceof Activity) {
                            ((Activity)context).runOnUiThread(() -> {
                                boolean hasTasks = !tasks.isEmpty();
                                boolean hasCompletedTasks = false;
                                boolean allCompleted = true;

                                if (hasTasks) {
                                    for (Task task : tasks) {
                                        if (task.isCompleted()) {
                                            hasCompletedTasks = true;
                                        } else {
                                            allCompleted = false;
                                        }
                                    }
                                }

                                indicator.setVisibility(hasTasks ? View.VISIBLE : View.GONE);

                                if (allCompleted) {
                                    indicator.setBackgroundResource(R.drawable.circle_completed);
                                } else if (hasCompletedTasks) {
                                    indicator.setBackgroundResource(R.drawable.circle_partial_completed);
                                } else if (hasTasks) {
                                    indicator.setBackgroundResource(R.drawable.circle_indicator);
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("Calendar", "Error checking tasks: " + e.getMessage());
                        if (context instanceof Activity) {
                            ((Activity)context).runOnUiThread(() -> {
                                indicator.setVisibility(View.GONE);
                            });
                        }
                    }
                });
    }


}