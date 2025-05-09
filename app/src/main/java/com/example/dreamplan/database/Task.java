package com.example.dreamplan.database;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.dreamplan.R;

public class Task implements Parcelable {
    private int id;
    private String title;
    private String notes;
    private String deadline;
    private int colorResId; // Changed from 'color' to store drawable resource ID
    private int iconResId;
    private int sectionId;

    private boolean isRecurring;
    private String startDate;
    private String schedule;
    private String timePreference;

    private String taskTypeDisplay;

    public Task(int id, String title, String notes, String dueDate, int colorResId, int iconResId, int sectionId,
                boolean isRecurring, String startDate, String schedule, String timePreference) {
        Log.d("TASK_DEBUG", "Creating task with icon: " + iconResId);
        this.id = id;
        this.title = title != null ? title : "";
        this.notes = notes != null ? notes : "";
        this.deadline = dueDate != null ? dueDate : "";  // Fixed: Set deadline from dueDate parameter
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
            // For recurring tasks, ALWAYS show at least "Recurring"
            return "🔄 Recurring";
        } else {
            // For one-time tasks, show the deadline
            return "📅 " + (TextUtils.isEmpty(deadline) ? "No deadline" : deadline);
        }
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

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

    public int getSectionId() { return sectionId; }
    public void setSectionId(int sectionId) { this.sectionId = sectionId; }

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
        id = in.readInt();
        title = in.readString();
        notes = in.readString();
        deadline = in.readString();
        colorResId = in.readInt();
        iconResId = in.readInt();
        sectionId = in.readInt();
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
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(notes);
        dest.writeString(deadline);
        dest.writeInt(colorResId);
        dest.writeInt(iconResId);
        dest.writeInt(sectionId);
        dest.writeByte((byte) (isRecurring ? 1 : 0));
        dest.writeString(startDate);
        dest.writeString(schedule);
        dest.writeString(timePreference);
        dest.writeString(taskTypeDisplay);
    }
}