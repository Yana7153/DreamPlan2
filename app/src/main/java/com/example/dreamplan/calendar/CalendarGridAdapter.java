package com.example.dreamplan.calendar;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.dreamplan.R;
import com.example.dreamplan.database.DatabaseManager;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CalendarGridAdapter extends BaseAdapter {
    private Context context;
    private List<Date> dates;
    private Calendar currentCalendar;
    private Date selectedDate;

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
        int day = dateCalendar.get(Calendar.DAY_OF_MONTH);
        dayText.setText(String.valueOf(day));

        // Highlight today
        if (isToday(date)) {
            dayText.setBackgroundResource(R.drawable.circle_today);
            dayText.setTextColor(ContextCompat.getColor(context, R.color.white));
        }
        // Highlight selected date
        else if (isSameDay(date, selectedDate)) {
            dayText.setBackgroundResource(R.drawable.circle_selected);
            dayText.setTextColor(ContextCompat.getColor(context, R.color.white));
        }
        // Normal day
        else {
            try {
                // Set day number color with fallback
                int textColor = ContextCompat.getColor(context,
                        dateCalendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH) ?
                                R.color.black : R.color.gray);
                dayText.setTextColor(textColor);
            } catch (Resources.NotFoundException e) {
                // Fallback to default colors
                dayText.setTextColor(Color.BLACK);  // Use Android default colors
            }
        }

        // Show indicator if day has tasks
        dayIndicator.setVisibility(hasTasks(date) ? View.VISIBLE : View.GONE);

        return view;
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
            DatabaseManager dbManager = new DatabaseManager(context);
            boolean hasTasks = dbManager.hasTasksForDate(date);
            dbManager.close();
            return hasTasks;
        } catch (Exception e) {
            Log.e("CalendarGrid", "Error checking tasks", e);
            return false;
        }
    }

    public void setSelectedDate(Date date) {
        this.selectedDate = date;
        notifyDataSetChanged();
    }
}