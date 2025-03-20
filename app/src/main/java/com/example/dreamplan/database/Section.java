package com.example.dreamplan.database;

import java.io.Serializable;

public class Section implements Serializable {
    private int id;
    private String name;
    private String color;  // Store color as a String (hex code)
    private String notes;

    // Constructor
    public Section(int id, String name, String color, String notes) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.notes = notes;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
