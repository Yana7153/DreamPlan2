package com.example.dreamplan.database;

import static androidx.room.RoomMasterTable.TABLE_NAME;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.example.dreamplan.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseManager extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "dreamplan.db";
    private static final int DATABASE_VERSION = 4;



    //
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
                    + "icon_res_id INTEGER, "
                    + COLUMN_TASK_SECTION_ID + " INTEGER, "
                    + "is_recurring INTEGER DEFAULT 0, "
                    + "start_date TEXT, "
                    + "schedule TEXT, "
                    + "time_preference TEXT, "
                    + "FOREIGN KEY(" + COLUMN_TASK_SECTION_ID + ") REFERENCES "
                    + TABLE_SECTIONS + "(" + COLUMN_ID + ")"
                    + ");";

    private static DatabaseManager instance;
    private SQLiteDatabase db;

    public DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.db = getWritableDatabase();
    }


    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        this.db = db; // Keep reference to the opened database
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_SECTIONS + " ("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_NAME + " TEXT NOT NULL, "
                    + COLUMN_COLOR + " TEXT NOT NULL, "
                    + COLUMN_NOTES + " TEXT)");

            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_TASKS + " ("
                    + COLUMN_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_TASK_TITLE + " TEXT NOT NULL, "
                    + COLUMN_TASK_DESCRIPTION + " TEXT, "
                    + COLUMN_TASK_DUE_DATE + " TEXT, "
                    + "color_res_id INTEGER, "
                    + "icon_res_id INTEGER, "
                    + COLUMN_TASK_SECTION_ID + " INTEGER, "
                    + "is_recurring INTEGER DEFAULT 0, "
                    + "start_date TEXT, "
                    + "schedule TEXT, "
                    + "time_preference TEXT, "
                    + "FOREIGN KEY(" + COLUMN_TASK_SECTION_ID + ") REFERENCES "
                    + TABLE_SECTIONS + "(" + COLUMN_ID + "))");

            db.execSQL("CREATE TABLE IF NOT EXISTS users ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "email TEXT UNIQUE,"
                    + "password TEXT,"
                    + "name TEXT)");
        } catch (SQLiteException e) {
            Log.e("Database", "Error creating tables", e);
        }
    }

    public SQLiteDatabase getReadableDatabase() {
        SQLiteDatabase db = super.getReadableDatabase();
        if (!db.isOpen()) {
            db = super.getReadableDatabase();
        }
        return db;
    }

    // Helper method to get writable database safely
    public SQLiteDatabase getWritableDatabase() {
        SQLiteDatabase db = super.getWritableDatabase();
        if (!db.isOpen()) {
            db = super.getWritableDatabase();
        }
        return db;
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Only perform incremental upgrades
        try {
            if (oldVersion < 2) {
                db.execSQL("ALTER TABLE " + TABLE_TASKS + " ADD COLUMN color_res_id INTEGER");
                db.execSQL("ALTER TABLE " + TABLE_TASKS + " ADD COLUMN icon_res_id INTEGER");
            }
            if (oldVersion < 3) {
                db.execSQL("ALTER TABLE " + TABLE_TASKS + " ADD COLUMN is_recurring INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE " + TABLE_TASKS + " ADD COLUMN start_date TEXT");
                db.execSQL("ALTER TABLE " + TABLE_TASKS + " ADD COLUMN schedule TEXT");
                db.execSQL("ALTER TABLE " + TABLE_TASKS + " ADD COLUMN time_preference TEXT");
            }

            // Don't drop tables unless absolutely necessary
            // db.execSQL("DROP TABLE IF EXISTS users");
        } catch (SQLiteException e) {
            Log.e("Database", "Error upgrading database", e);
        }
    }

    public void initializeDatabase() {
        SQLiteDatabase db = getWritableDatabase();
        db.close();
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

        values.put("is_recurring", task.isRecurring() ? 1 : 0);
        values.put("start_date", task.getStartDate());
        values.put("schedule", task.getSchedule());
        values.put("time_preference", task.getTimePreference());

        db.insert(TABLE_TASKS, null, values);
        db.close();
    }


    // 🔹 UPDATED GET ALL TASKS FOR SECTION
    public List<Task> getAllTasksForSection(int sectionId) {
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_TASKS,
                new String[] {
                        COLUMN_TASK_ID,
                        COLUMN_TASK_TITLE,
                        COLUMN_TASK_DESCRIPTION,
                        COLUMN_TASK_DUE_DATE,
                        "color_res_id",
                        "icon_res_id",
                        COLUMN_TASK_SECTION_ID,
                        "is_recurring",
                        "start_date",
                        "schedule",
                        "time_preference"
                },
                COLUMN_TASK_SECTION_ID + "=?",
                new String[]{String.valueOf(sectionId)},
                null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Task task = new Task(
                        // Parameters must match EXACTLY with your Task constructor
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TASK_ID)),       // id
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_TITLE)), // title
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_DESCRIPTION)), // notes
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_DUE_DATE)), // dueDate
                        cursor.getInt(cursor.getColumnIndexOrThrow("color_res_id")),       // colorResId
                        cursor.getInt(cursor.getColumnIndexOrThrow("icon_res_id")),       // iconResId
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TASK_SECTION_ID)), // sectionId
                        cursor.getInt(cursor.getColumnIndexOrThrow("is_recurring")) == 1,  // isRecurring
                        cursor.getString(cursor.getColumnIndexOrThrow("start_date")),      // startDate
                        cursor.getString(cursor.getColumnIndexOrThrow("schedule")),        // schedule
                        cursor.getString(cursor.getColumnIndexOrThrow("time_preference"))  // timePreference
                );
                task.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TASK_ID)));
                tasks.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return tasks;
    }



    public boolean updateSection(Section section) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", section.getName());
        values.put("color", section.getColor());
        values.put("notes", section.getNotes());

        int rows = db.update(
                "sections",
                values,
                "id = ?",
                new String[]{String.valueOf(section.getId())}
        );
        db.close();
        return rows > 0;
    }

    public boolean deleteSection(int sectionId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(
                "sections",
                "id = ?",
                new String[]{String.valueOf(sectionId)}
        );
        db.close();
        return rows > 0;
    }

    // 🔹 UPDATED SAVE TASK METHOD (handles both insert and update)
    public void saveTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_TASK_TITLE, task.getTitle());
            values.put(COLUMN_TASK_DESCRIPTION, task.getNotes());
            values.put(COLUMN_TASK_DUE_DATE, task.getDeadline());
            values.put("color_res_id", task.getColorResId());
            values.put("icon_res_id", task.getIconResId());
            values.put(COLUMN_TASK_SECTION_ID, task.getSectionId());
            values.put("is_recurring", task.isRecurring() ? 1 : 0);
            values.put("start_date", task.getStartDate());
            values.put("schedule", task.getSchedule());
            values.put("time_preference", task.getTimePreference());

            db.insert(TABLE_TASKS, null, values);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
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



//    public void migrateOldTasks() {
//        SQLiteDatabase db = getWritableDatabase();
//        // Set default values for any tasks missing color/icon
//        ContentValues values = new ContentValues();
//        values.put("color_res_id", R.drawable.circle_background_1);
//        values.put("icon_res_id", R.drawable.ic_default_task);
//        db.update(TABLE_TASKS, values,
//                "color_res_id IS NULL OR icon_res_id IS NULL",
//                null);
//        db.close();
//    }

    public boolean hasTasksForDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateStr = sdf.format(date);

        String query = "SELECT COUNT(*) FROM tasks WHERE " +
                "(is_recurring = 0 AND due_date = ?) OR " +
                "(is_recurring = 1 AND ? >= start_date)";

        Cursor cursor = db.rawQuery(query, new String[]{dateStr, dateStr});
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }

    public List<Task> getTasksForDate(Date date) {
        if (db == null || !db.isOpen()) {
            db = getReadableDatabase(); // Ensure db is valid
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateStr = sdf.format(date);

        List<Task> tasks = new ArrayList<>();
        try {
            String query = "SELECT * FROM tasks WHERE " +
                    "(is_recurring = 0 AND due_date = ?) OR " +
                    "(is_recurring = 1 AND ? >= start_date)";

            Cursor cursor = db.rawQuery(query, new String[]{dateStr, dateStr});
            while (cursor.moveToNext()) {
                tasks.add(cursorToTask(cursor));
            }
            cursor.close();
        } catch (Exception e) {
            Log.e("Database", "Error getting tasks", e);
        }
        return tasks;
    }

    private Task cursorToTask(Cursor cursor) {
        try {
            Task task = new Task(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TASK_ID)),       // id
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_TITLE)), // title
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_DESCRIPTION)), // notes
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_DUE_DATE)), // dueDate
                    cursor.getInt(cursor.getColumnIndexOrThrow("color_res_id")),       // colorResId
                    cursor.getInt(cursor.getColumnIndexOrThrow("icon_res_id")),       // iconResId
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TASK_SECTION_ID)), // sectionId
                    cursor.getInt(cursor.getColumnIndexOrThrow("is_recurring")) == 1,  // isRecurring
                    cursor.getString(cursor.getColumnIndexOrThrow("start_date")),      // startDate
                    cursor.getString(cursor.getColumnIndexOrThrow("schedule")),        // schedule
                    cursor.getString(cursor.getColumnIndexOrThrow("time_preference"))  // timePreference
            );
            task.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TASK_ID)));
            return task;
        } catch (Exception e) {
            Log.e("Database", "Error converting cursor to task", e);
            return null; // or handle appropriately
        }
    }


    public int getTasksDueTodayCount() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        return getTaskCountForDate(today);
    }

    public int getTasksDueTomorrowCount() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        String tomorrow = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(calendar.getTime());
        return getTaskCountForDate(tomorrow);
    }

    public int getTasksDueInWeekCount() {
        try {
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(new Date());

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, 7);
            String weekLater = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(calendar.getTime());

            String query = "SELECT COUNT(*) FROM " + TABLE_TASKS +
                    " WHERE date(due_date) BETWEEN date(?) AND date(?)";
            Cursor cursor = db.rawQuery(query, new String[]{today, weekLater});

            int count = 0;
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
            return count;
        } catch (Exception e) {
            Log.e("Database", "Error getting week's tasks", e);
            return 0;
        }
    }

    private int getTaskCountForDate(String date) {
        String query = "SELECT COUNT(*) FROM " + TABLE_TASKS +
                " WHERE date(due_date) = date(?)";
        Cursor cursor = db.rawQuery(query, new String[]{date});
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public static String convertDisplayDateToDatabaseFormat(String displayDate) {
        try {
            SimpleDateFormat displayFormat = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault());
            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = displayFormat.parse(displayDate);
            return dbFormat.format(date);
        } catch (Exception e) {
            Log.e("DateConversion", "Error converting date", e);
            return ""; // or handle error appropriately
        }
    }


    public boolean updateTask(Task task) {
        Log.d("DB_UPDATE", "Updating task ID: " + task.getId() +
                " with icon: " + task.getIconResId());

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_TASK_TITLE, task.getTitle());
        values.put(COLUMN_TASK_DESCRIPTION, task.getNotes());
        values.put(COLUMN_TASK_DUE_DATE, task.getDeadline());
        values.put("color_res_id", task.getColorResId());
        values.put("icon_res_id", task.getIconResId());
        values.put("is_recurring", task.isRecurring() ? 1 : 0);
        values.put("start_date", task.getStartDate());
        values.put("schedule", task.getSchedule());
        values.put("time_preference", task.getTimePreference());

        int rows = db.update("tasks", values, "id = ?",
                new String[]{String.valueOf(task.getId())});

        Log.d("DB_UPDATE", "Updated icon to: " + task.getIconResId());
        return rows > 0;
    }

    public boolean deleteTask(int taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(
                TABLE_TASKS,
                COLUMN_TASK_ID + "=?",
                new String[]{String.valueOf(taskId)}
        );
        db.close();
        return rowsAffected > 0;
    }


}
