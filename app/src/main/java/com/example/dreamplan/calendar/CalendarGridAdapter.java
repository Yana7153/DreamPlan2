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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Handler;

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
        // Default colors with fallbacks
        int currentMonthColor = getColorSafe(R.color.black);
        int otherMonthColor = getColorSafe(R.color.gray);
        int selectedColor = getColorSafe(R.color.white);

        // Clear previous state
        dayText.setBackgroundResource(0);

        if (isToday(date)) {
            dayText.setBackgroundResource(R.drawable.circle_today);
            dayText.setTextColor(selectedColor);
        }
        else if (isSameDay(date, selectedDate)) {
            dayText.setBackgroundResource(R.drawable.circle_selected);
            dayText.setTextColor(selectedColor);
        }
        else {
            dayText.setTextColor(
                    dateCalendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH)
                            ? currentMonthColor : otherMonthColor
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

    private boolean hasTasks(Date date) {
        try {
            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String dateString = dbFormat.format(date);

            // This needs to be implemented in FirebaseDatabaseManager
            FirebaseDatabaseManager.getInstance().getTaskCountForDate(dateString,
                    new FirebaseDatabaseManager.DatabaseCallback<Integer>() {
                        @Override
                        public void onSuccess(Integer count) {
                            // You'll need to update the view here if needed
                            // This is tricky with BaseAdapter - consider switching to RecyclerView
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e("CalendarGrid", "Error checking tasks", e);
                        }
                    });

            // Temporary solution - return false and update when data loads
            return false;
        } catch (Exception e) {
            Log.e("CalendarGrid", "Error checking tasks", e);
            return false;
        }
    }

    public void setSelectedDate(Date date) {
        this.selectedDate = date;
        notifyDataSetChanged();
        this.previouslySelectedDate = this.selectedDate;
    }

    private void checkTasksForDate(Date date, View indicator) {
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateString = dbFormat.format(date);

        FirebaseDatabaseManager.getInstance().hasTasksForDate(dateString,
                new FirebaseDatabaseManager.DatabaseCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean hasTasks) {
                        if (context != null) {
                            ((Activity) context).runOnUiThread(() -> {
                                indicator.setVisibility(hasTasks ? View.VISIBLE : View.GONE);
                            });
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("CalendarGrid", "Error checking tasks", e);
                    }
                });
    }

//    public void setSelectedDate(Date date) {
//        this.previouslySelectedDate = this.selectedDate;
//        this.selectedDate = date;
//        notifyDataSetChanged();
//    }
}