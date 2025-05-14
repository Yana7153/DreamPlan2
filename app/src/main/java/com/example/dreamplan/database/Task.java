package com.example.dreamplan.database;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.dreamplan.R;
import com.google.firebase.firestore.Exclude;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Task implements Parcelable {
    @Exclude
    private String id;
    private String title;
    private String notes;
    private String deadline;
    private int colorResId;
    private int iconResId;
    private String sectionId;

    private boolean isRecurring;
    private String startDate;
    private String schedule;
    private String timePreference;

    private String taskTypeDisplay;

    public Task() {}

    public Task(String id, String title, String notes, String dueDate, int colorResId, int iconResId, String sectionId,
                boolean isRecurring, String startDate, String schedule, String timePreference) {
        Log.d("TASK_DEBUG", "Creating task with icon: " + iconResId);
        this.id = id;
        this.title = title != null ? title : "";
        this.notes = notes != null ? notes : "";
        this.deadline = dueDate != null ? dueDate : "";
        this.colorResId = colorResId;
        this.iconResId = iconResId != 0 ? iconResId : R.drawable.star;
        this.sectionId = sectionId;
        this.isRecurring = isRecurring;
        this.startDate = startDate != null ? startDate : "";
        this.schedule = schedule != null ? schedule : "";
        this.timePreference = timePreference != null ? timePreference : "";

        if (isRecurring) {
            this.taskTypeDisplay = "🔄 Recurring • " +
                    (TextUtils.isEmpty(schedule) ? "" : schedule) +
                    (TextUtils.isEmpty(timePreference) ? "" : " • " + timePreference);
        } else {
            this.taskTypeDisplay = "📅 One-time • " +
                    (TextUtils.isEmpty(dueDate) ? "Not set" : dueDate);
        }
    }


    public String getTaskTypeDisplay() {
        if (isRecurring) {
            return "🔄 Recurring • " + (TextUtils.isEmpty(schedule) ? "" : schedule);
        }

        // For one-time tasks
        if (TextUtils.isEmpty(deadline)) {
            return "📅 No date set";
        }

        try {
            // First try parsing as yyyy-MM-dd (Firestore format)
            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = dbFormat.parse(deadline);
            SimpleDateFormat displayFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            return "📅 " + displayFormat.format(date);
        } catch (ParseException e1) {
            // If that fails, try other common formats
            try {
                SimpleDateFormat altFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                Date date = altFormat.parse(deadline);
                SimpleDateFormat displayFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
                return "📅 " + displayFormat.format(date);
            } catch (ParseException e2) {
                // If all parsing fails, just show the raw string
                return "📅 " + deadline;
            }
        }
    }

    // Getters and setters
    @Exclude
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }

    public int getColorResId() {
        return colorResId;
    }
    public void setColorResId(int colorResId) { this.colorResId = colorResId; }

    public String getSectionId() { return sectionId; }
    public void setSectionId(String sectionId) { this.sectionId = sectionId; }

    public int getIconResId() { return iconResId; }
    public void setIconResId(int iconResId) { this.iconResId = iconResId; }

    public boolean isRecurring() {
        return isRecurring;
    }

    public void setRecurring(boolean recurring) {
        isRecurring = recurring;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public String getTimePreference() {
        return timePreference;
    }

    public void setTimePreference(String timePreference) {
        this.timePreference = timePreference;
    }


    protected Task(Parcel in) {
        id = String.valueOf(in.readInt());
        title = in.readString();
        notes = in.readString();
        deadline = in.readString();
        colorResId = in.readInt();
        iconResId = in.readInt();
        sectionId = String.valueOf(in.readInt());
        isRecurring = in.readByte() != 0;
        startDate = in.readString();
        schedule = in.readString();
        timePreference = in.readString();
        taskTypeDisplay = in.readString();
    }

    public static final Creator<Task> CREATOR = new Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(Integer.parseInt(id));
        dest.writeString(title);
        dest.writeString(notes);
        dest.writeString(deadline);
        dest.writeInt(colorResId);
        dest.writeInt(iconResId);
        dest.writeInt(Integer.parseInt(sectionId));
        dest.writeByte((byte) (isRecurring ? 1 : 0));
        dest.writeString(startDate);
        dest.writeString(schedule);
        dest.writeString(timePreference);
        dest.writeString(taskTypeDisplay);
    }
}