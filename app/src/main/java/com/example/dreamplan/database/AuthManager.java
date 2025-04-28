package com.example.dreamplan.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class AuthManager {
    private DatabaseManager dbManager;

    public AuthManager(Context context) {
        dbManager = new DatabaseManager(context);
    }

    public boolean registerUser(User user) {
        SQLiteDatabase db = dbManager.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("email", user.getEmail());
        values.put("password", user.getPassword()); // Note: In production, hash this password
        values.put("name", user.getName());

        long result = db.insert("users", null, values);
        return result != -1;
    }

    public User loginUser(String email, String password) {
        SQLiteDatabase db = dbManager.getReadableDatabase();

        Cursor cursor = db.query("users",
                new String[]{"id", "email", "password", "name"},
                "email = ? AND password = ?",
                new String[]{email, password},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            User user = new User();
            user.setId(cursor.getInt(0));
            user.setEmail(cursor.getString(1));
            user.setPassword(cursor.getString(2));
            user.setName(cursor.getString(3));
            cursor.close();
            return user;
        }
        return null;
    }

    public boolean isEmailTaken(String email) {
        SQLiteDatabase db = dbManager.getReadableDatabase();

        Cursor cursor = db.query("users",
                new String[]{"id"},
                "email = ?",
                new String[]{email},
                null, null, null);

        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }
}
