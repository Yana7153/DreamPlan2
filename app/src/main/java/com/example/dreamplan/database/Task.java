package com.example.dreamplan.database;

public class Task {
    private int id;
    private String title;
    private String description;
    private String dueDate;
    private int sectionId;  // Foreign key: Links the task to a section

    // 🔹 Constructor for Fetching Tasks
    public Task(String title, String description, int sectionId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.sectionId = sectionId;
    }

    // 🔹 Constructor for Adding a New Task
    public Task(String title, String description, String dueDate, int sectionId) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.sectionId = sectionId;
    }

    // 🔹 Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public int getSectionId() {
        return sectionId;
    }

    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }
}
