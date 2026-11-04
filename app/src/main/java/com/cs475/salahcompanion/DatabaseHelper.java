package com.cs475.salahcompanion;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "salah_companion.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE prayer_log (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "date TEXT NOT NULL, " +
                "prayer_name TEXT NOT NULL, " +
                "status TEXT NOT NULL, " +
                "created_at TEXT DEFAULT CURRENT_TIMESTAMP)");

        db.execSQL("CREATE TABLE salah_steps (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "step_order INTEGER, title TEXT, arabic_text TEXT, " +
                "transliteration TEXT, meaning TEXT, description TEXT, image_name TEXT)");

        db.execSQL("CREATE TABLE athkar (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "category TEXT, arabic_text TEXT, transliteration TEXT, " +
                "meaning TEXT, repeat_count INTEGER)");

        db.execSQL("CREATE TABLE athkar_favorites (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "athkar_id INTEGER, saved_at TEXT, " +
                "FOREIGN KEY(athkar_id) REFERENCES athkar(id))");

        db.execSQL("CREATE TABLE flashcards (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "arabic_text TEXT, transliteration TEXT, meaning TEXT, " +
                "category TEXT, is_custom INTEGER DEFAULT 0)");

        db.execSQL("CREATE TABLE quiz_results (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "date TEXT, score INTEGER, total INTEGER, xp_earned INTEGER)");

        // M2: Seed salah_steps here.
        // M3: Seed flashcards here.
    }

    public boolean insertPrayerLog(String date, String prayer, String status) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("date", date);
        values.put("prayer_name", prayer);
        values.put("status", status);

        long result = db.insert("prayer_log", null, values);
        return result != -1;
    }

    public ArrayList<PrayerLog> getAllPrayerLogs() {
        ArrayList<PrayerLog> logs = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT id, date, prayer_name, status FROM prayer_log ORDER BY date DESC, id DESC",
                null
        );

        try {
            while (cursor.moveToNext()) {
                logs.add(new PrayerLog(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3)
                ));
            }
        } finally {
            cursor.close();
        }

        return logs;
    }

    public ArrayList<PrayerLog> searchPrayerLogs(String query) {
        ArrayList<PrayerLog> logs = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT id, date, prayer_name, status FROM prayer_log " +
                        "WHERE prayer_name LIKE ? OR date LIKE ? " +
                        "ORDER BY date DESC, id DESC",
                new String[]{"%" + query + "%", "%" + query + "%"}
        );

        try {
            while (cursor.moveToNext()) {
                logs.add(new PrayerLog(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3)
                ));
            }
        } finally {
            cursor.close();
        }

        return logs;
    }

    public boolean deletePrayerLog(int id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete("prayer_log", "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS athkar_favorites");
        db.execSQL("DROP TABLE IF EXISTS prayer_log");
        db.execSQL("DROP TABLE IF EXISTS salah_steps");
        db.execSQL("DROP TABLE IF EXISTS athkar");
        db.execSQL("DROP TABLE IF EXISTS flashcards");
        db.execSQL("DROP TABLE IF EXISTS quiz_results");
        onCreate(db);
    }
}
