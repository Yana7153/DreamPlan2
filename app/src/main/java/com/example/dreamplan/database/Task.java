package com.example.dreamplan.database;

public class Task {
    private int id;
    private String title;
    private String notes;
    private String deadline; // Add deadline field
    private int color; // Add color field
    private int sectionId; // Foreign key to associate with a section

    // Constructor
    public Task(String title, String notes, String deadline, int color, int sectionId) {
        this.title = title;
        this.notes = notes;
        this.deadline = deadline;
        this.color = color;
        this.sectionId = sectionId;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }

    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }

    public int getSectionId() { return sectionId; }
    public void setSectionId(int sectionId) { this.sectionId = sectionId; }
}