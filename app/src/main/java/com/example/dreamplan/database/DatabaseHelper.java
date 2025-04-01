package com.example.dreamplan.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "dreamplan.db";
    private static final int DATABASE_VERSION = 2; // Incremented version

    // Table creation SQL
    private static final String CREATE_TABLE_SECTIONS = "CREATE TABLE sections (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "name TEXT NOT NULL, " +
            "color TEXT, " +
            "description TEXT);";

    private static final String CREATE_TABLE_TASKS = "CREATE TABLE tasks (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "section_id INTEGER, " +
            "title TEXT NOT NULL, " +
            "description TEXT, " +
            "due_date TEXT, " +
            "color INTEGER, " + // Add color column
            "icon INTEGER, " +
            "is_completed INTEGER DEFAULT 0, " +
            "FOREIGN KEY (section_id) REFERENCES sections(id));";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DatabaseHelper", "Creating tables...");
        db.execSQL(CREATE_TABLE_SECTIONS);
        db.execSQL(CREATE_TABLE_TASKS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("DatabaseHelper", "Upgrading database from version " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS sections");
        db.execSQL("DROP TABLE IF EXISTS tasks");
        onCreate(db);
    }
}