package com.example.dreamplan.database;

import static androidx.room.RoomMasterTable.TABLE_NAME;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseManager extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "dreamplan.db";
    private static final int DATABASE_VERSION = 2;

    // Table Names
    private static final String TABLE_SECTIONS = "sections";
    private static final String TABLE_TASKS = "tasks";

    // Section Table Column Names
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_COLOR = "color";
    private static final String COLUMN_NOTES = "notes";

    // Task Table Column Names
    private static final String COLUMN_TASK_ID = "id";
    private static final String COLUMN_TASK_TITLE = "title";
    private static final String COLUMN_TASK_DESCRIPTION = "description";
    private static final String COLUMN_TASK_DUE_DATE = "due_date";
    private static final String COLUMN_TASK_SECTION_ID = "section_id";

    // Create Section Table Query
    private static final String CREATE_TABLE_SECTIONS =
            "CREATE TABLE " + TABLE_SECTIONS + " ("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_NAME + " TEXT NOT NULL, "
                    + COLUMN_COLOR + " TEXT NOT NULL, "
                    + COLUMN_NOTES + " TEXT"
                    + ");";

    // Create Task Table Query
    private static final String CREATE_TABLE_TASKS =
            "CREATE TABLE " + TABLE_TASKS + " ("
                    + COLUMN_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_TASK_TITLE + " TEXT NOT NULL, "
                    + COLUMN_TASK_DESCRIPTION + " TEXT, "
                    + COLUMN_TASK_DUE_DATE + " TEXT, "
                    + "color_res_id INTEGER, "
                    + "icon_res_id INTEGER, "  // New column for icons
                    + COLUMN_TASK_SECTION_ID + " INTEGER, "
                    + "FOREIGN KEY(" + COLUMN_TASK_SECTION_ID + ") REFERENCES "
                    + TABLE_SECTIONS + "(" + COLUMN_ID + ")"
                    + ");";

    private SQLiteDatabase db;

    public DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SECTIONS);
        db.execSQL(CREATE_TABLE_TASKS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Add the new column if upgrading from version 1
            db.execSQL("ALTER TABLE " + TABLE_TASKS + " ADD COLUMN color_res_id INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_TASKS + " ADD COLUMN icon_res_id INTEGER");
        }
    }

    // 🔹 INSERT SECTION
    public void insertSection(Section section) {
        SQLiteDatabase db = this.getWritableDatabase(); // Initialize the database
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, section.getName());
        values.put(COLUMN_COLOR, section.getColor());  // Store the selected color
        values.put(COLUMN_NOTES, section.getNotes());

        // Insert into the database
        db.insert(TABLE_SECTIONS, null, values);
        db.close(); // Close the database
    }


    // 🔹 INSERT TASK
// 🔹 INSERT TASK
    // 🔹 UPDATED INSERT TASK METHOD
    public void insertTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_TITLE, task.getTitle());
        values.put(COLUMN_TASK_DESCRIPTION, task.getNotes());
        values.put(COLUMN_TASK_DUE_DATE, task.getDeadline());
        values.put("color_res_id", task.getColorResId());
        values.put("icon_res_id", task.getIconResId());  // Add icon
        values.put(COLUMN_TASK_SECTION_ID, task.getSectionId());

        db.insert(TABLE_TASKS, null, values);
        db.close();
    }


    // 🔹 UPDATED GET ALL TASKS FOR SECTION
    public List<Task> getAllTasksForSection(int sectionId) {
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(TABLE_TASKS,
                    null,
                    COLUMN_TASK_SECTION_ID + "=?",
                    new String[]{String.valueOf(sectionId)},
                    null, null, null);

            if (cursor.moveToFirst()) {
                do {
                    Task task = new Task(
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_TITLE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_DESCRIPTION)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_DUE_DATE)),
                            cursor.getInt(cursor.getColumnIndexOrThrow("color_res_id")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("icon_res_id")),  // Get icon
                            sectionId
                    );
                    task.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TASK_ID)));
                    tasks.add(task);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
        return tasks;
    }



    public void updateSection(Section section) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, section.getName());
        values.put(COLUMN_COLOR, section.getColor());  // You might also want to update the color
        values.put(COLUMN_NOTES, section.getNotes());

        // Update the section by its ID
        db.update(TABLE_SECTIONS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(section.getId())});
        db.close();
    }

    // 🔹 UPDATED SAVE TASK METHOD (handles both insert and update)
    public void saveTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_TASK_TITLE, task.getTitle());
        values.put(COLUMN_TASK_DESCRIPTION, task.getNotes());
        values.put(COLUMN_TASK_DUE_DATE, task.getDeadline());
        values.put("color_res_id", task.getColorResId());
        values.put("icon_res_id", task.getIconResId());
        values.put(COLUMN_TASK_SECTION_ID, task.getSectionId());

        if (task.getId() > 0) {
            // Update existing task
            db.update(TABLE_TASKS, values,
                    COLUMN_TASK_ID + "=?",
                    new String[]{String.valueOf(task.getId())});
        } else {
            // Insert new task
            db.insert(TABLE_TASKS, null, values);
        }
        db.close();
    }


    public void insertMainSectionsIfNotExist() {
        List<Section> existingSections = getAllSections();
        if (existingSections.isEmpty()) {
            // Create predefined sections
            Section section1 = new Section(0, "Personal", "#C6F8E5", "Personal tasks section");
            Section section2 = new Section(0, "Work", "#F5CDDE", "Work-related tasks");
            Section section3 = new Section(0, "Groceries", "#CCE1F2", "Items to buy");

            // Insert predefined sections
            insertSection(section1);
            insertSection(section2);
            insertSection(section3);
        }
    }


    // 🔹 GET ALL SECTIONS
    public List<Section> getAllSections() {
        List<Section> sections = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SECTIONS, null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                String color = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COLOR));
                String notes = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTES));
                sections.add(new Section(id, name, color, notes));
                Log.d("DatabaseManager", "Section fetched: " + name); // Log each section
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return sections;
    }

    public void deleteSection(int sectionId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Correct table name: TABLE_SECTIONS
        db.delete(TABLE_SECTIONS, COLUMN_ID + " = ?", new String[]{String.valueOf(sectionId)});
        db.close();
    }
}
