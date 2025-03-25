package com.example.dreamplan.database;

public class Task {
    private int id;
    private String title;
    private String notes;
    private String deadline;
    private int colorResId; // Changed from 'color' to store drawable resource ID
    private int sectionId;

    public Task(String title, String notes, String deadline, int colorResId, int sectionId) {
        this.title = title;
        this.notes = notes;
        this.deadline = deadline;
        this.colorResId = colorResId;
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
}