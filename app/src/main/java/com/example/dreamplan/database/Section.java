package com.example.dreamplan.database;

import android.graphics.Color;

import java.io.Serializable;

public class Section implements Serializable {
    private String id;
    private String name;
    private String color;
    private String notes;
    private boolean isDefault;


    public Section() {}

    public Section(String id, String name, String color, String notes, boolean isDefault) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.notes = notes;
        this.isDefault = isDefault;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }

    public int getSafeColor() {
        try {
            if (color == null || color.isEmpty()) return Color.parseColor("#FFBB86FC");

            if (color.startsWith("#")) {
                return Color.parseColor(color);
            } else {
                switch (color) {
                    case "1": return Color.parseColor("#CCE1F2");
                    case "2": return Color.parseColor("#C6F8E5");
                    case "3": return Color.parseColor("#FBF7D5");
                    case "4": return Color.parseColor("#F9DED7");
                    case "5": return Color.parseColor("#F5CDDE");
                    case "6": return Color.parseColor("#E2BEF1");
                    case "7": return Color.parseColor("#D3D3D3");
                    default: return Color.parseColor("#D3D3D3");
                }
            }
        } catch (Exception e) {
            return Color.parseColor("#D3D3D3");
        }
    }
}
