package com.example.dreamplan.database;

import com.example.dreamplan.R;

public class Task {
    private int id;
    private String title;
    private String notes;
    private String deadline;
    private int colorResId; // Changed from 'color' to store drawable resource ID
    private int iconResId;
    private int sectionId;

    public Task(String title, String notes, String deadline, int colorResId, int iconResId, int sectionId) {
        this.title = title != null ? title : "";
        this.notes = notes != null ? notes : "";
        this.deadline = deadline != null ? deadline : "";
        this.colorResId = colorResId;
        this.iconResId = iconResId > 0 ? iconResId : R.drawable.ic_default_task;
        this.sectionId = sectionId;
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
}