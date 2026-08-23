package com.fulljournal.app.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public class AppPrefs {

    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_DEFAULT_REMINDER_MINUTES = "default_reminder_minutes";
    private static final String KEY_THEME_MODE = "theme_mode";

    public static int getDefaultReminderMinutes(Context context) {
        return prefs(context).getInt(KEY_DEFAULT_REMINDER_MINUTES, -1);
    }

    public static void setDefaultReminderMinutes(Context context, int minutes) {
        prefs(context).edit().putInt(KEY_DEFAULT_REMINDER_MINUTES, minutes).apply();
    }

    /** One of AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM / MODE_NIGHT_NO / MODE_NIGHT_YES. */
    public static int getThemeMode(Context context) {
        return prefs(context).getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public static void setThemeMode(Context context, int mode) {
        prefs(context).edit().putInt(KEY_THEME_MODE, mode).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
