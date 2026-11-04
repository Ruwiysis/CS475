package com.cs475.salahcompanion;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class StatsManager {

    private static final String PREF_NAME = "stats";

    // Add XP to the user's total
    public static void addXp(Context c, int amount) {
        SharedPreferences prefs =
                c.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        int currentXp = prefs.getInt("xp", 0);

        prefs.edit()
                .putInt("xp", currentXp + amount)
                .apply();
    }

    // Get total XP
    public static int getXp(Context c) {
        SharedPreferences prefs =
                c.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        return prefs.getInt("xp", 0);
    }

    // Calculate level (every 100 XP = 1 level)
    public static int getLevel(Context c) {
        return getXp(c) / 100 + 1;
    }

    // Update streak when all five prayers are completed
    public static void onDayCompleted(Context c, String today) {

        SharedPreferences prefs =
                c.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        String lastDay = prefs.getString("last_completed_day", "");
        int streak = prefs.getInt("streak", 0);

        // Avoid counting the same day twice
        if (today.equals(lastDay)) {
            return;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -1);

        SimpleDateFormat format =
                new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

        String yesterday = format.format(calendar.getTime());

        if (yesterday.equals(lastDay)) {
            streak++;
        } else {
            streak = 1;
        }

        prefs.edit()
                .putInt("streak", streak)
                .putString("last_completed_day", today)
                .apply();
    }

    // Get current streak
    public static int getStreak(Context c) {
        SharedPreferences prefs =
                c.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        return prefs.getInt("streak", 0);
    }

    /*
     * XP RULES:
     * Prayer on time: +10 XP
     * Prayer late: +5 XP
     * Athkar set completed: +15 XP
     * Each correct quiz answer: +5 XP
     */
}